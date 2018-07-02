/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.model.Connection;
import com.siemens.dls.archiveanalytics.model.JoinRow;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import com.siemens.dls.archiveanalytics.model.PortKey;
import com.siemens.dls.archiveanalytics.model.Tuple;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class FumHandler extends DlsLambdaHandler {

  protected static final int ENG_RESULT_CONNECTION_INDEX = 0;
  protected static final int ENG_RESULT_HW_STATUS_PORT_INDEX = 1;
  protected static final int ENG_RESULT_FUM_ALARM_PORT_INDEX = 2;
  protected static final int ENG_RESULT_HW_ALARM_PORT_INDEX = 3;

  public static final String PORT_NAME_ALARM = "ALARM";
  public static final List<Pattern> FUM_MODULE_PREFIX_MATCHERS = ImmutableList.<Pattern>builder()
      .add(Pattern.compile("(FUM210BT).*"))
      .add(Pattern.compile("(FUM210IO).*"))
      .add(Pattern.compile("(FUM211IO).*"))
      .add(Pattern.compile("(FUM230AT).*"))
      .add(Pattern.compile("(FUM230HART).*"))
      .add(Pattern.compile("(FUM231IO).*"))
      .add(Pattern.compile("(FUM232TT).*"))
      .add(Pattern.compile("(FUM280AO).*"))
      .add(Pattern.compile("(FUM280IO).*"))
      .add(Pattern.compile("(FUM511IO).*"))
      .add(Pattern.compile("(FUM531IO).*"))
      .add(Pattern.compile("(FUM).*")) // must be last in the list, or it will override everything
      .build();

  public static final List<Pattern> PORT_NAME_PREFIX_MATCHERS = ImmutableList.<Pattern>builder()
      .add(Pattern.compile("(STATUS_FUM).*"))
      .add(Pattern.compile("(STATUS_BUS1).*"))
      .add(Pattern.compile("(STATUS_BUS2).*"))
      .add(Pattern.compile("(STATUS_DP).*"))
      .add(Pattern.compile("(DI).*"))
      .add(Pattern.compile("(AI).*"))
      .build();

  private static final Map<String, Set<Integer>> FUM_SYMBOLS = ImmutableMap
      .of("FUM_ASMON", ImmutableSet.of(20, 30),
          "FUM_BSMON", ImmutableSet.of(10, 20),
          "FUM_TTMON", ImmutableSet.of(10, 20));

  private static final String STATUS_SUFFIX = "_STATUS";
  private static final String DDB_FIELDDIAG = "diag";
  private static final String DDB_FIELD_RISING = "rising";
  private static final String DDB_FIELD_MESSAGEID = "messageid";

  /**
   * Default constructor
   */
  public FumHandler() {
    super();
  }

  /**
   * Constructor for creating the handler, which is the starting point of the lambda function
   *
   * @param s3 The {@link AmazonS3} client that will be used
   * @param ddb the {@link AmazonDynamoDB} client that will be used
   */
  public FumHandler(AmazonS3 s3, AmazonDynamoDB ddb) {
    super(s3, ddb);
  }

  /**
   * Combine the engineering data to one {@link List}
   *
   * @param connections The {@link Set} of module connections
   * @param hwOutputPortsByKey Map of output ports indexed by port key
   * @param fumInputPortsByKey Map of input ports indexed by port key
   * @return A {@link List} of the joined ports by their connections
   */
  protected List<JoinRow> collectEngResult(Set<Connection> connections,
      ImmutableMap<PortKey, Port> hwOutputPortsByKey,
      ImmutableMap<PortKey, Port> fumInputPortsByKey) {
    return connections.stream()
        .peek(c -> {
          c.setInPort(fumInputPortsByKey.get(c.getIn()));
          c.setOutPort(hwOutputPortsByKey.get(c.getOut()));
        })
        .filter(c -> findStatusPort(c) != null)
        .map(c -> new JoinRow()
            .addValue(c)
            .addValue(findStatusPort(c))
            .addValue(findTrblAlPort(c))
            .addValue(findAlarmPort(c))
        )
        .collect(Collectors.toList());
  }

  /**
   * Find an alarm {@link Port} in a {@link Connection} instance
   *
   * @param conn a {@link Connection} instance
   * @return A {@link Port} instance of a status port or null if none exists
   */
  protected Port findAlarmPort(Connection conn) {
    Port outPort = conn.getOutPort();
    if (outPort == null) {
      return null;
    }
    Module hwMod = outPort.getModule();
    return hwMod.getOutPorts().stream()
        .filter(p -> p.getName().equals(PORT_NAME_ALARM))
        .findFirst().orElse(null);
  }

  /**
   * Provides the alerting {@link Port} of a {@link Connection}
   *
   * @param c The {@link Connection} to search
   * @return The resulting {@link Port}
   */
  protected Port findTrblAlPort(Connection c) {
    Port inPort = c.getInPort();
    if (inPort == null) {
      return null;
    }
    // we only loaded the TRBL_AL ports, so there must be only one in the list
    return inPort.getModule().getOutPorts().stream().limit(1).findFirst().orElse(null);
  }

  /**
   * Load the output ports of hardware modules from S3
   *
   * @param hwModulesById A {@link Map} of modules indexed by their id
   * @return A {@link List} of {@link Port}s
   */
  protected List<Port> loadHwOutputPorts(Map<Integer, Module> hwModulesById) throws IOException {
    List<Port> hwOutputPorts = s3DataLoader.loadPortsFromS3(
        line -> line.length > Port.IO_COLUMN_INDEX &&
            hwModulesById.containsKey(Port.extractAfiId(line)) &&
            PortDirection.O == Port.extractDirection(line), hwModulesById);
    LOGGER.info("loaded hw output ports: " + hwOutputPorts.size());
    return hwOutputPorts;
  }

  /**
   * Load the hardware modules from S3
   *
   * @param hwModuleIds A {@link Set} to limit the loaded modules
   * @return The hardware {@link Module} instances indexed by their id
   */
  protected Map<Integer, Module> loadHwModules(Set<Integer> hwModuleIds) throws IOException {
    Map<Integer, Module> hwModulesById = s3DataLoader.getModulesByIdFromS3(
        line -> line.length > Module.ID_COLUMN_INDEX &&
            hwModuleIds.contains(Module.extractId(line)));
    LOGGER.info("loaded hw modules: " + hwModulesById.size());
    return hwModulesById;
  }

  /**
   * Load {@link Connection}s to a {@link Set} of inport ports of FUM modules *
   *
   * @return A {@link Set} of {@link Connection} instances
   * @throws IOException if th loading operation failed
   */
  protected Set<Connection> loadConnectionsToFumInputPorts(
      Set<Tuple<Integer, Integer>> fumInputPortKeyRefs) throws IOException {
    Set<Connection> connections = s3DataLoader.loadConnections(
        line -> line.length > Connection.PORT2_COLUMN_INDEX
            && fumInputPortKeyRefs.contains(
            new Tuple<>(Connection.extractAfi2(line),
                Connection.extractPort2(line)))).collect(Collectors.toSet());
    LOGGER.info("loaded connections: " + connections.size());
    return connections;
  }

  /**
   * Converts a {@link List} of input {@link Port}s to a {@link Set} of their key references
   *
   * @param fumInputPorts The input {@link List}
   * @return The created {@link Set}
   */
  protected Set<Tuple<Integer, Integer>> collectFumInputPortKeyRefs(List<Port> fumInputPorts) {
    Set<Tuple<Integer, Integer>> fumInputPortKeyRefs = fumInputPorts.stream()
        .map(p -> new Tuple<>(p.getAfiId(), p.getId()))
        .collect(Collectors.toSet());
    LOGGER.info("Collected fumInputPorts key refs: " + fumInputPortKeyRefs.size());
    return fumInputPortKeyRefs;
  }

  /**
   * Load ports for specified FUM modules from S3
   *
   * @param fumModulesById An id-indexec {@link Map} of FUM {@link Module}s
   * @return A {@link List} of {@link Port}s
   */
  protected List<Port> loadFumInputAndTrblAlPorts(Map<Integer, Module> fumModulesById)
      throws IOException {
    List<Port> fumInputAndTrblAlPorts = s3DataLoader.loadPortsFromS3(
        line -> {
          if (line.length <= Port.IO_COLUMN_INDEX) {
            return false;
          }
          int afiid = Port.extractAfiId(line);
          return fumModulesById
              .containsKey(afiid) && // port belongs to a FUM module
              (PortDirection.I == Port.extractDirection(line) //port direction is I
                  && FUM_SYMBOLS.get(fumModulesById.get(afiid)
                  .getSymbol()) //port is one of the allowed input ports for the FUM module type
                  .contains(Port.extractId(line)) ||
                  PORT_NAME_TRBL_AL.equals(Port.extractName(line))); //port is TRBL_AL

        }, fumModulesById);

    LOGGER.info("Loaded fumInputAndTrblAlPorts from S3: " + fumInputAndTrblAlPorts.size());
    return fumInputAndTrblAlPorts;
  }

  /**
   * Load all FUM {@link Module}s from S3
   *
   * @return An id-indexed {@link Map} of all FUM {@link Module}s
   */
  protected Map<Integer, Module> loadFumModules() throws IOException {
    LOGGER.info("Reading FUM modules from S3");
    Map<Integer, Module> fumModulesById = s3DataLoader.getModulesByIdFromS3(
        line -> line.length > Module.SYMBOL_COLUMN_INDEX && FUM_SYMBOLS
            .containsKey(Module.extractSymbol(line)));
    LOGGER.info("Read and indexed FUM modules from S3: " + fumModulesById.size());
    return fumModulesById;
  }

  /**
   * Find a status {@link Port} in a {@link Connection} instance
   *
   * @param conn a {@link Connection} instance
   * @return A {@link Port} instance of a status port or null if none exists
   */
  protected Port findStatusPort(Connection conn) {
    Port outPort = conn.getOutPort();
    if (outPort == null) {
      return null;
    }
    Module hwMod = outPort.getModule();
    return hwMod.getOutPorts().stream()
        .filter(p -> p.getName().equals(outPort.getName() + STATUS_SUFFIX))
        .findFirst().orElse(null);
  }

  /**
   * Creates an array of measurements for a specific status port
   *
   * @param outPort The status port
   * @param counts A Map of values and corresponding event counts
   * @return A {@link JSONArray} of measurements
   */
  protected JSONArray getMeasurementsForStatusPort(Port outPort,
      Map<Integer, Integer> counts) {
    JSONArray measurements = new JSONArray();
    counts.entrySet().stream().map(e -> {

      JSONObject measurement = new JSONObject();
      int value = e.getKey();
      int count = e.getValue();

      String msg = String
          .format("%s__%s__%s", value,
              mapStringForMessageId(outPort.getModule().getSymbol(), FUM_MODULE_PREFIX_MATCHERS),
              mapStringForMessageId(outPort.getName(), PORT_NAME_PREFIX_MATCHERS));

      measurement.put(DDB_FIELDDIAG, value);
      measurement.put(DDB_FIELD_RISING, count);
      measurement.put(DDB_FIELD_MESSAGEID, msg);

      return measurement;
    }).forEach(measurements::put);
    return measurements;
  }

  protected String mapStringForMessageId(String original, List<Pattern> patterns) {
    return patterns.stream()
        .filter(p -> p.matcher(original).matches())
        .findFirst()
        .map(p -> {
          Matcher matcher = p.matcher(original);
          if (!matcher.find()) {
            throw new IllegalStateException("after filter, this pattern must find something");
          }
          String group = matcher.group(1);
          return group == null ? original : group;
        }).orElse(original);
  }

  /**
   * Calculate the engineering data for the defective measurements part
   *
   * @return A {@link List} of the engineering data
   * @throws IOException If no connections could be loaded
   */
  protected List<JoinRow> calculateEngData()
      throws IOException {
    Map<Integer, Module> fumModulesById = loadFumModules();
    List<Port> fumInputAndTrblAlPorts = loadFumInputAndTrblAlPorts(
        fumModulesById);
    Map<PortDirection, List<Port>> fumPortsGroupedByDirection = fumInputAndTrblAlPorts.stream()
        .collect(Collectors.groupingBy(Port::getDirection));
    Set<Tuple<Integer, Integer>> fumInputPortKeyRefs = collectFumInputPortKeyRefs(
        fumPortsGroupedByDirection.get(PortDirection.I));

    Set<Connection> connections = loadConnectionsToFumInputPorts(fumInputPortKeyRefs);

    Set<Integer> hwModuleIds = connections.stream().map(c -> c.getOut().getAfiId())
        .collect(Collectors.toSet());
    LOGGER.info("collected hwModuleIds: " + hwModuleIds.size());

    Map<Integer, Module> hwModulesById = loadHwModules(hwModuleIds);

    List<Port> hwOutputPorts = loadHwOutputPorts(hwModulesById);

    ImmutableMap<PortKey, Port> hwOutputPortsByKey = Maps
        .uniqueIndex(hwOutputPorts, Port::getKey);
    ImmutableMap<PortKey, Port> fumPortsByKey = Maps
        .uniqueIndex(fumInputAndTrblAlPorts, Port::getKey);
    LOGGER.info("indexed all ports");

    return collectEngResult(connections, hwOutputPortsByKey,
        fumPortsByKey);
  }

  /**
   * Get a {@link Map} of alert counts and corresponding defcodes where defcode is > 0 grouped by
   * the unique name of the creating module's uniquename
   *
   * @param uniquePortNames A unique {@link Set} of all port names
   * @return A Map of uniquenames containing maps of alert defcodes and their counts
   */
  protected Map<String, Map<Integer, Integer>> getAlertCountsByDefcodeByPortUniqueNames(
      Set<String> uniquePortNames) throws IOException {
    return Maps
        .transformEntries(s3DataLoader.loadAnalogTrends(uniquePortNames), (uniqueName, analogTrend) -> {
          Map<Integer, Integer> countsByValue = new HashMap<>();
          analogTrend.getTrends().stream()
              .filter(t -> t.getMillis() != 0)
              .filter(t -> t.getValue() > 0)  //defcode (diag) > 0
              .forEach(t -> countsByValue.merge((int) t.getValue(), 1, (i1, i2) -> i1 + i2));
          return countsByValue;
        });
  }

}
