/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.ddb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.siemens.dls.archiveanalytics.DlsProducerLambdaParams;
import com.siemens.dls.archiveanalytics.Utils;
import com.siemens.dls.archiveanalytics.model.Module;
import com.siemens.dls.archiveanalytics.model.Tuple;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Class to provide a writer interface to write data to DynamoDB
 */
public class DlsDdbClient {

  //dynamoDBTable: [tableprefix]-[ppid]-[workTitle]
  // e.g.:aa-aa-13273-fumdm-ng-T0002-hwpfum"
  public static final String DYNAMODB_TABLE_PREFIX = "TABLEPREFIX";
  private static final int MAX_BATCH_WRITE_LENGTH = 25;
  private static final int MAX_BATCH_GET_LENGTH = 100;
  public static final int ASCII_A_POS = 65;
  public static final int ALPHABBET_LEN = 26;
  private AmazonDynamoDB client;
  private DynamoDB documentClient;
  private DlsProducerLambdaParams params;
  private ParallelScanner scanner;
  private List<Double> consumedWriteCapacityUnits;

  private static final Logger LOG = Logger
      .getLogger(DlsDdbClient.class);

  /**
   * Constructor Sets the <code>{@link AmazonDynamoDB}</code> client and the <code>{@link
   * DlsProducerLambdaParams}</code> instances
   *
   * @param client The <code>{@link AmazonDynamoDB}</code> client instance
   */
  public DlsDdbClient(AmazonDynamoDB client) {
    this.client = client;
    consumedWriteCapacityUnits = new ArrayList<>();
  }

  public DlsDdbClient() {
    this(prepareDefaultDynamoDbClient());
  }

  public void setParams(DlsProducerLambdaParams params) {
    this.params = params;
  }

  /**
   * Returns a default client for DynamoDB
   *
   * @return a default {@link AmazonDynamoDB} client
   */
  private static AmazonDynamoDB prepareDefaultDynamoDbClient() {
    return AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
  }


  /**
   * Create and provide the DynamoDB table name string from params
   *
   * @param params The <code>{@link DlsProducerLambdaParams}</code> instance
   * @return The name of the DynamoDB table
   */
  private static String getDdbTableName(DlsProducerLambdaParams params, String useCase) {
    return Utils.getEnvironmentVariable(DYNAMODB_TABLE_PREFIX) + "-" +
        params.getPpid() + "-" + useCase;
  }

  /**
   * Write a list of <code>{@link Item}</code> instances to the DynamoDB
   *
   * @param ddbItems A <code>{@link List}</code> of <code>{@link Item}</code> instances to write
   */
  public int writeItems(List<Item> ddbItems, String useCase) {
    String dynamoDBTableName = getDdbTableName(params, useCase);
    LOG.info(
        "Preparing to write " + ddbItems.size() + "items into DDB table " + dynamoDBTableName);

    DynamoDB ddb = getDocumentClient();
    Lists.partition(ddbItems, MAX_BATCH_WRITE_LENGTH).forEach(b -> {
      TableWriteItems twi = new TableWriteItems(dynamoDBTableName);
      b.forEach(twi::addItemToPut);
      BatchWriteItemSpec bwi = new BatchWriteItemSpec();
      bwi.getRequest().setReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
      bwi.withTableWriteItems(twi);
      BatchWriteItemOutcome outcome = ddb.batchWriteItem(bwi);

      do {
        Map<String, List<WriteRequest>> unprocessedItems = outcome.getUnprocessedItems();
        if (outcome.getUnprocessedItems().size() == 0) {
          LOG.info("No unprocessed items found");
        } else {
          LOG.info(
              String.format("Retrying writing %d/%d items", unprocessedItems.size(), b.size()));
          outcome = ddb.batchWriteItemUnprocessed(unprocessedItems);
          addWriteStatistics(outcome, dynamoDBTableName);
        }
      } while (outcome.getUnprocessedItems().size() > 0);
      LOG.info("BatchWrite consumed the following write capacity units: " +
          getConsumedWriteCapacityUnits().stream().map(Object::toString)
              .collect(Collectors.joining(", ")));
    });
    return ddbItems.size();
  }

  /**
   * Get the consumed write capacity units and add them to a {@link List}
   *
   * @param outcome The BatchWriteItemResult
   * @param tableName The DynamoDB table name
   */
  private void addWriteStatistics(BatchWriteItemOutcome outcome, String tableName) {
    List<ConsumedCapacity> capacity = outcome.getBatchWriteItemResult()
        .getConsumedCapacity();
    if (null != capacity) {
      capacity.stream()
          .filter(r -> r.getTableName().equals(tableName))
          .map(ConsumedCapacity::getCapacityUnits)
          .forEach(consumedWriteCapacityUnits::add);
    }
  }

  /**
   * Get the collected consumed write capacity units statistics
   *
   * @return The list of consumed write capacity units
   */
  public List<Double> getConsumedWriteCapacityUnits() {
    return Collections.unmodifiableList(consumedWriteCapacityUnits);
  }

  /**
   * Reads a Map of <code>{@link Item}</code> instances from DynamoDB
   *
   * @param hashKeyName Name of the hash key(partition key)
   * @param rangeKeyName Name of the range key(sort key)
   * @param attributesMap A Map containing hashKeyValue,rangeKeyValue
   * @param useCase Table to read data
   * @return A list of <code>{@link Item}</code> instances
   */
  public Map<String, Item> readItems(String hashKeyName, String rangeKeyName,
      Map<String, String> attributesMap, String useCase) {
    Set<Tuple<String, String>> attributeSet = attributesMap.entrySet().stream()
        .map(stringStringEntry -> new Tuple<>(stringStringEntry.getKey(),
            stringStringEntry.getValue()))
        .collect(Collectors.toSet());

    List<Item> itemList = readItems(hashKeyName, rangeKeyName, attributeSet, useCase);

    return Maps.uniqueIndex(itemList, item -> item.getString(hashKeyName));
  }

  /**
   * Reads a list of <code>{@link Item}</code> instances from DynamoDB
   *
   * @param hashKeyName Name of the hash key(partition key)
   * @param rangeKeyName Name of the range key(sort key)
   * @param attributesSet Set of Tuples containing hashKeyValue,rangeKeyValue
   * @param useCase Table to read data
   * @return A list of <code>{@link Item}</code> instances
   */
  public List<Item> readItems(String hashKeyName, String rangeKeyName,
      Set<Tuple<String, String>> attributesSet, String useCase) {
    String dynamoDBTableName = getDdbTableName(params, useCase);
    LOG.info(
        "Preparing to read " + attributesSet.size() + " items from DDB table " + dynamoDBTableName);

    DynamoDB ddb = getDocumentClient();
    List<Item> outItems = new LinkedList<>();

    List<Tuple<String, String>> attributesList = new ArrayList<>(attributesSet);
    Lists.partition(attributesList, MAX_BATCH_GET_LENGTH).forEach(b -> {
      TableKeysAndAttributes keysAndAttributes = new TableKeysAndAttributes(dynamoDBTableName);
      BatchGetItemOutcome outcome;
      b.forEach(item -> keysAndAttributes
          .addHashAndRangePrimaryKey(hashKeyName, item.getLeft(), rangeKeyName, item.getRight()));

      outcome = ddb.batchGetItem(keysAndAttributes);
      outItems
          .addAll(outcome.getTableItems().getOrDefault(dynamoDBTableName, Collections.emptyList()));
      while (outcome.getUnprocessedKeys().size() > 0) {
        LOG.info(
            "Failed to read " + outcome.getUnprocessedKeys().get(dynamoDBTableName).getKeys().size()
                + "/" + b.size() + " Items. Retrying...");

        outcome = ddb.batchGetItemUnprocessed(outcome.getUnprocessedKeys());
        outItems.addAll(
            outcome.getTableItems().getOrDefault(dynamoDBTableName, Collections.emptyList()));
      }

    });

    LOG.info(outItems.size() + " items read from DDB table " + dynamoDBTableName);
    return outItems;
  }

  /**
   * Scan the DynamoDB for specific columns
   *
   * @param useCase The use Case for concatenation of the table name
   * @param filterValues Map of Filter Values
   * @param columns The column Set to retrieve
   * @return List of Maps of all retrieved Columns
   */
  public List<Map<String, AttributeValue>> searchItemsEqFilter(String useCase,
      Map<String, AttributeValue> filterValues, Set<String> columns) {
    String dynamoDBTableName = getDdbTableName(params, useCase);
    ScanRequest sReq = applyFilters(
        new ScanRequest()
            .withTableName(dynamoDBTableName)
            .withProjectionExpression(String.join(", ", columns)),
        filterValues
    );

    return getScanner().executeParallelScan(sReq);
  }


  /**
   * Apply the filters to the DynamoDB Request
   *
   * @param request The DynamoDB request
   * @param filterValues The filter columns and search values
   * @return The Scan request with filters applied
   */
  private ScanRequest applyFilters(ScanRequest request, Map<String, AttributeValue> filterValues) {
    AtomicInteger i = new AtomicInteger(0);
    Map<String, String> attributeNames = new HashMap<>();
    Map<String, AttributeValue> attributeValues = new HashMap<>();
    String filters = filterValues.entrySet().stream().map(entry -> {
      String namePlaceholder = String.format("#a%dn", i.getAndIncrement());
      String valuePlaceholder = String.format(":a%dv", i.get());
      attributeNames.put(namePlaceholder, entry.getKey());
      attributeValues.put(valuePlaceholder, entry.getValue());
      return String.format("%s = %s", namePlaceholder, valuePlaceholder);
    }).collect(Collectors.joining(" AND "));
    return request
        .withFilterExpression(filters)
        .withExpressionAttributeNames(attributeNames)
        .withExpressionAttributeValues(attributeValues);
  }

  /**
   * Getter for {@link ParallelScanner}. If none is set, create default.
   *
   * @return The scanner instance
   */
  public ParallelScanner getScanner() {
    if (scanner == null) {
      scanner = new ParallelScanner(client);
    }
    return scanner;
  }

  /**
   * Setter for {@link ParallelScanner}
   *
   * @param scanner The scanner instance
   */
  public void setScanner(ParallelScanner scanner) {
    this.scanner = scanner;
  }

  public Map<Integer, Boolean> hasEntryMultiple(Map<Module, String> ports, String useCase,
      String hashKeyName, String rangeKeyName) {
    Map<Integer, Boolean> result = ports.keySet().stream().collect(
        Collectors.toMap(Module::getId, m -> false)
    );

    DynamoDB ddb = getDocumentClient();
    String dynamoDBTableName = getDdbTableName(params, useCase);
    for (int block = 0; block <= Math.floor(ports.size() / MAX_BATCH_GET_LENGTH); block++) {
      TableKeysAndAttributes keySet = new TableKeysAndAttributes(dynamoDBTableName);
      List<String> paramList = new ArrayList<>();
      ports.values().stream().skip(block * MAX_BATCH_GET_LENGTH).limit(MAX_BATCH_GET_LENGTH)
          .forEach(m -> {
            paramList.add(params.getDate());
            paramList.add(m);
          });
      keySet.addHashAndRangePrimaryKeys(hashKeyName, rangeKeyName, paramList.toArray());

      BatchGetItemOutcome outcome = ddb.batchGetItem(keySet);
      Map<String, KeysAndAttributes> unprocessed;
      do {
        outcome.getTableItems().get(dynamoDBTableName)
            .forEach(i -> result.replace(i.getInt("afiid"), true));

        unprocessed = outcome.getUnprocessedKeys();
        if (! unprocessed.isEmpty()) {
          outcome = ddb.batchGetItemUnprocessed(unprocessed);
        }
      } while (!unprocessed.isEmpty());

    }

    return result;
  }

  public DynamoDB getDocumentClient() {
    if (documentClient == null) {
      documentClient = new DynamoDB(client);
    }
    return documentClient;
  }

  public void setDocumentClient(DynamoDB documentClient) {
    this.documentClient = documentClient;
  }
}
