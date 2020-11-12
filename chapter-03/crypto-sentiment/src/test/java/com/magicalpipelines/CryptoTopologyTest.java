package com.magicalpipelines;

import static org.assertj.core.api.Assertions.assertThat;

import com.magicalpipelines.language.DummyClient;
import com.magicalpipelines.model.EntitySentiment;
import com.magicalpipelines.serialization.Tweet;
import com.magicalpipelines.serialization.json.TweetSerdes;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessorAppTest {
  private TopologyTestDriver testDriver;
  private TestInputTopic<byte[], Tweet> inputTopic;
  private TestOutputTopic<byte[], EntitySentiment> outputTopic;

  @BeforeEach
  void setup() {
    // build the topology with a dummy client
    Topology topology = CryptoTopology.build(new DummyClient(), false);

    // create a test driver. we will use this to pipe data to our topology
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
    testDriver = new TopologyTestDriver(topology, props);

    // create the test input topic
    inputTopic =
        testDriver.createInputTopic(
            "tweets", Serdes.ByteArray().serializer(), new TweetSerdes().serializer());

    // create the test output topic
    outputTopic =
        testDriver.createOutputTopic(
            "crypto-sentiment",
            Serdes.ByteArray().deserializer(),
            AvroSerdes.get(EntitySentiment.class).deserializer());
  }

  @Test
  void sentimentEnrichment() {
    Tweet tweet = new Tweet();
    tweet.setCreatedAt(System.currentTimeMillis());
    tweet.setId(123L);
    tweet.setLang("en");
    tweet.setRetweet(false);
    tweet.setText("this shaky stock market has rekindled interest in both bitcoin and ethereum");

    inputTopic.pipeInput(new byte[] {}, tweet);

    assertThat(outputTopic.isEmpty()).isFalse();

    List<TestRecord<byte[], EntitySentiment>> outRecords = outputTopic.readRecordsToList();
    assertThat(outRecords).hasSize(2);

    EntitySentiment record1 = outRecords.get(0).getValue();
    EntitySentiment record2 = outRecords.get(1).getValue();

    assertThat(record1.getEntity()).isEqualTo("bitcoin");
    assertThat(record1.getSentimentScore()).isBetween(0.0, 1.0);
    assertThat(record1.getSentimentMagnitude()).isBetween(0.0, 1.0);

    assertThat(record2.getEntity()).isEqualTo("ethereum");
    assertThat(record2.getSentimentScore()).isBetween(0.0, 1.0);
    assertThat(record2.getSentimentMagnitude()).isBetween(0.0, 1.0);
  }
}
