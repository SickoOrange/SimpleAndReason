/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics;

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.AlarmType;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Connection;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class AlarmAndOrTestHelper {

  public static void prepareMockLoader(S3DataLoader loader,
      Map<Integer, Module> modules,
      Stream<Connection> connections,
      List<Port> ports,
      Map<Port, List<Alarm>> alarms,
      Map<Port, BinaryTrend> binTrends, Map<Integer, AlarmType> mockAlarmType) throws IOException {
    when(loader.loadNetwork(Mockito.anyMap(), Mockito.anyMap())).thenCallRealMethod();
    when(loader.loadConnections()).thenReturn(connections);
    when(loader.getModulesByIdFromS3(Mockito.any())).thenReturn(modules);
    when(loader.streamPortsFromS3(Mockito.any(), Mockito.anyMap())).then(
        (Answer<Stream<Port>>) invocation -> ports.stream());
    when(loader.getBinaryTrends(Mockito.any())).thenReturn(binTrends);
    when(loader.getAlarmsFromS3(Mockito.anySet())).thenReturn(alarms);
    when(loader.getAlarmTypesFromS3()).thenReturn(mockAlarmType);
  }

  public static Map<Integer, AlarmType> mockAlarmType() {
    return Maps
        .uniqueIndex(ImmutableList.of(
            new AlarmType().setId(0).setAbbrev("U"),
            new AlarmType().setId(1).setAbbrev("A"),
            new AlarmType().setId(3).setAbbrev("W"),
            new AlarmType().setId(5).setAbbrev("T"),
            new AlarmType().setId(6).setAbbrev("I&C"),
            new AlarmType().setId(7).setAbbrev("C"),
            new AlarmType().setId(8).setAbbrev("M"),
            new AlarmType().setId(9).setAbbrev("S"),
            new AlarmType().setId(11).setAbbrev("D"),
            new AlarmType().setId(12).setAbbrev("MP"),
            new AlarmType().setId(14).setAbbrev("O"),
            new AlarmType().setId(19).setAbbrev("TS"),
            new AlarmType().setId(20).setAbbrev("TW"),
            new AlarmType().setId(21).setAbbrev("DR"),
            new AlarmType().setId(101).setAbbrev("L"),
            new AlarmType().setId(102).setAbbrev("B"),
            new AlarmType().setId(103).setAbbrev("SA"),
            new AlarmType().setId(104).setAbbrev("Sz"),
            new AlarmType().setId(105).setAbbrev("LT")
        ), AlarmType::getId);
  }

}
