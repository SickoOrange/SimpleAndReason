/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.siemens.dls.archiveanalytics.model.PortDirection;

/**
 * Class for converting Strings to PortDirection
 */
public class CsvStringToPortDirectionConverter extends AbstractBeanField<PortDirection> {


  /**
   * Converts a String to a PortDirection
   *
   * @param value the String that will be converted to PortDirection
   * @return A PortDirection dependant on the parameter, PortDirection.I for I, PortDirection.O for
   * O
   * @throws CsvDataTypeMismatchException if the data of the csv file is not of the right type
   * @throws CsvConstraintViolationException if the csv is not formatted accordingly
   */
  @Override
  protected Object convert(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    if (PortDirection.I.toString().equals(value)) {
      return PortDirection.I;
    } else if (PortDirection.O.toString().equals(value)) {
      return PortDirection.O;
    } else {
      throw new CsvDataTypeMismatchException(
          "Cannot convert string " + value + " to PortDirection");
    }
  }
}
