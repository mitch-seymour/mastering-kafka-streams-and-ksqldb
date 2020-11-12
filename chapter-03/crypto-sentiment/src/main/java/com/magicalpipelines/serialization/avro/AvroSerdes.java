package com.magicalpipelines.serialization.avro;

import com.magicalpipelines.model.EntitySentiment;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;

public class AvroSerdes {

  public static Serde<EntitySentiment> EntitySentiment(String url, boolean isKey) {
    Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", url);
    Serde<EntitySentiment> serde = new SpecificAvroSerde<>();
    serde.configure(serdeConfig, isKey);
    return serde;
  }
}
