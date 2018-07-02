/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_DATE;
import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_SIM_DUP_OR_REASON;
import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_TAGNAMEALERT;
import static com.siemens.dls.archiveanalytics.TestUtils.alarm;
import static com.siemens.dls.archiveanalytics.TestUtils.alarms;
import static com.siemens.dls.archiveanalytics.TestUtils.conn;
import static com.siemens.dls.archiveanalytics.TestUtils.inPort;
import static com.siemens.dls.archiveanalytics.TestUtils.outPort;
import static com.siemens.dls.archiveanalytics.TestUtils.sameItem;
import static com.siemens.dls.archiveanalytics.TestUtils.trendsByPort;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_AFIIDALERT;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_ALARMTYPE;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_BASE;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_CODE;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_COUNT;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_DELAY_EXTEND;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_DELAY_PREFIX;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_EXTEND_PREFIX;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_INTER;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_MODULE;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_PORT;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_REASONS;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_TAGNAME;
import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateHandler.DDB_FIELD_VALUE;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.siemens.dls.archiveanalytics.ddb.DlsDdbClient;
import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.AlarmType;
import com.siemens.dls.archiveanalytics.model.BinaryTrend;
import com.siemens.dls.archiveanalytics.model.Connection;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.Trend;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class AlarmNotHandlerTest {


  private AlarmNotHandler handler;
  @Mock
  private DlsProducerLambdaParams params;
  @Mock
  private Context context;
  @Mock
  private DlsDdbClient ddb;
  @Mock
  private S3DataLoader loader;

  @Captor
  private ArgumentCaptor<List<Item>> captor;


  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
  private Map<Integer, AlarmType> alarmTypes;

  @Before
  public void setUp() {
    environmentVariables.set(DlsDdbClient.DYNAMODB_TABLE_PREFIX, "aa-branch");
    handler = new AlarmNotHandler(loader, ddb);
    when(params.getDate()).thenReturn("2016-12-06");
    alarmTypes = AlarmAndOrTestHelper.mockAlarmType();
  }

  @Test
  public void test(){
    assertEquals(1,1);
  }

  @Test
  public void testEmptyReason() throws IOException {

    Module notModule = new Module().setAfiTypeId(103).setId(10001).setSymbol("NOT");
    Port notOut = outPort(notModule, 1000).setUniqueName("NOT||1||OUT").setArchive(true)
        .setAlarm(true).setName("OUT");

    Port notIn = outPort(notModule, 10).setUniqueName("NOT||1||IN");
    List<Port> ports = ImmutableList
        .of(notIn, notOut
        );

    Map<Integer, Module> modules = Stream
        .of(notModule
        )
        .collect(Collectors.toMap(Module::getId, Function.identity()));

    Map<Port, List<Alarm>> alarms = alarms(
        alarm(notOut, 1500, 500, 2000),
        alarm(notOut, 3700, 2100, 2000),
        alarm(notOut, 9300, 6000, 2000)
    );

    AlarmAndOrTestHelper
        .prepareMockLoader(loader, modules, Stream.empty(), ports, alarms,
            Collections.emptyMap(),
            alarmTypes);
    DlsProducerLambdaResult handleRequest = handler.handleRequest(params, context);
    assertEquals(true, handleRequest.getSuccess());
    Map<String, Integer> delayExtendMap = ImmutableMap
        .<String, Integer>builder()
        .put(DDB_FIELD_COUNT, 3)
        .put(DDB_FIELD_DELAY_PREFIX + "1", 2)
        .put(DDB_FIELD_DELAY_PREFIX + "2", 2)
        .put(DDB_FIELD_DELAY_PREFIX + "3", 1)
        .put(DDB_FIELD_DELAY_PREFIX + "5", 1)
        .put(DDB_FIELD_DELAY_PREFIX + "10", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "20", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "30", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "60", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "1", 3)
        .put(DDB_FIELD_EXTEND_PREFIX + "2", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "3", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "5", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "10", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "20", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "30", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "60", 0).build();

    JSONArray jsonArray = new JSONArray();
    JSONObject emptyJson = new JSONObject();
    jsonArray.put(emptyJson);
    Item item = new Item()
        .with(DDB_FIELD_TAGNAMEALERT, notOut.getUniqueName())
        .with(DDB_FIELD_DATE, "2016-12-06")
        .with(DDB_FIELD_AFIIDALERT, notOut.getModule().getId())
        .with(DDB_FIELD_DELAY_EXTEND, delayExtendMap)
        .withJSON(DDB_FIELD_SIM_DUP_OR_REASON, jsonArray.toString());

    verify(ddb).writeItems(captor.capture(), eq("alarmnot"));
    assertThat(captor.getValue(),
        contains(sameItem(item)));
  }

  @Test
  public void testDuplicateReason() throws IOException {
    Module notModule = new Module().setAfiTypeId(103).setId(10001).setSymbol("NOT");
    Port notOut = outPort(notModule, 1000).setUniqueName("NOT||1||OUT").setArchive(true)
        .setAlarm(true).setName("OUT");
    Port notIn = inPort(notModule, 10).setUniqueName("NOT||1||IN");

    Module monitModule = new Module().setAfiTypeId(13).setId(10002).setSymbol("MONIT");
    Port monitQOut = outPort(monitModule, 1000).setUniqueName("MONIT||1||Q_OUT").setArchive(true)
        .setName("Q").setAlarmTypeId(1).setAlarm(true);
    Port monitQNOut = outPort(monitModule, 1010).setUniqueName("MONIT||1||Q_N_OUT").setArchive(true)
        .setName("QN").setAlarm(true);

    List<Port> ports = ImmutableList
        .of(notIn, notOut, monitQOut, monitQNOut
        );

    Map<Integer, Module> modules = Stream
        .of(notModule, monitModule
        )
        .collect(Collectors.toMap(Module::getId, Function.identity()));

    Stream<Connection> connections = Stream.of(conn(monitQOut, notIn));

    Map<Port, List<Alarm>> alarms = alarms(
        alarm(notOut, 1500, 500, 2000),
        alarm(notOut, 4000, 500, 1000),
        alarm(notOut, 5500, 500, 2000)
    );

    Map<Port, BinaryTrend> binTrends = trendsByPort(

        new BinaryTrend(monitQNOut).setUniqueName(monitQNOut.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 192, 0.0d),
                    new Trend(1500, 192, 1.0d),
                    new Trend(2000, 192, 0.0d),
                    new Trend(4000, 192, 1.0d),
                    new Trend(4500, 192, 0.0d),
                    new Trend(5500, 192, 1.0d),
                    new Trend(6000, 192, 0.0d))
        ),
        new BinaryTrend(monitQOut).setUniqueName(monitQOut.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 192, 1.0d),
                    new Trend(1500, 192, 0.0d),
                    new Trend(2000, 192, 1.0d),
                    new Trend(4000, 192, 0.0d),
                    new Trend(4500, 192, 1.0d),
                    new Trend(5500, 192, 0.0d),
                    new Trend(6000, 192, 1.0d))
        )
    );

    AlarmAndOrTestHelper
        .prepareMockLoader(loader, modules, connections, ports, alarms,
            binTrends,
            alarmTypes);
    DlsProducerLambdaResult handleRequest = handler.handleRequest(params, context);

    assertEquals(true, handleRequest.getSuccess());

    Map<String, Integer> delayExtendMap = ImmutableMap
        .<String, Integer>builder()
        .put(DDB_FIELD_COUNT, 3)
        .put(DDB_FIELD_DELAY_PREFIX + "1", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "2", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "3", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "5", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "10", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "20", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "30", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "60", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "1", 2)
        .put(DDB_FIELD_EXTEND_PREFIX + "2", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "3", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "5", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "10", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "20", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "30", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "60", 0).build();

    JSONArray jsonArray = new JSONArray();
    //duplicate reason
    JSONObject duplicate = new JSONObject();
    duplicate
        .put(DDB_FIELD_ALARMTYPE, Optional.ofNullable(alarmTypes.get(monitQOut.getAlarmTypeId()))
            .map(AlarmType::getAbbrev).orElse(Integer.toString(monitQOut.getAlarmTypeId())));
    duplicate.put(DDB_FIELD_REASONS, 3);
    duplicate.put(DDB_FIELD_MODULE, monitQOut.getModule().getSymbol());
    duplicate.put(DDB_FIELD_INTER, 0);
    duplicate.put(DDB_FIELD_TAGNAME, monitQOut.getUniqueName());
    duplicate.put(DDB_FIELD_PORT, monitQOut.getName());
    duplicate.put(DDB_FIELD_VALUE, 1);
    duplicate.put(DDB_FIELD_BASE, "Dupl");

    //simple reason
    JSONObject simple = new JSONObject();
    simple
        .put(DDB_FIELD_ALARMTYPE, Optional.ofNullable(alarmTypes.get(monitQOut.getAlarmTypeId()))
            .map(AlarmType::getAbbrev).orElse(Integer.toString(monitQOut.getAlarmTypeId())));
    simple.put(DDB_FIELD_REASONS, 3);
    simple.put(DDB_FIELD_CODE, 1);
    simple.put(DDB_FIELD_MODULE, monitQOut.getModule().getSymbol());
    simple.put(DDB_FIELD_INTER, 0);
    simple.put(DDB_FIELD_TAGNAME, monitQOut.getUniqueName());
    simple.put(DDB_FIELD_PORT, monitQOut.getName());
    simple.put(DDB_FIELD_VALUE, 0);
    simple.put(DDB_FIELD_BASE, "AOReason");

    jsonArray.put(duplicate);
    jsonArray.put(simple);
    Item item = new Item()
        .with(DDB_FIELD_TAGNAMEALERT, notOut.getUniqueName())
        .with(DDB_FIELD_DATE, "2016-12-06")
        .with(DDB_FIELD_AFIIDALERT, notOut.getModule().getId())
        .with(DDB_FIELD_DELAY_EXTEND, delayExtendMap)
        .withJSON(DDB_FIELD_SIM_DUP_OR_REASON, jsonArray.toString());

    verify(ddb).writeItems(captor.capture(), eq("alarmnot"));
    assertThat(captor.getValue(),
        contains(sameItem(item)));
  }

  @Test
  public void testSimpleAndReason() throws IOException {
    Module notModule = new Module().setAfiTypeId(103).setId(10001).setSymbol("NOT");
    Port notOut = outPort(notModule, 1000).setUniqueName("NOT||1||OUT").setArchive(true)
        .setAlarm(true).setName("OUT");
    Port notIn = inPort(notModule, 10).setUniqueName("NOT||1||IN");

    Module orModule = new Module().setAfiTypeId(101).setId(10002).setSymbol("OR");
    Port orOut = outPort(orModule, 1000).setUniqueName("OR||1||OUT").setArchive(true).setAlarm(true)
        .setName("OUT");
    Port orIn1 = inPort(orModule, 10).setUniqueName("OR||1||IN1")
        .setName("IN1");
    Port orIn2 = inPort(orModule, 20).setUniqueName("OR||1||IN2")
        .setName("IN2");
    Port orIn3 = inPort(orModule, 30).setUniqueName("OR||1||IN3")
        .setName("IN3");

    Module rsffModule = new Module().setAfiTypeId(105).setId(10003).setSymbol("RS_FF");
    Port rsffOut = outPort(rsffModule, 1000).setUniqueName("RS_FF||1||OUT").setArchive(false)
        .setName("Q");

    Module slcModule = new Module().setAfiTypeId(404).setId(10004).setSymbol("SLC");
    Port slcOut = outPort(slcModule, 1040).setUniqueName("SLC||1||ON").setArchive(false)
        .setName("ON");

    List<Port> ports = ImmutableList
        .of(notIn, notOut, orOut, orIn1, orIn2, orIn3, slcOut, rsffOut
        );

    Map<Integer, Module> modules = Stream
        .of(notModule, orModule, rsffModule, slcModule
        )
        .collect(Collectors.toMap(Module::getId, Function.identity()));

    Stream<Connection> connections = Stream
        .of(conn(orOut, notIn), conn(rsffOut, orIn1), conn(slcOut, orIn2));

    Map<Port, List<Alarm>> alarms = alarms(
        alarm(notOut, 1500, 500, 2000),
        alarm(notOut, 4000, 500, 1000),
        alarm(notOut, 5500, 500, 2000)
    );

    Map<Port, BinaryTrend> binTrends = trendsByPort(

        new BinaryTrend(rsffOut).setUniqueName(rsffOut.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 192, 0.0d))
        ),
        new BinaryTrend(slcOut).setUniqueName(slcOut.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 192, 0.0d))
        ),
        new BinaryTrend(orOut).setUniqueName(orOut.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 192, 1.0d),
                    new Trend(1500, 192, 0.0d),
                    new Trend(2000, 192, 1.0d),
                    new Trend(4000, 192, 0.0d),
                    new Trend(4500, 192, 0.0d),
                    new Trend(5500, 192, 0.0d),
                    new Trend(6000, 192, 0.0d))
        )
    );

    AlarmAndOrTestHelper
        .prepareMockLoader(loader, modules, connections, ports, alarms,
            binTrends,
            alarmTypes);
    DlsProducerLambdaResult handleRequest = handler.handleRequest(params, context);

    assertEquals(true, handleRequest.getSuccess());

    Map<String, Integer> delayExtendMap = ImmutableMap
        .<String, Integer>builder()
        .put(DDB_FIELD_COUNT, 3)
        .put(DDB_FIELD_DELAY_PREFIX + "1", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "2", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "3", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "5", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "10", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "20", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "30", 0)
        .put(DDB_FIELD_DELAY_PREFIX + "60", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "1", 2)
        .put(DDB_FIELD_EXTEND_PREFIX + "2", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "3", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "5", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "10", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "20", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "30", 0)
        .put(DDB_FIELD_EXTEND_PREFIX + "60", 0).build();

    JSONArray jsonArray = new JSONArray();
    //simple reason
    JSONObject simple1 = new JSONObject();
    simple1
        .put(DDB_FIELD_ALARMTYPE, Optional.ofNullable(alarmTypes.get(orOut.getAlarmTypeId()))
            .map(AlarmType::getAbbrev).orElse(Integer.toString(orOut.getAlarmTypeId())));
    simple1.put(DDB_FIELD_REASONS, 3);
    simple1.put(DDB_FIELD_MODULE, orOut.getModule().getSymbol());
    simple1.put(DDB_FIELD_INTER, 0);
    simple1.put(DDB_FIELD_TAGNAME, orOut.getUniqueName());
    simple1.put(DDB_FIELD_PORT, orOut.getName());
    simple1.put(DDB_FIELD_VALUE, 1);
    simple1.put(DDB_FIELD_BASE, "Dupl");

    JSONObject simple2 = new JSONObject();
    simple2
        .put(DDB_FIELD_ALARMTYPE, Optional.ofNullable(alarmTypes.get(rsffOut.getAlarmTypeId()))
            .map(AlarmType::getAbbrev).orElse(Integer.toString(rsffOut.getAlarmTypeId())));
    simple2.put(DDB_FIELD_REASONS, 3);
    simple2.put(DDB_FIELD_CODE, 2);
    simple2.put(DDB_FIELD_MODULE, rsffOut.getModule().getSymbol());
    simple2.put(DDB_FIELD_INTER, 1);
    simple2.put(DDB_FIELD_TAGNAME, rsffOut.getUniqueName());
    simple2.put(DDB_FIELD_PORT, rsffOut.getName());
    simple2.put(DDB_FIELD_VALUE, -1);
    simple2.put(DDB_FIELD_BASE, "AOReason");

    JSONObject simple3 = new JSONObject();
    simple3
        .put(DDB_FIELD_ALARMTYPE, Optional.ofNullable(alarmTypes.get(slcOut.getAlarmTypeId()))
            .map(AlarmType::getAbbrev).orElse(Integer.toString(slcOut.getAlarmTypeId())));
    simple3.put(DDB_FIELD_REASONS, 3);
    simple3.put(DDB_FIELD_CODE, 2);
    simple3.put(DDB_FIELD_MODULE, slcOut.getModule().getSymbol());
    simple3.put(DDB_FIELD_INTER, 1);
    simple3.put(DDB_FIELD_TAGNAME, slcOut.getUniqueName());
    simple3.put(DDB_FIELD_PORT, slcOut.getName());
    simple3.put(DDB_FIELD_VALUE, -1);
    simple3.put(DDB_FIELD_BASE, "AOReason");

    jsonArray.put(simple1);
    jsonArray.put(simple2);
    jsonArray.put(simple3);
    Item item = new Item()
        .with(DDB_FIELD_TAGNAMEALERT, notOut.getUniqueName())
        .with(DDB_FIELD_DATE, "2016-12-06")
        .with(DDB_FIELD_AFIIDALERT, notOut.getModule().getId())
        .with(DDB_FIELD_DELAY_EXTEND, delayExtendMap)
        .withJSON(DDB_FIELD_SIM_DUP_OR_REASON, jsonArray.toString());

    verify(ddb).writeItems(captor.capture(), eq("alarmnot"));
    assertThat(captor.getValue(),
        contains(sameItem(item)));
  }


  @Test
  public void testRefactor() throws IOException {
    Module notModule = new Module().setAfiTypeId(103).setId(442860).setSymbol("NOT");
    Port notOut = outPort(notModule, 1000).setUniqueName("NOT||442860||OUT").setArchive(true)
        .setAlarm(true).setName("OUT");
    Port notIn = inPort(notModule, 10).setUniqueName("NOT||442860||IN");

    Module andModule_442854 = new Module().setAfiTypeId(100).setId(442854).setSymbol("AND");
    Port andModule_442854_out = outPort(andModule_442854, 1000).setUniqueName("AND||442854||OUT")
        .setName("OUT");
    Port andModule_442854_in1 = inPort(andModule_442854, 10).setUniqueName("AND||442854||IN1")
        .setName("IN1");
    Port andModule_442854_in2 = inPort(andModule_442854, 20).setUniqueName("AND||442854||IN2")
        .setName("IN2");

    Module bsmonModule_342688 = new Module().setAfiTypeId(201).setId(342388).setSymbol("BSMON");
    Port bsmonModule_342688_out = outPort(bsmonModule_342688, 1000)
        .setUniqueName("BSMON||342388||OUT").setArchive(true)
        .setName("TRBL_ALARM");

    Module andModule_442980 = new Module().setAfiTypeId(100).setId(442980).setSymbol("AND");
    Port andModule_442980_out = outPort(andModule_442980, 1000).setUniqueName("AND||442980||OUT")
        .setName("OUT");
    Port andModule_442980_in1 = inPort(andModule_442980, 10).setUniqueName("AND||442980||IN1")
        .setName("IN1");
    Port andModule_442980_in2 = inPort(andModule_442980, 20).setUniqueName("AND||442980||IN2")
        .setName("IN2");
    Port andModule_442980_in3 = inPort(andModule_442980, 30).setUniqueName("AND||442980||IN3")
        .setName("IN3");

    Module andModule_442843 = new Module().setAfiTypeId(100).setId(442843).setSymbol("AND");
    Port andModule_442843_out = outPort(andModule_442843, 1000).setUniqueName("AND||442843||OUT")
        .setName("OUT");
    Port andModule_442843_in1 = inPort(andModule_442843, 10).setUniqueName("AND||442843||IN1")
        .setName("IN1");
    Port andModule_442843_in2 = inPort(andModule_442843, 20).setUniqueName("AND||442843||IN2")
        .setName("IN2");

    Module andModule_442839 = new Module().setAfiTypeId(100).setId(442839).setSymbol("AND");
    Port andModule_442839_out = outPort(andModule_442839, 1000).setUniqueName("AND||442839||OUT")
        .setArchive(true)
        .setName("OUT");

    Module andModule_442855 = new Module().setAfiTypeId(100).setId(442855).setSymbol("AND");
    Port andModule_442855_out = outPort(andModule_442855, 1000).setUniqueName("AND||442855||OUT")
        .setArchive(true)
        .setName("OUT");

    Module andModule_442858 = new Module().setAfiTypeId(100).setId(442858).setSymbol("AND");
    Port andModule_442858_out = outPort(andModule_442858, 1000).setUniqueName("AND||442858||OUT")
        .setArchive(true)
        .setName("OUT");

    Module notModule_426970 = new Module().setAfiTypeId(103).setId(426970).setSymbol("NOT");
    Port notModule_426970_out = outPort(notModule_426970, 1000).setUniqueName("NOT||426970||OUT")
        .setName("OUT");
    Port notModule_426970_in = inPort(notModule_426970, 10).setUniqueName("NOT||426970||IN")
        .setName("IN");

    Module fumAsmonModule_426969 = new Module().setAfiTypeId(760018).setId(426969)
        .setSymbol("FUM_ASMON");
    Port fumAsmonModule_426969_out = outPort(fumAsmonModule_426969, 1060)
        .setUniqueName("FUM_ASMON||426969||OUT")
        .setArchive(true).setAlarm(true)
        .setName("TRBL_AL");

    List<Port> ports = ImmutableList
        .of(notIn, notOut,
            andModule_442854_in1, andModule_442854_in2, andModule_442854_out,
            bsmonModule_342688_out,
            andModule_442980_in1, andModule_442980_in2, andModule_442980_in3, andModule_442980_out,
            andModule_442843_in1, andModule_442843_in2, andModule_442843_out,
            andModule_442839_out,
            andModule_442855_out,
            andModule_442858_out,
            notModule_426970_out,
            notModule_426970_in,
            fumAsmonModule_426969_out
        );

    Map<Integer, Module> modules = Stream
        .of(notModule, andModule_442854, bsmonModule_342688, andModule_442980, andModule_442843,
            andModule_442839, andModule_442855, andModule_442858, notModule_426970,
            fumAsmonModule_426969
        )
        .collect(Collectors.toMap(Module::getId, Function.identity()));

    Stream<Connection> connections = Stream
        .of(conn(andModule_442854_out, notIn),
            conn(bsmonModule_342688_out, andModule_442854_in2),
            conn(andModule_442980_out, andModule_442854_in1),
            conn(andModule_442843_out, andModule_442980_in1),
            conn(andModule_442839_out, andModule_442980_in2),
            conn(andModule_442855_out, andModule_442980_in3),
            conn(andModule_442858_out, andModule_442843_in1),
            conn(notModule_426970_out, andModule_442843_in2),
            conn(fumAsmonModule_426969_out, notModule_426970_in));

    Map<Port, List<Alarm>> alarms = alarms(
        alarm(notOut, 9078205, 23800, 10201),
        alarm(notOut, 9112206, 12000, 4000),
        alarm(notOut, 9128206, 15951, 5538902),
        alarm(notOut, 14683059, 28001, 4162176),
        alarm(notOut, 18873236, 22000, 100000000)
    );

    Map<Port, BinaryTrend> binTrends = trendsByPort(

        new BinaryTrend(bsmonModule_342688_out)
            .setUniqueName(bsmonModule_342688_out.getUniqueName()).setTrends(
            ImmutableList
                .of(new Trend(0, 227, 0.0d),
                    new Trend(9061686, 192, 1.0d),
                    new Trend(9077735, 192, 0.0d),
                    new Trend(9101734, 192, 1.0d),
                    new Trend(9111335, 192, 0.0d),
                    new Trend(9123336, 192, 1.0d),
                    new Trend(9127286, 192, 0.0d),
                    new Trend(9143735, 192, 1.0d))
        ),
        new BinaryTrend(andModule_442855_out).setUniqueName(andModule_442855_out.getUniqueName())
            .setTrends(
                ImmutableList
                    .of(new Trend(0, 227, 1.0d))
            ),
        new BinaryTrend(andModule_442839_out).setUniqueName(andModule_442839_out.getUniqueName())
            .setTrends(
                ImmutableList
                    .of(new Trend(0, 195, 1.0d))
            ),
        new BinaryTrend(andModule_442858_out).setUniqueName(andModule_442858_out.getUniqueName())
            .setTrends(
                ImmutableList
                    .of(new Trend(0, 195, 1.0d),
                        new Trend(6933016, 192, 0.0d),
                        new Trend(6953016, 192, 1.0d),
                        new Trend(7357024, 192, 0.0d),
                        new Trend(7377225, 192, 1.0d),
                        new Trend(8029037, 192, 0.0d),
                        new Trend(8049037, 192, 1.0d),
                        new Trend(8191088, 192, 0.0d),
                        new Trend(8211039, 192, 1.0d),
                        new Trend(14683059, 192, 0.0d),
                        new Trend(14711060, 192, 1.0d),
                        new Trend(18873236, 192, 0.0d),
                        new Trend(18895236, 192, 1.0d)
                    )
            ),
        new BinaryTrend(fumAsmonModule_426969_out)
            .setUniqueName(fumAsmonModule_426969_out.getUniqueName())
            .setTrends(
                ImmutableList.of(new Trend(0, 227, 0.0d)))
    );

    AlarmAndOrTestHelper
        .prepareMockLoader(loader, modules, connections, ports, alarms,
            binTrends,
            alarmTypes);
    DlsProducerLambdaResult handleRequest = handler.handleRequest(params, context);

    assertEquals(true, handleRequest.getSuccess());
  }

}
/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */