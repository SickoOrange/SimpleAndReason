/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines a Module.
 */
public class Module {

  //afiid;nodeid;afitypeid;symbol;name;designation;afcid;cycle
  public static final int ID_COLUMN_INDEX = 0;
  public static final int NODE_COLUMN_INDEX = 1;
  public static final int AFI_TYPE_COLUMN_INDEX = 2;
  public static final int SYMBOL_COLUMN_INDEX = 3;
  public static final int NAME_COLUMN_INDEX = 4;

  @CsvBindByPosition(position = AFI_TYPE_COLUMN_INDEX)
  private int afiTypeId;
  @CsvBindByPosition(position = NODE_COLUMN_INDEX)
  private int node; // node
  @CsvBindByPosition(position = ID_COLUMN_INDEX)
  private int id; // afi
  @CsvBindByPosition(position = SYMBOL_COLUMN_INDEX)
  private String symbol;
  @CsvBindByPosition(position = NAME_COLUMN_INDEX)
  private String name;

  private List<Port> inPorts;
  private List<Port> outPorts;

  /**
   * default constructor
   */
  public Module() {
    //noop
  }

  /**
   * Extracts afi id from a split CSV line.
   */
  public static int extractId(String[] line) {
    return toInt(line[ID_COLUMN_INDEX]);
  }

  /**
   * Extracts node from a split CSV line.
   */
  public static int extractNode(String[] line) {
    return toInt(line[NODE_COLUMN_INDEX]);
  }
  /**
   * Extracts afi type id from a split CSV line.
   */
  public static int extractAfiType(String[] line) {
    return toInt(line[AFI_TYPE_COLUMN_INDEX]);
  }

  /**
   * Extracts symbol (module name) from a split CSV line.
   */
  public static String extractSymbol(String[] line) {
    return line[SYMBOL_COLUMN_INDEX];
  }

  /**
   * Extracts symbol (module name) from a split CSV line.
   */
  public static String extractName(String[] line) {
    return line[NAME_COLUMN_INDEX];
  }



  /**
   * Getter for afiTypeId that defines the specifig typeId of a module
   */
  public int getAfiTypeId() {
    return afiTypeId;
  }

  /**
   * Sets the afiTypeId of a module
   *
   * @param afiTypeId specific typeId of a module
   * @return Fluent Interface
   */
  public Module setAfiTypeId(int afiTypeId) {
    this.afiTypeId = afiTypeId;
    return this;
  }

  /**
   * Getter for id.
   *
   * @return the id (afiid) of a module
   */
  public int getId() {
    return id;
  }

  /**
   * Setter for the unique identifier of a module
   *
   * @param id the unique identifier that will be set
   * @return Fluent Interface
   */
  public Module setId(int id) {
    this.id = id;
    return this;
  }

  /**
   * Getter for symbol.
   *
   * @return the symbol of a module
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Setter for the symbol of a module
   *
   * @param symbol the symbol that will be set for the module
   * @return Fluent Interface
   */
  public Module setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  /**
   * Getter for name.
   *
   * @return The name of a module
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for the name of a module
   *
   * @param name The name that will be set for the module
   * @return Fluent Interface
   */
  public Module setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * getter for node
   *
   * @return node number
   */
  public int getNode() {
    return node;
  }

  /**
   * setter for node
   */
  public Module setNode(int node) {
    this.node = node;
    return this;
  }

  /**
   * Adds a Port as an outPort to a module
   *
   * @param port that will be added as an outPort
   * @return Fluent Interface
   */
  private Module addOutPort(Port port) {
    if (outPorts == null) {
      outPorts = new ArrayList<>();
    }
    outPorts.add(port);
    port.setModule(this);
    return this;
  }

  /**
   * Adds a Port as an inPort to a module
   *
   * @param port that will be added as an inPort
   * @return Fluent Interface
   */
  private Module addInPort(Port port) {
    if (inPorts == null) {
      inPorts = new ArrayList<>();
    }
    inPorts.add(port);
    port.setModule(this);
    return this;
  }

  /**
   * Getter for outPorts
   *
   * @return A list of all outPorts of a module
   */
  public List<Port> getOutPorts() {
    return outPorts == null ? Collections.emptyList() : Collections.unmodifiableList(outPorts);
  }

  /**
   * Getter for inPorts
   *
   * @return A list of all inPorts of a module
   */
  public List<Port> getInPorts() {
    return inPorts == null ? Collections.emptyList() : Collections.unmodifiableList(inPorts);
  }

  /**
   * Finds a single port of this module by its id
   */
  public Optional<Port> findPortById(int portId){
    return Stream.concat(getInPorts().stream(), getOutPorts().stream())
        .filter(p -> p.getId() == portId).findAny();
  }

  /**
   * Finds all ports of this module that match given ids. Any ids not found are ignored
   */
  public Set<Port> findPortsByIds(Integer ... portId){
    Set<Integer> portIds = Arrays.stream(portId).collect(Collectors.toSet());
    return Stream.concat(getInPorts().stream(), getOutPorts().stream())
        .filter(p -> portIds.contains(p.getId())).collect(Collectors.toSet());
  }

  /**
   * Adds a port according to its direction to a module
   *
   * @param p Port that should be added to the module. Must have a direction and must have a unique
   * id within the module
   * @return fluent interface
   */
  public Module addPort(Port p) {
    if (p.getDirection() == null){
      throw new IllegalArgumentException(String.format("Port %d must have a direction to be added to module %d", p.getId(), getId()));
    }
    if (findPortById(p.getId()).isPresent()){
      throw new IllegalArgumentException(String.format("Module %d already has a port with id %d", getId(), p.getId()));
    }
    switch (p.getDirection()) {
      case I:
        addInPort(p);
        break;
      case O:
        addOutPort(p);
        break;
      default:
        throw new IllegalArgumentException("illegal port direction");
    }
    return this;
  }

  /**
   * Checks if object is equal
   *
   * @param o the Object that has to be checked
   * @return boolean
   */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Module module = (Module) o;
    return afiTypeId == module.afiTypeId &&
        node == module.node &&
        id == module.id &&
        Objects.equals(symbol, module.symbol) &&
        Objects.equals(name, module.name);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    return Objects.hash(afiTypeId, node, id, symbol, name);
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Module{" +
        "afiTypeId=" + afiTypeId +
        ", node=" + node +
        ", id=" + id +
        ", symbol='" + symbol + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
