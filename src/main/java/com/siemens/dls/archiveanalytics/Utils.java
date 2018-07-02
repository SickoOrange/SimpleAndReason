/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class that provide useful utility functions for working with DynamoDB and AWS in general
 */
public class Utils {

  public static final long NANOS_PER_MILLISECOND = 1000000L;

  /**
   * Default constructor
   */
  private Utils() {}

  /**
   * Returns a String of the fitting environmental variable of the parameter
   *
   * @param varName the name of the variable for which a environmental variable is needed
   * @return the environmental variable needed
   */
  public static String getEnvironmentVariable(String varName) {
    String var = System.getenv(varName);
    if (var == null || var.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Environment variable %s must be defined", varName));
    }
    return var;
  }

  /**
   * Makes an iterator usable as a stream
   *
   * @param iterator that shall be used as a stream
   * @param <T> type of the stream
   * @return a Stream of the iterator
   */
  public static <T> Stream<T> iteratorAsStream(Iterator<T> iterator) {
    return StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(iterator,
            Spliterator.ORDERED), false);
  }

  public static int toMillisOfDay(LocalDateTime time) {
    return (int) (time.toLocalTime().toNanoOfDay() / NANOS_PER_MILLISECOND);
  }
}
