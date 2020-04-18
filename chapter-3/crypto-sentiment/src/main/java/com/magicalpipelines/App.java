package com.magicalpipelines;

import java.util.Properties;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

class App {
  public static void main(String[] args) {
    Topology topology = CryptoTopology.build();

    // set the required properties for running Kafka Streams
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev-consumer");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

    // config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
    // config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
    // config.put("schema.registry.url", "http://localhost:8081");

    // build the topology and start streaming!
    System.out.println("Starting Twitter streams");
    KafkaStreams streams = new KafkaStreams(topology, config);
    streams.start();
  }
}
