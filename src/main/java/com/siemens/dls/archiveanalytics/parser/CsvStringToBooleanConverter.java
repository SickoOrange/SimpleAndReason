/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.parser;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

/**
 * Class for converting Strings to Boolean
 */
public class CsvStringToBooleanConverter extends AbstractBeanField<Boolean> {

  public static final String CSV_TRUE = "X";

  /**
   * Converts a CSV String value to boolean. Value 'X' is interpreted as true, anything else as false.
   *
   * @param value that shall be converted to boolean
   * @return true or false dependant on the parameter true for X, false for every other String
   * @throws CsvDataTypeMismatchException if the data of the csv file is not of the right type
   * @throws CsvConstraintViolationException if the csv is not formatted accordingly
   */
  @Override
  protected Boolean convert(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    return toBoolean(value);
  }

  /**
   * Converts a CSV String value to boolean. Value 'X' is interpreted as true, anything else as false.
   */
  public static boolean toBoolean(String value) {
    return CSV_TRUE.equals(value);
  }
}
