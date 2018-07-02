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

public class PortKeyTest {

  @Test
  public void testConstructorGetAfiGetPort() {
    PortKey sut = new PortKey(42, 1337);
    assertEquals(PortKey.class, sut.getClass());
    assertEquals(42, sut.getAfiId());
    assertEquals(1337, sut.getPortId());
  }

  @Test
  public void testEquals() {
    PortKey sut = new PortKey(42, 1337);
    PortKey other = new PortKey(42, 1337);
    assertTrue(sut.equals(other));
  }

  @Test
  public void testEqualsNegative() {
    PortKey sut = new PortKey(42, 1337);
    PortKey other = new PortKey(42, 1338);
    PortKey yetAnother = new PortKey(43, 1337);
    assertFalse(sut.equals(other));
    assertFalse(sut.equals(yetAnother));
  }

  @Test
  public void testHashCodeToString() {
    PortKey sut = new PortKey(42, 1337);
    assertEquals(31 * sut.getAfiId() + sut.getPortId(), sut.hashCode());
    assertEquals("PortKey{afiId=42, portId=1337}", sut.toString());
  }
}
