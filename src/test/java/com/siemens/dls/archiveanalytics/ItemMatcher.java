/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_ITEMS;
import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_SIM_DUP_OR_REASON;
import static com.siemens.dls.archiveanalytics.DlsLambdaHandler.DDB_FIELD_SIM_OR_REASON;

import com.amazonaws.services.dynamodbv2.document.Item;
import java.util.Objects;
import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.json.JSONArray;

class ItemMatcher extends BaseMatcher<Item> {

  private final Item expected;

  ItemMatcher(Item item) {
    expected = item;
  }

  static ItemMatcher sameItem(Item item) {
    return new ItemMatcher(item);
  }

  @Override
  public boolean matches(Object item) {

    if (!(item instanceof Item)) {
      return false;
    }

    Item argument = (Item) item;
    Set<String> expectedKeys = expected.asMap().keySet();
    Set<String> actualKeys = argument.asMap().keySet();
    if (!expectedKeys.equals(actualKeys)) {
      return false;
    }

    for (String key : expectedKeys) {
      if (key.equals(DDB_FIELD_ITEMS) || key
          .equals(DDB_FIELD_SIM_DUP_OR_REASON) || key.equals(DDB_FIELD_SIM_OR_REASON)) {
        JSONArray expectedJson = new JSONArray(expected.getJSON(key));
        JSONArray actualJson = new JSONArray(argument.getJSON(key));
        if (!Matchers.containsInAnyOrder(expectedJson.toList().toArray())
            .matches(actualJson.toList())) {
          return false;
        }
      } else if (!Objects.equals(expected.get(key), argument.get(key))) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(expected.toString());
  }
}

/*
 * Copyright (c) Siemens AG 2018 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
