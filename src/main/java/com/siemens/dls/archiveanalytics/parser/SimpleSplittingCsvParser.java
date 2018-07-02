/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

import com.opencsv.ICSVParser;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.IOException;

/**
 * Class for simple splitting of csv files without special checks
 */
class SimpleSplittingCsvParser implements ICSVParser {

  private char separator;
  private String separatorAsString;

  /**
   * Constructor for SimpleSplittingCsvParser
   *
   * @param separator by which the csv file should be split
   */
  SimpleSplittingCsvParser(char separator) {
    this.separator = separator;
    separatorAsString = Character.toString(separator);
  }

  /**
   * Getter for the separator of the csv file
   *
   * @return the seperator
   */
  @Override
  public char getSeparator() {
    return separator;
  }

  /**
   * Getter for quotechar
   *
   * @return always 0 because no quotechar is defined
   */
  @Override
  public char getQuotechar() {
    return 0;
  }

  /**
   * Getter for isPending
   *
   * @return always false
   */
  @Override
  public boolean isPending() {
    return false;
  }

  /**
   * Splits multiple lines by separator
   *
   * @param nextLine line that should be split by separator
   * @return the next line split by the given separator
   * @throws IOException if an error with a line occurred
   */
  @Override
  public String[] parseLineMulti(String nextLine) throws IOException {
    return nextLine.split(separatorAsString);
  }

  /**
   * Splits a line by separator
   *
   * @param nextLine line that should be split by separator
   * @return the next line split by the given separator
   * @throws IOException if an error with the line occurred
   */
  @Override
  public String[] parseLine(String nextLine) throws IOException {
    return parseLineMulti(nextLine);
  }

  /**
   * nullFieldIndicator
   *
   * @return always return null
   */
  @Override
  public CSVReaderNullFieldIndicator nullFieldIndicator() {
    return null;
  }
}
