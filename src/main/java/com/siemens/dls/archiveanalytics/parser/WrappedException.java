/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

/**
 * Wrapping Exceptions and extending RuntimeException
 */
class WrappedException extends RuntimeException {

  /**
   * Constructor for the class
   *
   * @param cause the cause of the exception
   */
  public WrappedException(Throwable cause) {
    super(cause);
  }

  @FunctionalInterface
  public interface ExceptionWrapperFunction<R, E extends Throwable> {

    R run() throws E;
  }

  // Generic
  public static <R> R wrapException(ExceptionWrapperFunction<R, Exception> function) {
    try {
      return function.run();
    } catch (Exception exception) {
      throw new WrappedException(exception);
    }
  }
}
