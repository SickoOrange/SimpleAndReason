/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;


import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.andOperator;
import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.binOperator;
import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.nandOperator;
import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.norOperator;
import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.notOperator;
import static com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.orOperator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.siemens.dls.archiveanalytics.alertsongate.ModuleOperatorWrapper.ModuleOperator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlertsOnGateConstants {

  static final int OR_AFITYPE_ID = 101;

  public static final int T2000P_OR_AFITYPE_ID = 160503;

  public static final int BIN_AFITYPE_ID = 207;

  static final int T2000P_BDMZ_AFITYPE_ID = 160193;

  static final int T2000P_BDMZ2_AFITYPE_ID = 160194;

  static final int T2000P_BDMZ3_AFITYPE_ID = 160198;

  static final int AND_AFITYPE_ID = 100;

  public static final int T2000P_AND_AFITYPE_ID = 160501;

  public static final int T2000P_NOT_AFITYPE_ID = 160505;
  static final int BDMZ_OUT_PUT_PORT_ID = 1010;
  public static final int BDMZ_IN_PUT_PORT_ID = 70;

  static final int NOT_AFITYPE_ID = 103;
  public static final int BSEL_AFITYPE_ID = 115;

  static final int NOR_AFITYPE_ID = 910100;
  static final int NAND_AFITYPE_ID = 910088;

  private static final int T2000P_NAND_AFITYPE_ID = 160502;
  private static final int T2000P_NOR_AFITYPE_ID = 160504;


  public static final int BSIG_AFITYPE_ID = 107;
  public static final int T2000P_KON1_AFITYPE_ID = 160539;

  public static final int BSEL_CONSTANT_PORT_ID = 170;

  //AND MODULE
  public static final Map<Integer, Set<Integer>> ALLOWED_AND_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(AND_AFITYPE_ID, ImmutableSet.of(10, 20, 30, 40, 50, 60, 70, 80, 1000))
      .put(T2000P_AND_AFITYPE_ID,
          ImmutableSet.of(70, 80, 90, 100, 110, 120, 130, 140, 1000))
      .build();
  //OR MODULE
  public static final Map<Integer, Set<Integer>> ALLOWED_OR_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(OR_AFITYPE_ID, ImmutableSet.of(10, 20, 30, 40, 50, 60, 70, 80, 1000))
      .put(T2000P_OR_AFITYPE_ID, ImmutableSet.of(70, 80, 90, 100, 110, 120, 130, 140, 1000))
      .build();


  //NOT MODULE
  public static final Map<Integer, Set<Integer>> ALLOWED_NOT_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(NOT_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(T2000P_NOT_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .build();

  //T2000P_BDMZ MODULE
  public static final Map<Integer, Set<Integer>> ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(T2000P_BDMZ_AFITYPE_ID,
          ImmutableSet.of(70, 1010))
      .put(T2000P_BDMZ2_AFITYPE_ID,
          ImmutableSet.of(70, 1010))
      .put(T2000P_BDMZ3_AFITYPE_ID,
          ImmutableSet.of(70, 1010))
      .build();

  public static final Map<Integer, Set<Integer>> ALARM_AND_OUT_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .putAll(ALLOWED_AND_MODULE_PORTS_IDS)
      .putAll(ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS)
      .build();


  public static final Map<Integer, Set<Integer>> ALARM_OR_OUT_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .putAll(ALLOWED_OR_MODULE_PORTS_IDS)
      .putAll(ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS)
      .build();

  public static final Map<Integer, Set<Integer>> ALARM_NOT_OUT_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .putAll(ALLOWED_NOT_MODULE_PORTS_IDS)
      .putAll(ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS)
      .build();

  //NOT MODULE
  public static final Map<Integer, Set<Integer>> NOT_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(NOT_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .build();


  //BSEL MODULE
  public static final Map<Integer, Set<Integer>> BSEL_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(BSEL_AFITYPE_ID, ImmutableSet
          .of(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 1000))
      .build();

  //BIN MODULE
  public static final Map<Integer, Set<Integer>> BIN_MODULE_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(BIN_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .build();


  //ALL PORTS OF OTHER INTER MODULE
  public static final Map<Integer, Set<Integer>> OTHER_T2000P_INTER_MODULES_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(T2000P_NAND_AFITYPE_ID, ImmutableSet.of(70, 80, 90, 100, 110, 120, 130, 140, 1000))
      .put(T2000P_NOR_AFITYPE_ID, ImmutableSet.of(70, 80, 90, 100, 110, 120, 130, 140, 1000))
      .build();

  public static final Map<Integer, Set<Integer>> ALLOWED_INTER_MODULE_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .putAll(ALLOWED_OR_MODULE_PORTS_IDS)
      .putAll(ALLOWED_AND_MODULE_PORTS_IDS)
      .putAll(ALLOWED_NOT_MODULE_PORTS_IDS)
      .putAll(BSEL_MODULE_PORTS_IDS)
      .putAll(BIN_MODULE_PORTS_IDS)
      .putAll(ALLOWED_T2000P_INTER_BDMZ_PORTS_IDS)
      .putAll(OTHER_T2000P_INTER_MODULES_PORTS_IDS)
      .build();

  private static final int T2000P_TIMER03_AFITYPE_ID = 160514;
  private static final int T2000P_TIM_FBE_AFITYPE_ID = 160007;
  private static final int T2000P_TIMER05_AFITYPE_ID = 160516;
  private static final int T2000P_TIM_FBA_AFITYPE_ID = 160009;
  private static final int T2000P_TIMER01_AFITYPE_ID = 160512;
  private static final int T2000P_TIM_FBI_AFITYPE_ID = 160005;
  private static final int T2000P_TIMER02_AFITYPE_ID = 160513;
  private static final int T2000P_TIM_FBVI_AFITYPE_ID = 160006;

  //ALL PORTS OF BIN MODULE
  public static final Map<Integer, Set<Integer>> OTHER_T2000P_NON_INTER_MODULES_PORTS_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(T2000P_TIMER03_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIM_FBE_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIMER05_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIM_FBA_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIMER01_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIM_FBI_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIMER02_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .put(T2000P_TIM_FBVI_AFITYPE_ID, ImmutableSet.of(70, 1000))
      .build();

  private static final int T_ON_AFITYPE_ID = 112;
  private static final int T_OFF_AFITYPE_ID = 114;
  private static final int PULSE_AFITYPE_ID = 110;
  private static final int LPULSE_AFITYPE_ID = 111;
  private static final int MINPULS_AFITYPE_ID = 109;
  private static final int T_LON_AFITYPE_ID = 113;

  public static final Map<Integer, Set<Integer>> ALLOWED_NON_INTER_MODULE_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(T_ON_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(T_OFF_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(PULSE_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(MINPULS_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(LPULSE_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .put(T_LON_AFITYPE_ID, ImmutableSet.of(10, 1000))
      .putAll(OTHER_T2000P_NON_INTER_MODULES_PORTS_IDS)
      .build();

  private static final int MONIT_AFITYPE_ID = 13;
  private static final int RS_FF_AFITYPE_ID = 105;
  private static final int JK_FF_AFITYPE_ID = 106;
  public static final Map<Integer, Set<Integer>> ALLOWED_NEGATED_MODULE_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(MONIT_AFITYPE_ID, ImmutableSet.of(10, 20, 30, 40, 1000, 1010))
      .put(RS_FF_AFITYPE_ID, ImmutableSet.of(10, 20, 30, 1000, 1010))
      .put(JK_FF_AFITYPE_ID, ImmutableSet.of(10, 20, 30, 1000, 1010))
      .build();


  public static final Map<Integer, Set<Integer>> CONST_MODULE_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .put(BSIG_AFITYPE_ID, ImmutableSet.of(10, 1000)) //BSIG
      .put(T2000P_KON1_AFITYPE_ID, ImmutableSet.of(70, 1000)) //T2000P_KON1
      .build();

  //ALL ALLOWED MODULE
  public static final Map<Integer, Set<Integer>> ALLOWED_MODULE_PORT_IDS = ImmutableMap.<Integer, Set<Integer>>builder()
      .putAll(ALLOWED_INTER_MODULE_PORT_IDS)          //INTER MODULE
      .putAll(ALLOWED_NON_INTER_MODULE_PORT_IDS)           //NON INTER MODULE
      .putAll(CONST_MODULE_PORT_IDS)
      .build();

  public static final List<Integer> RELEVENT_NOT_MODULE_AFI_IDS = ImmutableList
      .of(NOT_AFITYPE_ID, NOR_AFITYPE_ID, NAND_AFITYPE_ID);

  public static final Map<Integer, ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer>> MODULE_OPERATOR_MAP
      = ImmutableMap.<Integer, ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer>>builder()
      .put(AND_AFITYPE_ID, andOperator)
      .put(OR_AFITYPE_ID, orOperator)
      .put(BIN_AFITYPE_ID, binOperator)
      .put(NOT_AFITYPE_ID, notOperator)
      .put(NOR_AFITYPE_ID, norOperator)
      .put(NAND_AFITYPE_ID, nandOperator)
      .build();

  public static final Map<Integer, Integer> negatedPortMapping = ImmutableMap
      .of(1000, 1010, 1010, 1000);


}
/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
