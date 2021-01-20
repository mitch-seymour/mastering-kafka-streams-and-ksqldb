package com.magicalpipelines;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

class TopicConfigsExample {

  public static void main(String[] args) {
    Topology topology = getTopology();
    App.runTopology(topology);
  }

  public static Topology getTopology() {
    Map<String, String> topicConfigs = new HashMap<>();
    topicConfigs.put("segment.bytes", "536870912");
    topicConfigs.put("min.cleanable.dirty.ratio", "0.3");

    StreamsBuilder builder = new StreamsBuilder();
    KStream<byte[], String> stream = builder.stream("patient-events");

    KTable<byte[], Long> counts =
        stream
            .groupByKey()
            .count(
                Materialized.<byte[], Long, KeyValueStore<Bytes, byte[]>>as("counts")
                    .withKeySerde(Serdes.ByteArray())
                    .withValueSerde(Serdes.Long())
                    .withLoggingEnabled(topicConfigs));

    return builder.build();
  }
}
