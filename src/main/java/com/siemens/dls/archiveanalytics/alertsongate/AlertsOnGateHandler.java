/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;

import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_AND_MODULE_PORTS_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_INTER_MODULE_PORT_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_OR_MODULE_PORTS_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.AND_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.BDMZ_IN_PUT_PORT_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.BIN_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.BSEL_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.BSIG_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.CONST_MODULE_PORT_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.MODULE_OPERATOR_MAP;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.NAND_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.NOR_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.NOT_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.OR_AFITYPE_ID;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.RELEVENT_NOT_MODULE_AFI_IDS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.T2000P_KON1_AFITYPE_ID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.siemens.dls.archiveanalytics.DlsLambdaHandler;
import com.siemens.dls.archiveanalytics.S3DataLoader;
import com.siemens.dls.archiveanalytics.Utils;
import com.siemens.dls.archiveanalytics.ddb.DlsDdbClient;
import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Network;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.Trend;
import com.siemens.dls.archiveanalytics.model.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AlertsOnGateHandler extends DlsLambdaHandler {

  private static final Logger LOG = Logger.getLogger(AlertsOnGateHandler.class);

  private static final int SECOND_TO_MILLIS = 1000;

  //time interval
  private static final List<Integer> DELAY_TIMES = ImmutableList.of(1, 2, 3, 5, 10, 20, 30, 60);

  public static final String DDB_FIELD_EXTEND_PREFIX = "extend";
  public static final String DDB_FIELD_DELAY_PREFIX = "delay";
  //delay and extend items
  public static final String DDB_FIELD_AFIIDALERT = "afiidalert";
  public static final String DDB_FIELD_DELAY_EXTEND = "DelayExtend";
  public static final String DDB_FIELD_COUNT = "count";
  //SimDuplAOReason items
  public static final String DDB_FIELD_TAGNAME = "tagname";
  public static final String DDB_FIELD_BASE = "base";
  public static final String DDB_FIELD_MODULE = "module";
  public static final String DDB_FIELD_PORT = "port";
  public static final String DDB_FIELD_ALARMTYPE = "alarmtype";
  public static final String DDB_FIELD_VALUE = "state";
  public static final String DDB_FIELD_INTER = "inter";
  public static final String DDB_FIELD_REASONS = "reasons";
  public static final String DDB_FIELD_CODE = "code";


  public static final String DDB_FIELD_SIM_DUP_OR_REASON = "SimDuplAOReason";
  public static final String DUPL_BASE = "Dupl";

  //base string
  public final String SIMPLE_AND_OR_BASE = "AOReason";


  public final int UNIFIED_OUT_PUT_ID = 1000;
  public int UNIFIED_BDMZ_OUT_PUT_ID = 1010;
  public static final int MAX_DEPTH = 8;
  public static final int MILLIS_TO_SECOND = 1000;
  public static final int CYCLE = 1;

  public AlertsOnGateHandler() {
    super();
  }

  public AlertsOnGateHandler(S3DataLoader s3DataLoader,
      DlsDdbClient ddbClient) {
    super(s3DataLoader, ddbClient);
  }

  public Map<Port, JSONObject> generateDelayExtendMapping(
      Map<Port, List<Alarm>> moduleWithAlarms) {
    return Maps.transformValues(moduleWithAlarms, this::generateDelayExtendJSONObject);
  }

  private JSONObject generateDelayExtendJSONObject(List<Alarm> alarms) {

    Map<String, Long> delayExtendCounts = new HashMap<>();
    long allCount = alarms.size();
    DELAY_TIMES.forEach(duration -> {

      long delayCount = alarms.stream()
          .filter(a -> a.getDuration() > duration * SECOND_TO_MILLIS).count();
      long extendCount = alarms.stream()
          .filter(a -> a.getTimeToNext() > duration * SECOND_TO_MILLIS).count();

      String delay = DDB_FIELD_DELAY_PREFIX + duration;
      String extend = DDB_FIELD_EXTEND_PREFIX + duration;
      delayExtendCounts.put(delay, delayCount);
      delayExtendCounts.put(extend, extendCount);

    });
    delayExtendCounts.put(DDB_FIELD_COUNT, allCount);

    //generate Json Object
    JSONObject delayExtend = new JSONObject();
    DELAY_TIMES.forEach(i -> {
      String delay = DDB_FIELD_DELAY_PREFIX + i;
      String extend = DDB_FIELD_EXTEND_PREFIX + i;
      delayExtend.put(delay, delayExtendCounts.getOrDefault(delay, (long) 0));
      delayExtend.put(extend, delayExtendCounts.getOrDefault(extend, (long) 0));
    });

    delayExtend.put(DDB_FIELD_COUNT,
        delayExtendCounts.getOrDefault(DDB_FIELD_COUNT, (long) 0));
    return delayExtend;
  }


  public boolean isSourceSignalRelevant(Module module, Integer sourceRelevantValue, int value) {
    if (isRelevantNotModule(module)) {
      return true;
    }
    return sourceRelevantValue + value > 0;
  }

  private Tuple<Integer, Integer> updateRelevantValue(AlertsOnGateReason reason,
      Map<Module, Tuple<Integer, Integer>> moduleWithRelevance,
      Map<Module, Integer> moduleWithPropagationOutputValue,
      int relevantValue, Module currentSourceModule,
      Module currentTargetModule) {
    if (isRelevantNotModule(currentSourceModule)
        && moduleWithRelevance.get(currentTargetModule).getLeft() == 1) {
      relevantValue = 0;
    } else if (isRelevantNotModule(currentSourceModule)
        && moduleWithRelevance.get(currentTargetModule).getLeft() == 0) {
      relevantValue = 1;
    } else if (!isRelevantNotModule(currentSourceModule)) {
      relevantValue = moduleWithRelevance.get(currentTargetModule).getLeft();
    }

    //update relevance
    //this code maybe be used later
    int relevance = 0;
    //reserve, maybe can this function in the future be used again
//    if (((relevantValue == reason.getValue() && !isRelevantNotModule(currentTargetModule)) || (
//        relevantValue != reason.getValue() && isRelevantNotModule(currentTargetModule))) && (
//        relevantValue == moduleWithPropagationOutputValue.get(currentTargetModule))) {
//      relevance = 1;
//    } else {
//      relevance = 0;
//    }

    return Tuple.of(relevantValue, relevance);

  }

  public Stream<Port> getInputPorts(Port alertPort) {
    if (alertPort == null) {
      return Stream.empty();
    }
    return alertPort.getModule().getInPorts()
        .stream()
        // .filter(Port::isConnected)
        .filter(port ->
            ALLOWED_INTER_MODULE_PORT_IDS.keySet().contains(port.getModule().getAfiTypeId()) &&
                ALLOWED_INTER_MODULE_PORT_IDS.get(port.getModule().getAfiTypeId())
                    .contains(port.getId()));
  }


  public boolean isFunctionalOr(Module module) {
    //check module is OR or AND with only one input port
    // include T2000P_OR and T2000P_AND
    return ALLOWED_OR_MODULE_PORTS_IDS.keySet().contains(module.getAfiTypeId())
        || ALLOWED_AND_MODULE_PORTS_IDS.keySet().contains(module.getAfiTypeId())
        && module.getInPorts().stream().filter(Port::isConnected).count() == 1;
  }


  private String getConstantValue(Module reasonModule) {
    Optional<Port> constantPort = reasonModule.getInPorts().stream()
        .filter(p -> CONST_MODULE_PORT_IDS.get(reasonModule.getAfiTypeId()).contains(p.getId()))
        .findAny();
    assert constantPort.isPresent();
    return constantPort.get().getParameter();
  }

  private boolean isRelevantNotModule(Module targetModule) {

    return RELEVENT_NOT_MODULE_AFI_IDS.contains(targetModule.getAfiTypeId());
  }

  private boolean isInterModule(Module targetModule) {
    return ALLOWED_INTER_MODULE_PORT_IDS.keySet().contains(targetModule.getAfiTypeId());
  }

  private boolean isFunctionalInter(
      Module module) {
    return ALLOWED_INTER_MODULE_PORT_IDS.keySet()
        .contains(module.getAfiTypeId());
  }

  private Predicate<Port> isFunctionalT2000PModule(int afiid) {
    return p -> p.getModule().getAfiTypeId() == afiid;
  }


  public JSONObject generateReasonJSONObject(
      AlertsOnGateReason reason,
      Map<Port, JSONObject> delayExtendMapping, String base, String date,
      Map<Integer, String> alarmTypeAbbrevs) {
    JSONObject json = new JSONObject();

    Port alertPort = reason.getRootPort();
    Port reasonPort = reason.getSourcePort();

    json.put(DDB_FIELD_TAGNAMEALERT, alertPort.getUniqueName());
    json.put(DDB_FIELD_DATE, date);
    json.put(DDB_FIELD_AFIIDALERT, alertPort.getModule().getId());
    json.put(DDB_FIELD_DELAY_EXTEND,
        delayExtendMapping.getOrDefault(alertPort, new JSONObject()).toString());

    json.put(DDB_FIELD_TAGNAME, reasonPort.getUniqueName());
    json.put(DDB_FIELD_BASE, base);
    json.put(DDB_FIELD_MODULE, reasonPort.getModule().getSymbol());
    json.put(DDB_FIELD_ALARMTYPE, alarmTypeAbbrevs.getOrDefault(reasonPort.getAlarmTypeId(),
        Integer.toString(reasonPort.getAlarmTypeId())));
    json.put(DDB_FIELD_PORT, reasonPort.getName());
    json.put(DDB_FIELD_VALUE, reason.getValue());
    json.put(DDB_FIELD_INTER, reason.getInter());
    json.put(DDB_FIELD_REASONS, reason.getReasons());

    if (base.equals(SIMPLE_AND_OR_BASE)) {
      json.put(DDB_FIELD_CODE, reason.getCode());
    }
    return json;
  }


  public Optional<Port> findDelegatePort(Port proxyPort, int afiid) {
    if (ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS.keySet()
        .contains(proxyPort.getModule().getAfiTypeId())) {
      //its a alarm from BDMZ* module, we need to find connected T2000P module output port
      Optional<Port> inputPort = proxyPort.getModule().findPortById(BDMZ_IN_PUT_PORT_ID);
      if (inputPort.isPresent() && inputPort.get().getConnectedOutPort().isPresent()
          && isFunctionalT2000PModule(afiid).test(inputPort.get().getConnectedOutPort().get())) {
        return inputPort.get().getConnectedOutPort();
      }
      return Optional.empty();
    }
    return Optional.of(proxyPort);
  }

  public Stream<JSONObject> collectFlatItems(Map<Port, JSONObject> delayExtendMapping,
      Stream<JSONObject> reasonStream, String date) {

    List<JSONObject> reasonList = reasonStream.collect(Collectors.toList());
    //delay and extend stream
    Map<Port, JSONObject> delayExtendStream = Maps.transformEntries(delayExtendMapping,
        (port, value) -> {
          JSONObject json = new JSONObject();
          assert port != null;
          json.put(DDB_FIELD_TAGNAMEALERT, port.getUniqueName());
          json.put(DDB_FIELD_DATE, date);
          json.put(DDB_FIELD_AFIIDALERT, port.getModule().getId());
          json.put(DDB_FIELD_DELAY_EXTEND, value != null ? value.toString() :
              new JSONObject().toString());
          JSONArray array = new JSONArray();
          array.put(new JSONObject());
          json.put(DDB_FIELD_SIM_DUP_OR_REASON, array.toString());
          return json;
        });

    Set<Object> reasonNames = reasonList.stream()
        .map(jsonObject -> jsonObject.get(DDB_FIELD_TAGNAMEALERT))
        .collect(Collectors.toSet());

    SetView<String> difference = Sets
        .difference(delayExtendStream.keySet().stream().map(Port::getUniqueName)
            .collect(Collectors.toSet()), reasonNames);

    // all flat items
    return Stream.concat(reasonList.stream(), delayExtendStream.entrySet().stream()
        .filter(e -> difference.contains(e.getKey().getUniqueName()))
        .map(Entry::getValue));

  }

  //-------------------------new implementation functions------------------------------//

  protected List<AlertsOnGateReason> breadthFirstSearch(Entry<Port, List<Alarm>> startVertexMap,
      Map<Port, BinaryTrend> binaryTrends,
      Function<FilterParams, Optional<AlertsOnGateReason>> filter) {
    return breadthFirstSearch(startVertexMap, binaryTrends, null, filter);

  }

  protected List<AlertsOnGateReason> breadthFirstSearch(Entry<Port, List<Alarm>> startVertexMap,
      Map<Port, BinaryTrend> binaryTrends, Network network,
      Function<FilterParams, Optional<AlertsOnGateReason>> filter) {

    Port startVertex = startVertexMap.getKey();
    int depth = 0;

    //search queue
    LinkedList<Port> searchQueue = Lists.newLinkedList();

    // collections for visited module id
    HashMap<Port, Port> visitedAdjacentMap = Maps.newHashMap();

    //collections for node which satisfy with given filter
    List<AlertsOnGateReason> resultSets = Lists.newArrayList();

    //mark the first<current> vertex as visited and enqueue it into searchQueue
    visitedAdjacentMap.put(startVertex, null);
    searchQueue.add(startVertex);
    //null object to record the depth of BFS
    searchQueue.add(null);

    while (searchQueue.size() > 1) {
      //first de queue a node
      Port currentNode = searchQueue.poll();
      if (Objects.isNull(currentNode)) {
        depth++;
        //we consumer the null object, for next round, we need to add new null object
        searchQueue.add(null);
      } else {
//        reserve comment for debug
//        if (startVertex.getModule().getId() == 434213) {
//          LOG.info(
//              "current node: " + currentNode.getModule().getId() + " start node: " + startVertex
//                  .getModule().getId());
//        }

        Optional<AlertsOnGateReason> result;
        //check if this node is start vertex
        if (currentNode != startVertex) {
          result = filter.
              apply(new FilterParams.Builder(currentNode, startVertex)
                  .setDepth(depth)
                  .setBinaryTrends(binaryTrends)
                  .setAlarms(startVertexMap.getValue())
                  .setVisitedAdjacentMap(visitedAdjacentMap)
                  .setNetwork(network)
                  .build());


        } else {
          result = Optional.empty();
        }

        if (result.isPresent()) {
          resultSets.add(result.get());
        } else {
          //enqueue new vertices into search queue
          //current module is not archived and is not binary module
          if (ALLOWED_INTER_MODULE_PORT_IDS.keySet()
              .contains(currentNode.getModule().getAfiTypeId())) {
            getInputPorts(currentNode)
                .map(Port::getConnectedOutPort)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(nextVertex -> {
                  if (!visitedAdjacentMap.keySet().contains(nextVertex)) {
                    visitedAdjacentMap.put(nextVertex, currentNode);
                    searchQueue.add(nextVertex);
                  }
                });
          }

        }
      }
      if (depth >= MAX_DEPTH) {
        LOG.info(String.format("reached the maximal depth, current depth is %d", depth));
        break;
      }

    }

    LOG.info(String.format("for alert module %d, %d modules haven been visited",
        startVertex.getModule().getId(),
        visitedAdjacentMap.keySet().size()));
    //after BFS, we return the result sets
    return resultSets;
  }

  protected Collector<Trend, List<Integer>, List<Integer>> signalClassificationCollector = new Collector<Trend, List<Integer>, List<Integer>>() {
    @Override
    public Supplier<List<Integer>> supplier() {
      return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Integer>, Trend> accumulator() {
      return (list, t) -> list.add(t.getMillis());
    }

    @Override
    public BinaryOperator<List<Integer>> combiner() {
      return (l1, l2) -> {
        l1.addAll(l2);
        return l1;
      };
    }

    @Override
    public Function<List<Integer>, List<Integer>> finisher() {
      return (list) -> list;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return EnumSet.of(Characteristics.IDENTITY_FINISH);
    }
  };

  private boolean isConnectedProxyModule(Module module) {
    return isFunctionalOr(module) || BIN_AFITYPE_ID == module.getAfiTypeId()
        || ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS.keySet().contains(module.getAfiTypeId());
  }

  protected AtomicInteger calculateOverlappingCount(Port targetOutputPort,
      Port alertPort, FilterParams filterParams) {

    AtomicInteger count = new AtomicInteger(0);

    BinaryTrend sourceTrends = filterParams.getBinaryTrends().get(targetOutputPort);
    if (Objects.isNull(sourceTrends)) {
      return new AtomicInteger(0);
    }

    List<Alarm> alarms = filterParams.getAlarms();
    if (Objects.isNull(alarms)) {
      throw new IllegalStateException(
          "for alert: " + alertPort.getUniqueName() + " : no alarms dates");
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
          //find rising signal
          Optional<Integer> risingSignal = risingTimeLists.stream()
              .filter(t -> t >= startMillis - CYCLE * MILLIS_TO_SECOND
                  && t <= startMillis)
              .reduce((t1, t2) -> t2);

          if (!risingSignal.isPresent()) {
            count.updateAndGet(prev -> prev);
          } else {
            //find falling signal
            calculateCount(count, fallingTimeLists, startMillis, risingSignal.get());
          }

        });

    return count;
  }

  private void calculateCount(AtomicInteger count, List<Integer> fallingTimeLists, int startMillis,
      Integer risingSignal) {
    Optional<Integer> fallingSignal = fallingTimeLists.stream()
        .filter(t -> t > risingSignal)
        .findFirst();
    if (!fallingSignal.isPresent() || fallingSignal.get() > startMillis) {
      count.updateAndGet(prev -> prev + 1);
    }
  }

  private AtomicInteger calculateSignalBeforeOrSameCount(Port archivedPort, Port rootPort,
      FilterParams filterParams) {

    BinaryTrend sourceTrends = filterParams.getBinaryTrends().get(archivedPort);
    if (Objects.isNull(sourceTrends)) {
      return new AtomicInteger(0);
    }

    List<Alarm> alarms = filterParams.getAlarms();
    if (Objects.isNull(alarms)) {
      throw new IllegalStateException(
          "for alert: " + rootPort.getUniqueName() + " : no alarms dates");
    }

    AtomicInteger validCount = new AtomicInteger(0);

    //sourceTrendsMap: <value,Set<millis>>
    Map<Integer, List<Integer>> sourceTrendsMap = sourceTrends.getAcceptableQualityTrends()
        .stream()
        .collect(Collectors
            .groupingBy(t -> (int) t.getValue(), signalClassificationCollector));

    List<Integer> risingTimeLists = sourceTrendsMap.get(1);
    List<Integer> fallingTimeLists = sourceTrendsMap.get(0);

    if (risingTimeLists == null) {
      return validCount;
    }

    if (fallingTimeLists == null) {
      return new AtomicInteger(alarms.size());
    }

    alarms.forEach(
        alarm -> {
          int startMillis = Utils.toMillisOfDay(alarm.getTime());
          //find rising signal
          Optional<Integer> risingSignal = risingTimeLists.stream()
              .filter(t -> t <= startMillis)
              .reduce((t1, t2) -> t2);
          if (!risingSignal.isPresent()) {
            validCount.updateAndGet(prev -> prev);
          } else {
            //find falling signal
            calculateCount(validCount, fallingTimeLists, startMillis, risingSignal.get());
          }

        });

    return validCount;

  }

  private Function<FilterParams, Optional<AlertsOnGateReason>> simpleReasonFilter =
      filterParams -> {
        Port startNode = filterParams.getStartNode();
        Port currentNode = filterParams.getCurrentNode();
        int depth = filterParams.getDepth();

        //current node's output port is written into archived
        if (currentNode.isArchive()) {
          //inter of source signal which is written into archive >0
          if (isFunctionalInter(currentNode.getModule()) && depth == 1) {
            return Optional.empty();
          }

          AlertsOnGateReason reason = new AlertsOnGateReason(currentNode, currentNode,
              startNode, filterParams.getDepth() - 1, 0, 0);
          return Optional.of(updateSimpleReason(reason, filterParams));
        } else {
          Optional<Port> archivedProxyPort = findArchivedProxyPort(currentNode, filterParams);
          // inter module output port oder its proxy port must be written into archived
          if (isFunctionalInter(currentNode.getModule()) && !archivedProxyPort
              .isPresent()) {
            return Optional.empty();
          }

          return archivedProxyPort.isPresent() ? archivedProxyPort
              .map(
                  port -> updateSimpleReason(new AlertsOnGateReason(currentNode, port, startNode,
                      depth - 1, 0,
                      0), filterParams)) : Optional
              .of(updateSimpleReason(new AlertsOnGateReason(currentNode, currentNode,
                  startNode,
                  depth - 1, -1,
                  0), filterParams));
        }
      };

  private AlertsOnGateReason updateSimpleReason(AlertsOnGateReason reason,
      FilterParams filterParams) {
    return updateCodeAttribute(updateAttributes(reason, filterParams), filterParams);
  }

  private AlertsOnGateReason updateCodeAttribute(AlertsOnGateReason reason,
      FilterParams filterParams) {
    return propagationValue(reason, filterParams);
  }

  private AlertsOnGateReason propagationValue(AlertsOnGateReason reason,
      FilterParams filterParams) {

    //from source module to root module, not include source and root
    LinkedList<Module> propagationPath = backTracePath(reason, filterParams);

    Module sourceModule = reason.getSourcePort().getModule();

    Map<Module, Tuple<Integer, Integer>> moduleWithRelevance = Maps.newHashMap();
    Map<Module, Integer> moduleWithPropagationOutputValue = Maps.newHashMap();

    //between source and root, no inter modules
    if (propagationPath.size() == 0) {
      //update code attribute
      return updateCodeForReason(reason, propagationPath, sourceModule, moduleWithRelevance,
          moduleWithPropagationOutputValue);
    }

    int currentOutputValue = reason.getValue();

    //calculate only the output value for first adjacent module of source module
    Module adjacentModule = propagationPath.get(0);
    currentOutputValue = calculateAdjacentOutputValue(reason, currentOutputValue,
        adjacentModule, filterParams);

    moduleWithPropagationOutputValue.put(adjacentModule, currentOutputValue);

    List<Module> proxyList = new LinkedList<>(propagationPath);
    proxyList.add(0, sourceModule);

    if (proxyList.size() >= 2) {

      Module targetModule = proxyList.get(proxyList.size() - 1);
      Tuple<Integer, Integer> tuple = initialTargetRelevantValue(reason.getRootPort().getModule(),
          targetModule);
      // Tuple<Integer, Integer> tuple = initialTargetRelevantValue(targetModule);
      moduleWithRelevance.put(targetModule, tuple);

      int relevantValue = tuple.getLeft();

      for (int i = proxyList.size() - 2; i >= 0; i--) {

        Module currentSourceModule = proxyList.get(i);
        Module currentTargetModule = proxyList.get(i + 1);

        //update relevant value and relevance
        tuple = updateRelevantValue(reason,
            moduleWithRelevance,
            moduleWithPropagationOutputValue,
            relevantValue,
            currentSourceModule,
            currentTargetModule);

        relevantValue = tuple.getLeft();
        moduleWithRelevance.put(currentSourceModule, tuple);
      }
    }

    return updateCodeForReason(reason, propagationPath, sourceModule, moduleWithRelevance,
        moduleWithPropagationOutputValue);
  }

  private int calculateAdjacentOutputValue(
      AlertsOnGateReason reason,
      int currentOutputValue,
      Module adjacentModule,
      FilterParams filterParams) {
    int adjacentOutputValue = 0;
    switch (adjacentModule.getAfiTypeId()) {
      case AND_AFITYPE_ID:
        //and module, if value=0 return 0, if value=1, need to checkout all input signal
        //if no signal is 1, not all signal is 0, return -1
        return currentOutputValue == 0 ? 0
            : passOnModule(reason, currentOutputValue,
                adjacentModule, filterParams);
      case OR_AFITYPE_ID:
        return currentOutputValue == 1 ? 1
            : passOnModule(reason, currentOutputValue,
                adjacentModule, filterParams);
      case BSEL_AFITYPE_ID:
        return passOnModule(reason, currentOutputValue,
            adjacentModule, filterParams);
      case BIN_AFITYPE_ID:
        return currentOutputValue;
      case NOT_AFITYPE_ID:
        return currentOutputValue == -1 ? -1 : currentOutputValue == 1 ? 0 : 1;

      case NAND_AFITYPE_ID:
        return currentOutputValue == 0 ? 0
            : passOnModule(reason, currentOutputValue,
                adjacentModule, filterParams);

      case NOR_AFITYPE_ID:
        return currentOutputValue == 0 ? 1
            : passOnModule(reason, currentOutputValue,
                adjacentModule, filterParams);
    }

    return adjacentOutputValue;
  }

  private int passOnModule(AlertsOnGateReason reason, int currentOutputValue,
      Module adjacentModule, FilterParams filterParams) {

    Stream<Integer> inputPortsValuesStreamOfModule = adjacentModule.getInPorts()
        .stream()
        .map(Port::getConnectedOutPort)
        .filter(Optional::isPresent)
        .map(Optional::get)
        //delete reason port
        .filter(port -> !reason.getSourcePort().getModule().getOutPorts().contains(port))
        .map(port -> {
          if (!port.isArchive()) {
            //Optional<Port> proxyPort = findProxyPorts(port);
            Optional<Port> archivedProxyPort = findArchivedProxyPort(port, filterParams);
            return archivedProxyPort
                .map(p -> calculateSignalBeforeOrSameCount(p, reason.getRootPort(), filterParams))
                .map(AtomicInteger::get)
                .map(count -> count > 0 ? 1 : 0)
                .orElse(-1);
          } else {
            return
                calculateSignalBeforeOrSameCount(port, reason.getRootPort(), filterParams).get() > 0
                    ? 1
                    : 0;
          }
        });

    List<Integer> inputValuesListOfModule = inputPortsValuesStreamOfModule
        .collect(Collectors.toList());
    inputValuesListOfModule.add(currentOutputValue);

    Set<Integer> inputValuesSetOfModule = new HashSet<>(inputValuesListOfModule);

    if (adjacentModule.getAfiTypeId() != BSEL_AFITYPE_ID) {
      return MODULE_OPERATOR_MAP
          .get(adjacentModule.getAfiTypeId())
          .apply(inputValuesListOfModule, inputValuesSetOfModule, currentOutputValue);
    } else {
      return ModuleOperatorWrapper.bselOperator
          .apply(inputValuesListOfModule, adjacentModule, currentOutputValue);
    }
  }


  private LinkedList<Module> backTracePath(AlertsOnGateReason reason,
      FilterParams filterParams) {
    Port sourcePort = reason.getSourcePort();
    HashMap<Port, Port> visitedAdjacentMap = filterParams.getVisitedAdjacentMap();

    LinkedList<Module> path = Lists.newLinkedList();
    // int startPoint = sourcePort.getModule().getId();
    while (visitedAdjacentMap.size() != 0) {
      Port ancestorPoint = visitedAdjacentMap.get(sourcePort);
      if (ancestorPoint == null || ancestorPoint == reason.getRootPort()) {
        break;
      }
      //path.add(0, filterParams.getNetwork().getModules().get(accestorPoint));
      path.add(0, ancestorPoint.getModule());
      //startPoint = ancestorPoint;
      sourcePort = ancestorPoint;
    }
    Collections.reverse(path);
    return path;
  }


  //user signal before and same at alarm start millis
  private void updateCountAttribute(AlertsOnGateReason reason,
      FilterParams filterParams) {
    if (reason.getPotentialArchivedPort().isArchive()) {
      AtomicInteger count = calculateSignalBeforeOrSameCount(reason.getPotentialArchivedPort(),
          reason.getRootPort(), filterParams);
      if (count.get() > 0) {
        reason.setReasons(count.get());
        reason.setValue(1);
      } else {
        reason.setReasons(filterParams.getAlarms().size());
      }
    } else {
      reason.setReasons(filterParams.getAlarms().size());
    }
  }

  private AlertsOnGateReason updateAttributes(AlertsOnGateReason reason,
      FilterParams filterParams) {

    Module sourceModule = reason.getSourcePort().getModule();
    if (CONST_MODULE_PORT_IDS.keySet().contains(sourceModule.getAfiTypeId())) {
      String constant = getConstantValue(sourceModule);
      if (sourceModule.getAfiTypeId() == BSIG_AFITYPE_ID) {
        if (Boolean.parseBoolean(constant)) {
          reason.setValue(1);
        } else {
          reason.setValue(0);
        }
      } else if (sourceModule.getAfiTypeId() == T2000P_KON1_AFITYPE_ID) {
        reason.setValue(Integer.parseInt(constant));
      }

    } else {
      //if count>0, we need also to update value
      updateCountAttribute(reason, filterParams);
    }
    return reason;
  }

  protected Function<FilterParams, Optional<AlertsOnGateReason>> getSimpleReasonFilter() {
    return simpleReasonFilter;
  }

  protected Optional<Port> findArchivedProxyPort(Port currentNode, FilterParams filterParams) {
    return currentNode.getConnectedPorts().stream()
        .filter(connectedInputPort -> !isVisitedModule(filterParams, connectedInputPort)
        )
        .filter(connectedInputPort ->
            isConnectedProxyModule(connectedInputPort.getModule()))
        .map(connectedInputPort -> {
          Module module = connectedInputPort.getModule();
          // find output port of module
          // OR,AND,BIN: 1000, BDMZ*: 1010
          Optional<Port> port = module.findPortById(UNIFIED_OUT_PUT_ID);
          return port.isPresent() ? port : module.findPortById(UNIFIED_BDMZ_OUT_PUT_ID);
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(p -> p.isArchive() && p.isAlarm())
        .findAny();
  }

  private AlertsOnGateReason updateCodeForReason(AlertsOnGateReason reason,
      List<Module> interModules,
      Module sourceModule, Map<Module, Tuple<Integer, Integer>> moduleWithRelevance,
      Map<Module, Integer> moduleWithPropagationOutputValue) {

    //update code of problem for module, that is not a inter module
    if (interModules.size() == 0) {

      if (!isInterModule(sourceModule)
          && reason.getValue() == -1) {
        //source module is not inter module and output value of source signal is -1
        reason.setCode(2);
      } else if (!RELEVENT_NOT_MODULE_AFI_IDS
          .contains(reason.getRootPort().getModule().getAfiTypeId())) {
        // alarming module is not NOT,NAND,NOR, code =value of source signal
        reason.setCode(reason.getValue());
      } else if (RELEVENT_NOT_MODULE_AFI_IDS
          .contains(reason.getRootPort().getModule().getAfiTypeId())) {
        // if alarming module is NOT, NAND or NOR and value of Source signal = 1,
        // code of problem as 0
        // if alarming module is NOT, NAND or NOR and value of Source signal = 0,
        // code of problem as 1
        reason.setCode(reason.getValue() == 1 ? 0 : 1);
      }
    } else {

      Module proxyTargetModule = interModules.get(0);

      Integer sourceRelevantValue = moduleWithRelevance.get(sourceModule).getLeft();
      Integer targetOutputValue = moduleWithPropagationOutputValue
          .get(proxyTargetModule);

      //update code of problem for source module except inter module
      if (!ALLOWED_INTER_MODULE_PORT_IDS.keySet().contains(sourceModule.getAfiTypeId())) {
        if (reason.getValue() == -1) {
          reason.setCode(2);
        } else if (
            sourceRelevantValue.equals(targetOutputValue) && isSourceSignalRelevant(
                reason.getRootPort().getModule(), sourceRelevantValue, reason.getValue())
                && (
                (sourceRelevantValue == reason.getValue()
                    && !isRelevantNotModule(proxyTargetModule)) || (
                    sourceRelevantValue != reason.getValue()
                        && isRelevantNotModule(proxyTargetModule)))) {
          reason.setCode(1);
        } else {
          reason.setCode(0);
        }
      } else {
        //module is inter module
        if (sourceRelevantValue.equals(targetOutputValue)
            && isSourceSignalRelevant(reason.getRootPort().getModule(), sourceRelevantValue,
            reason.getValue())
            && reason.getPotentialArchivedPort().isArchive() && (
            (sourceRelevantValue == reason.getValue()
                && !isRelevantNotModule(proxyTargetModule)) || (
                sourceRelevantValue != reason.getValue()
                    && isRelevantNotModule(proxyTargetModule)))) {
          reason.setCode(1);
        } else {
          reason.setCode(0);
        }
      }
    }
    return reason;
  }

  private boolean isVisitedModule(FilterParams filterParams, Port connectedInputPort) {
    Module module = connectedInputPort.getModule();
    Optional<Port> proxy = module.findPortById(UNIFIED_OUT_PUT_ID);
    if (!proxy.isPresent()) {
      proxy = module.findPortById(UNIFIED_BDMZ_OUT_PUT_ID);
    }
    return isFunctionalInter(connectedInputPort.getModule()) && proxy.isPresent() &&
        filterParams.getVisitedAdjacentMap().containsKey(proxy.get());
  }

  private Tuple<Integer, Integer> initialTargetRelevantValue(Module rootModule,
      Module initialTarget) {

    //firstly,initial target module is root module,
    int relevance = 1;
    if (isRelevantNotModule(rootModule)) {
      if (isRelevantNotModule(initialTarget)) {
        return Tuple.of(1, relevance);
      } else {
        return Tuple.of(0, relevance);
      }
    } else {
      if (isRelevantNotModule(initialTarget)) {
        return Tuple.of(0, relevance);
      } else {
        return Tuple.of(1, relevance);
      }
    }
  }

  //***********************************Refactor********************//


  public LinkedHashMultimap<Integer, NodeReason> splitNetwork(Port root) {

    LinkedHashMultimap<Integer, NodeReason> hierarchyNetwork = LinkedHashMultimap.create();

    AtomicInteger depth = new AtomicInteger(-1);

    //search queue
    LinkedList<Port> searchQueue = Lists.newLinkedList();

    // collections for visited module with key=visitedNode, value=ancestorNode
    HashMap<Port, Port> visitedAdjacentMap = Maps.newHashMap();

    visitedAdjacentMap.put(root, null);
    searchQueue.add(root);
    searchQueue.add(null);

    while (searchQueue.size() > 1) {

      Port currentNode = searchQueue.poll();

      if (Objects.isNull(currentNode)) {
        depth.incrementAndGet();
        searchQueue.add(null);
      } else {

        Set<Port> extendableChildren = Sets.newHashSet();
        if (Objects.equals(currentNode, root) || isNodeExtendable(currentNode)) {
          extendableChildren = getInputPorts(currentNode)
              .map(Port::getConnectedOutPort)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .peek(descendant -> {
                    if (!visitedAdjacentMap.keySet().contains(descendant)) {
                      visitedAdjacentMap.put(descendant, currentNode);
                      searchQueue.add(descendant);
                    }
                  }
              ).collect(Collectors.toSet());
        }

        if (!Objects.equals(currentNode, root)) {
          hierarchyNetwork.put(depth.get(), new NodeReason.Builder(root, currentNode)
              .setAncestor(currentNode)
              .setChildren(extendableChildren)
              .setDepth(depth.get())
              .build());
        }

      }

      if (depth.get() >= MAX_DEPTH) {
        LOG.info(String.format("reached the maximal depth, current depth is %d", depth.get()));
        break;
      }
    }

    return hierarchyNetwork;
  }

  private boolean isNodeExtendable(Port node) {
    return !node.isArchive() && isFunctionalInter(
        node.getModule());
  }
}
