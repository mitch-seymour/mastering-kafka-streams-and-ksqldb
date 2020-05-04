package com.magicalpipelines;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

class DslExample {

  public static void main(String[] args) {
    // the builder is used to construct the topology
    StreamsBuilder builder = new StreamsBuilder();

    // read from the source topic, "hello-world"
    KStream<String, String> stream = builder.stream("hello-world");

    // for each record that appears in the source topic,
    // print the value
    stream.foreach(
        (k, v) -> {
          System.out.println("(DSL) Hello, " + v);
        });

    // you can also print using the `print` operator
    // stream.print(Printed.<String, String>toSysOut().withLabel("source"));

    // set the required properties for running Kafka Streams
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev-consumer");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    // build the topology and start streaming!
    KafkaStreams streams = new KafkaStreams(builder.build(), config);
    streams.start();

    // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }
}
