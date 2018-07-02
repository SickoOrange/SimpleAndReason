/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import com.amazonaws.services.dynamodbv2.document.Item;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DlsLambdaHandlerTest {

  @Mock
  DlsLambdaHandler handler;


  @Test
  public void testGenerateDdbItemsFromItemData() {
    when(handler.generateDdbItemsFromItemData(Mockito.any(), Mockito.any())).thenCallRealMethod();

    Stream<JSONObject> jsonObjects = Stream.of(new JSONObject("{\"date\": \"2017-12-20\", \"tagnamealert\":\"tagnamealert\", \"value\": 1}"),
        new JSONObject("{\"date\": \"2017-12-20\", \"tagnamealert\":\"tagnamealert\", \"value\": 2}"));
    List<Item> items = handler
        .generateDdbItemsFromItemData(jsonObjects, ItemCorrelator.defaultCorrelator());

    List<Item> expected = Arrays.asList(
        new Item().withPrimaryKey("tagnamealert", "tagnamealert", "date", "2017-12-20")
            .withJSON("items", "[{\"value\": 1}, {\"value\": 2}]"));

    assertEquals(expected, items);
  }

}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
