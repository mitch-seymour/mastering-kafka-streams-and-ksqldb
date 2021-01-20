package com.magicalpipelines;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StateRestoreListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MyRestoreListener implements StateRestoreListener {

  private static final Logger log = LoggerFactory.getLogger(MyRestoreListener.class);

  @Override
  public void onRestoreStart(
      // topic partition
      TopicPartition topicPartition,
      // store name
      String storeName,
      // starting offset
      long startingOffset,
      // ending offset
      long endingOffset) {
    log.info("The following state store is being restored: {}", storeName);
  }

  @Override
  public void onRestoreEnd(
      // topic partition
      TopicPartition topicPartition,
      // store name
      String storeName,
      // total restored
      long totalRestored) {
    log.info("Restore complete for the following state store: {}", storeName);
  }

  @Override
  public void onBatchRestored(
      // topic partition
      TopicPartition topicPartition,
      // store name
      String storeName,
      // batch end offset
      long batchEndOffset,
      // number of records restoredString storeName,
      long numRestored) {
    // this is very noisy. don't log anything
  }
}
