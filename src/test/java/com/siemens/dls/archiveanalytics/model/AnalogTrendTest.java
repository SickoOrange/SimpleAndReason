/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class AnalogTrendTest {

  @Test
  public void testEqualsAndHashcode(){
    AnalogTrend t1 = new AnalogTrend().setPpid("ppid").setUniqueName("uname").setTrends(
        ImmutableList.of(new Trend(0,0,0),
            new Trend(100,100,1.23d),
            new Trend(200,192,2.345d)));
    AnalogTrend t2 = new AnalogTrend().setPpid("ppid").setUniqueName("uname").setTrends(
        ImmutableList.of(new Trend(0,0,0),
            new Trend(100,100,1.23d),
            new Trend(200,192,2.345d)));

    assertTrue("trends not equal", t1.equals(t2));
    assertEquals("trend hashcodes not equal", t1.hashCode(), t2.hashCode());
    t2.setPpid("different");
    assertFalse("trends equal", t1.equals(t2));
    assertNotEquals("trend hashcodes equal", t1.hashCode(), t2.hashCode());
  }
}
