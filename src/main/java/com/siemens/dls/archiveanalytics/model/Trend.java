/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

/**
 * Defines a Trend. A Trend always consists of 3 values (time, quality, value)
 */
public class Trend {

  private int millis;
  private int quality;
  private double value;
  /**
   * constructor of a Trend
   *
   * @param millis the time the signal occurred in milliseconds counted from the start of the day
   * @param quality the quality of the signal, <64 = bad quality, else good quality
   * @param value the value of a signal
   */
  public Trend(int millis, int quality, double value) {
    this.millis = millis;
    this.quality = quality;
    this.value = value;
  }

  /**
   * Copy constructor
   * @param trend
   */
  public Trend(Trend trend){
    this(trend.getMillis(), trend.getQuality(), trend.getValue());
  }

  /**
   * Getter for millis counted from the start of the day in milliseconds
   *
   * @return the milliseconds when the signal occurred
   */
  public int getMillis() {
    return millis;
  }

  /**
   * Setter for millis
   *
   * @param millis when the signal occurred
   */
  public Trend setMillis(int millis) {
    this.millis = millis;
    return this;
  }

  /**
   * Getter for the quality of a signal
   *
   * @return the quality of a signal
   */
  public int getQuality() {
    return quality;
  }

  /**
   * Setter for the quality of a signal
   *
   * @param quality that will be set for the signal
   */
  public void setQuality(int quality) {
    this.quality = quality;
  }

  /**
   * Getter for the value of a signal
   *
   * @return the value of a signal
   */
  public double getValue() {
    return value;
  }

  /**
   * Setter for the value of a signal
   *
   * @param value that will be set for the signal
   * @return Fluent interface
   */
  public Trend setValue(double value) {
    this.value = value;
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

    Trend trend = (Trend) o;

    return millis == trend.millis && quality == trend.quality
        && Double.compare(trend.value, value) == 0;
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result;
    long temp;
    result = millis;
    result = 31 * result + quality;
    temp = Double.doubleToLongBits(value);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Trend{" +
        "millis=" + millis +
        ", quality=" + quality +
        ", value=" + value +
        '}';
  }
}
