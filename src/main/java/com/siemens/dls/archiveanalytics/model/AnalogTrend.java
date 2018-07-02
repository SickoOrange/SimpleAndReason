/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.siemens.dls.archiveanalytics.parser.CsvStringToTrendListConverter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Class that defines an AnalogTrend. The AnalogTrend defines the analog signals that belong to a
 * specific uniqueName
 */
public class AnalogTrend extends AbstractTrend<AnalogTrend> {

  public static final int TREND_COLUMN_INDEX = 2;
  @CsvBindByPosition(position = CLIENT_COLUMN_INDEX)
  private String ppid;
  @CsvBindByPosition(position = TAGNAME_COLUMN_INDEX)
  private String uniqueName;
  @CsvCustomBindByPosition(position = TREND_COLUMN_INDEX, converter = CsvStringToTrendListConverter.class)
  private List<Trend> trends;
  private int badCount;
  private Optional<Double> minValue;
  private Optional<Double> maxValue;

  public AnalogTrend() {
    // noop
  }

  public AnalogTrend(Port port) {
    setPort(port);
  }

  private static String extractTrends(String[] line) {
    return line[TREND_COLUMN_INDEX];
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
  public AnalogTrend setPpid(String ppid) {
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
  public AnalogTrend setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
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

  @Override
  public AnalogTrend copy() {
    if (getPort() != null) {
      throw new IllegalArgumentException("Cannot copy a trend that has been assigned a port");
    }
    AnalogTrend copy = new AnalogTrend();
    copy.setTrends(copyTrends(this));
    copy.setUniqueName(getUniqueName());
    copy.setPpid(getPpid());
    return copy;
  }

  /**
   * Setter for the Trends
   *
   * @param trends The trends
   * @return Fluent interface
   */
  public AnalogTrend setTrends(List<Trend> trends) {
    this.trends = trends;
    badCount = (int) trends.stream().filter(t -> t.getQuality() < NOT_BAD_QUALITY_THRESHOLD).count();
    minValue = trends.stream().min(Comparator.comparing(Trend::getValue)).map(Trend::getValue);
    maxValue = trends.stream().max(Comparator.comparing(Trend::getValue)).map(Trend::getValue);
    return this;
  }

  public int getBadCount() {
    return badCount;
  }

  public Optional<Double> getMinValue() {
    return minValue;
  }

  public Optional<Double> getMaxValue() {
    return maxValue;
  }

  public static AnalogTrend lineToTrend(String[] line){
    AnalogTrend t = new AnalogTrend();
    t.setPpid(extractClient(line));
    t.setUniqueName(extractTagname(line));
    try {
      t.setTrends(CsvStringToTrendListConverter.stringToTrendList(extractTrends(line)));
    } catch (CsvDataTypeMismatchException e) {
      throw new IllegalArgumentException(e);
    }
    return t;
  }


  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "AnalogTrend{" +
        "ppid='" + ppid + '\'' +
        ", uniqueName='" + uniqueName + '\'' +
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

    AnalogTrend that = (AnalogTrend) o;

    if (ppid != null ? !ppid.equals(that.ppid) : that.ppid != null) {
      return false;
    }
    if (uniqueName != null ? !uniqueName.equals(that.uniqueName) : that.uniqueName != null) {
      return false;
    }
    return trends != null ? trends.equals(that.trends) : that.trends == null;
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = ppid != null ? ppid.hashCode() : 0;
    result = 31 * result + (uniqueName != null ? uniqueName.hashCode() : 0);
    result = 31 * result + (trends != null ? trends.hashCode() : 0);
    return result;
  }
}
