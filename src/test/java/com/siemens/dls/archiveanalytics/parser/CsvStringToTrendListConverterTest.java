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
import com.siemens.dls.archiveanalytics.model.Trend;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CsvStringToTrendListConverterTest {

  private CsvStringToTrendListConverter converter;

  @Before
  public void setUp(){
    converter = new CsvStringToTrendListConverter();
  }

  @Test
  public void testConvert() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    List<Trend> t = (List<Trend>) converter.convert("0,195,6.0|5926600,192,0.0|7218880,192,1.0|7225480,192,6.0|");
    assertThat(t, hasSize(4));
    assertThat(t.get(2), equalTo(new Trend(7218880, 192, 1.0d)));
  }

  @Test(expected = CsvDataTypeMismatchException.class)
  public void testConvertFails() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    converter.convert("0,123 this is not a number,6.0|5926600,192,0.0|7218880,192,1.0|7225480,192,6.0|");
  }

}
