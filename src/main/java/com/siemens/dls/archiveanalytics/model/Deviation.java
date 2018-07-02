/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines a Deviation with multiple channels
 */
public class Deviation implements Comparable<Deviation> {

  private final Double deviationValue;
  private final Map<String, Double> values;

  /**
   * Constructor of the Tuple. Both sides must exist
   *
   * @param deviationValue The calculated deviationValue
   * @param values The set of values leading to the deviation
   */
  public Deviation(double deviationValue, Map<String,Double> values) {
    this.deviationValue = deviationValue;
    this.values = new HashMap<>();
    values.forEach((s, aDouble) -> {
      this.values.put(s,aDouble);
    });
  }

  public static Deviation empty() {
    Map<String,AnalogTrend> map = new HashMap<>();
    map.put("Null",null);
    return new Deviation(0d, new HashMap<>());
  }

  /**
   * Getter for the deviationValue part of the Tuple
   *
   * @return the deviationValue part of the tuple
   */
  public double getDeviationValue() {
    return deviationValue;
  }

  /**
   * Getter for the values part of the Tuple
   *
   * @return the values part of the tuple
   */
  public Map<String, Double> getValues() {
    return values;
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
    Deviation other = (Deviation) o;
    return deviationValue.equals(other.getDeviationValue()) && values.equals(other.getValues());
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = deviationValue.hashCode();
    result = 31 * result + values.hashCode();
    return result;
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Tuple{" +
        "deviationValue=" + deviationValue +
        ", values=[" + values.entrySet().stream()
        .map(e -> String.format(
            "(%s:%f)", e.getKey(), e.getValue())
        ).collect(Collectors.joining(", ")) +
        "]}";
  }

  /**
   * @param o The other {@link Deviation}
   * @return int
   * @see Comparable::compareTo
   */
  @Override
  public int compareTo(Deviation o) {
    if (o == null) {
      return 1;
    }
    return deviationValue.compareTo(o.getDeviationValue());
  }
}
