/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TupleTest {

  @Test
  public void testConstructorGetLeftGetRight() {
    Tuple<Integer, Integer> sut = new Tuple<>(42, 1337);
    assertEquals(42, sut.getLeft().intValue());
    assertEquals(1337, sut.getRight().intValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorFailingLeft() {
    Tuple<Integer, Integer> sut = new Tuple<>(null, 1337);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorFailingRight() {
    Tuple<Integer, Integer> sut = new Tuple<>(42, null);
  }

  @Test
  public void testEquals() {
    Tuple<Integer, Integer> sut = new Tuple<>(42, 1337);
    Tuple<Integer, Integer> other = new Tuple<>(42, 1337);
    assertTrue(sut.equals(other));
  }

  @Test
  public void testEqualsNegative() {
    Tuple<Integer, Integer> sut = new Tuple<>(42, 1337);
    Tuple<Integer, Integer> other = new Tuple<>(423, 1337);
    Tuple<Integer, Integer> yetAnother = new Tuple<>(42, 1338);
    assertFalse(sut.equals(other));
    assertFalse(sut.equals(yetAnother));
  }

  @Test
  public void testHashCodeToString() {
    Tuple<Integer, Integer> sut = new Tuple<>(42, 1337);
    assertEquals(31 * sut.getLeft() + sut.getRight(), sut.hashCode());
    assertEquals("Tuple{left=42, right=1337}", sut.toString());
  }
}
