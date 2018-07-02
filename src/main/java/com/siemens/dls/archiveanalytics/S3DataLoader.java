/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.collect.*;
import com.opencsv.bean.CsvToBeanFilter;
import com.siemens.dls.archiveanalytics.model.*;
import com.siemens.dls.archiveanalytics.parser.DlsCsvParser;
import com.siemens.dls.archiveanalytics.parser.S3Reader;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides high-level access to data source for instances of {@link DlsLambdaHandler}, converting
 * data from source to instances of classes from {@link com.siemens.dls.archiveanalytics.model}
 *
 * @see com.siemens.dls.archiveanalytics.model
 */
public class S3DataLoader {

  public static final String AFI_CSV = "Afi.csv";
  public static final String PORTS_CSV = "Ports.csv";
  public static final String CONNECTIONS_CSV = "Connections.csv";
  protected static final String ALARM_CSV = "Alarm.csv";
  protected static final String OPER_ACT_CSV = "OperAct.csv";
  protected static final String ALARM_TYPE_CSV = "AlarmType.csv";
  protected static final String ANALOG_TREND_CSV = "AnalogTrend.csv";
  protected static final String BINARY_TREND_CSV = "BinaryTrend.csv";
  private static final ImmutableMap<Class<? extends AbstractTrend>, String> TREND_FILE_NAMES =
      ImmutableMap.of(
          AnalogTrend.class, ANALOG_TREND_CSV,
          BinaryTrend.class, BINARY_TREND_CSV
      );

  private final AmazonS3 s3Client;
  private String bucket;
  private DlsProducerLambdaParams params;

  private static final Logger LOGGER = Logger.getLogger(S3DataLoader.class);

  S3DataLoader() {
    this(prepareDefaultS3Client());
  }

  S3DataLoader(AmazonS3 s3Client) {
    this.s3Client = s3Client;
  }

  S3DataLoader(AmazonS3 s3Client, String bucketName) {
    this.s3Client = s3Client;
    this.bucket = bucketName;
  }

  S3DataLoader(String bucketName) {
    this(prepareDefaultS3Client(), bucketName);
  }

  /**
   * Returns a default client for S3
   *
   * @return a default {@link AmazonS3} client
   */
  private static AmazonS3 prepareDefaultS3Client() {
    return AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
  }

  /**
   * Setter for the params. Internally overrides the bucket settings if set.
   *
   * @param params Thze event parameters
   */
  void setParams(DlsProducerLambdaParams params) {
    this.params = params;
    // If bucket is set from params, override default
    if (params.getBucket() != null) {
      this.bucket = params.getBucket();
    }
  }

  /**
   * Same as {@link #loadPortsFromS3(CsvToBeanFilter, Map)}, but returns the {@link Stream}
   * collected to {@link List}
   *
   * @see #loadPortsFromS3(CsvToBeanFilter, Map)
   */
  public List<Port> loadPortsFromS3(CsvToBeanFilter filter, Map<Integer, Module> moduleLookup)
      throws IOException {
    return streamPortsFromS3(filter, moduleLookup)
        .collect(Collectors.toList());
  }

  /**
   * Load ports from S3 as {@link Port} instances. As the ports file is huge, in practice,
   * the data source MUST be filtered using {@link CsvToBeanFilter}
   * <p>
   * filtered by an CSV-validation-filter and by a {@link Map} of modules.
   *
   * @param filter a filter to reduce the number of CSV records to be converted to {@link Port}s
   * @param moduleLookup id-indexed {@link Map} of modules. When not null, each loaded port will be
   * assigned to the corresponding module from moduleLookup (if found) by using
   * {@link Module#addPort(Port)}
   */
  public Stream<Port> streamPortsFromS3(CsvToBeanFilter filter, Map<Integer, Module> moduleLookup)
      throws IOException {
    S3Reader portsReader = new S3Reader(s3Client, bucket, getEngFileKey(params, PORTS_CSV));
    return Utils.iteratorAsStream(getDlsCsvParser().loadPorts(portsReader, filter))
        .peek(p -> Optional.ofNullable(moduleLookup).map(l -> l.get(p.getAfiId()))
            .ifPresent(m -> m.addPort(p)));
  }

  DlsCsvParser getDlsCsvParser() {
    return new DlsCsvParser();
  }

  /**
   * Load all {@link Connection}s from S3
   */
  public Stream<Connection> loadConnections() throws IOException {
    return loadConnections(null);
  }

  /**
   * Load {@link Connection}s from S3, filtered by {@link CsvToBeanFilter}
   */
  public Stream<Connection> loadConnections(CsvToBeanFilter filter) throws IOException {
    S3Reader connReader = new S3Reader(s3Client, bucket,
        getEngFileKey(params, S3DataLoader.CONNECTIONS_CSV));
    DlsCsvParser parser = getDlsCsvParser();
    return Utils.iteratorAsStream(parser.loadConnections(connReader, filter));
  }

  /**
   * Load alarm types from S3 and provide them as a {@link Map} of id-indexed Strings
   *
   * @return An id-indexed {@link Map} of AlarmTypes
   */
  public Map<Integer, AlarmType> getAlarmTypesFromS3() {
    S3Reader alarmTypesReader = new S3Reader(
        s3Client, bucket, getEngFileKey(params, ALARM_TYPE_CSV)
    );
    return Utils.iteratorAsStream(getDlsCsvParser().loadAlarmTypes(alarmTypesReader))
        .collect(Collectors.toMap(AlarmType::getId, at -> at));
  }

  /**
   * Build and provide the fully prefixed S3 object key of an arbitrary engineering object
   *
   * @param params The handler param set
   * @param fileName An arbitrary relative file/object name
   * @return The fully prefixed object name
   */
  String getEngFileKey(DlsProducerLambdaParams params, String fileName) {
    return String.format("%s/%s", params.getPaths()
        .getEngineering()
        .replaceAll("/$", ""), fileName);
  }

  /**
   * Build and provide the fully prefixed S3 object key of an arbitrary archive object
   *
   * @param params The handler param set
   * @param fileName An arbitrary relative file/object name
   * @return The fully prefixed object name
   */
  private String getArcFileKey(DlsProducerLambdaParams params, String fileName) {
    return String.format("%s/%s", params.getPaths()
        .getArchive()
        .replaceAll("/$", ""), fileName);
  }

  /**
   * Load modules from S3 and provide them as a {@link Map} of id-indexed {@link Module} instances.
   *
   * @param filter A validation filter for the CSV-format
   * @return An id-indexed {@link Map} of {@link Module}s
   */
  public Map<Integer, Module> getModulesByIdFromS3(CsvToBeanFilter filter) throws IOException {
    S3Reader modulesReader = new S3Reader(s3Client, bucket,
        getEngFileKey(params, S3DataLoader.AFI_CSV));
    return Utils.iteratorAsStream(getDlsCsvParser().loadModules(modulesReader, filter))
        .collect(Collectors.toMap(Module::getId, m -> m));
  }

  /**
   * Load {@link Alarm}s from S3, filtered by {@link CsvToBeanFilter}
   */
  public Set<Alarm> getAlarmsFromS3(@Nullable CsvToBeanFilter filter) {
    S3Reader alarmReader = new S3Reader(s3Client, bucket,
        getArcFileKey(params, S3DataLoader.ALARM_CSV));
    return Utils.iteratorAsStream(getDlsCsvParser().loadAlarms(alarmReader, filter))
        .collect(Collectors.toSet());
  }

  /**
   * Load {@link Alarm}s that were raised on given set ot {@link Port}s. All loaded alarms have
   * their respective ports assigned to them. See {@link Alarm#getPort()}
   *
   * @return {@link List}s of {@link Alarm}s indexed by the respective port. The order of
   * alarms in the lists is not specified.
   */
  public Map<Port, List<Alarm>> getAlarmsFromS3(Set<Port> ports) {
    ImmutableMap<String, Port> portsByTagname = Maps
        .uniqueIndex(ports, Port::getUniqueName);

    Set<Alarm> alarmsFromS3 = getAlarmsFromS3(
        line -> portsByTagname.containsKey(Alarm.extractTagname(line)));

    return alarmsFromS3.stream()
        .peek(a -> a.setPort(portsByTagname.get(a.getTagname())))
        .collect(Collectors.groupingBy(Alarm::getPort));

  }

  /**
   * Load analog trend values for a {@link Set} of ports
   *
   * @param uniquePortNames A {@link Set} of port names
   * @return A {@link Map} of {@link AbstractTrend} values, indexed by the original port name
   */
  public Map<String, AnalogTrend> loadAnalogTrends(Set<String> uniquePortNames) throws IOException {
    return loadTrends(AnalogTrend.class, uniquePortNames, AnalogTrend::lineToTrend);
  }

  /**
   * Load binary trend values for a {@link Set} of ports
   *
   * @param uniquePortNames A {@link Set} of port names
   * @return A {@link Map} of {@link AbstractTrend} values, indexed by the original port name
   */
  public Map<String, BinaryTrend> loadBinaryTrends(Set<String> uniquePortNames) throws IOException {
    return loadTrends(BinaryTrend.class, uniquePortNames, BinaryTrend::lineToTrend);
  }

  /**
   * Load binary trend values for a {@link Set} of ports
   *
   * @return A {@link Map} of {@link AbstractTrend} values, indexed by the original port
   */
  public Map<Port, BinaryTrend> getBinaryTrends(Set<Port> ports) throws IOException {
    return getTrends(BinaryTrend.class, ports, BinaryTrend::lineToTrend);
  }

  /**
   * Load {@link OperatorAction}s from S3, filtered by {@link CsvToBeanFilter}
   */
  private Set<OperatorAction> loadOperatorActionsFromS3(@Nullable CsvToBeanFilter filter) {
    S3Reader operActReader = new S3Reader(s3Client, bucket,
        getArcFileKey(params, S3DataLoader.OPER_ACT_CSV));
    return Utils.iteratorAsStream(getDlsCsvParser().loadOperatorActions(operActReader, filter))
        .collect(Collectors.toSet());
  }

  public Multimap<Port, OperatorAction> getOperatorActions(Set<Port> ports){
    ImmutableMap<String, Port> portsByTagname = Maps
        .uniqueIndex(ports, Port::getUniqueName);

    Set<OperatorAction> operActsFromS3 = loadOperatorActionsFromS3(
        line -> portsByTagname.keySet().contains(OperatorAction.extractTagname(line)));
    HashMultimap<Port, OperatorAction> result = HashMultimap.create();
    operActsFromS3.stream()
        .peek(oa -> oa.setPort(portsByTagname.get(oa.getTagname())))
        .forEach(oa -> result.put(oa.getPort(), oa));
    return result;
  }

  /**
   * Load analog trend values for a {@link Set} of ports
   *
   * @return A {@link Map} of {@link AbstractTrend} values, indexed by the original port
   */
  public Map<Port, AnalogTrend> getAnalogTrends(Set<Port> ports) throws IOException {
    return getTrends(AnalogTrend.class, ports, AnalogTrend::lineToTrend);
  }

  /**
   * Load trend values of arbitary values for a {@link Set} of ports
   *
   * @param uniquePortNames A {@link Set} of port names
   * @param transformer
   * @return A {@link Map} of {@link AbstractTrend} values, indexed by the original port name
   */
  private <T extends AbstractTrend<T>> Map<String, T> loadTrends(Class<T> trendClazz,
      Set<String> uniquePortNames, Function<String[], T> transformer) throws IOException {
    LOGGER.info("Reading trends from S3");
    S3Reader trendsReader = new S3Reader(s3Client, bucket,
        getArcFileKey(params, TREND_FILE_NAMES.get(trendClazz)));
    Map<String, T> trendsByUniqueName = Utils.iteratorAsStream(getDlsCsvParser()
        .<T>loadTrends(trendsReader,
            line -> uniquePortNames.contains(AbstractTrend.extractTagname(line)), transformer))
        .collect(Collectors.toMap(T::getUniqueName, m -> m));
    LOGGER.info("Read and indexed trends from S3: " + trendsByUniqueName.size());
    return trendsByUniqueName;
  }

  private <T extends AbstractTrend<T>> Map<Port, T> getTrends(Class<T> clazz, Set<Port> ports,
      Function<String[], T> transformer)
      throws IOException {
    // In the rare case when the plant has been engineered incorrectly and has multiple ports
    // with the same uniquename, the trends for these ports will be cloned and assigned to
    // all of them.
    ImmutableListMultimap<String, Port> portsByTagname = Multimaps
        .index(ports, Port::getUniqueName);

    Map<String, T> trendsFromS3 = loadTrends(clazz, portsByTagname.keySet(), transformer);
    return trendsFromS3.values().stream()
        .flatMap(trend -> {
          ImmutableList<Port> portsForUniqueName = portsByTagname.get(trend.getUniqueName());
          if (portsForUniqueName.size() == 1) {
            trend.setPort(portsForUniqueName.get(0));
            return Stream.of(trend);
          } else {
            return portsForUniqueName.stream()
                .map(p -> {
                  T clonedTrend = trend.copy();
                  clonedTrend.setPort(p);
                  return clonedTrend;
                });
          }
        })
        .collect(Collectors.toMap(T::getPort, t -> t));
  }

  /**
   * Load an interconnected {@link Network} of modules, filtering the available data to provide
   * a sub-view on the complete plant network.
   *
   * @param moduleFilter filters what modules are to be loaded
   * @param portFilterCreator given an id-indexed {@link Map} of loaded modules,
   * provides a {@link CsvToBeanFilter} to filter {@link Port}s. Ports that do not belong to the
   * loaded modules are filtered out automatically. {@link CsvToBeanFilter} provided by
   * {@code portFilterCreator} only has to perform any necessary additional filtering on top of that
   * @see Network
   */
  public Network loadNetwork(CsvToBeanFilter moduleFilter,
      Function<Map<Integer, Module>, CsvToBeanFilter> portFilterCreator) throws IOException {
    Stream<Connection> connectionStream = loadConnections();

    return loadNetworkInternal(moduleFilter, portFilterCreator, connectionStream);
  }

  private Network loadNetworkInternal(CsvToBeanFilter moduleFilter,
      Function<Map<Integer, Module>, CsvToBeanFilter> portFilterCreator,
      Stream<Connection> connectionStream) throws IOException {
    LOGGER.info("Loading complete plant network. This may take a while.");
    Map<Integer, Module> modulesById = getModulesByIdFromS3(moduleFilter);
    LOGGER.info("Loaded modules: " + modulesById.size());

    CsvToBeanFilter portFilter = portFilterCreator.apply(modulesById);

    Stream<Port> ports = streamPortsFromS3(
        (String[] line) -> modulesById.containsKey(Port.extractAfiId(line)) &&
            portFilter.allowLine(line),
        modulesById);

    return new Network(modulesById, connectionStream, ports);
  }

  /**
   * Load an interconnected {@link Network} of modules, filtering the available data to provide
   * a sub-view on the complete plant network.
   *
   * @param portIdsByModuleTypeIds The filter which is defined as a map of afiTypeIds to sets of
   * portIds that have to be included into network. An empty set of port ids indicates that all
   * ports of the given module have to be loaded.
   * @see Network
   */
  public Network loadNetwork(Map<Integer, Set<Integer>> portIdsByModuleTypeIds) throws IOException {
    return loadNetwork(portIdsByModuleTypeIds, Collections.emptyMap());
  }

  /**
   * Load an interconnected {@link Network} of modules, filtering the available data to provide
   * a sub-view on the complete plant network. This method allows loading a network of known
   * (by their afiTypeId) modules together with any unknown modules connected to some known ports.
   * This method provides a better performing alternative to loading a network, searching for
   * dangling ports, and then extending the network by {@link S3DataLoader#extendNetwork}, because
   * the latter requires two passes over Ports.csv.
   *
   * @param portIdsByModuleTypeIds The filter which is defined as a map of afiTypeIds to sets of
   * portIds that have to be included into network. An empty set of port ids indicates that all
   * ports of the given module have to be loaded.
   * @param extensionPoints A map of afiTypeIds to sets portIds which defines the extension points
   * where the network of known modules has to be extended with any modules connected to the ports
   * defined by this filter. Has to be a subset of {@code portIdsByModuleTypeIds}, otherwise the
   * returned network may not make much sense.
   * @see Network
   */
  public Network  loadNetwork(Map<Integer, Set<Integer>> portIdsByModuleTypeIds,
      Map<Integer, Set<Integer>> extensionPoints) throws IOException {
    Stream<Connection> connectionStream = loadConnections();
    final Set<PortKey> additionalPortKeysToLoad;
    final Stream<Connection> readableConnectionsStream;

    // not strictly necessary, but saves iterating over connections and loading modules
    // when no extensions are requested
    if (extensionPoints.isEmpty()) {
      additionalPortKeysToLoad = Collections.emptySet();
      readableConnectionsStream = connectionStream;
    } else {
      Set<Connection> allConnections = connectionStream.collect(Collectors.toSet());
      Map<Integer, Module> modulesById = getModulesByIdFromS3(line ->
          portIdsByModuleTypeIds.keySet().contains(Module.extractAfiType(line)));

      additionalPortKeysToLoad = allConnections.stream()
          .filter(c ->
              isPortAcceptedByFilterMap(c.getOut(), extensionPoints, modulesById) ||
                  isPortAcceptedByFilterMap(c.getIn(), extensionPoints, modulesById))
          .map(c -> isPortAcceptedByFilterMap(c.getIn(), extensionPoints, modulesById) ? c.getOut()
              : c.getIn())
          .collect(Collectors.toSet());
      readableConnectionsStream = allConnections.stream();
    }

    LOGGER.info(String.format("Found %d additional port keys to load", additionalPortKeysToLoad.size()));

    Set<Integer> additionalModuleIdsToLoad = additionalPortKeysToLoad.stream()
        .map(PortKey::getAfiId).collect(Collectors.toSet());

    CsvToBeanFilter moduleFilter = line ->
        portIdsByModuleTypeIds.keySet().contains(Module.extractAfiType(line)) ||
            additionalModuleIdsToLoad.contains(Module.extractId(line));

    Function<Map<Integer, Module>, CsvToBeanFilter> portFilterCreator = modulesById ->
        (line -> {
          int afiId = Port.extractAfiId(line);
          int portId = Port.extractId(line);
          return isPortAcceptedByFilterMap(modulesById.get(afiId).getAfiTypeId(), portId,
              portIdsByModuleTypeIds
          ) ||
              additionalPortKeysToLoad.contains(new PortKey(afiId, portId));
        });
    return loadNetworkInternal(moduleFilter, portFilterCreator, readableConnectionsStream);
  }

  private boolean isPortAcceptedByFilterMap(PortKey portKey,
      Map<Integer, Set<Integer>> map,
      Map<Integer, Module> modulesById) {
    return modulesById.containsKey(portKey.getAfiId()) && isPortAcceptedByFilterMap(
        modulesById.get(portKey.getAfiId()).getAfiTypeId(), portKey.getPortId(), map
    );
  }

  private boolean isPortAcceptedByFilterMap(int afiTypeId, int portId,
      Map<Integer, Set<Integer>> map) {
    return map.containsKey(afiTypeId) && (map.get(afiTypeId).contains(portId) || map.get(afiTypeId)
        .isEmpty());
  }

  /**
   * Extend the {@link Network} by loading the modules connected to the given {@link Set} of input
   * ports. The modules are loaded only with necessary output ports to form the connections.
   * No other ports of the newly loaded modules are loaded.
   */
  public Network extendNetwork(Network network, Set<Port> danglingInputPorts) throws IOException {
    Set<PortKey> portKeys = network.getMissingPortKeys(danglingInputPorts);
    Set<Integer> moduleIds = portKeys.stream().map(PortKey::getAfiId).collect(Collectors.toSet());
    Map<Integer, Module> newModules = getModulesByIdFromS3(
        line -> moduleIds.contains(Module.extractId(line)));
    Stream<Port> newPorts = streamPortsFromS3(
        line -> portKeys.contains(new PortKey(Port.extractAfiId(line), Port.extractId(line))),
        newModules);
    return network.extendWith(newModules, newPorts);
  }
}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
