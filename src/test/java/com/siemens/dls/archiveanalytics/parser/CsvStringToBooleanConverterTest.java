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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CsvStringToBooleanConverterTest {

  private CsvStringToBooleanConverter converter;

  @Before
  public void setUp()
  {
    converter = new CsvStringToBooleanConverter();
  }

  @Test
  public void testConvert() throws CsvDataTypeMismatchException, CsvConstraintViolationException {

    boolean trueTest = converter.convert("X");
    assertThat(trueTest, equalTo(true));

    List<Boolean>  falseTests = new ArrayList<>();
    falseTests.add(converter.convert("XYZ"));
    falseTests.add(converter.convert("123"));
    falseTests.add(converter.convert("x"));
    falseTests.add(converter.convert(""));

    assertThat(falseTests, hasSize(4));
    assertThat(falseTests.get(0), equalTo(false));
    assertThat(falseTests.get(1), equalTo(false));
    assertThat(falseTests.get(2), equalTo(false));
    assertThat(falseTests.get(3), equalTo(false));

  }

}

