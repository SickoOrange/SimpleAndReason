/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static com.siemens.dls.archiveanalytics.TestUtils.conn;
import static com.siemens.dls.archiveanalytics.TestUtils.inPort;
import static com.siemens.dls.archiveanalytics.TestUtils.outPort;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;

public class NetworkTest {

  @Test
  public void testCreation() {
    Module m1 = new Module().setId(1);
    Module m2 = new Module().setId(2);
    Map<Integer, Module> modules = ImmutableMap.of(1, m1, 2, m2);
    Port p1 = inPort(m1, 10);
    Port p2 = outPort(m2, 1000);

    Network net = new Network(modules, Stream.of(conn(p2, p1)), Stream.of(p1, p2));

    Optional<Port> inPort = net.getModules().get(1).findPortById(10);
    assertTrue(inPort.isPresent());
    assertEquals(p1, inPort.get());
    assertTrue(p1.getConnectedOutPort().isPresent());
    assertEquals(p2, p1.getConnectedOutPort().get());
  }

  @Test
  public void testGetMissingPortKeys() {
    Module m1 = new Module().setId(1);
    Module m2 = new Module().setId(2);
    Map<Integer, Module> modules = ImmutableMap.of(1, m1, 2, m2);
    Port p1 = inPort(m1, 10);
    Port p2 = outPort(m2, 1000);
    Port p3 = inPort(m1, 20);
    Network net = new Network(modules,
        Stream.of(conn(p2, p1), new Connection(new PortKey(3, 1010), new PortKey(1, 20))),
        Stream.of(p1, p2, p3));

    assertThat(net.getMissingPortKeys(ImmutableSet.of(p3)),
        contains(equalTo(new PortKey(3, 1010))));
  }

  @Test
  public void testExtendWith() {
    Module m1 = new Module().setId(1);
    Module m2 = new Module().setId(2);
    Map<Integer, Module> modules = ImmutableMap.of(1, m1, 2, m2);
    Port p1 = inPort(m1, 10);
    Port p2 = outPort(m2, 1000);
    Network net = new Network(modules,
        Stream.of(conn(p2, p1), new Connection(new PortKey(3, 1010), new PortKey(1, 20))),
        Stream.of(p1, p2));
    Module m3 = new Module().setId(3);

    Port p3 = inPort(m1, 20);
    Port p4 = outPort(m3, 1010);
    net.extendWith(ImmutableMap.of(3, m3), Stream.of(p3, p4));

    Optional<Port> inPort1 = net.getModules().get(1).findPortById(10);
    assertTrue(inPort1.isPresent());
    assertThat(inPort1.get(), is(p1));
    assertTrue(p1.getConnectedOutPort().isPresent());
    assertThat(p1.getConnectedOutPort().get(), is(p2));

    Optional<Port> inPort2 = net.getModules().get(1).findPortById(20);
    assertTrue(inPort2.isPresent());
    assertThat(inPort2.get(), is(p3));
    assertTrue(p3.getConnectedOutPort().isPresent());
    assertThat(p3.getConnectedOutPort().get(), is(p4));
  }
}

/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
