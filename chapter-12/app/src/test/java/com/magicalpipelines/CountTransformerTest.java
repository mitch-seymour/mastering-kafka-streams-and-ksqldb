package com.magicalpipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CountTransformerTest {
  MockProcessorContext processorContext;

  @BeforeEach
  public void setup() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
    processorContext = new MockProcessorContext(props);

    KeyValueStore<String, Long> store =
        Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore("my-store"), Serdes.String(), Serdes.Long())
            .withLoggingDisabled()
            .build();

    store.init(processorContext, store);
    processorContext.register(store, null);
  }

  @Test
  public void testTransformer() {
    String key = "123";
    String value = "some value";
    CountTransformer transformer = new CountTransformer();
    transformer.init(processorContext);
    assertThat(transformer.transform(key, value)).isEqualTo(1L);
    assertThat(transformer.transform(key, value)).isEqualTo(2L);
    assertThat(transformer.transform(key, value)).isEqualTo(3L);
    assertThat(transformer.transform(key, null)).isNull();
    assertThat(transformer.transform(key, value)).isEqualTo(1L);
  }
}
