/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An abstract class for any kind of XXXTrend bean.
 * Trends do always contain the client, the unique/tagname and a set of {@link Trend}s
 */
public abstract class AbstractTrend<T extends AbstractTrend> {

  public static final int CLIENT_COLUMN_INDEX = 0;
  public static final int TAGNAME_COLUMN_INDEX = 1;
  public static final int NOT_BAD_QUALITY_THRESHOLD = 64;
  private Port port;

  /**
   * Extracts client from a split CSV line.
   *
   * @param line A split CSV line
   * @return The raw value at the client index
   */
  public static String extractClient(String[] line) {
    return line[CLIENT_COLUMN_INDEX];
  }

  /**
   * Extracts tagname from a split CSV line.
   *
   * @param line A split CSV line
   * @return The raw value at the tagname index
   */
  public static String extractTagname(String[] line) {
    return line[TAGNAME_COLUMN_INDEX];
  }

  /**
   * Getter for ppid. Every {@link AbstractTrend} instance must provide a valid getter.
   *
   * @return Power Plant (Client) ID
   */
  public abstract String getPpid();

  /**
   * Getter for UniqueNames. Every {@link AbstractTrend} instance must provide a unique name.
   *
   * @return UniqueName
   */
  public abstract String getUniqueName();

  /**
   * Getter for the {@link Trend} values
   *
   * @return The {@link Trend} values as {@link List}
   */
  public abstract List<Trend> getTrends();

  public Port getPort() {
    return port;
  }

  public void setPort(Port port) {
    this.port = port;
  }

  /**
   * get trends filtered with quality above "not bad" threshold
   */
  public List<Trend> getAcceptableQualityTrends(){
    return getTrends().stream()
        .filter(t -> t.getQuality() >= NOT_BAD_QUALITY_THRESHOLD)
        .collect(Collectors.toList());
  }

  public abstract T copy();

  List<Trend> copyTrends(AbstractTrend<?> original) {
    return original.getTrends() == null ? null
        : original.getTrends().stream().map(Trend::new).collect(Collectors.toList());
  }
}
