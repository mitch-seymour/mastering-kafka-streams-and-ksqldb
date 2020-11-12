package com.magicalpipelines.serialization.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

class JsonSerializer<T> implements Serializer<T> {
  private Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();

  /** Default constructor needed by Kafka */
  public JsonSerializer() {}

  @Override
  public void configure(Map<String, ?> props, boolean isKey) {}

  @Override
  public byte[] serialize(String topic, T type) {
    return gson.toJson(type).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public void close() {}
}
