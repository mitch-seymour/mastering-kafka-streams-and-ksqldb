package com.magicalpipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.magicalpipelines.model.DigitalTwin;
import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Power;
import com.magicalpipelines.model.TurbineState.Type;
import com.magicalpipelines.serialization.json.JsonSerdes;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProcessorAppTest {

  @Test
  @DisplayName("1 + 1 = 2")
  void addTwoNumbers() {
    // instantiate topology
    Topology topology = ProcessorApp.getTopology();

    // create a test driver. we will use this to pipe data to our
    // topology
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
    TopologyTestDriver testDriver = new TopologyTestDriver(topology, props);

    // create a test input topic
    TestInputTopic<String, TurbineState> reportedStatesInputTopic =
        testDriver.createInputTopic(
            "reported-state-events",
            Serdes.String().serializer(),
            JsonSerdes.TurbineState().serializer());

    TurbineState ts = new TurbineState("2020-11-23T09:02:00.000Z", 68.0, Power.ON, Type.REPORTED);
    reportedStatesInputTopic.pipeInput("1", ts);

    TestOutputTopic<String, DigitalTwin> outputTopic =
        testDriver.createOutputTopic(
            "digital-twins",
            Serdes.String().deserializer(),
            JsonSerdes.DigitalTwin().deserializer());
    // TODO: use assertJ
    // List<KeyValue<String, DigitalTwin>> list = outputTopic.readKeyValuesToList();
    System.out.println("\n\n\n\nKey values!" + outputTopic.readKeyValue());

    // assertEquals(outputTopic.readKeyValue(), new KeyValue<>("key", null));

    // assertEquals(1, 2, "1 + 1 should equal 2");
  }

  @Test
  @DisplayName("1 + 1 = 2")
  void placeholderUnitTest() {
    int expected = 2;
    int actual = 2;
    assertEquals(expected, actual, "1 + 1 should equal 2");
  }
}
