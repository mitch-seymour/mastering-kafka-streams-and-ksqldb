package com.magicalpipelines;

import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.*;

public class CountTransformer implements ValueTransformerWithKey<String, String, Long> {
  private KeyValueStore<String, Long> store;

  @Override
  public void init(ProcessorContext context) {
    this.store = (KeyValueStore<String, Long>) context.getStateStore("my-store");
  }

  @Override
  public Long transform(String key, String value) {
    // process tombstones
    if (value == null) {
      store.delete(key);
      return null;
    }
    // get the previous count for this key, or set to 0 if this is the first time
    // we've seen this key
    Long previousCount = store.get(key);
    if (previousCount == null) {
      previousCount = 0L;
    }
    // calculate the new count
    Long newCount = previousCount + 1;
    store.put(key, newCount);
    return newCount;
  }

  @Override
  public void close() {}
}
