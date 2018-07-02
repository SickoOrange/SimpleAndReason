/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a partial view on the network of modules of the power plant connected to each other.
 * Can be extended in a step-by-step fashion so that loading of all modules and ports is not
 * necessary.
 */
public class Network {

  public static final Comparator<Port> PORT_COMPARATOR = Comparator.comparing(Port::getAfiId)
      .thenComparing(Port::getId);
  private Map<Integer, Module> modules;
  private Multimap<PortKey, Connection> connections;
  private Set<Port> ports;
  protected static final Logger LOG = Logger.getLogger(Network.class);


  private Network() {
    modules = new HashMap<>();
    ports = new HashSet<>();
  }

  /**
   * Constructs a {@link Network} given an id-indexed map of modules, a {@link Stream} of
   * {@link Connection}s, and a {@link Stream} of {@link Port}s.
   * The network assumes that all ports already have been assigned to respective modules via
   * {@link Module#addPort(Port)}. This assumption may be relaxed at a later date.
   *
   * The Network connects all input and output {@link Port}s together, as defined by given
   * {@link Connection}s.
   */
  public Network(
      Map<Integer, Module> modules,
      Stream<Connection> connectionStream,
      Stream<Port> portStream) {
    this();

    connections = HashMultimap.create();
    connectionStream.forEach(c -> connections.put(c.getIn(), c));
    LOG.info("Indexed connections: " + connections.size());

    extendWith(modules, portStream);
  }

  /**
   * Entrance point to the network
   *
   * @return Id-indexed map of all {@Module}s in the network
   */
  public Map<Integer, Module> getModules() {
    return Collections.unmodifiableMap(modules);
  }

  /**
   * Given a set of input ports finds all PortKeys of output ports to which those input ports
   * should be connected. Is used to determine what ports and/or modules should be loaded
   * additionally to complete connections to the given input ports.
   *
   * @return {@link Set} of {@PortKey}s the given input ports should be connected to
   */
  public Set<PortKey> getMissingPortKeys(Set<Port> danglingInputPorts) {
    return danglingInputPorts.stream()
        .map(p -> connections.get(p.getKey()))
        .flatMap(Collection::stream)
        .map(Connection::getOut)
        .collect(Collectors.toSet());
  }

  /**
   * Extends the network by adding new modules and new ports to it, connecting any ports as defined
   * by connections that were given during creation (old ports get connected to new ports and new
   * ports to each other)
   *
   * @return fluent interface
   */
  public Network extendWith(Map<Integer, Module> newModules, Stream<Port> newPorts) {
    modules.putAll(newModules);
    Set<Port> newPortsSet = newPorts.collect(Collectors.toSet());
    ports.addAll(newPortsSet);
    List<Port> sortedPorts = new ArrayList<>(ports);
    sortedPorts.sort(PORT_COMPARATOR);
    LOG.info("Sorted ports: " + sortedPorts.size());

    ports.stream()
        .filter(p -> p.getDirection() == PortDirection.I)
        .forEach(port -> {
          PortKey key = port.getKey();
          if (connections.containsKey(key)) {
            Collection<Connection> portConnections = connections.get(key);
            portConnections.forEach(c -> {
              int outPortIndex = Collections
                  .binarySearch(sortedPorts, new Port().setKey(c.getOut()), PORT_COMPARATOR);
              if (outPortIndex >= 0) {
                port.addToConnectedPorts(sortedPorts.get(outPortIndex));
              } else { // corresponding out port has not been loaded
                // mark the port as connected to something, because there exists a connection
                port.markConnected();
              }
            });
          }
        });
    LOG.info("Connected ports");
    return this;
  }

  /**
   * Returns a single {@link Set} of all {@link Port}s in the network that match the specified
   * filter. The filter is defined as a map of afiTypeIds to sets of port ids which are to be
   * accepted.
   */
  public Set<Port> getFilteredPorts(Map<Integer, Set<Integer>> filter) {
    Predicate<Module> moduleFilter = m -> filter.containsKey(m.getAfiTypeId());
    Function<Module, Predicate<Port>> portFilterCreator = m -> p -> filter.get(m.getAfiTypeId()).contains(p.getId());
    return getFilteredPorts(moduleFilter, portFilterCreator);
  }

  /**
   * Returns a single {@link Set} of all {@link Port}s in the network that match the specified
   * predicates.
   * @param moduleFilter predicate to match modules
   * @param portFilterCreator function that creates the predicate to match ports, given an
   * id-indexed map of modules
   */
  public Set<Port> getFilteredPorts(Predicate<Module> moduleFilter, Function<Module, Predicate<Port>> portFilterCreator) {
    return getModules().values().stream()
        .filter(moduleFilter) // is one of searched modules
        .map(m -> m.getOutPorts().stream()
            .filter(p -> portFilterCreator.apply(m).test(p)))
        .flatMap(s -> s)
        .collect(Collectors.toSet());
  }


}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
