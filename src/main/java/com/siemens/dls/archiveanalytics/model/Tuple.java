/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

/**
 * Defines a Tuple with left and right
 *
 * @param <L> the left part of the Tuple
 * @param <R> the right part of the Tuple
 */
public class Tuple<L, R> {

  private final L left;
  private final R right;

  /**
   * Constructor of the Tuple. Both sides must exist
   *
   * @param left the left part of the Tuple
   * @param right the right part of the Tuple
   */
  public Tuple(L left, R right) {
    if (left == null || right == null) {
      throw new IllegalArgumentException("null argument");
    }
    this.left = left;
    this.right = right;
  }

  /**
   * Shorthand for constructor. Both sides must exist
   *
   * @param left the left part of the Tuple
   * @param right the right part of the Tuple
   */
  public static <L, R> Tuple<L, R> of(L left, R right){
    return new Tuple<>(left, right);
  }

  /**
   * Getter for the left part of the Tuple
   *
   * @return the left part of the tuple
   */
  public L getLeft() {
    return left;
  }

  /**
   * Getter for the right part of the Tuple
   *
   * @return the right part of the tuple
   */
  public R getRight() {
    return right;
  }

  /**
   * Checks if object is equal
   *
   * @param o the Object that has to be checked
   * @return boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Tuple<?, ?> tuple = (Tuple<?, ?>) o;

    return left.equals(tuple.left) && right.equals(tuple.right);
  }

  /**
   * @return hashcode
   * @see Object::hashCode
   */
  @Override
  public int hashCode() {
    int result = left.hashCode();
    result = 31 * result + right.hashCode();
    return result;
  }

  /**
   * @return String
   * @see Object::toString
   */
  @Override
  public String toString() {
    return "Tuple{" +
        "left=" + left +
        ", right=" + right +
        '}';
  }
}
