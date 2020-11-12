package com.magicalpipelines;

import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.jmx.JmxCollector;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import javax.management.MalformedObjectNameException;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.*;

class App {

  static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    Topology topology = GreeterTopology.build();

    // load the Kafka Streams configuration
    Properties config = AppConfig.getKafkaStreamsConfig();

    // build the topology and start streaming
    log.info("Starting Kafka Streams");
    KafkaStreams streams = new KafkaStreams(topology, config);

    // register the shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    streams.start();

    // this is only needed if running the Prometheus server as an independent
    // HTTP server
    startPrometheusExporter();
  }

  private static void startPrometheusExporter() {
    log.info("Starting the metric exporter");
    log.info("Registering OpenCensus collectors");
    PrometheusStatsCollector.createAndRegister();
    MyStats.registerAllViews();
    try {
      /**
       * We're running the JMX collector using an independent HTTP server that we instantiate below.
       * Alternatively, we could also run the exporter as a Java agent (which is what we do for
       * ksqlDB since we don't have this level of control over the runtime). For the Java agent
       * version, see the note in the build.gradle file
       */
      log.info("Registering the JMX collector");
      new JmxCollector(new File("jmx_prometheus_exporter.yaml")).register();
      log.info("Creating the HTTP server for serving Prometheus metrics");
      new HTTPServer(new InetSocketAddress(9010), CollectorRegistry.defaultRegistry);
    } catch (IOException ioe) {
      log.error("Failed to start metrics exporter", ioe);
      System.exit(1);
    } catch (MalformedObjectNameException mone) {
      log.error("Malformed object name exception");
      System.exit(1);
    }
  }
}
