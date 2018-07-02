/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.ddb.DlsDdbClient;
import com.siemens.dls.archiveanalytics.model.Port;
import com.siemens.dls.archiveanalytics.model.Tuple;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract Class for implementing a DlsLambdaHandler
 */
public abstract class DlsLambdaHandler {

  protected static final String PORT_NAME_TRBL_AL = "TRBL_AL";
  protected static final String PORT_NAME_Q_AL = "Q_AL";

  protected static final String DDB_FIELD_TAGNAMEALERT = "tagnamealert";
  protected static final String DDB_FIELD_DATE = "date";
  protected static final String DDB_FIELD_ITEMS = "items";
  protected static final String DDB_FIELD_SIM_DUP_OR_REASON = "SimDuplAOReason";
  protected static final String DDB_FIELD_SIM_OR_REASON = "SimAOReason";
  protected static final String DDB_FIELD_AFIID = "afiid";



  protected static final int TRENDS_MIN_QUALITY = 64;

  protected final S3DataLoader s3DataLoader;
  protected final DlsDdbClient ddbClient;

  protected static final Logger LOGGER = Logger.getLogger(DlsLambdaHandler.class);

  /**
   * Default constructor with default S3 und DynamoDB clients
   */
  protected DlsLambdaHandler() {
    this(prepareDefaultS3DataLoader(), prepareDefaultDdbWriter());
  }

  private static DlsDdbClient prepareDefaultDdbWriter() {
    return new DlsDdbClient();
  }

  private static S3DataLoader prepareDefaultS3DataLoader() {
    return new S3DataLoader();
  }

  /**
   * Constructor with a given {@link AmazonS3} client and {@link AmazonDynamoDB} client
   *
   * @param s3Client the S3 client that will be used
   * @param ddbClient the DynamoDB client that will be used
   */
  protected DlsLambdaHandler(AmazonS3 s3Client, AmazonDynamoDB ddbClient) {
    this.s3DataLoader = new S3DataLoader(s3Client);
    this.ddbClient = new DlsDdbClient(ddbClient);
  }

  /**
   * Constructor with a given {@link S3DataLoader} client and {@link AmazonDynamoDB} client
   *
   * @param s3DataLoader the S3DataLoader that will be used
   * @param ddbClient the DlsDdbClient that will be used
   */
  protected DlsLambdaHandler(S3DataLoader s3DataLoader, DlsDdbClient ddbClient) {
    this.s3DataLoader = s3DataLoader;
    this.ddbClient = ddbClient;
  }

  /**
   * Provides the name of the current use case
   *
   * @return The name of the current use case
   */
  public abstract String getUseCase();


  /**
   * Get the S3 bucket name from the environment variables
   *
   * @return The S3 bucket name
   * @deprecated
   */
  @Deprecated
  protected static String getBucketName() {
    return null;
  }

  /**
   * Handler entry point for the producer handler
   *
   * @param params The params for the specific handler
   * @param context The specific context object
   * @return The result as DlsProducerLambdaResult
   */
  public final DlsProducerLambdaResult handleRequest(DlsProducerLambdaParams params,
      Context context) throws IOException {
    s3DataLoader.setParams(params);
    ddbClient.setParams(params);
    return handleRequestInternal(params, context);
  }

  /**
   * the actual implementation of the producer handler
   *
   * @param params The params for the specific handler
   * @param context The specific context object
   * @return The result as DlsProducerLambdaResult
   */
  protected abstract DlsProducerLambdaResult handleRequestInternal(DlsProducerLambdaParams params,
      Context context) throws IOException;

  /**
   * Create a {@link List} of DynamoDB {@link Item}s from a {@link Stream} of {@link Tuple}s
   * containing the data, indexed by the Primary Key as {@link Tuple}.
   *
   * @param dataTuplesStream The data with primary key
   * @return A {@link List} of DynamoDB {@link Item}s
   * @deprecated use {@link #generateDdbItemsFromItemData(Stream, ItemCorrelator)} instead
   */
  @Deprecated
  protected List<Item> generateDdbItemsFromItemData(
      Stream<Tuple<Tuple<String, String>, JSONObject>> dataTuplesStream) {
    Map<Tuple<String, String>, List<Tuple<Tuple<String, String>, JSONObject>>> dataTuplesGroupedByKey = dataTuplesStream
        .collect(Collectors.groupingBy(Tuple::getLeft));
    Map<Tuple<String, String>, JSONArray> itemsByKey = Maps
        .transformValues(dataTuplesGroupedByKey, list -> {
          JSONArray array = new JSONArray();
          list.forEach(item -> array.put(item.getRight()));
          return array;
        });
    return itemsByKey.entrySet().stream().map(entry -> new Item()
        .withPrimaryKey(
            DDB_FIELD_TAGNAMEALERT, entry.getKey().getLeft(),
            DDB_FIELD_DATE, entry.getKey().getRight()
        )
        .withJSON(DDB_FIELD_ITEMS, entry.getValue().toString())
    ).collect(Collectors.toList());
  }

  /**
   * Transforms flat item data in form of {@link JSONObject}s to DynamoDB {@link Item}s, grouping
   * them by fields defined by {@link ItemCorrelator}, such that any flat items with the same set of
   * correlated fields become one {@link Item} that has multiple entries in the array field 'items'.
   * The correlated fields go on top level, the rest become part of the sub-items. {@link
   * #DDB_FIELD_TAGNAMEALERT} is always the primary key and {@link #DDB_FIELD_DATE} is always the
   * sort key, therefore these two fields MUST always be present in all flat items.
   *
   * @see ItemCorrelator
   */
  protected List<Item> generateDdbItemsFromItemData(
      Stream<JSONObject> dataItems, ItemCorrelator correlator) {
// fixme: this method should be somehow unified with the other overload
//    return generateDdbItemsFromItemData(dataItems, correlator, Collections.emptyList());
    Map<JSONObject, JSONArray> itemsByCorrelationKeys = getItemsByCorrelationKeys(
        dataItems, correlator);

    return itemsByCorrelationKeys.entrySet().stream().map(entry -> {
          Item item = new Item()
              .withPrimaryKey(
                  DDB_FIELD_TAGNAMEALERT, entry.getKey().get(DDB_FIELD_TAGNAMEALERT),
                  DDB_FIELD_DATE, entry.getKey().get(DDB_FIELD_DATE)
              )
              .withJSON(DDB_FIELD_ITEMS, entry.getValue().toString());
          entry.getKey().keySet()
              .forEach(correlationKey -> {
                if (!ItemCorrelator.PRIMARY_KEYS_STRING
                    .contains(correlationKey)) { // skip adding primary keys again
                  item.with(correlationKey, entry.getKey().get(correlationKey));
                }
              });

          return item;
        }
    ).collect(Collectors.toList());
  }

  /**
   * Transforms flat item data in form of {@link JSONObject}s to DynamoDB {@link Item}s, The
   * correlated fields go on top level, the rest become part of the fields, which come from given
   * keysWithJsonStringValues {@link #DDB_FIELD_TAGNAMEALERT} is always the primary key and {@link
   * #DDB_FIELD_DATE} is always the sort key, therefore these two fields MUST always be present in
   * all flat items.
   *
   * @param dataItems flat json stream
   * @param correlator First-level attributes except primary keys
   * @param keysWithJsonStringValues First-level attributes whose structure is also a json
   * @param itemsEntry items attributes
   * @see ItemCorrelator
   */
  protected List<Item> generateDdbItemsFromItemData(
      Stream<JSONObject> dataItems, ItemCorrelator correlator,
      List<String> keysWithJsonStringValues, String itemsEntry) {
    Map<JSONObject, JSONArray> itemsByCorrelationKeys = getItemsByCorrelationKeys(dataItems,
        correlator);

    return itemsByCorrelationKeys.entrySet().stream().map(entry -> {
          Item item = new Item()
              .withPrimaryKey(
                  DDB_FIELD_TAGNAMEALERT, entry.getKey().get(DDB_FIELD_TAGNAMEALERT),
                  DDB_FIELD_DATE, entry.getKey().get(DDB_FIELD_DATE)
              )
              .withJSON(itemsEntry, validateItemsEntry(entry.getValue(), itemsEntry));
          entry.getKey().keySet()
              .forEach(correlationKey -> {
                if (!ItemCorrelator.PRIMARY_KEYS_STRING
                    .contains(correlationKey)) { // skip adding primary keys again
                  //whether value of key is json string
                  if (keysWithJsonStringValues.contains(correlationKey)) {
                    item.withJSON(correlationKey, (String) entry.getKey().get(correlationKey));
                  } else {
                    item.with(correlationKey, entry.getKey().get(correlationKey));
                  }
                }
              });

          return item;
        }
    ).collect(Collectors.toList());
  }

  /**
   * Transforms flat item data in form of {@link JSONObject}s to DynamoDB {@link Item}s, The
   * correlated fields go on top level, the rest become part of the fields, which come from given
   * keysWithJsonStringValues {@link #DDB_FIELD_TAGNAMEALERT} is always the primary key and {@link
   * #DDB_FIELD_DATE} is always the sort key, therefore these two fields MUST always be present in
   * all flat items.
   *
   * @param dataItems flat json stream
   * @param correlator First-level attributes except primary keys
   * @param keysWithJsonStringValues First-level attributes whose structure is also a json
   * @see ItemCorrelator
   */
  protected List<Item> generateNumberDdbItemsFromItemData(
      Stream<JSONObject> dataItems, ItemCorrelator correlator,
      List<String> keysWithJsonStringValues) {
    Map<JSONObject, JSONArray> itemsByCorrelationKeys = getItemsByCorrelationKeys(dataItems,
        correlator);

    return itemsByCorrelationKeys.entrySet().stream().map(entry -> {
          Item item = new Item()
              .withPrimaryKey(
                  DDB_FIELD_AFIID, entry.getKey().get(DDB_FIELD_AFIID),
                  DDB_FIELD_DATE, entry.getKey().get(DDB_FIELD_DATE)
              )
              .withJSON(DlsLambdaHandler.DDB_FIELD_ITEMS, entry.getValue().toString());
          entry.getKey().keySet()
              .forEach(correlationKey -> {
                if (!ItemCorrelator.PRIMARY_KEYS_NUMBER
                    .contains(correlationKey)) { // skip adding primary keys again
                  //whether value of key is json string
                  if (keysWithJsonStringValues.contains(correlationKey)) {
                    item.withJSON(correlationKey, (String) entry.getKey().get(correlationKey));
                  } else {
                    item.with(correlationKey, entry.getKey().get(correlationKey));
                  }
                }
              });

          return item;
        }
    ).collect(Collectors.toList());
  }

  /**
   * validate items entry
   * if items is empty, then need to remove redundant key
   */
  private String validateItemsEntry(JSONArray value, String itemsEntry) {
    if (value.length() == 1) {
      JSONObject object = (JSONObject) value.get(0);
      if (object.has(itemsEntry)) {
        return object.get(itemsEntry).toString();
      }
    }
    return value.toString();
  }


  /**
   * grouping by fields defined by {@link ItemCorrelator}
   */
  private Map<JSONObject, JSONArray> getItemsByCorrelationKeys(Stream<JSONObject> dataItems,
      ItemCorrelator correlator) {
    Map<Map<String, Object>, List<JSONObject>> itemDataGroupedByCorrelatedFields = dataItems
        .collect(Collectors.groupingBy(correlator::getCorrelation));

    return itemDataGroupedByCorrelatedFields.entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> convertMapToJsonObject(entry.getKey()),
            entry -> entry.getValue().stream()
                .map(correlator::removeCorrelatedFields)
                .collect(toJSONArray())
        ));
  }

  private Collector<JSONObject, JSONArray, JSONArray> toJSONArray() {
    return new Collector<JSONObject, JSONArray, JSONArray>() {
      @Override
      public Supplier<JSONArray> supplier() {
        return JSONArray::new;
      }

      @Override
      public BiConsumer<JSONArray, JSONObject> accumulator() {
        return JSONArray::put;
      }

      @Override
      public BinaryOperator<JSONArray> combiner() {
        return (arr1, arr2) -> {
          JSONArray result = new JSONArray();
          for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
          }
          for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
          }
          return result;
        };
      }

      @Override
      public Function<JSONArray, JSONArray> finisher() {
        return a -> a;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
      }
    };
  }

  /**
   * fixme: javadoc
   */
  public static JSONObject convertMapToJsonObject(Map<String, Object> map) {
    JSONObject jsonKey = new JSONObject();
    map.forEach(jsonKey::put);
    return jsonKey;
  }

  static List<Tuple<Port, Integer>> convertPortsToTuples(List<Port> inPorts) {
    return inPorts.stream().map(p -> Tuple.of(p, 0)).collect(Collectors.toList());
  }

  /**
   * Getter for the {@link DlsDdbClient}
   *
   * @return The DLS DynamoDB connection instance
   */
  public DlsDdbClient getDdbClient() {
    return ddbClient;
  }
}
