/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;

import com.siemens.dls.archiveanalytics.model.Port;
import java.util.List;
import java.util.Set;

public class NodeReason {

  private Port root;
  private Port currentNode;

  //ancestor node for current node
  private Port ancestor;
  //all children nodes for current node
  private Set<Port> children;

  //source state, at the time, when a alert is generated
  private int value;
  //code of problem
  private int code;
  //expect input value at target module
  private int relevantValue;

  private int relevant;

  //intermediately modules between source and alert module
  private int depth;

  private NodeReason(Builder builder) {

    this.root = builder.getRoot();
    this.currentNode = builder.getCurrentNode();
    this.ancestor = builder.getAncestor();
    this.children = builder.getChildren();
    this.value = builder.getValue();
    this.code = builder.getCode();
    this.relevantValue = builder.getRelevantValue();
    this.relevant = builder.getRelevant();
    this.depth = builder.getDepth();
  }

  static class Builder {

    private final Port root;
    private final Port currentNode;
    private Port ancestor;
    private Set<Port> children;

    private int value;
    private int code;
    private int relevantValue;
    private int relevant;
    private int depth;

    public Builder(Port root, Port currentNode) {
      this.root = root;
      this.currentNode = currentNode;
    }

    public Port getRoot() {
      return root;
    }

    public Port getCurrentNode() {
      return currentNode;
    }

    public Port getAncestor() {
      return ancestor;
    }

    public Builder setAncestor(Port ancestor) {
      this.ancestor = ancestor;
      return this;
    }

    public Set<Port> getChildren() {
      return children;
    }

    public Builder setChildren(Set<Port> children) {
      this.children = children;
      return this;
    }

    public int getValue() {
      return value;
    }

    public Builder setValue(int value) {
      this.value = value;
      return this;
    }

    public int getCode() {
      return code;
    }

    public Builder setCode(int code) {
      this.code = code;
      return this;
    }

    public int getRelevantValue() {
      return relevantValue;
    }

    public Builder setRelevantValue(int relevantValue) {
      this.relevantValue = relevantValue;
      return this;
    }

    public int getRelevant() {
      return relevant;
    }

    public Builder setRelevant(int relevant) {
      this.relevant = relevant;
      return this;
    }

    public int getDepth() {
      return depth;
    }

    public Builder setDepth(int depth) {
      this.depth = depth;
      return this;
    }

    public NodeReason build() {
      return new NodeReason(this);
    }
  }

  public Port getRoot() {
    return root;
  }

  public Port getCurrentNode() {
    return currentNode;
  }

  public Port getAncestor() {
    return ancestor;
  }

  public Set<Port> getChildren() {
    return children;
  }

  public int getValue() {
    return value;
  }

  public int getCode() {
    return code;
  }

  public int getRelevantValue() {
    return relevantValue;
  }

  public int getRelevant() {
    return relevant;
  }

  public int getDepth() {
    return depth;
  }

}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
