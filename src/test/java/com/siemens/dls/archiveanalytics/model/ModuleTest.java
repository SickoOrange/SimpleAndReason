/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class ModuleTest {

  @Test
  public void testConstructor() {
    Module sut = new Module();
    assertEquals(Module.class, sut.getClass());
  }

  @Test
  public void testExtractIdAndSymbol() {
    String[] line = {"1337", "A", "B", "SymbolName", "FoooBar"};
    assertEquals(1337, Module.extractId(line));
    assertEquals("SymbolName", Module.extractSymbol(line));
  }

  @Test
  public void testSetGetAfiTypeId() {
    Module sut = new Module();
    assertEquals(0, sut.getAfiTypeId());
    assertEquals(sut, sut.setAfiTypeId(42));
    assertEquals(42, sut.getAfiTypeId());
  }

  @Test
  public void testSetGetId() {
    Module sut = new Module();
    assertEquals(0, sut.getId());
    assertEquals(sut, sut.setId(123));
    assertEquals(123, sut.getId());
  }

  @Test
  public void testSetGetSymbol() {
    Module sut = new Module();
    assertEquals(null, sut.getSymbol());
    assertEquals(sut, sut.setSymbol("SymbolName"));
    assertEquals("SymbolName", sut.getSymbol());
  }

  @Test
  public void testAddPortDirectionIn() {
    Module sut = new Module();
    Port inPort1 = new Port().setId(1).setDirection(PortDirection.I);
    Port inPort2 = new Port().setId(2).setDirection(PortDirection.I);
    assertNull(inPort1.getModule());
    sut.addPort(inPort1);
    sut.addPort(inPort2);
    assertEquals(sut, inPort1.getModule());
    assertEquals(Arrays.asList(inPort1, inPort2), sut.getInPorts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddPortDuplicate() {
    Module sut = new Module();
    Port p1 = new Port().setId(1).setDirection(PortDirection.I);
    Port p2 = new Port().setId(1).setDirection(PortDirection.I);
    assertNull(p1.getModule());
    sut.addPort(p1);
    sut.addPort(p2);
  }

  @Test
  public void testAddPortDirectionOut() {
    Module sut = new Module();
    Port outPort = new Port();
    outPort.setDirection(PortDirection.O);
    assertNull(outPort.getModule());
    sut.addPort(outPort);
    assertEquals(sut, outPort.getModule());
    assertEquals(Arrays.asList(outPort), sut.getOutPorts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddPortDirectionNull() {
    Module sut = new Module();
    Port outPort = new Port();
    assertNull(outPort.getModule());
    sut.addPort(outPort);
  }

  @Test(expected = NullPointerException.class)
  public void testAddPortNull() {
    Module sut = new Module();
    sut.addPort(null);
  }

  @Test
  public void testEquals() {
    Module sut = new Module();
    Module other = new Module();
    assertTrue(sut.equals(other));
    sut.setId(42).setAfiTypeId(1337);
    other.setId(42).setAfiTypeId(1337);
    assertTrue(sut.equals(other));
    sut.setSymbol("FOO");
    other.setSymbol("FOO");
    sut.setName("bar");
    other.setName("bar");
    assertTrue(sut.equals(other));
  }

  @Test
  public void testEqualsNegative() {
    Module sut = new Module();
    Module other = new Module();
    assertTrue(sut.equals(other));
    sut.setId(42).setAfiTypeId(1337);
    assertFalse(sut.equals(other));
    other.setId(41).setAfiTypeId(1337);
    assertFalse(sut.equals(other));
    other.setId(42).setAfiTypeId(1338);
    assertFalse(sut.equals(other));
    sut.setSymbol("FOO");
    other.setSymbol("FOO=");
    assertFalse(sut.equals(other));
  }

  @Test
  public void testHashCodeToString() {
    Module sut = new Module();
    sut.setId(42).setAfiTypeId(1337).setNode(13).setSymbol("FOO").setName("bar");
    assertEquals(Objects.hash(1337, 13, 42, "FOO", "bar"), sut.hashCode());
    assertEquals("Module{afiTypeId=1337, node=13, id=42, symbol='FOO', name='bar'}", sut.toString());
  }

  @Test
  public void testFindPortById() {
    Module m = new Module();
    Port p = new Port().setId(1).setDirection(PortDirection.I);
    m.addPort(p);
    Optional<Port> portById = m.findPortById(1);
    assertTrue(portById.isPresent());
    assertThat(portById.get(), is(p));

    Optional<Port> portById1 = m.findPortById(99);
    assertTrue(!portById1.isPresent());
  }

  @Test
  public void testFindPortsByIds() {
    Module m = new Module();
    Port p1 = new Port().setId(1).setDirection(PortDirection.I);
    Port p2 = new Port().setId(2).setDirection(PortDirection.O);
    Port p3 = new Port().setId(3).setDirection(PortDirection.I);
    m.addPort(p1);
    m.addPort(p2);
    m.addPort(p3);
    Set<Port> portsByIds = m.findPortsByIds(1, 2, 99);
    assertThat(portsByIds, containsInAnyOrder(p1, p2));

    Set<Port> portsByIds1 = m.findPortsByIds(98,99);
    assertThat(portsByIds1, empty());
  }
}
