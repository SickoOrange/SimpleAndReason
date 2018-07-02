/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;

/**
 * Defines an Alarm.
 */
public class AlarmType {

  //alarmtype;abbrev;name
  public static final int ALARMTYPE_ID_COLUMN_INDEX = 0;
  public static final int ABBREV_COLUMN_INDEX = 1;

  @CsvBindByPosition(position = ALARMTYPE_ID_COLUMN_INDEX)
  private int id;
  @CsvBindByPosition(position = ABBREV_COLUMN_INDEX)
  private String abbrev;

  /**
   * default constructor
   */
  public AlarmType() {
    //noop
  }

  /**
   * Extracts id from a split CSV line.
   */
  public static int extractid(String[] line) {
    return toInt(line[ALARMTYPE_ID_COLUMN_INDEX]);
  }

  /**
   * Extracts abbrev from a split CSV line.
   */
  public static String extractAbbrev(String[] line) {
    return line[ABBREV_COLUMN_INDEX];
  }

  /**
   * Getter for abbrev of a module
   */
  public String getAbbrev() {
    return abbrev;
  }

  /**
   * Sets the abbrev of a module
   *
   * @param abbrev specific abbrev of a module
   * @return Fluent Interface
   */
  public AlarmType setAbbrev(String abbrev) {
    this.abbrev = abbrev;
    return this;
  }

  /**
   * Getter for alarm type id.
   *
   * @return the id of an Alarm type
   */
  public int getId() {
    return id;
  }

  /**
   * Setter for the alarm type id
   *
   * @param id The alarm type id that will be set
   * @return Fluent Interface
   */
  public AlarmType setId(int id) {
    this.id = id;
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

    AlarmType module = (AlarmType) o;

    return id == module.id && (abbrev != null ? abbrev
        .equals(module.abbrev) : module.abbrev == null);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (abbrev != null ? abbrev.hashCode() : 0);
    return result;
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "AlarmType{" +
        "id=" + id +
        ", abbrev=" + abbrev +
        '}';
  }
}
