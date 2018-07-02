/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for Lambda parameters that will be used for different cases, like orchestration
 */
public class DlsProducerLambdaResult {

  private Boolean success;
  private List<Integer> processed = new ArrayList<>();

  /**
   * Default Constructor
   */
  public DlsProducerLambdaResult() {
  }

  /**
   * Constructor with the full param set
   *
   * @param success The current power plant id
   * @param archiveItems List of the dates to process
   */
  public DlsProducerLambdaResult(Boolean success, Integer... archiveItems) {
    this.success = success;
    this.processed.addAll(Arrays.asList(archiveItems));
  }

  /**
   * Provide the lambda success state
   *
   * @return Boolean for success
   */
  public Boolean getSuccess() {
    return success;
  }

  /**
   * Get the lambda success state
   *
   * @param success Boolean for success
   */
  public void setSuccess(Boolean success) {
    this.success = success;
  }

  /**
   * Provide the number of items written by lambda
   *
   * @return Integer number of processed items
   */
  public List<Integer> getProcessed() {
    return processed;
  }

  /**
   * Set the number of items written by lambda
   *
   * @param itemCollection Collection of Integers representing the number of processed items
   */
  public void setProcessed(Collection<Integer> itemCollection) {
    this.processed.addAll(itemCollection);
  }

  /**
   * Add a number of items written by lambda
   *
   * @param items Integer number of processed items
   */
  public void addProcessed(Integer items) {
    this.processed.add(items);
  }

  @Override
  public String toString() {
    return "DlsProducerLambdaResult={success=" + success + ", processed:[" +
        processed.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]}";
  }
}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
