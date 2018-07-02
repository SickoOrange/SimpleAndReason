/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;

import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Network;
import com.siemens.dls.archiveanalytics.model.Port;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterParams {

  private final Port currentNode;
  private final Port startNode;
  private final int depth;
  private final Map<Port, BinaryTrend> binaryTrends;
  private final List<Alarm> alarms;
  private final HashMap<Port, Port> visitedAdjacentMap;
  private final Network network;

  private FilterParams(Builder builder) {
    this.currentNode = builder.currentNode;
    this.startNode = builder.startNode;
    this.depth = builder.depth;
    this.binaryTrends = builder.binaryTrends;
    this.alarms = builder.alarms;
    this.visitedAdjacentMap = builder.visitedAdjacentMap;
    this.network = builder.network;
  }


  static class Builder {

    private final Port currentNode;
    private final Port startNode;
    private int depth;
    private Map<Port, BinaryTrend> binaryTrends;
    private List<Alarm> alarms;
    private HashMap<Port, Port> visitedAdjacentMap;
    private Network network;

    public Builder(Port currentNode, Port startNode) {
      this.currentNode = currentNode;
      this.startNode = startNode;
    }

    public Builder setDepth(int depth) {
      this.depth = depth;
      return this;
    }

    public Builder setBinaryTrends(
        Map<Port, BinaryTrend> binaryTrends) {
      this.binaryTrends = binaryTrends;
      return this;
    }

    public Builder setAlarms(List<Alarm> alarms) {
      this.alarms = alarms;
      return this;
    }

    public Builder setVisitedAdjacentMap(
        HashMap<Port, Port> visitedAdjacentMap) {
      this.visitedAdjacentMap = visitedAdjacentMap;
      return this;
    }

    public Builder setNetwork(Network network) {
      this.network = network;
      return this;
    }

    public FilterParams build() {
      return new FilterParams(this);
    }
  }


  public Port getCurrentNode() {
    return currentNode;
  }

  public Port getStartNode() {
    return startNode;
  }

  public int getDepth() {
    return depth;
  }

  public Map<Port, BinaryTrend> getBinaryTrends() {
    return binaryTrends;
  }

  public List<Alarm> getAlarms() {
    return alarms;
  }

  public HashMap<Port, Port> getVisitedAdjacentMap() {
    return visitedAdjacentMap;
  }

  public Network getNetwork() {
    return network;
  }
}
/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */