package com.magicalpipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GreeterTopologyTest {
  private TopologyTestDriver testDriver;
  private TestInputTopic<Void, String> inputTopic;
  private TestOutputTopic<Void, String> outputTopic;

  @BeforeEach
  void setup() {
    // build the topology
    Topology topology = GreeterTopology.build();

    // create a test driver. we will use this to pipe data to our topology
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    testDriver = new TopologyTestDriver(topology, props);

    // create the test input topic
    inputTopic =
        testDriver.createInputTopic(
            "users", Serdes.Void().serializer(), Serdes.String().serializer());

    // create the test output topic
    outputTopic =
        testDriver.createOutputTopic(
            "greetings", Serdes.Void().deserializer(), Serdes.String().deserializer());
  }

  @AfterEach
  void teardown() {
    // make sure resources are cleaned up properly
    testDriver.close();
  }

  @Test
  @DisplayName("greetings topic should contain expected greeting")
  void testUsersGreeted() {
    // pipe the test record to our Kafka topic
    String value = "Izzy";

    inputTopic.pipeInput(value);

    assertThat(outputTopic.isEmpty()).isFalse();

    // save each record that appeared in the output topic to a list
    List<TestRecord<Void, String>> outRecords = outputTopic.readRecordsToList();

    // ensure the output topic contains exactly one record
    assertThat(outRecords).hasSize(1);

    // ensure the generated greeting is the expected value
    String greeting = outRecords.get(0).getValue();
    assertThat(greeting).isEqualTo("Hello Izzy");
  }

  @ParameterizedTest(name = "greetings topic should contain greeting for {0}? expected = {1}")
  @CsvSource({
    "Izzy, true",
    "Mitch, true",
    "Randy, false",
    "Elyse, true",
  })
  void testUsersGreeted(String value, boolean shouldProduceGreeting) {
    // pipe the test record to our Kafka topic
    inputTopic.pipeInput(value);

    // save each record that appeared in the output topic to a list
    List<TestRecord<Void, String>> outRecords = outputTopic.readRecordsToList();

    if (shouldProduceGreeting) {
      // if we expected a greeting, ensure the output topic only generated one
      // output record for a given input
      assertThat(outRecords).hasSize(1);
      // ensure the generated greeting is the expected value
      String greeting = outRecords.get(0).getValue();
      assertThat(greeting).isEqualTo("Hello " + value);
    } else {
      // if we did not expect a greeting, assert that no records appeared in
      // the output topic
      assertThat(outRecords).isEmpty();
    }
  }
}
