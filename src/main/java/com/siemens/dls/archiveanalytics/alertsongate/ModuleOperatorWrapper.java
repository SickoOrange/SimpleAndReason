/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.alertsongate;

import static com.siemens.dls.archiveanalytics.alertsongate.AlertsOnGateConstants.BSEL_CONSTANT_PORT_ID;

import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Port;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * User: Ya Yin
 * Date: 15.04.2018
 */
public class ModuleOperatorWrapper {

  @FunctionalInterface
  public interface ModuleOperator<A, B, C, R> {

    //R is like Return, but doesn't have to be last in the list nor named R.
    R apply(A a, B b, C c);
  }


  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> andOperator = (inputList, inputSet, value) -> {
    if (inputSet.contains(0)) {
      value = 0;
    } else if (inputSet.contains(1) && inputSet.size() == 1) {
      value = 1;
    } else if (!inputSet.contains(0) && inputSet
        .contains(-1)) {
      value = -1;
    }
    return value;
  };

  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> orOperator = (inputList, inputSet, value) -> {
    if (inputSet.contains(1)) {
      value = 1;
    } else if (inputSet.contains(0) && inputSet.size() == 1) {
      value = 0;
    } else if (!inputSet.contains(1) && inputSet.contains(1)) {
      value = -1;
    }
    return value;
  };

  public static ModuleOperator<List<Integer>, Module, Integer, Integer> bselOperator = (inputList, module, value) -> {
    Optional<Port> constantPort = module.findPortById(BSEL_CONSTANT_PORT_ID);
    long parameterValue = Long
        .parseLong(constantPort.isPresent() ? constantPort.get().getParameter() : "0");

    long countForOne = inputList.stream().filter(v -> v == 1)
        .count();

    long countForZero = inputList.stream().filter(v -> v == 0)
        .count();

    long countForMinusOne = inputList.stream()
        .filter(v -> v != 0 && v != 1).count();

    if (countForOne >= parameterValue) {
      value = 1;
    } else if (countForZero >= 16 - parameterValue) {
      value = 0;
    } else if (countForMinusOne == inputList.size()) {
      value = -1;
    }
    return value;
  };

  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> binOperator = (inputList, inputSet, value) -> {
    assert inputSet.size() == 1;
    value = inputList.get(0);
    return value;
  };

  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> notOperator = (inputList, inputSet, value) -> {
    assert inputSet.size() == 1;
    switch (inputList.get(0)) {
      case 1:
        value = 0;
        break;
      case 0:
        value = 1;
        break;
      case -1:
        value = -1;
        break;
    }
    return value;
  };

  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> norOperator = (inputList, inputSet, value) -> {
    if (inputSet.size() == 1 && inputSet.contains(1)) {
      value = 0;
    } else if (inputSet.size() > 1 && inputSet.contains(0)) {
      value = 1;
    } else if (!inputSet.contains(0) && inputSet.contains(-1)) {
      value = -1;
    }
    return value;
  };
  public static ModuleOperator<List<Integer>, Set<Integer>, Integer, Integer> nandOperator = (inputList, inputSet, value) -> {
    if (inputSet.size() == 1 && inputSet.contains(0)) {
      value = 1;
    } else if (inputSet.size() > 1 && inputSet.contains(1)) {
      value = 0;
    } else if (!inputSet.contains(1) && inputSet.contains(-1)) {
      value = -1;
    }
    return value;
  };
}
/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */