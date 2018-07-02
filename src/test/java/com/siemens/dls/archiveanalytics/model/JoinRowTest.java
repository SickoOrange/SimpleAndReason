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
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class JoinRowTest {

  @Test
  public void testEqualsAndHashcode() {
    JoinRow r1 = new JoinRow().addValue("val1").addValue(15);
    JoinRow r2 = new JoinRow(r1.getValues());

    assertTrue("joinrows not equal", r1.equals(r2));
    assertEquals("joinrow hashcodes not equal", r1.hashCode(), r2.hashCode());
    r2.addValue("another");
    assertFalse("joinrows equal", r1.equals(r2));
    assertNotEquals("joinrow hashcodes equal", r1.hashCode(), r2.hashCode());
  }
  @Test
  public void testToString(){
    assertEquals("JoinRow{values=[a]}", new JoinRow().addValue("a").toString());
  }
}
