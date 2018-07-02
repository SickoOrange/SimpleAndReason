/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a JoinRow of a given length. A JoinRow can store any amount of values, default length is
 * 3.
 */
public class JoinRow {

  public static final int VALUE_LIST_INITIAL_CAPACITY = 3;
  private List<Object> values;

  /**
   * JoinRow costructor with a given length of 3
   */
  public JoinRow() {
    this.values = new ArrayList<>(VALUE_LIST_INITIAL_CAPACITY);
  }

  /**
   * JoinRow constructor with variable length
   *
   * @param values length of the JoinRow
   */
  public JoinRow(List<Object> values) {
    this.values = new ArrayList<>(values);
  }

  /**
   * Adds a value to a JoinRow
   *
   * @param value that should be added to the JoinRow
   * @return Fluent interface
   */
  public JoinRow addValue(Object value) {
    values.add(value);
    return this;
  }

  /**
   * getter for JoinRow values
   *
   * @return Values of the JoinRow
   */
  public List<Object> getValues() {
    return Collections.unmodifiableList(values);
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "JoinRow{" +
        "values=" + values +
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

    JoinRow joinRow = (JoinRow) o;

    return values.equals(joinRow.values);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
