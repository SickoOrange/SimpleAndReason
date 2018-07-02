/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;
import java.util.Objects;

/**
 * Defines a Suppressed Alarm.
 */
public class SuppressedAlarm {

  //Client;TagName;AlarmType;Suppressed;DisplaySuppressed;AutoSuppressed;
  public static final int CLIENT_COLUMN_INDEX = 0;
  public static final int TAGNAME_COLUMN_INDEX = 1;
  public static final int ALARMTYPE_COLUMN_INDEX = 2;
  public static final int SUPPRESSED_COLUMN_INDEX = 3;
  public static final int DISPLAY_SUPPRESSED_COLUMN_INDEX = 4;
  public static final int AUTO_SUPPRESSED_COLUMN_INDEX = 5;

  @CsvBindByPosition(position = CLIENT_COLUMN_INDEX)
  private int id;
  @CsvBindByPosition(position = TAGNAME_COLUMN_INDEX)
  private String tagname;
  @CsvBindByPosition(position = ALARMTYPE_COLUMN_INDEX)
  private int alarmtypeid;
  @CsvBindByPosition(position = SUPPRESSED_COLUMN_INDEX)
  private int suppressed;
  @CsvBindByPosition(position = DISPLAY_SUPPRESSED_COLUMN_INDEX)
  private int displaySuppressed;
  @CsvBindByPosition(position = AUTO_SUPPRESSED_COLUMN_INDEX)
  private int autoSuppressed;


  /**
   * default constructor
   */
  public SuppressedAlarm() {
    //noop
  }

  /**
   * Extracts tagname id from a split CSV line.
   */
  public static String extractTagname(String[] line) {
    return line[TAGNAME_COLUMN_INDEX];
  }

  /**
   * Extracts alarmtype id from a split CSV line.
   */
  public static int extractAlarmTypeId(String[] line) { return toInt(line[ALARMTYPE_COLUMN_INDEX]);
  }

  /**
   * Extracts suppressed from a split CSV line.
   */
  public static int extractSuppressed(String[] line) { return toInt(line[SUPPRESSED_COLUMN_INDEX]);
  }

  /**
   * Extracts display suppressed from a split CSV line.
   */
  public static int extractDisplaySuppressed(String[] line) { return toInt(line[DISPLAY_SUPPRESSED_COLUMN_INDEX]);
  }

  /**
   * Extracts auto suppressed from a split CSV line.
   */
  public static int extractAutoSuppressed(String[] line) { return toInt(line[AUTO_SUPPRESSED_COLUMN_INDEX]);
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTagname() {
    return tagname;
  }

  public void setTagname(String tagname) {
    this.tagname = tagname;
  }

  public int getAlarmtypeid() {
    return alarmtypeid;
  }

  public void setAlarmtypeid(int alarmtypeid) {
    this.alarmtypeid = alarmtypeid;
  }

  public int getSuppressed() {
    return suppressed;
  }

  public void setSuppressed(int suppressed) {
    this.suppressed = suppressed;
  }

  public int getDisplaySuppressed() {
    return displaySuppressed;
  }

  public void setDisplaySuppressed(int displaySuppressed) {
    this.displaySuppressed = displaySuppressed;
  }

  public int getAutoSuppressed() {
    return autoSuppressed;
  }

  public void setAutoSuppressed(int autoSuppressed) {
    this.autoSuppressed = autoSuppressed;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuppressedAlarm suppressedAlarm = (SuppressedAlarm) o;
    return id == suppressedAlarm.id &&
        Objects.equals(tagname, suppressedAlarm.tagname) &&
        Objects.equals(alarmtypeid, suppressedAlarm.alarmtypeid) &&
        Objects.equals(suppressed, suppressedAlarm.suppressed) &&
        Objects.equals(displaySuppressed, suppressedAlarm.displaySuppressed) &&
        Objects.equals(autoSuppressed, suppressedAlarm.autoSuppressed);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id, tagname, alarmtypeid, suppressed, displaySuppressed, autoSuppressed);
  }
}
