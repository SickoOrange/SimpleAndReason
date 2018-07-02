/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static com.siemens.dls.archiveanalytics.TestUtils.conn;
import static com.siemens.dls.archiveanalytics.TestUtils.inPortBare;
import static com.siemens.dls.archiveanalytics.TestUtils.outPortBare;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opencsv.bean.CsvToBeanFilter;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Network;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.parser.DlsCsvParser;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class S3DataLoaderTest {

  @Mock
  private S3DataLoader loader;

  @Mock
  private DlsCsvParser parser;

  @Before
  public void setUp() throws Exception {
    when(loader.getEngFileKey(any(), any())).thenReturn("file");

    Module m1 = new Module().setAfiTypeId(100).setId(1);
    Module m2 = new Module().setAfiTypeId(100).setId(2);
    Module m3 = new Module().setAfiTypeId(200).setId(3);
    Module m4 = new Module().setAfiTypeId(400).setId(4);
    Module m5 = new Module().setAfiTypeId(500).setId(5);
    Module m6 = new Module().setAfiTypeId(200).setId(6);

    List<Module> modules = Arrays.asList(m1, m2, m3, m4, m5, m6);

    Port p1_10 = inPortBare(m1, 10);
    Port p1_20 = inPortBare(m1, 20);
    Port p2_10 = inPortBare(m2, 10);
    Port p2_20 = inPortBare(m2, 20);
    Port p3_10 = inPortBare(m3, 10);
    Port p3_1000 = outPortBare(m3, 1000);
    Port p4_70 = inPortBare(m4, 70);
    Port p4_1010 = outPortBare(m4, 1010);
    Port p5_50 = inPortBare(m5, 50);
    Port p5_60 = outPortBare(m5, 60);
    Port p5_5000 = inPortBare(m5, 5000);
    Port p6_2000 = outPortBare(m6, 2000);

    List<Port> ports = Arrays.asList(p1_10, p1_20, p2_10, p2_20, p3_10, p3_1000, p4_70, p4_1010,
        p6_2000, p5_50, p5_60, p5_5000);
    when(loader.loadConnections())
        .thenReturn(Stream.of(conn(p3_1000, p1_10), conn(p4_1010, p3_10), conn(p6_2000, p1_20)));

    when(loader.getDlsCsvParser()).thenReturn(parser);
    when(parser.loadPorts(any(), any())).then((Answer<Iterator<Port>>) invocation -> {
      CsvToBeanFilter filter = invocation.getArgument(1);
      return ports.stream().filter(p -> filter.allowLine(TestUtils.portToLine(p))).iterator();
    });
    when(parser.loadModules(any(), any())).then((Answer<Iterator<Module>>) invocation -> {
      CsvToBeanFilter filter = invocation.getArgument(1);
      return modules.stream().filter(m -> filter.allowLine(TestUtils.moduleToLine(m))).iterator();
    });
    when(loader.getModulesByIdFromS3(any())).thenCallRealMethod();
    when(loader.streamPortsFromS3(any(), any())).thenCallRealMethod();
    when(loader.loadNetwork(anyMap())).thenCallRealMethod();
    when(loader.loadNetwork(anyMap(), anyMap())).thenCallRealMethod();
  }

  @Test
  public void testLoadNetwork() throws Exception {
    Network network = loader
        .loadNetwork(ImmutableMap.of(100, ImmutableSet.of(10, 20), 200, ImmutableSet.of(10, 1000)));
    assertEquals(4, network.getModules().size());
    assertThat(network.getModules().keySet(), Matchers.containsInAnyOrder(1, 2, 3, 6));
    Module m1 = network.getModules().get(1);
    Module m2 = network.getModules().get(2);
    Module m3 = network.getModules().get(3);
    Module m6 = network.getModules().get(6);
    Optional<Port> p1_10 = m1.findPortById(10);
    Optional<Port> p1_20 = m1.findPortById(20);
    Optional<Port> p3_1000 = m3.findPortById(1000);
    Optional<Port> p3_10 = m3.findPortById(10);
    assertEquals(true, p1_10.isPresent());
    assertEquals(true, p1_10.get().getConnectedOutPort().isPresent());
    assertEquals(true, p3_1000.isPresent());
    assertThat(p1_10.get().getConnectedOutPort().get(), is(p3_1000.get()));
    assertEquals(true, m6.getOutPorts().isEmpty());
    assertEquals(true, p1_20.isPresent());
    assertEquals(true, p1_20.get().isConnected());
    assertEquals(false, p1_20.get().getConnectedOutPort().isPresent());
    assertEquals(2, m2.getInPorts().size());
    assertEquals(true, p3_10.get().isConnected());
    assertEquals(false, p3_10.get().getConnectedOutPort().isPresent());
  }

  @Test
  public void testLoadNetworkWithExtensions() throws Exception {
    Network network = loader
        .loadNetwork(ImmutableMap.of(100, ImmutableSet.of(10, 20), 200, ImmutableSet.of(10, 1000)),
            ImmutableMap.of(100, ImmutableSet.of(20), 200, ImmutableSet.of(10)));
    assertEquals(5, network.getModules().size());
    assertThat(network.getModules().keySet(), Matchers.containsInAnyOrder(1, 2, 3, 4, 6));
    Module m1 = network.getModules().get(1);
    Module m2 = network.getModules().get(2);
    Module m3 = network.getModules().get(3);
    Module m4 = network.getModules().get(4);
    Module m6 = network.getModules().get(6);
    Optional<Port> p1_10 = m1.findPortById(10);
    Optional<Port> p1_20 = m1.findPortById(20);
    Optional<Port> p3_1000 = m3.findPortById(1000);
    Optional<Port> p3_10 = m3.findPortById(10);
    Optional<Port> p4_1010 = m4.findPortById(1010);
    assertEquals(true, p1_10.isPresent());
    assertEquals(true, p1_10.get().getConnectedOutPort().isPresent());
    assertEquals(true, p3_1000.isPresent());
    assertThat(p1_10.get().getConnectedOutPort().get(), is(p3_1000.get()));
    assertEquals(1, m6.getOutPorts().size());
    assertEquals(true, p1_20.isPresent());
    assertEquals(true, p1_20.get().isConnected());
    assertEquals(true, p1_20.get().getConnectedOutPort().isPresent());
    assertEquals(2, m2.getInPorts().size());
    assertEquals(true, p3_10.isPresent());
    assertEquals(true, p3_10.get().getConnectedOutPort().isPresent());
    assertEquals(true, p4_1010.isPresent());
    assertThat(p3_10.get().getConnectedOutPort().get(), is(p4_1010.get()));
  }

  @Test
  public void testLoadNetworkAllPortsOfModule() throws Exception {
    Network network = loader
        .loadNetwork(ImmutableMap.of(500, ImmutableSet.of()));
    assertEquals(1, network.getModules().size());
    assertThat(network.getModules().keySet(), Matchers.containsInAnyOrder(5));
    Module m5 = network.getModules().get(5);
    Optional<Port> p5_50 = m5.findPortById(50);
    Optional<Port> p5_60 = m5.findPortById(60);
    Optional<Port> p5_5000 = m5.findPortById(5000);
    assertEquals(true, p5_50.isPresent());
    assertEquals(true, p5_60.isPresent());
    assertEquals(true, p5_5000.isPresent());
  }
}

/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
