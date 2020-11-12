package com.magicalpipelines;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.openjdk.jmh.annotations.*;

public class TopologyBench {
  @State(org.openjdk.jmh.annotations.Scope.Thread)
  public static class MyState {
    public TestInputTopic<Void, String> inputTopic;

    @Setup(Level.Trial)
    public void setupState() {
      Properties props = new Properties();
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
      props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

      // build the topology
      Topology topology = GreeterTopology.build();

      // create a test driver. we will use this to pipe data to our topology
      TopologyTestDriver testDriver = new TopologyTestDriver(topology, props);

      testDriver = new TopologyTestDriver(topology, props);

      // create the test input topic
      inputTopic =
          testDriver.createInputTopic(
              "users", Serdes.Void().serializer(), Serdes.String().serializer());
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  public void benchmarkTopology(MyState state) {
    state.inputTopic.pipeInput("Izzy");
  }
}
