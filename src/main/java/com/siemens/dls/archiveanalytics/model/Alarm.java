/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.siemens.dls.archiveanalytics.parser.CsvStringToLocalDateTimeConverter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Defines an Alarm.
 */
public class Alarm {
//      "Client;Time;Quality;TagName;AlarmType;Suppressed;Auto-Supprssed;Disp-Suppressed;Duration;TimeToNext;\n"

  public static final int TIME_COLUMN_INDEX = 1;
  public static final int QUALITY_COLUMN_INDEX = 2;
  public static final int TAGNAME_COLUMN_INDEX = 3;
  public static final int ALARM_TYPE_ID_COLUMN_INDEX = 4;
  public static final int SUPPRESSED_COLUMN_INDEX = 5;
  public static final int AUTO_SUPPRESSED_COLUMN_INDEX = 6;
  public static final int DISP_SUPPRESSED_COLUMN_INDEX = 7;
  public static final int DURATION_COLUMN_INDEX = 8;
  public static final int TIME_TO_NEXT_COLUMN_INDEX = 9;

  @CsvBindByPosition(position = QUALITY_COLUMN_INDEX)
  private int quality;
  @CsvBindByPosition(position = TAGNAME_COLUMN_INDEX)
  private String tagname;
  @CsvCustomBindByPosition(position = TIME_COLUMN_INDEX, converter = CsvStringToLocalDateTimeConverter.class)
  private LocalDateTime time;
  @CsvBindByPosition(position = ALARM_TYPE_ID_COLUMN_INDEX)
  private int alarmTypeId;
  @CsvBindByPosition(position = SUPPRESSED_COLUMN_INDEX)
  private boolean suppressed;
  @CsvBindByPosition(position = AUTO_SUPPRESSED_COLUMN_INDEX)
  private boolean autoSuppressed;
  @CsvBindByPosition(position = DISP_SUPPRESSED_COLUMN_INDEX)
  private boolean dispSuppressed;
  @CsvBindByPosition(position = DURATION_COLUMN_INDEX)
  private int duration;
  @CsvBindByPosition(position = TIME_TO_NEXT_COLUMN_INDEX)
  private int timeToNext;

  private Port port;

  /**
   * default constructor
   */
  public Alarm() {
    //noop
  }

  /**
   * Extracts tagname id from a split CSV line.
   */
  public static String extractTagname(String[] line) {
    return line[TAGNAME_COLUMN_INDEX];
  }

  /**
   * Extracts quality from a split CSV line.
   */
  public static int extractQuality(String[] line) {
    return toInt(line[QUALITY_COLUMN_INDEX]);
  }

  /**
   * Getter for tagname of a module
   */
  public String getTagname() {
    return tagname;
  }

  /**
   * Sets the tagname of a module
   *
   * @param tagname specific typeId of a module
   * @return Fluent Interface
   */
  public Alarm setTagname(String tagname) {
    this.tagname = tagname;
    return this;
  }

  /**
   * Getter for quality.
   *
   * @return the quality of an Alarm
   */
  public int getQuality() {
    return quality;
  }

  /**
   * Setter for the quality of an Alarm
   *
   * @param quality The quality that will be set
   * @return Fluent Interface
   */
  public Alarm setQuality(int quality) {
    this.quality = quality;
    return this;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public Alarm setTime(LocalDateTime time) {
    this.time = time;
    return this;
  }

  public int getAlarmTypeId() {
    return alarmTypeId;
  }

  public Alarm setAlarmTypeId(int alarmTypeId) {
    this.alarmTypeId = alarmTypeId;
    return this;
  }

  public boolean isSuppressed() {
    return suppressed;
  }

  public Alarm setSuppressed(boolean suppressed) {
    this.suppressed = suppressed;
    return this;
  }

  public boolean isAutoSuppressed() {
    return autoSuppressed;
  }

  public Alarm setAutoSuppressed(boolean autoSuppressed) {
    this.autoSuppressed = autoSuppressed;
    return this;
  }

  public boolean isDispSuppressed() {
    return dispSuppressed;
  }

  public Alarm setDispSuppressed(boolean dispSuppressed) {
    this.dispSuppressed = dispSuppressed;
    return this;
  }

  public int getDuration() {
    return duration;
  }

  public Alarm setDuration(int duration) {
    this.duration = duration;
    return this;
  }

  public int getTimeToNext() {
    return timeToNext;
  }

  public Alarm setTimeToNext(int timeToNext) {
    this.timeToNext = timeToNext;
    return this;
  }

  public Port getPort() {
    return port;
  }

  public Alarm setPort(Port port) {
    this.port = port;
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
    Alarm alarm = (Alarm) o;
    return quality == alarm.quality &&
        alarmTypeId == alarm.alarmTypeId &&
        suppressed == alarm.suppressed &&
        autoSuppressed == alarm.autoSuppressed &&
        dispSuppressed == alarm.dispSuppressed &&
        duration == alarm.duration &&
        timeToNext == alarm.timeToNext &&
        Objects.equals(tagname, alarm.tagname) &&
        Objects.equals(time, alarm.time) &&
        Objects.equals(port, alarm.port);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {

    return Objects
        .hash(quality, tagname, time, alarmTypeId, suppressed, autoSuppressed, dispSuppressed,
            duration, timeToNext, port);
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Alarm{" +
        "quality=" + quality +
        ", tagname='" + tagname + '\'' +
        ", time=" + time +
        ", alarmTypeId=" + alarmTypeId +
        ", suppressed=" + suppressed +
        ", autoSuppressed=" + autoSuppressed +
        ", dispSuppressed=" + dispSuppressed +
        ", duration=" + duration +
        ", timeToNext=" + timeToNext +
        '}';
  }
}
