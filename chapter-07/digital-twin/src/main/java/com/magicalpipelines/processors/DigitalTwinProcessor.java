package com.magicalpipelines.processors;

import com.magicalpipelines.model.DigitalTwin;
import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Type;
import java.time.Duration;
import java.time.Instant;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalTwinProcessor implements Processor<String, TurbineState, String, DigitalTwin> {
  private static final Logger log = LoggerFactory.getLogger(DigitalTwinProcessor.class);

  private ProcessorContext<String, DigitalTwin> context;
  private KeyValueStore<String, DigitalTwin> kvStore;
  private Cancellable punctuator;

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext<String, DigitalTwin> context) {
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
    punctuator =
        this.context.schedule(
            Duration.ofMinutes(5), PunctuationType.WALL_CLOCK_TIME, this::enforceTtl);

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
  public void process(Record<String, TurbineState> record) {
    String key = record.key();
    TurbineState value = record.value();
    System.out.println("Processing: " + value);

    if (value.getType() == null) {
      log.warn("Skipping state update due to unset type (must be: desired, reported)");
      return;
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
    Record<String, DigitalTwin> newRecord =
        new Record<>(record.key(), digitalTwin, record.timestamp());
    context.forward(newRecord);

    // lesson: note that calling commit here would lead to low throughput, because commit
    // is an expensive operation (it flushes the state store)
    // context.commit();
  }

  @Override
  public void close() {
    // cancel the punctuator
    punctuator.cancel();
  }

  public void enforceTtl(Long timestamp) {
    // wrap the kvStore.all() invocation in a try-with-resources to ensure there aren't any resource
    // leaks!
    try (KeyValueIterator<String, DigitalTwin> iter = kvStore.all()) {
      while (iter.hasNext()) {
        KeyValue<String, DigitalTwin> entry = iter.next();
        log.info("Checking to see if digital twin record has expired: {}", entry.key);
        TurbineState lastReportedState = entry.value.getReported();
        if (lastReportedState == null) {
          continue;
        }

        Instant lastUpdated = Instant.parse(lastReportedState.getTimestamp());
        long daysSinceLastUpdate = Duration.between(lastUpdated, Instant.now()).toDays();
        if (daysSinceLastUpdate >= 7) {
          kvStore.delete(entry.key);
        }
      }
    }
  }
}
