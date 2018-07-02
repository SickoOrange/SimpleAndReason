/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import java.util.List;

/**
 * Class for Lambda parameters that will be used for different cases, like orchestration
 */
public class DlsProducerLambdaParams {

  private String ppid;
  private List<String> dates;
  private Integer index;
  private String bucket;
  private Paths paths;

  /**
   * Default Constructor
   */
  public DlsProducerLambdaParams() {
  }

  /**
   * Constructor with the full param set
   *
   * @param ppid The current power plant id
   * @param dates List of the dates to process
   * @param index The date index to be used in the current execution
   */
  public DlsProducerLambdaParams(String ppid, List<String> dates, Integer index, String bucket,
      Paths paths) {
    this.ppid = ppid;
    this.dates = dates;
    this.index = index;
    this.bucket = bucket;
    this.paths = paths;
  }

  /**
   * Provide the current power plant id
   *
   * @return String
   */
  public String getPpid() {
    return ppid;
  }

  /**
   * Set the current power plant id
   *
   * @param ppid The current power plant id
   */
  public void setPpid(String ppid) {
    this.ppid = ppid;
  }

  /**
   * Provide the current date based on the index value
   *
   * @return String
   */
  public String getDate() {
    return dates.get(index);
  }

  /**
   * Provide the dates list
   *
   * @return List<String>
   */
  public List<String> getDates() {
    return dates;
  }

  /**
   * Set the dates list
   *
   * @param dates The current dates list
   */
  public void setDates(List<String> dates) {
    this.dates = dates;
  }

  /**
   * Provide the current execution index
   *
   * @return Integer
   */
  public Integer getIndex() {
    return index;
  }

  /**
   * Set the current execution index
   *
   * @param index The current execution index
   */
  public void setIndex(Integer index) {
    this.index = index;
  }

  /**
   * Getter for the path information
   *
   * @return Paths
   */
  public Paths getPaths() {
    return paths;
  }

  /**
   * Setter for the path information
   *
   * @param paths The Paths to set
   */
  public void setPaths(Paths paths) {
    this.paths = paths;
  }

  /**
   * Get the set bucket
   *
   * @return The bucket name
   */
  public String getBucket() {
    return bucket;
  }

  /**
   * Set the bucket name
   *
   * @param bucket The bucket name
   */
  public void setBucket(String bucket) {
    this.bucket = bucket;
  }
}

/**
 * POJO to nest path informations
 */
class Paths {

  private String archive;
  private String engineering;

  /**
   * Dafault constructor
   */
  public Paths() {
  }

  /**
   * Parametrized constructor
   *
   * @param archive The archive path
   * @param engineering The engineering path
   */
  public Paths(String archive, String engineering) {
    this.archive = archive;
    this.engineering = engineering;
  }

  public String getArchive() {
    return archive;
  }

  public void setArchive(String archive) {
    this.archive = archive;
  }

  public String getEngineering() {
    return engineering;
  }

  public void setEngineering(String engineering) {
    this.engineering = engineering;
  }
}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
