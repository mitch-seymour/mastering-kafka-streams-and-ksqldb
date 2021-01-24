package com.magicalpipelines;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

class App {
  public static void runTopology(Topology topology) {
    // set the required properties for running Kafka Streams
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev-consumer");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    // build the topology
    System.out.println("Starting Application");
    KafkaStreams streams = new KafkaStreams(topology, props);

    // state listener example
    streams.setStateListener(
        (oldState, newState) -> {
          if (newState.equals(State.REBALANCING)) {
            // do something
          }
        });

    // state restore listener example
    streams.setGlobalStateRestoreListener(new MyRestoreListener());

    // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    // clean up local state since many of the tutorials write to the same location
    // you should run this sparingly in production since it will force the state
    // store to be rebuilt on start up
    streams.cleanUp();

    // start streaming
    streams.start();
  }
}
