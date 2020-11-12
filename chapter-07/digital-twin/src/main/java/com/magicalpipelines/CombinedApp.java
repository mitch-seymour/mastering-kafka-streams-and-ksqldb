package com.magicalpipelines;

import com.magicalpipelines.model.DigitalTwin;
import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Power;
import com.magicalpipelines.model.TurbineState.Type;
import com.magicalpipelines.processors.DigitalTwinValueTransformerWithKey;
import com.magicalpipelines.serialization.json.JsonSerdes;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// offshore windfarm
class CombinedApp {

  private static final Config config = ConfigFactory.load();
  private static final Logger log = LoggerFactory.getLogger(CombinedApp.class);

  public static void main(String[] args) {
    // DSL way of building a topology
    StreamsBuilder builder = new StreamsBuilder();

    // note how we don't name this source processor. Kafka Streams creates
    // a default, internal name for us
    KStream<String, TurbineState> desiredStateEvents =
        builder.stream(
            "desired-state-events", Consumed.with(Serdes.String(), JsonSerdes.TurbineState()));

    KStream<String, TurbineState> highWinds =
        builder
            .stream(
                "reported-state-events", Consumed.with(Serdes.String(), JsonSerdes.TurbineState()))
            // generate shutdown signals
            .flatMapValues(
                (key, reported) -> {
                  List<TurbineState> records = new ArrayList<>();
                  records.add(reported);
                  if (reported.getWindSpeedMph() > 65 && reported.getPower() == Power.ON) {
                    log.info("high winds detected. sending shutdown signal");
                    TurbineState desired = TurbineState.clone(reported);
                    desired.setPower(Power.OFF);
                    desired.setType(Type.DESIRED);
                    // forward the new desired state
                    records.add(desired);
                  }
                  return records;
                })
            .merge(desiredStateEvents);

    // state store for digital twin records
    StoreBuilder<KeyValueStore<String, DigitalTwin>> storeBuilder =
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore("digital-twin-store"),
            Serdes.String(),
            JsonSerdes.DigitalTwin());

    // note: we drop the parent node argument
    builder.addStateStore(storeBuilder);

    // 1:N
    // you can either return 1 value to be returned,
    // or use ProcessorContext#forward to send multiple values to a downstream operator
    // KStream<String, DigitalTwin> digitalTwins =
    //   highWinds.transform(DigitalTwinTransformer::new, "digital-twin-store");

    // 1:1 mapping.
    // while you still have access to the ProcessorContext, you will get a StreamsException
    // if you try to emit multiple records via ProcessorContext#forward
    // highWinds.transformValues(DigitalTwinValueTransformer::new, "digital-twin-store");

    // 1:1 mapping with read-only key
    // while you still have access to the ProcessorContext, you will get a StreamsException
    // if you try to emit multiple records via ProcessorContext#forward
    highWinds
        .transformValues(DigitalTwinValueTransformerWithKey::new, "digital-twin-store")
        // sink processor
        .to("digital-twins", Produced.with(Serdes.String(), JsonSerdes.DigitalTwin()));

    // 1:N
    // key access
    // you can return multiple values directly
    // highWinds.flatTransform(DigitalTwinFlatTransformer::new, "digital-twin-store");

    // 1:N
    // no key-access
    // you can return multiple values directly
    // highWinds.flatTransformValues(DigitalTwinFlatValueTransformer::new, "digital-twin-store");

    // terminal operation, so no fluent-method chaining allowed
    // highWinds.process(DigitalTwinProcessor::new, "digital-twin-store");

    // required for interactive queries
    String host = config.getString("host");
    Integer port = config.getInt("port");
    String stateDir = config.getString("stateDir");
    String endpoint = String.format("%s:%s", host, port);

    // set the required properties for running Kafka Streams
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev-consumer");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(
        StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
    props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
    props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

    // build the topology and start streaming!
    // System.out.println("\n\nDescribe:\n\n" + builder.describe());
    // this changed!!
    KafkaStreams streams = new KafkaStreams(builder.build(), props);

    log.info("Starting Digital Twin Streams App");

    streams.start();
    // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    HostInfo hostInfo = new HostInfo(host, port);
    RestService service = new RestService(hostInfo, streams);
    log.info("Starting Digital Twin REST Service");
    service.start();
  }
}
