/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

import static com.siemens.dls.archiveanalytics.parser.WrappedException.wrapException;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import com.siemens.dls.archiveanalytics.model.Trend;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for converting a String to a TrendList
 */
public class CsvStringToTrendListConverter extends AbstractBeanField<PortDirection> {

  /**
   * Splits a String at "|" and produces {@link Trend} of every single String. Each Trend consists
   * of 3 values (time, quality value)
   *
   * @param value String that should be split in Trends
   * @return all Trends of the given String
   * @throws CsvDataTypeMismatchException if the data of the csv file is not of the right type
   * @throws CsvConstraintViolationException if the csv is not formatted accordingly
   */
  @Override
  protected Object convert(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {

    return stringToTrendList(value);
  }

  public static List<Trend> stringToTrendList(String value) throws CsvDataTypeMismatchException {
    try {
      return Arrays.stream(value.split("\\|"))
          .filter(s -> s.length() > 0)
          .map(s -> wrapException(() -> stringToTrend(s)))
          .collect(Collectors.toList());
    } catch (WrappedException e) {
      if (e.getCause() instanceof CsvDataTypeMismatchException) {
        throw (CsvDataTypeMismatchException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  /**
   * Splits a String and creates {@link Trend} out of it
   */
  private static Trend stringToTrend(String s) throws CsvDataTypeMismatchException {
    String[] parts = s.split(",");
    try {
      return new Trend(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
          Double.parseDouble(parts[2]));
    } catch (NumberFormatException e) {
      throw new CsvDataTypeMismatchException("Cannot convert string " + s + " to Trend");
    }
  }

}
