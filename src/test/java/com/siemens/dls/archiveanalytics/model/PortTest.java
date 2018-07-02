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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class PortTest {

  @Test
  public void testEqualsAndHashcode(){
    Module m = mock(Module.class);

    Port p1 = new Port().setModule(m).setUniqueName("uname").setName("name").setAfiId(123)
        .setId(321).setDirection(PortDirection.I).setParameter("param").setAbbrev("A");
    Port p2 = new Port().setModule(m).setUniqueName("uname").setName("name").setAfiId(123)
        .setId(321).setDirection(PortDirection.I).setParameter("param").setAbbrev("A");

    assertTrue("ports not equal", p1.equals(p2));
    assertEquals("port hashcodes not equal", p1.hashCode(), p2.hashCode());
    p2.setUniqueName("different");
    assertFalse("ports equal", p1.equals(p2));
    assertNotEquals("port hashcodes equal", p1.hashCode(), p2.hashCode());
  }
}
