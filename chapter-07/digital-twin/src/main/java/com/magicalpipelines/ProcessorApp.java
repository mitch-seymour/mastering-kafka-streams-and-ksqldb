package com.magicalpipelines;

import com.magicalpipelines.model.DigitalTwin;
import com.magicalpipelines.processors.DigitalTwinProcessor;
import com.magicalpipelines.processors.HighWindsFlatmapProcessor;
import com.magicalpipelines.serialization.json.JsonSerdes;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessorApp {

  private static final Config config = ConfigFactory.load().getConfig("streams");
  private static final Logger log = LoggerFactory.getLogger(ProcessorApp.class);

  public static void main(String[] args) {
    Topology topology = getTopology();

    // set the required properties for running Kafka Streams
    Properties props = new Properties();
    config.entrySet().forEach(e -> props.setProperty(e.getKey(), config.getString(e.getKey())));

    KafkaStreams streams = new KafkaStreams(topology, props);

    // clean up local state since many of the tutorials write to the same location
    // you should run this sparingly in production since it will force the state
    // store to be rebuilt on start up
    streams.cleanUp();

    log.info("Starting Digital Twin Streams App");
    streams.start();
    // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    @SuppressWarnings("StringSplitter")
    String[] endpointParts = config.getString(StreamsConfig.APPLICATION_SERVER_CONFIG).split(":");
    HostInfo hostInfo = new HostInfo(endpointParts[0], Integer.parseInt(endpointParts[1]));
    RestService service = new RestService(hostInfo, streams);
    log.info("Starting Digital Twin REST Service");
    service.start();
  }

  public static Topology getTopology() {
    // instantiate a topology instance directly.
    // note: this is actually what gets built when you call new
    // StreamsBuilder().build() after all your DSL operators have been added
    Topology builder = new Topology();

    // add a source processor for desired state events
    builder.addSource(
        "Desired State Events", // name
        Serdes.String().deserializer(),
        JsonSerdes.TurbineState().deserializer(),
        "desired-state-events"); // topic

    // add a source processor for reported state events
    builder.addSource(
        "Reported State Events", // name
        Serdes.String().deserializer(),
        JsonSerdes.TurbineState().deserializer(),
        "reported-state-events"); // topic

    // Note: if you need to rekey (which we don't for this tutorial), you would
    // 1: add a processor to perform the rekey
    //    builder.addProcessor("Rekey", RekeyProcessor::new, "Reported State Events", "Desired State
    // Events")
    // 2. add a sink to write to the repartition topic
    //    builder.addSink("Repartition Sink", "repartition-state-events", ...)
    // 3. add a new source processor to read from the repartition topic
    //    builder.addSource("Repartition source", ..., ..., "repartition-state-events")

    // builder.addGlobalStore(storeBuilder, sourceName, keyDeserializer, valueDeserializer, topic,

    // add a stateless high winds stream processor. this will create an extra
    // desired state event, with power == OFF (i.e. a shutdown signal) if the
    // wind speed reaches dangerous thresholds for operation
    builder.addProcessor(
        "High Winds Flatmap Processor", // name
        HighWindsFlatmapProcessor::new, // process supplier
        "Reported State Events"); // parent

    // add a stateful processor that saves digital twin records (comprised of a
    // reported and desired state) to a key-value store.
    // note: multiple parent nodes is essentially the same as a merge operator
    builder.addProcessor(
        "Digital Twin Processor", // name
        DigitalTwinProcessor::new, // process supplier
        "High Winds Flatmap Processor", // parents
        "Desired State Events");

    // state store for digital twin records
    StoreBuilder<KeyValueStore<String, DigitalTwin>> storeBuilder =
        Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore("digital-twin-store"),
            Serdes.String(),
            JsonSerdes.DigitalTwin());

    // add the state store to our topology and connect it to the "Digital Twin Processor"
    builder.addStateStore(storeBuilder, "Digital Twin Processor");

    // finally, add a sink processor named "Sink", which writes to an output topic
    // named "sensor-clean". Set the parent processor to the "Process" processor
    // publish device state for downstream analytics
    builder.addSink(
        "Digital Twin Sink", // name
        "digital-twins", // topic
        Serdes.String().serializer(),
        JsonSerdes.DigitalTwin().serializer(),
        "Digital Twin Processor"); // parent

    return builder;
  }
}
