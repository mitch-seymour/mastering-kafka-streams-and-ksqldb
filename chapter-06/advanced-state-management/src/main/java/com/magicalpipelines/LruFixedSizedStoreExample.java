package com.magicalpipelines;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

class LruFixedSizedStoreExample {

  public static void main(String[] args) {
    Topology topology = getTopologyDsl();
    App.runTopology(topology);
  }

  public static Topology getTopologyDsl() {
    KeyValueBytesStoreSupplier storeSupplier = Stores.lruMap("counts", 10);

    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> stream = builder.stream("patient-events");

    stream
        .groupByKey()
        .count(
            Materialized.<String, Long>as(storeSupplier)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long()));

    return builder.build();
  }

  public static Topology getTopologyProcessorApi() {
    KeyValueBytesStoreSupplier storeSupplier = Stores.lruMap("counts", 10);
    StoreBuilder<KeyValueStore<String, Long>> lruStoreBuilder =
        Stores.keyValueStoreBuilder(storeSupplier, Serdes.String(), Serdes.Long());

    StreamsBuilder builder = new StreamsBuilder();
    builder.addStateStore(lruStoreBuilder);

    // ...

    return builder.build();
  }
}
