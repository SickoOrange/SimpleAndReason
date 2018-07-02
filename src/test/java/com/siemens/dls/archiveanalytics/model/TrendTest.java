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

public class TrendTest {

  @Test
  public void testConstructorGetters() {
    Trend sut = new Trend(1337, 129, 42);
    assertEquals(1337, sut.getMillis());
    assertEquals(129, sut.getQuality());
    assertEquals(42, sut.getValue(), .001);
  }

  @Test
  public void testSetters() {
    Trend sut = new Trend(1337, 129, 42);
    sut.setMillis(12);
    assertEquals(12, sut.getMillis());
    sut.setQuality(0);
    assertEquals(0, sut.getQuality());
    sut.setValue(13.37);
    assertEquals(13.37, sut.getValue(), .001);
  }

  @Test
  public void testEquals() {
    Trend sut = new Trend(42, 1337, .9);
    Trend other = new Trend(42, 1337, .9);
    assertTrue(sut.equals(other));
  }

  @Test
  public void testEqualsNegative() {
    Trend sut = new Trend(42, 1337, .9);
    Trend other = new Trend(423, 1337, .8);
    assertFalse(sut.equals(other));
  }

  @Test
  public void testHashCode() {
    Trend sut = new Trend(42, 1337, .9);

    long temp = Double.doubleToLongBits(sut.getValue());
    long result = 31 * ((31 * sut.getMillis()) + sut.getQuality()) + (int) (temp ^ (temp >>> 32));
    assertEquals(result, sut.hashCode());
  }
}
