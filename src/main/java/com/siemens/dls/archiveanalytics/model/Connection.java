/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

/**
 * Class that defines an Connection. Connections describe the connection between two modules on a
 * through a specific port
 */
public class Connection {

  //afiid1;portid1;portname1;type1;afiid2;portid2;portname2;type2
  public static final int AFI1_COLUMN_INDEX = 0;
  public static final int PORT1_COLUMN_INDEX = 1;
  public static final int AFI2_COLUMN_INDEX = 4;
  public static final int PORT2_COLUMN_INDEX = 5;
  private PortKey out;
  private PortKey in;

  private Port outPort;
  private Port inPort;

  /**
   * Constructor Sets the <code>{@link PortKey}</code> out and the <code>{@link PortKey}</code> in
   *
   * @param out out port of a connection
   * @param in in port of a connection
   */
  public Connection(PortKey out, PortKey in) {
    assert out != null;
    assert in != null;
    this.out = out;
    this.in = in;
  }

  /**
   * Get the port 2 value from a splitted csv line
   *
   * @param line A CSV-line splitted on separator
   * @return The port 2 value as int
   */
  public static int extractPort2(String[] line) {
    return toInt(line[PORT2_COLUMN_INDEX]);
  }

  /**
   * Get the afi 2 value from a splitted csv line
   *
   * @param line A CSV-line splitted on separator
   * @return The afi 2 value as int
   */
  public static int extractAfi2(String[] line) {
    return toInt(line[AFI2_COLUMN_INDEX]);
  }

  /**
   * Get the port 1 value from a splitted csv line
   *
   * @param line A CSV-line splitted on separator
   * @return The port 1 value as int
   */
  public static int extractPort1(String[] line) {
    return toInt(line[PORT1_COLUMN_INDEX]);
  }

  /**
   * Get the afi 1 value from a splitted csv line
   *
   * @param line A CSV-line splitted on separator
   * @return The afi 1 value as int
   */
  public static int extractAfi1(String[] line) {
    return toInt(line[AFI1_COLUMN_INDEX]);
  }

  /**
   * Getter for inPort
   *
   * @return Port
   */
  public Port getInPort() {
    return inPort;
  }

  /**
   * Setter for the InPort
   *
   * @param inPort The inPort that should be added
   */
  public void setInPort(Port inPort) {
    this.inPort = inPort;
  }

  /**
   * Getter for outPort
   *
   * @return Port
   */
  public Port getOutPort() {
    return outPort;
  }

  /**
   * Setter for the outPort
   *
   * @param outPort The outPort that should be added
   */
  public void setOutPort(Port outPort) {
    this.outPort = outPort;
  }

  /**
   * Getter for Out
   *
   * @return out
   */
  public PortKey getOut() {
    return out;
  }

  /**
   * Getter for In
   *
   * @return in
   */
  public PortKey getIn() {
    return in;
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

    Connection that = (Connection) o;

    return out.equals(that.out) && in.equals(that.in);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = out.hashCode();
    result = 31 * result + in.hashCode();
    return result;
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Connection{" +
        "out=" + out +
        ", in=" + in +
        ", outPort=" + outPort +
        ", inPort=" + inPort +
        '}';
  }
}
