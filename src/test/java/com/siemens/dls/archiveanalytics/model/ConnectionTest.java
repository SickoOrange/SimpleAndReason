/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;

import org.hamcrest.Matchers;
import org.junit.Test;

public class ConnectionTest {

  @Test
  public void testConstructorValidGetInGetOut() {
    Connection sut = new Connection(new PortKey(42, 1337), new PortKey(0, 815));
    assertEquals(new PortKey(42, 1337), sut.getOut());
    assertEquals(new PortKey(0, 815), sut.getIn());
  }

  @Test(expected = AssertionError.class)
  public void testConstructorErrorOut() {
    Connection sut = new Connection(null, new PortKey(0, 815));
  }

  @Test(expected = AssertionError.class)
  public void testConstructorErrorIn() {
    Connection sut = new Connection(new PortKey(42, 1337), null);
  }

  @Test
  public void testExtractPort2Afi2() {
    String[] line = {"N", "A", "F", "T", "42", "1337", "F", "T"};
    assertEquals(1337, Connection.extractPort2(line));
    assertEquals(42, Connection.extractAfi2(line));
  }

  @Test
  public void testSetGetInPort() {
    Connection sut = new Connection(new PortKey(1, 2), new PortKey(3, 4));
    Port inPort = new Port();
    inPort.setAfiId(1337); // Used in Port::equals
    assertNull(sut.getInPort());
    sut.setInPort(inPort);
    assertEquals(inPort, sut.getInPort());
  }

  @Test
  public void testSetGetOutPort() {
    Connection sut = new Connection(new PortKey(1, 2), new PortKey(3, 4));
    Port outPort = new Port();
    outPort.setAfiId(1337); // Used in Port::equals
    assertNull(sut.getOutPort());
    sut.setOutPort(outPort);
    assertEquals(outPort, sut.getOutPort());
  }

  @Test
  public void testEqualsHashCode() {
    PortKey out = new PortKey(1, 2);
    PortKey in = new PortKey(3, 4);
    Connection sut = new Connection(out, in);
    Connection other = new Connection(out, in);
    assertTrue(sut.equals(other));
    assertEquals(31 * out.hashCode() + in.hashCode(), sut.hashCode());
  }

  @Test
  public void testToString() {
    PortKey out = new PortKey(1, 2);
    PortKey in = new PortKey(3, 4);
    Connection sut = new Connection(out, in);
    assertEquals("Connection{out=PortKey{afiId=1, portId=2}, in=PortKey{afiId=3, portId=4}, " +
        "outPort=null, inPort=null}", sut.toString());
  }

  @Test
  public void testToStringPorts() {
    PortKey out = new PortKey(1, 2);
    PortKey in = new PortKey(3, 4);
    Connection sut = new Connection(out, in);
    assertThat(sut.toString(), containsString("Connection"));
    sut.setOutPort(new Port().setId(4242));
    assertThat(sut.toString(), containsString("4242"));
    sut.setInPort(new Port().setId(1337));
    assertThat(sut.toString(), containsString("1337"));
  }
}
