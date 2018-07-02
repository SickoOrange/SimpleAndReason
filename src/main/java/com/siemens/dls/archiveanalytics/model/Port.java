/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toFloat;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.siemens.dls.archiveanalytics.parser.CsvStringToBooleanConverter;
import com.siemens.dls.archiveanalytics.parser.CsvStringToPortDirectionConverter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Defines a Port
 */
public class Port {

  private static final Logger LOG = Logger.getLogger(Port.class);

  //afiid;portid;portname;portdesc;afitypeid;symbol;type;io;parameter;isarchive;isalarm;alarmtypeid;
  // <cont> abbrev;activerule;inactiverule;active;inactive;minvalue;maxvalue;percent;engunit;signal;sgnalinfo;connafiid;connportid;uniquename
  public static final int AFI_COLUMN_INDEX = 0;
  public static final int ID_COLUMN_INDEX = 1;
  public static final int NAME_COLUMN_INDEX = 2;
  public static final int IO_COLUMN_INDEX = 7;
  public static final int PARAMETER_COLUMN_INDEX = 8;
  public static final int IS_ARCHIVE_COLUMN_INDEX = 9;
  public static final int IS_ALARM_COLUMN_INDEX = 10;
  public static final int ALARMTYPE_ID_COLUMN_INDEX = 11;
  public static final int ABBREV_COLUMN_INDEX = 12;
  public static final int ACTIVE_COLUMN_INDEX = 15;
  public static final int MIN_COLUMN_INDEX = 17;
  public static final int MAX_COLUMN_INDEX = 18;
  public static final int ENGINEERING_UNIT_COLUMN_INDEX = 20;
  public static final int CONNAFIID_COLUMN_INDEX = 23;
  public static final int CONNPORT_COLUMN_INDEX = 24;
  public static final int UNIQUENAME_COLUMN_INDEX = 25;

  @CsvBindByPosition(position = ID_COLUMN_INDEX)
  private int id;
  @CsvBindByPosition(position = AFI_COLUMN_INDEX)
  private int afiId;
  @CsvBindByPosition(position = NAME_COLUMN_INDEX)
  private String name;
  @CsvBindByPosition(position = PARAMETER_COLUMN_INDEX)
  private String parameter;
  @CsvBindByPosition(position = UNIQUENAME_COLUMN_INDEX)
  private String uniqueName;
  @CsvCustomBindByPosition(position = IO_COLUMN_INDEX, converter = CsvStringToPortDirectionConverter.class)
  private PortDirection direction;

  @CsvCustomBindByPosition(position = IS_ARCHIVE_COLUMN_INDEX, converter = CsvStringToBooleanConverter.class)
  private boolean isArchive;
  @CsvCustomBindByPosition(position = IS_ALARM_COLUMN_INDEX, converter = CsvStringToBooleanConverter.class)
  private boolean isAlarm;
  @CsvBindByPosition(position = ALARMTYPE_ID_COLUMN_INDEX)
  private int alarmTypeId;
  @CsvBindByPosition(position = ABBREV_COLUMN_INDEX)
  private String abbrev;
  @CsvBindByPosition(position = ACTIVE_COLUMN_INDEX)
  private String active;
  @CsvBindByPosition(position = MIN_COLUMN_INDEX)
  private float minValue;
  @CsvBindByPosition(position = MAX_COLUMN_INDEX)
  private float maxValue;

  @CsvBindByPosition(position = CONNAFIID_COLUMN_INDEX)
  private int connAfiId;
  @CsvBindByPosition(position = CONNPORT_COLUMN_INDEX)
  private int connPortId;

  @CsvBindByPosition(position = ENGINEERING_UNIT_COLUMN_INDEX)
  private String engineeringUnit;

  private Module module;
  private Set<Port> connectedPorts;
  private boolean connected = false;

  /**
   * Default constructor
   */
  public Port() {
    //noop
  }

  /**
   * Extracts afi id from a split CSV line.
   */
  public static int extractAfiId(String[] line) {
    return toInt(line[AFI_COLUMN_INDEX]);
  }

  /**
   * Extracts direction from a split CSV line.
   */
  public static PortDirection extractDirection(String[] line) {
    return PortDirection.valueOf(line[IO_COLUMN_INDEX]);
  }

  public static int extractId(String[] line) {
    return toInt(line[ID_COLUMN_INDEX]);
  }

  public static PortKey extractPortKey(String[] line) {
    return new PortKey(extractAfiId(line), extractId(line));
  }

  public static String extractIsArchive(String[] line) {
    return line[IS_ARCHIVE_COLUMN_INDEX];
  }

  public static String extractIsAlarm(String[] line) {
    return line[IS_ALARM_COLUMN_INDEX];
  }

  public static String extractActive(String[] line) {
    return line[ACTIVE_COLUMN_INDEX];
  }

  /**
   * Extract connected afiid. If none connected, return 0
   *
   * @param line The line from Ports.csv
   * @return The afiid as int
   */
  public static int extractConnAfiid(String[] line) {
    return toInt(line[CONNAFIID_COLUMN_INDEX]);
  }

  /**
   * Extract connected Portid. If none connected, return 0
   *
   * @param line The line from Ports.csv
   * @return The portid as int
   */
  public static int extractConnPortid(String[] line) {
    return toInt(line[CONNPORT_COLUMN_INDEX]);
  }

  public static String extractName(String[] line) {
    return line[NAME_COLUMN_INDEX];
  }

  public static String extractParameter(String[] line) {
    return line[PARAMETER_COLUMN_INDEX];
  }

  public static int extractAlarmTypeId(String[] line) {
    return toInt(line[ALARMTYPE_ID_COLUMN_INDEX]);
  }

  public static String extractAbbrev(String[] line) {
    return line[ABBREV_COLUMN_INDEX];
  }

  public static float extractMinValue(String[] line) {
    return toFloat(line[MIN_COLUMN_INDEX]);
  }

  public static float extractMaxValue(String[] line) {
    return toFloat(line[MAX_COLUMN_INDEX]);
  }

  public static String extractEngineeringUnit(String[] line) {
    return line[ENGINEERING_UNIT_COLUMN_INDEX];
  }

  public static String extractUniqueName(String[] line) {
    return line[UNIQUENAME_COLUMN_INDEX];
  }


  /**
   * Getter for a PortKey consisting of the afiId and the id (PortId)
   *
   * @return A PortKey consisting of the afiId and the id (PortId)
   */
  public PortKey getKey() {
    return new PortKey(afiId, id);
  }

  public Port setKey(PortKey key) {
    setAfiId(key.getAfiId());
    setId(key.getPortId());
    return this;
  }

  /**
   * Getter for the PortId. A PortId is unique for every port
   *
   * @return the id of the port
   */
  public int getId() {
    return id;
  }

  /**
   * Setter for the PortId. A PortId is unique for every afiId
   *
   * @param id the PortId that will be set
   * @return Fluent Interface
   */
  public Port setId(int id) {
    this.id = id;
    return this;
  }

  /**
   * Getter for the AfiId. The AfiId defines the related module of the port
   *
   * @return the AfiId of the port
   */
  public int getAfiId() {
    return afiId;
  }

  /**
   * Setter for the afiId. The AfiId defines the related module of the port
   *
   * @param afiId that will be set
   * @return Fluent Interface
   */
  public Port setAfiId(int afiId) {
    this.afiId = afiId;
    return this;
  }

  /**
   * Getter for the name of a Port. Each Port has a unique name
   *
   * @return the name of the Port
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for the name of a Port. Each Port has a unique name
   *
   * @param name the name that will be given to the port
   * @return Fluent Interface
   */
  public Port setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * getter for the parameter of a port
   *
   * @return the parameter of a port
   */
  public String getParameter() {
    return parameter;
  }

  /**
   * Setter for the parameter of a port
   *
   * @param parameter that will be given to the port
   * @return Fluent Interface
   */
  public Port setParameter(String parameter) {
    this.parameter = parameter;
    return this;
  }

  /**
   * getter for the active of a port
   *
   * @return the active of a port
   */
  public String getActive() {
    return active;
  }

  /**
   * Setter for the active of a port
   *
   * @param active that will be given to the port
   * @return Fluent Interface
   */
  public Port setActive(String active) {
    this.active = active;
    return this;
  }

  /**
   * Getter for the uniqueName
   *
   * @return the uniqueName of a port
   */
  public String getUniqueName() {
    return uniqueName;
  }

  /**
   * Setter for the uniqueName of a port
   *
   * @param uniqueName that will be added to the port
   * @return Fluent Interface
   */
  public Port setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
    return this;
  }

  /**
   * Getter for the direction of a port. The direction can be I (In) or O (Out)
   *
   * @return the direction I or O
   */
  public PortDirection getDirection() {
    return direction;
  }

  /**
   * Setter for the direction of a port
   *
   * @param direction the direction that will be set
   * @return Fluent Interface
   */
  public Port setDirection(PortDirection direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Return true if the port has the isArchive flag set, false otherwise
   *
   * @return true if isArchive flag is set, false otherwise
   */
  public boolean isArchive() {
    return isArchive;
  }

  /**
   * Setter for the isArchive flag
   *
   * @param archive true or false
   */
  public Port setArchive(boolean archive) {
    isArchive = archive;
    return this;
  }

  /**
   * Return true if the port has the isAlarm flag set, false otherwise
   *
   * @return true if isAlarm flag is set, false otherwise
   */
  public boolean isAlarm() {
    return isAlarm;
  }

  /**
   * Setter for the isAlarm flag
   *
   * @param alarm true or false
   */
  public Port setAlarm(boolean alarm) {
    isAlarm = alarm;
    return this;
  }


  /**
   * Getter for the alarmTypeId
   */
  public int getAlarmTypeId() {
    return alarmTypeId;
  }

  /**
   * Getter for Abbrev
   */
  public String getAbbrev() {
    return abbrev;
  }

  /**
   * Setter for the alarmTypeId of a port
   *
   * @return Fluent Interface
   */
  public Port setAlarmTypeId(int alarmTypeId) {
    this.alarmTypeId = alarmTypeId;
    return this;
  }

  /**
   * Setter for the Abbrev of a port
   *
   * @return Fluent Interface
   */
  public Port setAbbrev(String abbrev) {
    this.abbrev = abbrev;
    return this;
  }

  /**
   * Getter for the module of a port. Every port has exactly one module
   *
   * @return the module of the port
   */
  public Module getModule() {
    return module;
  }

  /**
   * Setter for the module of a port, every port can have exactly one module
   *
   * @param module that will be set for the port
   * @return Fluent interface
   */
  public Port setModule(Module module) {
    this.module = module;
    return this;
  }

  public int getConnAfiId() {
    return connAfiId;
  }

  public void setConnAfiId(int connAfiId) {
    this.connAfiId = connAfiId;
  }

  public int getConnPortId() {
    return connPortId;
  }

  public void setConnPortId(int connPortId) {
    this.connPortId = connPortId;
  }

  public Set<Port> getConnectedPorts() {
    if (connectedPorts == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(connectedPorts);
  }

  public Optional<Port> getConnectedOutPort() {
    return getConnectedPorts().stream().filter(p -> p.getDirection() == PortDirection.O)
        .findFirst();
  }

  public Port addToConnectedPorts(Port other) {
    if (getDirection() == other.getDirection()) {
      LOG.warn(
          "Attempt to create a connection between ports with the same direction ignored. Port1: "
              + this.toString() + " Port2: " + other.toString());
      return this;
    }
    if (connectedPorts == null) {
      connectedPorts = new HashSet<>();
    }
    if (!connectedPorts.contains(other)) {
      connectedPorts.add(other);
      other.addToConnectedPorts(this);
    }
    if (!connectedPorts.isEmpty()) {
      markConnected();
    }
    return this;
  }

  public boolean isDangling() {
    return !getConnectedOutPort().isPresent() && isConnected();
  }

  public boolean isConnected() {
    return connected;
  }

  public void markConnected() {
    this.connected = true;
  }

  public float getMinValue() {
    return minValue;
  }

  public Port setMinValue(float minValue) {
    this.minValue = minValue;
    return this;
  }

  public float getMaxValue() {
    return maxValue;
  }

  public Port setMaxValue(float maxValue) {
    this.maxValue = maxValue;
    return this;
  }

  public String getEngineeringUnit() {
    return engineeringUnit;
  }

  public Port setEngineeringUnit(String engineeringUnit) {
    this.engineeringUnit = engineeringUnit;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Port port = (Port) o;
    return id == port.id &&
        afiId == port.afiId &&
        isArchive == port.isArchive &&
        isAlarm == port.isAlarm &&
        alarmTypeId == port.alarmTypeId &&
        abbrev.equals(port.abbrev) &&
        Float.compare(port.minValue, minValue) == 0 &&
        Float.compare(port.maxValue, maxValue) == 0 &&
        Objects.equals(name, port.name) &&
        Objects.equals(parameter, port.parameter) &&
        Objects.equals(uniqueName, port.uniqueName) &&
        direction == port.direction &&
        Objects.equals(active, port.active) &&
        Objects.equals(engineeringUnit, port.engineeringUnit);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id, afiId, name, parameter, uniqueName, direction, isArchive, isAlarm, alarmTypeId,
            abbrev, active, minValue, maxValue, engineeringUnit);
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Port{" +
        "id=" + id +
        ", afiId=" + afiId +
        ", name='" + name + '\'' +
        ", parameter='" + parameter + '\'' +
        ", uniqueName='" + uniqueName + '\'' +
        ", direction=" + direction +
        ", isArchive=" + isArchive +
        ", isAlarm=" + isAlarm +
        ", alarmTypeId=" + alarmTypeId +
        ", abbrev=" + abbrev +
        ", active='" + active + '\'' +
        ", minValue=" + minValue +
        ", maxValue=" + maxValue +
        ", engineeringUnit='" + engineeringUnit + '\'' +
        ", module=" + module +
        ", connected=" + connected +
        '}';
  }
}

