/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.model.AbstractTrend;
import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.AlarmType;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Connection;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.OperatorAction;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.hamcrest.Matcher;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class TestUtils {

  private TestUtils() {
  }

  public static void prepareMockS3FileContent(String csvFileName, String csvFileContent,
      AmazonS3 s3) {
    when(s3.getObject(eq("bucket"), endsWith(csvFileName))).then(invocation -> {
      S3Object s3Object = mock(S3Object.class);
      try {
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(
            IOUtils.toInputStream(csvFileContent, "utf-8"), mock(HttpRequestBase.class)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return s3Object;
    });
  }

  public static Port outPortBare(Module mod, int id) {
    return newPort(mod, id).setDirection(PortDirection.O);
  }

  public static Port outPort(Module mod, int id) {
    Port port = outPortBare(mod, id);
    mod.addPort(port);
    return port;
  }

  public static Port inPortBare(Module mod, int id) {
    return newPort(mod, id).setDirection(PortDirection.I);
  }

  public static Port inPort(Module mod, int id) {
    Port port = inPortBare(mod, id);
    mod.addPort(port);
    return port;
  }

  private static Port newPort(Module mod, int id) {
    return new Port().setAfiId(mod.getId()).setId(id);
  }

  public static Connection conn(Port out, Port in) {
    return new Connection(out.getKey(), in.getKey());
  }

  @SafeVarargs
  public static final <T extends AbstractTrend> Map<String, T> trends(T... trends) {
    return Arrays.stream(trends)
        .collect(Collectors.toMap(AbstractTrend::getUniqueName, Function.identity()));
  }

  @SafeVarargs
  public static final <T extends AbstractTrend> Map<Port, T> trendsByPort(T... trends) {
    return Arrays.stream(trends)
        .collect(Collectors.toMap(AbstractTrend::getPort, Function.identity()));
  }

  public static final Map<Port, List<Alarm>> alarms(Alarm... alarms) {
    return Arrays.stream(alarms)
        .collect(Collectors.groupingBy(Alarm::getPort));
  }

  public static Alarm alarm(Port port, int alarmStartMillis, int duration, int timeToNext) {
    LocalDateTime time = LocalDateTime.of(LocalDate.of(2017, 9, 12),
        LocalTime.ofNanoOfDay(alarmStartMillis * Utils.NANOS_PER_MILLISECOND));
    return new Alarm().setDuration(timeToNext).setPort(port).setTime(time)
        .setTagname(port.getUniqueName()).setQuality(192).setDuration(duration)
        .setTimeToNext(timeToNext);
  }


  //Multimap<Port, OperatorAction>
  public static HashMultimap<Port, OperatorAction> opracts(
      OperatorAction... operatorActions) {
    HashMultimap<Port, OperatorAction> opaMap = HashMultimap.create();
    Arrays.stream(operatorActions).forEach(opa -> opaMap.put(opa.getPort(), opa));
    return opaMap;
  }

  /**
   * Creates a {@link Matcher<Item>} which matches {@link Item}s irrespective of the order
   * the fields were added and matches the field with name {@link DlsLambdaHandler#DDB_FIELD_ITEMS}
   * as a json array ignoring order of items in it
   */
  public static final Matcher<Item> sameItem(Item item) {
    return ItemMatcher.sameItem(item);
  }

  static String[] moduleToLine(Module m) {

    return new String[]{
        Integer.toString(m.getId()), //afiid
        Integer.toString(m.getNode()), // nodeid
        Integer.toString(m.getAfiTypeId()), // afitypeid
        m.getSymbol(), // symbol
        "", // name
        "", // designation
        "", // afcid
        "" // cycle
    };
  }

  static String[] portToLine(Port p) {
    return new String[]{Integer.toString(p.getAfiId()),
        Integer.toString(p.getId()),
        p.getName(),
        "", //portdesc
        "", //afitypeid
        "", // symbol
        "", // type
        p.getDirection().toString(), // io
        p.getParameter(), // parameter
        boolToX(p.isArchive()), // isarchive
        boolToX(p.isAlarm()), // isalarm
        Integer.toString(p.getAlarmTypeId()), // alarmtypeid
        "", // abbrev
        "", // activerule
        "", // inactiverule
        "", // active
        "", // inactive
        Float.toString(p.getMinValue()), // minvalue
        Float.toString(p.getMaxValue()), // maxvalue
        "", // percent
        p.getEngineeringUnit(), // engunit
        "", // signal
        "", // sgnalinfo
        "", // connafiid
        "", // connportid
        p.getUniqueName() // uniquename
    };
  }

  private static String boolToX(boolean b) {
    return b ? "X" : "";
  }

  /**
   *  assert object is not null and return the same object
   */
  public static Object notNull(Object o) {
    assert o != null;
    return o;
  }



}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
