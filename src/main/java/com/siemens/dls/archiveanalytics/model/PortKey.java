/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

/**
 * Defines a PortKey. Each PortKey consists of an aifId and a portId
 */
public class PortKey {

  private int afiId;
  private int portId;

  /**
   * PortKey constructor
   *
   * @param afiId the afiid of the PortKey
   * @param portId the portId of the PortKey
   */
  public PortKey(int afiId, int portId) {
    this.afiId = afiId;
    this.portId = portId;
  }

  /**
   * Getter for the afiId of the PortKey
   *
   * @return the afiId of the PortKey
   */
  public int getAfiId() {
    return afiId;
  }

  /**
   * Getter for the portId of the PortKey
   *
   * @return the PortId of the PortKey
   */
  public int getPortId() {
    return portId;
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

    PortKey portKey = (PortKey) o;

    return afiId == portKey.afiId && portId == portKey.portId;
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = afiId;
    result = 31 * result + portId;
    return result;
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "PortKey{" +
        "afiId=" + afiId +
        ", portId=" + portId +
        '}';
  }
}
