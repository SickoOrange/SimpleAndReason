package com.siemens.dls.archiveanalytics.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.siemens.dls.archiveanalytics.model.Alarm;
import com.siemens.dls.archiveanalytics.model.AnalogTrend;
import com.siemens.dls.archiveanalytics.model.Connection;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import com.siemens.dls.archiveanalytics.model.PortKey;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DlsCsvParserTest {

  private DlsCsvParser parser;

  private static final String AFI_CSV =
      "id;node;afitype;symbol;isinmacro;name;designation;comment;afc;cycle;diagram;modified\n"
          + "340212;337246;770002;S7_CPU_V45;K0 CRP21.BA|208958;~CPU Rack;;;\n"
          + "340213;337246;770021;S7_FS;K0 CRP21.BA|208959;~CPU Rack;;;\n"
          + "340214;337246;770010;S7_COMM;K0 CRP21.BA|208960;~CPU Rack;;;\n"
          + "340215;337247;700000;S7_IM153_2;K0 CRP22.AA|001;~BGT 1;;;\n"
          + "340216;337247;751011;S7_SM326F_1BK0X;K0 CRP22.AA|006;~BGT 1;;;\n"
          + "340217;337247;750010;S7_SM322_1BL00;K0 CRP22.AA|004;~BGT 1;;;\n"
          + "340218;337247;751021;S7_SM336F_4GEXX_HART;K0 CRP22.AA|007;~BGT 1;;;\n"
          + "340219;337247;751021;S7_SM336F_4GEXX_HART;K0 CRP22.AA|008;~BGT 1;;;\n"
          + "340220;337247;751021;S7_SM336F_4GEXX_HART;K0 CRP22.AA|009;~BGT 1;;;\n"
          + "340221;337247;751021;S7_SM336F_4GEXX_HART;K0 CRP22.AA|010;~BGT 1;;;\n";
  private static final String PORTS_CSV =
      "afiid;portid;portname;portdesc;afitypeid;symbol;type;io;parameter;isarchive;isalarm;alarmtypeid;abbrev;activerule;inactiverule;active;inactive;minvalue;maxvalue;percent;engunit;signal;signalinfo;connafiid;connportid;uniquename;\n"
          + "337428;10;IN1;Addend 1;;A2of3_CC;float;I;;X;X;1;A;;;;;;;1.0;mbar;;;340909;1000;null|IN1;\n"
          + "337428;20;EN1;Binary Input Value;;A2of3_CC;bool;I;true;;;1;A;;;KanG;;;;;;;;340916;1000;null|EN1;\n"
          + "337428;30;IN2;Addend 1;;A2of3_CC;float;I;;;;1;A;;;;;;;1.0;mbar;;;340918;1000;null|IN2;\n"
          + "337428;40;EN2;Binary Input Value;;A2of3_CC;bool;I;true;;;1;A;;;KanG;;;;;;;;340925;1000;null|EN2;\n"
          + "337428;50;IN3;Addend 1;;A2of3_CC;float;I;;;;1;A;;;;;;;1.0;mbar;;;340927;1000;null|IN3;\n"
          + "337428;60;EN3;Binary Input Value;;A2of3_CC;bool;I;true;;;1;A;;;KanG;;;;;;;;340934;1000;null|EN3;\n"
          + "337428;70;MODE;Output Mode;;A2of3_CC;float;I;5.0;;;1;A;;;;;;;;;;;;;null|MODE;\n"
          + "337428;80;UL;Addend 1;;A2of3_CC;float;I;60.0;;;1;A;;;;;;;;;;;;;null|UL;\n"
          + "337428;100;EU;Engineering  Units;;A2of3_CC;string;I;mbar;;;1;A;;;;;;;;;;;;;null|EU;\n"
          + "337428;1100;DEV_DB;Addend 1;;A2of3_CC;float;O;12.0;;;1;A;;;;;;;;;;;;;null|DEV_DB;\n";

  public static final String ALARMS_CSV =
      "Client;Time;Quality;TagName;AlarmType;Suppressed;Auto-Supprssed;Disp-Suppressed;Duration;TimeToNext;\n"
          + "T0003;2016.09.01 00:03:36.296;192;10PGB20CP001||ZV52;5;0;0;0;22399;17600;\n"
          + "T0003;2016.09.01 00:04:16.295;192;10PGB20CP001||ZV52;5;0;0;0;17602;6398;\n"
          + "T0003;2016.09.01 00:04:40.295;192;10PGB20CP001||ZV52;5;0;0;0;33601;17602;\n"
          + "T0003;2016.09.01 00:05:31.498;192;10PGB20CP001||ZV52;5;0;0;0;132798;89601;\n"
          + "T0003;2016.09.01 00:08:38.494;192;10MAY01EZ200_S||ZV01;9;0;0;0;1999;3610607;\n"
          + "T0003;2016.09.01 00:08:42.593;192;10MAY01EZ200_S||ZV02;9;0;0;0;2000;3610606;\n"
          + "T0003;2016.09.01 00:08:46.693;192;10MAY01EZ200_S||ZV03;9;1;1;1;2000;3610606;\n";

  @Before
  public void setUp() {
    parser = new DlsCsvParser();
  }

  @Test
  public void testLoadModules() throws IOException {
    S3Reader s3Reader = mock(S3Reader.class);
    when(s3Reader.getReader()).thenReturn(new StringReader(AFI_CSV));
    //TODO: test with non-null filter
    Iterator<Module> moduleIterator = parser.loadModules(s3Reader, null);
    List<Module> modules = IteratorUtils.toList(moduleIterator);
    assertThat(modules, hasSize(10));
    Module m1 = new Module().setId(340212).setAfiTypeId(770002).setSymbol("S7_CPU_V45")
        .setName("K0 CRP21.BA|208958").setNode(337246);
    assertThat(modules.get(0), equalTo(m1));
  }

  @Test
  public void testLoadPorts() throws IOException {
    S3Reader s3Reader = mock(S3Reader.class);
    when(s3Reader.getReader()).thenReturn(new StringReader(PORTS_CSV));
    Iterator<Port> portIterator = parser.loadPorts(s3Reader, null);
    List<Port> ports = IteratorUtils.toList(portIterator);
    assertThat(ports, hasSize(10));
    Port m1 = new Port().setId(10).setAfiId(337428).setName("IN1").setDirection(PortDirection.I)
        .setUniqueName("null|IN1").setArchive(true).setAlarm(true).setAlarmTypeId(1).setAbbrev("A").setEngineeringUnit("mbar");
    assertThat(ports.get(0), equalTo(m1));
    assertThat(ports.get(0).getConnAfiId(), equalTo(340909));
    assertThat(ports.get(0).getConnPortId(), equalTo(1000));
    Port m2 = new Port().setId(1100).setAfiId(337428).setName("DEV_DB").setDirection(PortDirection.O)
        .setParameter("12.0").setAlarmTypeId(1).setUniqueName("null|DEV_DB").setAbbrev("A");
    assertThat(ports.get(9), equalTo(m2));
    assertThat(ports.get(9).getConnAfiId(), equalTo(0));
    assertThat(ports.get(9).getConnPortId(), equalTo(0));
  }

  private Connection getConn(int afi1, int id1, int afi2, int id2) {
    return new Connection(new PortKey(afi1, id1), new PortKey(afi2, id2));
  }

  @Test
  public void testLoadConnections() throws IOException {
    S3Reader s3Reader = mock(S3Reader.class);
    when(s3Reader.getReader()).thenReturn(new StringReader(CONNECTIONS_CSV));
    Iterator<Connection> connectionIterator = parser.loadConnections(s3Reader, null);
    List<Connection> connections = IteratorUtils.toList(connectionIterator);
    assertThat(connections, hasSize(9));

    List<Connection> c = Arrays.asList(
        getConn(337428, 180, 341018, 10),
        getConn(337428, 190, 484103, 30),
        getConn(337518, 220, 341289, 10),
        getConn(337518, 220, 341298, 20),
        getConn(337524, 220, 341381, 10),
        getConn(337524, 220, 341389, 10),
        getConn(337524, 220, 341393, 10),
        getConn(337531, 20, 341476, 10),
        getConn(337533, 20, 341478, 10)
    );
    assertEquals(connections, c);
  }

  @Test
  public void testLoadTrends() throws IOException {
    S3Reader s3Reader = mock(S3Reader.class);
    when(s3Reader.getReader()).thenReturn(new StringReader(ANALOG_TRENDS_CSV));
    Iterator<AnalogTrend> analogTrendIterator = parser
        .loadTrends(s3Reader, null, AnalogTrend::lineToTrend);
    List<AnalogTrend> analogTrends = IteratorUtils.toList(analogTrendIterator);
    assertThat(analogTrends, hasSize(8));
  }

  @Test
  public void testLoadAlarms() {
    S3Reader s3Reader = mock(S3Reader.class);
    when(s3Reader.getReader()).thenReturn(new StringReader(ALARMS_CSV));
    Iterator<Alarm> alarmsIter = parser
        .loadAlarms(s3Reader, null);
    List<Alarm> alarms = IteratorUtils.toList(alarmsIter);
    assertThat(alarms, hasSize(7));
    Alarm exp1 = new Alarm().setTime(LocalDateTime.of(2016,9,01,0,3,36,296000000))
        .setQuality(192).setTagname("10PGB20CP001||ZV52").setAlarmTypeId(5).setSuppressed(false)
        .setAutoSuppressed(false).setDispSuppressed(false).setDuration(22399).setTimeToNext(17600);
    Alarm exp2 = new Alarm().setTime(LocalDateTime.of(2016,9,01,0,8,46,693000000))
        .setQuality(192).setTagname("10MAY01EZ200_S||ZV03").setAlarmTypeId(9).setSuppressed(true)
        .setAutoSuppressed(true).setDispSuppressed(true).setDuration(2000).setTimeToNext(3610606);
    assertEquals(exp1, alarms.get(0));
    assertEquals(exp2, alarms.get(6));
  }

  private static final String CONNECTIONS_CSV = "afiid1;portid1;portname1;type1;afiid2;portid2;portname2;type2;\n"
      + "337428;180;Y;float;341018;10;IN;float;\n"
      + "337428;190;Y OK;bool;484103;30;IN3;bool;\n"
      + "337518;220;SUM;float;341289;10;IN1;float;\n"
      + "337518;220;SUM;float;341298;20;IN2;float;\n"
      + "337524;220;SUM;float;341381;10;IN;float;\n"
      + "337524;220;SUM;float;341389;10;IN1;float;\n"
      + "337524;220;SUM;float;341393;10;IN1;float;\n"
      + "337531;20;OUT;bool;341476;10;IN1;bool;\n"
      + "337533;20;OUT;bool;341478;10;IN1;bool;\n";

  private static final String ANALOG_TRENDS_CSV = "Client;TagName;Trend\n"
      + "T0002;K1 HLD10 CT901|-52|XQ01;0,7,0.0|\n"
      + "T0002;K1 HLD10 CT901|745320|OUT;0,7,0.0|\n"
      + "T0002;K1 HLD10 CT901|745320|O_MODE;0,227,5.0|\n"
      + "T0002;K1 HLD10 EC001|XA93|STEP_A;0,195,6.0|5925780,192,0.0|5932570,192,6.0|\n"
      + "T0002;K1 HNA00 EC001|XA93|STEP_A;0,195,6.0|5926600,192,0.0|7218880,192,1.0|7225480,192,6.0|\n"
      + "T0002;K1 HNC10 AN901|BZ_ZAEHL_pf_2|MAINT_N;0,227,0.0|\n"
      + "T0002;K1 HNC10 AN901|BZ_ZAEHL_pf_2|MAINT_S;0,227,15561.0|\n"
      + "T0002;K2 HTF11 AP901||XQ02;0,227,1998.0|77339990,192,2000.0|\n";

}
