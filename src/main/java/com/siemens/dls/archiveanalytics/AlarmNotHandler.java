/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALARM_NOT_OUT_PORT_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_MODULE_PORT_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_NEGATED_MODULE_PORT_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.T2000P_NOT_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.negatedPortMapping;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler;
import com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateReason;
import com.siemens.dls.archiveanalytics.alertsongate.FilterParams;
import com.siemens.dls.archiveanalytics.alertsongate.NodeReason;
import com.siemens.dls.archiveanalytics.ddb.DlsDdbClient;
import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.AlarmType;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Network;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import com.siemens.dls.archiveanalytics.model.Trend;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.json.JSONObject;


public class AlarmNotHandler extends AlertsOnGateHandler {

  private static final String USECASE = "alarmnot";

  private static final Logger LOG = Logger.getLogger(AlarmNotHandler.class);
  private String date;

  private Map<Port, BinaryTrend> binaryTrends;


  public AlarmNotHandler() {

    // noop
  }

  public AlarmNotHandler(S3DataLoader loader, DlsDdbClient ddb) {
    super(loader, ddb);
  }

  /**
   * @see DlsLambdaHandler::getUseCase
   */
  @Override
  public String getUseCase() {
    return USECASE;
  }

  @Override
  protected DlsProducerLambdaResult handleRequestInternal(DlsProducerLambdaParams params,
      Context context) throws IOException {
    LOG.info(String.format("Alarm NOT Handler: begin handling request for plant %s and date %s",
        params.getPpid(), params.getDate()));

    date = params.getDate();

    ImmutableMap<Integer, Set<Integer>> moduleMapping = ImmutableMap.<Integer, Set<Integer>>builder()
        .putAll(ALLOWED_MODULE_PORT_IDS)
        .putAll(ALLOWED_NEGATED_MODULE_PORT_IDS).build();

    Network network = s3DataLoader.loadNetwork(moduleMapping, moduleMapping);

    Map<Port, List<Alarm>> potentialAlarms = s3DataLoader
        .getAlarmsFromS3(network.getFilteredPorts(ALARM_NOT_OUT_PORT_IDS));

    //load binary for specified ports in the network
    binaryTrends = s3DataLoader
        .getBinaryTrends(network
            .getFilteredPorts(m -> true, m -> p -> p.getDirection() == PortDirection.O));

    //load alarm type
    Map<Integer, String> alarmTypes = Maps
        .transformValues(s3DataLoader.getAlarmTypesFromS3(), AlarmType::getAbbrev);

    Map<Port, List<Alarm>> alarms = potentialAlarms.entrySet().stream()
        .filter(entrySet ->
            findDelegatePort(entrySet.getKey(), T2000P_NOT_AFITYPE_ID).isPresent())
        .collect(Collectors
            .toMap(entry -> findDelegatePort(entry.getKey(), T2000P_NOT_AFITYPE_ID)
                .orElseGet(entry::getKey), Map.Entry::getValue));

    Map<Port, JSONObject> delayExtendMapping = generateDelayExtendMapping(alarms);

    //simple and_or reason
    LOG.info("start generate simple  and or flat items:");
    Stream<JSONObject> simpleAndOrStream = alarms.entrySet().stream()
        .peek(entry->{
          LinkedHashMultimap<Integer, NodeReason> hierarchy = splitNetwork(
              entry.getKey());
          System.out.println(hierarchy);
        })
        .map(e -> breadthFirstSearch(e, binaryTrends, getSimpleReasonFilter()))
        .flatMap(Collection::stream)
        .filter(reason -> reason.getCode() != 0)
        .map(
            reason -> generateReasonJSONObject(reason, delayExtendMapping, SIMPLE_AND_OR_BASE, date,
                alarmTypes));

    //duplicate reason
    LOG.info("start generate duplicate flat items: ");
//    Stream<JSONObject> duplicateStream = alarms.entrySet().stream()
//        .map(e -> breadthFirstSearch(e, binaryTrends, duplicateFilter))
//        .flatMap(Collection::stream)
//        .map(reason -> generateReasonJSONObject(reason, delayExtendMapping, DUPL_BASE, date
//            , alarmTypes));

    //Stream<JSONObject> reasonStream = Stream.of(simpleAndOrStream, duplicateStream)
    //  .flatMap(s -> s);

    //collect all flat items: duplicate reason + simple and or reason
    Stream<JSONObject> flatItems = collectFlatItems(delayExtendMapping, simpleAndOrStream, date);

    List<Item> items = generateDdbItemsFromItemData(flatItems,
        ItemCorrelator.correlating(DDB_FIELD_AFIIDALERT, DDB_FIELD_DELAY_EXTEND),
        ImmutableList.of(DDB_FIELD_DELAY_EXTEND), DDB_FIELD_SIM_DUP_OR_REASON);
    ddbClient.writeItems(items, getUseCase());

    return new DlsProducerLambdaResult(true, items.size());
  }

  private void updateNotGateDuplicateReasonsCount(AlertsOnGateReason reason,
      Map<Port, BinaryTrend> binaryTrends, Map<Port, List<Alarm>> alarms) {
    Port potentialArchivedPort = reason.getPotentialArchivedPort();

    //if port isn't written into archived, reasons=0
    if (!potentialArchivedPort.isArchive()) {
      reason.setReasons(0);
    }

    Port root = reason.getRootPort();
    List<Alarm> rootAlarms = alarms.getOrDefault(root, null);
    if (rootAlarms == null) {
      //if no alarm at root port, reasons=0
      reason.setReasons(0);
    } else {
      long reasons = rootAlarms.stream()
          .filter(a -> isNegatedOverlapping(a, potentialArchivedPort, binaryTrends))
          .count();
      reason.setReasons(reasons);

    }
  }

  private Function<FilterParams, Optional<AlertsOnGateReason>>
      duplicateFilter = params -> {
    Port currentNode = params.getCurrentNode();
    Port rootNode = params.getStartNode();

    if (ALLOWED_NEGATED_MODULE_PORT_IDS.keySet().contains(
        currentNode.getModule().getAfiTypeId())) {
      //this is a negated specified module, checkout Q and Q_N are negated.
      Optional<Port> negatedPortOptional = findNegatedPort(currentNode);
      if (negatedPortOptional.isPresent() && negatedPortOptional.get().isArchive()
          && negatedPortOptional.get().isAlarm() &&
          currentNode.isArchive() && currentNode.isAlarm()) {
        int negatedCount = calculateSignalNegatedCount(currentNode, negatedPortOptional.get());
        if (negatedCount == 0) {
          return Optional.empty();
        }
        //Q and Q_N are negated, this is duplicate reason what we need
        return Optional.of(new AlertsOnGateReason(currentNode, currentNode, rootNode,
            params.getDepth() - 1, 1, negatedCount));
      } else {
        return Optional.empty();
      }
    } else {
      //its a normal module
      // checkout if output port is written into archived and signal is negated overlapping
      if (currentNode.isArchive() && currentNode.isAlarm()) {
        AtomicInteger count = calculateSignalNegatedOverlappingCount(currentNode, params);
        return count.get() > 0 ? Optional.of(
            new AlertsOnGateReason(currentNode, currentNode, rootNode, params.getDepth() - 1, 1,
                count.get())) : Optional.empty();
      } else {
        //current node is not archived, we try to search the proxy port
        Optional<Port> archivedProxyPort = findArchivedProxyPort(currentNode, params);
        return archivedProxyPort.flatMap(port -> {
          AtomicInteger count = calculateSignalNegatedOverlappingCount(archivedProxyPort.get(),
              params);
          return count.get() > 0 ? Optional.of(
              new AlertsOnGateReason(currentNode, archivedProxyPort.get(), rootNode,
                  params.getDepth() - 1, 1,
                  count.get())) : Optional.empty();
        });
      }
    }
  };


  private AtomicInteger calculateSignalNegatedOverlappingCount(Port port, FilterParams params) {

    AtomicInteger count = new AtomicInteger(0);
    BinaryTrend sourceTrends = params.getBinaryTrends().get(port);
    if (Objects.isNull(sourceTrends)) {
      return new AtomicInteger(0);
    }

    List<Alarm> alarms = params.getAlarms();
    if (Objects.isNull(alarms)) {
      throw new IllegalStateException(
          "for alert: " + params.getStartNode().getUniqueName() + " : no alarms dates");
    }
    //sourceTrendsMap: <value,Set<millis>>
    Map<Integer, List<Integer>> sourceTrendsMap = sourceTrends.getAcceptableQualityTrends()
        .stream()
        .collect(Collectors
            .groupingBy(t -> (int) t.getValue(), signalClassificationCollector));

    List<Integer> risingTimeLists = sourceTrendsMap.get(1);

    List<Integer> fallingTimeLists = sourceTrendsMap.get(0);

    if (risingTimeLists == null) {
      return count;
    }

    if (fallingTimeLists == null) {
      return new AtomicInteger(alarms.size());
    }

    alarms.forEach(
        alarm -> {
          int startMillis = Utils.toMillisOfDay(alarm.getTime());
          //find falling signal

          Optional<Integer> fallingSignal = fallingTimeLists.stream()
              .filter(t -> t >= startMillis - MILLIS_TO_SECOND * CYCLE && t <= startMillis)
              .reduce((t1, t2) -> t2);

          if (!fallingSignal.isPresent()) {
            count.updateAndGet(prev -> prev);
          } else {
            //find rising signal
            Optional<Integer> risingSignal = risingTimeLists.stream()
                .filter(t -> t > fallingSignal.get())
                .findFirst();
            if (!risingSignal.isPresent() || risingSignal.get() > startMillis) {
              count.updateAndGet(prev -> prev + 1);
            }
          }
        });

    return count;
  }

  private int calculateSignalNegatedCount(Port outputPort, Port negatedPort) {
    BinaryTrend outputTrends = binaryTrends.get(outputPort);
    BinaryTrend negatedTrends = binaryTrends.get(negatedPort);

    long notMatchedCount = outputTrends.getAcceptableQualityTrends().stream()
        .filter(trend -> {
          int millis = trend.getMillis();
          double value = trend.getValue();
          boolean isNegated = negatedTrends.getAcceptableQualityTrends().stream()
              .anyMatch(negatedTrend -> negatedTrend.getMillis() == millis
                  && negatedTrend.getValue() + value == 1);
          return !isNegated;
        }).count();
    return notMatchedCount == 0 ? outputTrends.getRisingCount() : 0;
  }

  private boolean isNegatedOverlapping(Alarm alarm, Port outputPort,
      Map<Port, BinaryTrend> binaryTrends) {

    int startMillis = Utils.toMillisOfDay(alarm.getTime());

    BinaryTrend binaryTrend = binaryTrends.get(outputPort);
    if (binaryTrend == null) {
      return false;
    }

    //find falling signal
    Optional<Trend> fallingSignal = binaryTrend.getAcceptableQualityTrends().stream()
        .filter(trend -> trend.getValue() == 0)
        .filter(trend -> trend.getMillis() <= startMillis &&
            trend.getMillis() >= startMillis - CYCLE * MILLIS_TO_SECOND)
        .reduce((t1, t2) -> t2);

    if (!fallingSignal.isPresent()) {
      return false;
    }

    //find rising signal
    Optional<Trend> risingSignal = binaryTrend.getAcceptableQualityTrends().stream()
        .filter(trend -> trend.getValue() == 1)
        .filter(trend -> trend.getMillis() > fallingSignal.get().getMillis())
        .findFirst();

    return !risingSignal.isPresent() || risingSignal.get().getMillis() > startMillis;

  }

  private Optional<Port> findNegatedPort(Port outputPort) {
    Integer negatedPortId = negatedPortMapping.get(outputPort.getId());
    return outputPort.getModule().findPortById(negatedPortId);
  }

}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
