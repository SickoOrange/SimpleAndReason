/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.parser;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Class for converting Strings to Boolean
 */
public class CsvStringToLocalDateTimeConverter extends AbstractBeanField<Boolean> {

  private static final String DATE_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS";
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      DATE_TIME_FORMAT);

  /**
   * Converts a CSV String value to {@link java.time.LocalDateTime}. The string must have the format
   * 'yyyy.MM.dd HH:mm:ss.SSS'
   *
   * @param value that shall be converted to {@link java.time.LocalDateTime}
   * @throws CsvDataTypeMismatchException if the data of the csv file is not of the right type
   * @throws CsvConstraintViolationException if the csv is not formatted accordingly
   * @see java.time.format.DateTimeFormatter
   */
  @Override
  protected LocalDateTime convert(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    try {
      return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new CsvDataTypeMismatchException(String
          .format("Couldn't convert string '%s' to LocalDateTime according to pattern %s", value,
              DATE_TIME_FORMAT));
    }
  }
}
