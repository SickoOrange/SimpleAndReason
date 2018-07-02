/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */
package com.siemens.dls.archiveanalytics.ddb;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

/**
 * This class enables to execute parallel Scan Requests to DynamoDB
 */
public class ParallelScanner {

  private static final Logger LOG = Logger
      .getLogger(ParallelScanner.class);

  protected AmazonDynamoDB client;

  /**
   * Create an instance to create parallel scans
   *
   * @param client The {@link AmazonDynamoDB} client to work with
   */
  public ParallelScanner(AmazonDynamoDB client) {
    this.client = client;
  }

  /**
   * Create a parallel scan operation from a single {@link ScanRequest}
   *
   * @param sReq the single scanR
   * @return A List of result maps of key/value pairs
   */
  public List<Map<String, AttributeValue>> executeParallelScan(ScanRequest sReq) {
    List<Map<String, AttributeValue>> resultItems = new ArrayList<>();
    // Parallel Scan
    List<ScanSegmentTask> scanSegmentTasks = new ArrayList<>();
    ExecutorService executor = Executors.newFixedThreadPool(ScanSegmentTask.TASK_COUNT);
    for (int i = 0; i < ScanSegmentTask.TASK_COUNT; i++) {
      scanSegmentTasks.add(new ScanSegmentTask(sReq, i));
    }

    try {
      List<Future<List<Map<String, AttributeValue>>>> futures = executor
          .invokeAll(scanSegmentTasks);
      try {
        for (Future<List<Map<String, AttributeValue>>> f : futures) {
          resultItems.addAll(f.get());
        }
      } catch (InterruptedException | ExecutionException e1) {
        LOG.error(e1);
      }
    } catch (InterruptedException e) {
      LOG.error(e);
      // Just add nothing to results
    }
    return resultItems;
  }

  /**
   * A {@link Callable} class to Split the {@link ScanRequest} in parallel executable segments
   */
  private class ScanSegmentTask implements Callable<List<Map<String, AttributeValue>>> {

    static final int TASK_COUNT = 10;
    private ScanRequest segmentRequest;

    /**
     * Take a {@link ScanRequest} task, clone it and add the specific segment information. Then wrap
     * it into a Runnable for parallel Execution
     *
     * @param sReq The original single-threaded {@link ScanRequest}
     * @param curSegment The index of the current segment
     */
    private ScanSegmentTask(ScanRequest sReq, int curSegment) {
      segmentRequest = sReq.clone();
      segmentRequest.withSegment(curSegment);
      segmentRequest.withTotalSegments(ScanSegmentTask.TASK_COUNT);
    }

    /**
     * Execute a Scan for a specific segment
     */
    @Override
    public List<Map<String, AttributeValue>> call() {
      List<Map<String, AttributeValue>> resultItems = new ArrayList<>();
      Map<String, AttributeValue> lastKeyEvaluated = null;
      do {
        segmentRequest.withExclusiveStartKey(lastKeyEvaluated);
        ScanResult result = client.scan(segmentRequest);
        resultItems.addAll(result.getItems());
        lastKeyEvaluated = result.getLastEvaluatedKey();
      } while (lastKeyEvaluated != null);
      return resultItems;
    }
  }
}
