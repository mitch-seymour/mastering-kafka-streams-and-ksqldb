package com.magicalpipelines;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.HostInfo;

class App {
  public static void main(String[] args) {
    Topology topology = PatientMonitoringTopology.build();

    // we allow the following system properties to be overridden
    String host = System.getProperty("host");
    Integer port = Integer.parseInt(System.getProperty("port"));
    String stateDir = System.getProperty("stateDir");
    String endpoint = String.format("%s:%s", host, port);

    // set the required properties for running Kafka Streams
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev-consumer");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
    props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
    // an example of setting the timestamp extractor using a streams config
    // note that we override this in our topology implementation, this is
    // just here for demonstration purposes
    props.put(
        StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);

    // build the topology
    System.out.println("Starting Patient Monitoring Application");
    KafkaStreams streams = new KafkaStreams(topology, props);
    // close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    // clean up local state since many of the tutorials write to the same location
    // you should run this sparingly in production since it will force the state
    // store to be rebuilt on start up
    streams.cleanUp();

    // start streaming
    streams.start();

    // start the REST service
    HostInfo hostInfo = new HostInfo(host, port);
    RestService service = new RestService(hostInfo, streams);
    service.start();
  }
}
