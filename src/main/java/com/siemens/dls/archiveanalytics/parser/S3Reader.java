/*
 * Copyright (c) Siemens AG 2017 ALL RIGHTS RESERVED.
 *
 * Digital Lifecycle Service (DLS)
 */

package com.siemens.dls.archiveanalytics.parser;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class for creating a S3Reader
 */
public class S3Reader {

  private AmazonS3 client;
  private String bucketName;
  private String objectKey;


  /**
   * Constructor for S3Reader
   *
   * @param client the AmazonS3 client that will be used
   * @param bucketName name of the bucket where the data is stored in S3
   * @param objectKey the object key that will be used
   */
  public S3Reader(AmazonS3 client, String bucketName, String objectKey) {
    this.client = client;
    this.bucketName = bucketName;
    this.objectKey = objectKey;
  }

  /**
   * Getter for a S3Reader that reads from the bucketName and objectKey location
   *
   * @return a Reader that points on the delivered bucketname and objectKey
   */
  public Reader getReader() {
    S3Object s3Object = client.getObject(bucketName, objectKey);
    return new InputStreamReader(s3Object.getObjectContent());
  }
}
