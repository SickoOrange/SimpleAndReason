/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.siemens.dls.archiveanalytics.model.PortDirection;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CsvStringToPortDirectionConverterTest {

  private CsvStringToPortDirectionConverter converter;

  @Before
  public void setUp(){
    converter = new CsvStringToPortDirectionConverter();
  }

  @Test
  public void testConvert() throws CsvDataTypeMismatchException, CsvConstraintViolationException {

    List<PortDirection> goodTests = new ArrayList<>();
    goodTests.add((PortDirection)converter.convert("I"));
    goodTests.add((PortDirection)converter.convert("O"));

    assertThat(goodTests, hasSize(2));
    assertThat(goodTests.get(0), equalTo(PortDirection.I));
    assertThat(goodTests.get(1), equalTo(PortDirection.O));

  }

  @Test(expected = CsvDataTypeMismatchException.class)
  public void testConvertFails1() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    PortDirection badTest1 = (PortDirection) converter.convert("");
  }

  @Test(expected = CsvDataTypeMismatchException.class)
  public void testConvertFails2() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    PortDirection badTest2 = (PortDirection) converter.convert("i");
  }

  @Test(expected = CsvDataTypeMismatchException.class)
  public void testConvertFails3() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    PortDirection badTest3 = (PortDirection) converter.convert("123");
  }

}
