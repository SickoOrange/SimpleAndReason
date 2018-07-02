/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;

import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import java.util.List;
import java.util.Objects;

public class AlertsOnGateReason {

  private Port sourcePort;
  private Port potentialArchivedPort;
  private Port rootPort;
  private int inter;
  private int value;

  private long reasons;
  private int code;

  private List<Module> interModules;

  public AlertsOnGateReason(Port sourcePort, Port potentialArchivedPort,
      Port rootPort, int inter, int value,
      List<Module> interModules) {
    code = -1;
    this.sourcePort = sourcePort;
    this.potentialArchivedPort = potentialArchivedPort;
    this.rootPort = rootPort;
    this.inter = inter;
    this.value = value;
    this.interModules = interModules;
  }

  public AlertsOnGateReason(Port sourcePort, Port potentialArchivedPort, Port rootPort, int inter, int value,
      int count) {
    code = -1;
    this.sourcePort = sourcePort;
    this.potentialArchivedPort = potentialArchivedPort;
    this.rootPort = rootPort;
    this.inter = inter;
    this.value = value;
    this.reasons = count;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public long getReasons() {
    return reasons;
  }

  public void setReasons(long reasons) {
    this.reasons = reasons;
  }

  public Port getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(Port sourcePort) {
    this.sourcePort = sourcePort;
  }

  public Port getPotentialArchivedPort() {
    return potentialArchivedPort;
  }

  public void setPotentialArchivedPort(Port potentialArchivedPort) {
    this.potentialArchivedPort = potentialArchivedPort;
  }

  public Port getRootPort() {
    return rootPort;
  }

  public void setRootPort(Port rootPort) {
    this.rootPort = rootPort;
  }

  public int getInter() {
    return inter;
  }

  public void setInter(int inter) {
    this.inter = inter;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public List<Module> getInterModules() {
    return interModules;
  }

  public void setInterModules(List<Module> interModules) {
    this.interModules = interModules;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourcePort, potentialArchivedPort, rootPort, inter, value, reasons, code,
        interModules);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    AlertsOnGateReason reason = (AlertsOnGateReason) obj;
    return this.sourcePort == reason.getSourcePort() &&
        this.potentialArchivedPort == reason.getPotentialArchivedPort() &&
        this.rootPort == reason.getRootPort() &&
        this.inter == reason.getInter() &&
        this.value == reason.getValue() &&
        this.reasons == reason.getReasons() &&
        this.code == reason.getCode() &&
        this.interModules.size() == reason.getInterModules().size();
  }

  @Override
  public String toString() {
    return "AlertsOnGateReason{" +
        "sourcePort=" + sourcePort +
        ", potentialArchivedPort=" + potentialArchivedPort +
        ", rootPort=" + rootPort +
        ", inter=" + inter +
        ", value=" + value +
        ", reasons=" + reasons +
        ", code=" + code +
        ", interModules=" + interModules +
        '}';
  }
}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
