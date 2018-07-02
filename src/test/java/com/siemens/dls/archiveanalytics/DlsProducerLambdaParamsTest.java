/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class DlsProducerLambdaParamsTest {

  @Test
  public void testDlsProducerLambdaParams() {
    DlsProducerLambdaParams sut = new DlsProducerLambdaParams();
    assertEquals(DlsProducerLambdaParams.class, sut.getClass());
  }

  @Test
  public void testDlsProducerLambdaParamsParams() {
    Paths paths = new Paths("FOO", "BAR");
    DlsProducerLambdaParams sut = new DlsProducerLambdaParams(
        "UT0054",
        Arrays.asList("2015-12-06", "2016-12-06", "2017-01-01"),
        1,
        "LeBucket",
        paths
    );
    assertEquals(DlsProducerLambdaParams.class, sut.getClass());
    assertEquals("UT0054", sut.getPpid());
    assertEquals("2016-12-06", sut.getDate());
    assertEquals(Arrays.asList("2015-12-06", "2016-12-06", "2017-01-01"), sut.getDates());
    assertEquals(1, sut.getIndex(), .01);
    assertEquals("LeBucket", sut.getBucket());
    assertEquals("FOO", sut.getPaths().getArchive());
    assertEquals("BAR", sut.getPaths().getEngineering());
  }

  @Test
  public void testSetGetPpid() {
    Paths paths = new Paths("FOO", "BAR");
    DlsProducerLambdaParams sut = new DlsProducerLambdaParams(
        "UT0054",
        Arrays.asList("2015-12-06", "2016-12-06", "2017-01-01"),
        1,
        "LeBucket",
        paths
    );
    assertEquals(DlsProducerLambdaParams.class, sut.getClass());
    assertEquals("UT0054", sut.getPpid());
    sut.setPpid("F0008");
    assertEquals("F0008", sut.getPpid());
    assertEquals("LeBucket", sut.getBucket());
    assertEquals("FOO", sut.getPaths().getArchive());
    assertEquals("BAR", sut.getPaths().getEngineering());
  }

  @Test
  public void testSetGetDates() {
    Paths paths = new Paths("FOO", "BAR");
    DlsProducerLambdaParams sut = new DlsProducerLambdaParams(
        "UT0054",
        Arrays.asList("2015-12-06", "2016-12-06", "2017-01-01"),
        1,
        "LeBucket",
        paths
    );
    assertEquals(DlsProducerLambdaParams.class, sut.getClass());
    assertEquals("2016-12-06", sut.getDate());
    assertEquals(Arrays.asList("2015-12-06", "2016-12-06", "2017-01-01"), sut.getDates());
    sut.setDates(Arrays.asList("2016-09-09", "2016-09-10", "2016-09-11", "2016-09-12"));
    assertEquals("2016-09-10", sut.getDate());
    assertEquals(Arrays.asList("2016-09-09", "2016-09-10", "2016-09-11", "2016-09-12"),
        sut.getDates());
    assertEquals("LeBucket", sut.getBucket());
    assertEquals("FOO", sut.getPaths().getArchive());
    assertEquals("BAR", sut.getPaths().getEngineering());
  }
}
/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */