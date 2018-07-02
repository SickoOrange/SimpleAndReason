/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.json.JSONObject;

/**
 * Helper class for grouping flat items for DynamoDB by a set of fields. {@link
 * DlsLambdaHandler#DDB_FIELD_TAGNAMEALERT} and {@link DlsLambdaHandler#DDB_FIELD_DATE} are always
 * used for grouping as they are primary keys.
 *
 * @see DlsLambdaHandler#generateDdbItemsFromItemData(Stream, ItemCorrelator)
 */
public class ItemCorrelator {

  private enum TYPE {
    STRING, NUMBER
  }

  private Set<String> fields;

  static final Set<String> PRIMARY_KEYS_STRING = ImmutableSet
      .of(DlsLambdaHandler.DDB_FIELD_DATE, DlsLambdaHandler.DDB_FIELD_TAGNAMEALERT);
  static final Set<String> PRIMARY_KEYS_NUMBER = ImmutableSet
      .of(DlsLambdaHandler.DDB_FIELD_DATE, DlsLambdaHandler.DDB_FIELD_AFIID);

  private ItemCorrelator(String[] fields,
      TYPE type) {
    switch (type) {
      case STRING:
        this.fields = Streams.concat(PRIMARY_KEYS_STRING.stream(),
            Arrays.stream(fields)).collect(Collectors.toSet());
        break;
      case NUMBER:
        this.fields = Streams.concat(PRIMARY_KEYS_NUMBER.stream(),
            Arrays.stream(fields)).collect(Collectors.toSet());
        break;
      default:
        this.fields = Collections.emptySet();
        break;
    }


  }

  /**
   * Creates a new ItemCorrelator, grouping by primary keys and {@code fields}
   */
  public static ItemCorrelator correlating(String... fields) {
    return new ItemCorrelator(fields, TYPE.STRING);

  }

  /**
   * Creates a new ItemCorrelator, grouping by primary keys and {@code fields}
   */
  public static ItemCorrelator correlatingForNumber(String... fields) {
    return new ItemCorrelator(fields, TYPE.NUMBER);
  }


  /**
   * Creates a new ItemCorrelator, grouping only by primary keys
   */
  public static ItemCorrelator defaultCorrelator() {
    return new ItemCorrelator(new String[]{}, TYPE.STRING);
  }

  Map<String, Object> getCorrelation(JSONObject itemData) {
    return fields.stream().collect(Collectors.toMap(s -> s, itemData::get));
  }

  JSONObject removeCorrelatedFields(JSONObject item) {
    fields.forEach(item::remove);
    return item;
  }


}

/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
