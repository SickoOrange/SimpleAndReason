/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import com.siemens.dls.archiveanalytics.parser.CsvStringToLocalDateTimeConverter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Defines an Operative Activity.
 */
public class OperatorAction {

  //Client;Time;Quality;Value;TagName;Afi;Kind;
  public static final int CLIENT_COLUMN_INDEX = 0;
  public static final int TIME_COLUMN_INDEX = 1;
  public static final int QUALITY_COLUMN_INDEX = 2;
  public static final int VALUE_COLUMN_INDEX = 3;
  public static final int TAGNAME_COLUMN_INDEX = 4;
  public static final int AFI_COLUMN_INDEX = 5;
  public static final int KIND_COLUMN_INDEX = 6;

  public static final int NOT_BAD_QUALITY_THRESHOLD = 64;

  @CsvBindByPosition(position = CLIENT_COLUMN_INDEX)
  private int id;
  @CsvCustomBindByPosition(position = TIME_COLUMN_INDEX, converter = CsvStringToLocalDateTimeConverter.class)
  private LocalDateTime time;
  @CsvBindByPosition(position = QUALITY_COLUMN_INDEX)
  private int quality;
  @CsvBindByPosition(position = VALUE_COLUMN_INDEX)
  private double value;
  @CsvBindByPosition(position = TAGNAME_COLUMN_INDEX)
  private String tagname;
  @CsvBindByPosition(position = AFI_COLUMN_INDEX)
  private String afi;
  @CsvBindByPosition(position = KIND_COLUMN_INDEX)
  private String kind;

  private Port port;

  /**
   * default constructor
   */
  public OperatorAction() {
    //noop
  }

  /**
   * Extracts tagname id from a split CSV line.
   */
  public static String extractTagname(String[] line) {
    return line[TAGNAME_COLUMN_INDEX];
  }

  /**
   * Extracts quality from a split CSV line.
   */
  public static int extractQuality(String[] line) {
    return toInt(line[QUALITY_COLUMN_INDEX]);
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public OperatorAction setTime(LocalDateTime time) {
    this.time = time;
    return this;
  }

  public int getQuality() {
    return quality;
  }

  public OperatorAction setQuality(int quality) {
    this.quality = quality;
    return this;
  }

  public double getValue() {
    return value;
  }

  public OperatorAction setValue(double value) {
    this.value = value;
    return this;
  }

  public String getTagname() {
    return tagname;
  }

  public OperatorAction setTagname(String tagname) {
    this.tagname = tagname;
    return this;
  }

  public String getAfi() {
    return afi;
  }

  public void setAfi(String afi) {
    this.afi = afi;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public Port getPort() {
    return port;
  }

  public void setPort(Port port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperatorAction operatorAction = (OperatorAction) o;
    return id == operatorAction.id &&
        Objects.equals(time, operatorAction.time) &&
        Objects.equals(quality, operatorAction.quality) &&
        Objects.equals(value, operatorAction.value) &&
        Objects.equals(tagname, operatorAction.tagname) &&
        Objects.equals(afi, operatorAction.afi) &&
        Objects.equals(kind, operatorAction.kind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, time, quality, value, tagname, afi, kind);
  }
}
