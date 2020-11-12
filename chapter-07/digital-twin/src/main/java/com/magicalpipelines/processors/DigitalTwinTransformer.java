package com.magicalpipelines.processors;

import com.magicalpipelines.model.DigitalTwin;
import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Type;
import java.time.Duration;
import java.time.Instant;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// <key type, value type, return type>
public class DigitalTwinTransformer
    implements Transformer<String, TurbineState, KeyValue<String, DigitalTwin>> {
  private static final Logger log = LoggerFactory.getLogger(DigitalTwinProcessor.class);

  private ProcessorContext context;
  private KeyValueStore<String, DigitalTwin> kvStore;

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext context) {
    // keep the processor context locally because we need it in punctuate() and commit()
    this.context = context;

    /*
    Headers headers = context.headers();
    headers.add("hello", "world".getBytes(StandardCharsets.UTF_8));
    headers.remove("goodbye");
    headers.toArray();
    */

    // retrieve the key-value store named "Counts"
    this.kvStore = (KeyValueStore) context.getStateStore("digital-twin-store");

    // schedule a punctuate() method every 5 minutes based on wall clock time
    this.context.schedule(
        Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, this::enforceTtl);

    // commit progress every 20 seconds. commiting progress means committing the offset for the task
    //
    // note that the commit may not happen immediately,
    // it's just a request to Kafka Streams to perform the commit when it can. for example,
    // if we have downstream operators from this processor, committing immediately from here
    // would be committing a partially processed record. therefore, Kafka Streams will determine
    // when handle the commit request accordingly (e.g. after a batch of records have been
    // processed)
    //
    // note that these commits happen in addition to the commits that Kafka Streams performs
    // automatically, at the interval specified by commit.interval.ms
    this.context.schedule(
        Duration.ofSeconds(20), PunctuationType.WALL_CLOCK_TIME, (ts) -> context.commit());
  }

  @Override
  public KeyValue<String, DigitalTwin> transform(String key, TurbineState value) {
    // save the key to our alerts store, which we'll iterate over on a periodic basis and process
    System.out.println("Processing: " + value);

    if (value.getType() == null) {
      log.warn("Skipping state update due to unset type (must be: desired, reported)");
      // you can return null in a transformer if no record should be forwarded downstream
      return null;
    }

    DigitalTwin digitalTwin = kvStore.get(key);
    if (digitalTwin == null) {
      digitalTwin = new DigitalTwin();
    }

    if (value.getType() == Type.DESIRED) {
      digitalTwin.setDesired(value);
    } else if (value.getType() == Type.REPORTED) {
      digitalTwin.setReported(value);
    }

    // store the device shadow in the state store
    log.info("Storing digital twin: {}", digitalTwin);
    kvStore.put(key, digitalTwin);

    // forward to downstream processors
    return KeyValue.pair(key, digitalTwin);
  }

  @Override
  public void close() {
    // nothing to do
  }

  public void enforceTtl(Long timestamp) {
    KeyValueIterator<String, DigitalTwin> iter = kvStore.all();
    while (iter.hasNext()) {
      KeyValue<String, DigitalTwin> entry = iter.next();
      log.info("Checking to see if digital twin record has expired: {}", entry.key);
      TurbineState lastReportedState = entry.value.getReported();
      if (lastReportedState == null) {
        continue;
      }

      Instant lastUpdated = Instant.parse(lastReportedState.getTimestamp());
      long hoursSinceUpdate = Duration.between(lastUpdated, Instant.now()).toHours();
      if (hoursSinceUpdate >= 24) {
        kvStore.delete(entry.key);
      }
    }
    iter.close();
  }
}
