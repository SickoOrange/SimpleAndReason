/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.siemens.dls.archiveanalytics.parser.CsvStringToBooleanConverter;
import com.siemens.dls.archiveanalytics.parser.CsvStringToTrendListConverter;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.Interval;

/**
 * Class that defines an AnalogTrend. The AnalogTrend defines the analog signals that belong to a
 * specific uniqueName
 */
public class BinaryTrend extends AbstractTrend<BinaryTrend> {

  public static final int ALARM_COLUMN_INDEX = 2;
  public static final int ACTIVE_COLUMN_INDEX = 3;
  public static final int TREND_COLUMN_INDEX_BINARY = 4;

  @CsvBindByPosition(position = CLIENT_COLUMN_INDEX)
  protected String ppid;
  @CsvBindByPosition(position = TAGNAME_COLUMN_INDEX)
  protected String uniqueName;
  @CsvBindByPosition(position = ALARM_COLUMN_INDEX)
  protected Boolean alarm;
  @CsvBindByPosition(position = ACTIVE_COLUMN_INDEX)
  protected Boolean active;
  @CsvCustomBindByPosition(position = TREND_COLUMN_INDEX_BINARY, converter = CsvStringToTrendListConverter.class)
  protected List<Trend> trends;
  private int count;
  private int risingCount;
  private int totalCount;
  private int fallingCount;

  public BinaryTrend() {
    // noop
  }

  public BinaryTrend(Port port) {
    setPort(port);
  }

  /**
   * Getter for ppid
   *
   * @return ppid
   */
  public String getPpid() {
    return ppid;
  }

  /**
   * Setter for the ppid
   *
   * @param ppid The ppid
   * @return Fluent interface
   */
  public BinaryTrend setPpid(String ppid) {
    this.ppid = ppid;
    return this;
  }

  /**
   * Getter for UniqueNames
   *
   * @return uniqueName
   */
  public String getUniqueName() {
    return uniqueName;
  }

  /**
   * Setter for the uniqueName
   *
   * @param uniqueName The Uniquename
   * @return Fluent interface
   */
  public BinaryTrend setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
    return this;
  }

  /**
   * Provides if isAlarm is set
   *
   * @return The Alarm state
   */
  public Boolean isAlarm() {
    return alarm;
  }

  /**
   * Set the alarm state
   *
   * @param alarm The alarm state
   * @return Fluent interface
   */
  public BinaryTrend setAlarm(Boolean alarm) {
    this.alarm = alarm;
    return this;
  }

  /**
   * Provides if isActive is set
   *
   * @return The Active state
   */
  public Boolean isActive() {
    return active;
  }

  /**
   * Set the active state
   *
   * @param active The active state
   * @return Fluent interface
   */
  public BinaryTrend setActive(Boolean active) {
    this.active = active;
    return this;
  }

  /**
   * Getter for Trends
   *
   * @return trends
   */
  public List<Trend> getTrends() {
    return trends;
  }

  /**
   * Setter for the Trends
   *
   * @param trends The trends
   * @return Fluent interface
   */
  public BinaryTrend setTrends(List<Trend> trends) {
    this.trends = trends;
    count = 0;
    risingCount = 0;
    totalCount = 0;
    trends.stream().filter(t -> t.getMillis() > 0)
        .forEach(t -> {
          if (t.getQuality() >= NOT_BAD_QUALITY_THRESHOLD){
            count += 1;
            if (t.getValue() == 1){ // 1 encodes 'true'
              risingCount += 1;
            }
          }
          totalCount += 1;
        });
    fallingCount = count - risingCount;
    return this;
  }

  @Override
  public BinaryTrend copy() {
    if (getPort() != null) {
      throw new IllegalArgumentException("Cannot copy a trend that has been assigned a port");
    }
    BinaryTrend copy = new BinaryTrend();
    copy.setTrends(copyTrends(this));
    copy.setUniqueName(getUniqueName());
    copy.setPpid(getPpid());
    copy.setActive(isActive());
    copy.setAlarm(isAlarm());
    return copy;
  }

  public List<Interval> asIntervals(){
    List<Trend> aqTrends = getAcceptableQualityTrends();
    return getIntervalsFromTrends(aqTrends);
  }

  public static List<Interval> getIntervalsFromTrends(List<Trend> aqTrends) {
    List<Interval> result = new ArrayList<>();

    long currentIntervalStart = -1;
    for (Trend trend : aqTrends) {
      if (trend.getValue() == 1) {
        if (trend.getMillis() != 0 && currentIntervalStart == -1) {
          currentIntervalStart = trend.getMillis();
        }
      } else { // value == 0
        if (currentIntervalStart != -1) {
          result.add(new Interval(currentIntervalStart, trend.getMillis()));
          currentIntervalStart = -1;
        }
      }
    }
    return result;
  }

  public int getCount() {
    return count;
  }

  public int getRisingCount() {
    return risingCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getFallingCount() {
    return fallingCount;
  }

  public static BinaryTrend lineToTrend(String[] line){
    BinaryTrend t = new BinaryTrend();
    t.setPpid(extractClient(line));
    t.setUniqueName(extractTagname(line));
    t.setAlarm(extractIsAlarm(line));
    t.setActive(extractIsActive(line));
    try {
      t.setTrends(CsvStringToTrendListConverter.stringToTrendList(extractTrends(line)));
    } catch (CsvDataTypeMismatchException e) {
      throw new IllegalArgumentException(e);
    }
    return t;
  }

  public static String extractTrends(String[] line) {
    return line[TREND_COLUMN_INDEX_BINARY];
  }

  public static boolean extractIsAlarm(String[] line) {
    return CsvStringToBooleanConverter.toBoolean(line[ALARM_COLUMN_INDEX]);
  }

  public static boolean extractIsActive(String[] line) {
    return CsvStringToBooleanConverter.toBoolean(line[ACTIVE_COLUMN_INDEX]);
  }


  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "BinaryTrend{" +
        "ppid='" + ppid + '\'' +
        ", uniqueName='" + uniqueName + '\'' +
        ", alarm='" + alarm + '\'' +
        ", active='" + active + '\'' +
        ", trends=" + trends +
        '}';
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

    BinaryTrend that = (BinaryTrend) o;

    return (ppid != null ? ppid.equals(that.ppid) : that.ppid == null) && (uniqueName != null
        ? uniqueName.equals(that.uniqueName) : that.uniqueName == null) && alarm == that.alarm
        && active == that.active && (trends != null ? trends.equals(that.trends)
        : that.trends == null);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = ppid != null ? ppid.hashCode() : 0;
    result = 31 * result + (uniqueName != null ? uniqueName.hashCode() : 0);
    result = 31 * result + (alarm != null ? alarm.hashCode() : 0);
    result = 31 * result + (active != null ? active.hashCode() : 0);
    result = 31 * result + (trends != null ? trends.hashCode() : 0);
    return result;
  }
}
