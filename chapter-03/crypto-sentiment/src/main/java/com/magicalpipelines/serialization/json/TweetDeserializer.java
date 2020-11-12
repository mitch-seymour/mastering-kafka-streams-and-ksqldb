package com.magicalpipelines.serialization.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magicalpipelines.serialization.Tweet;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Deserializer;

public class TweetDeserializer implements Deserializer<Tweet> {
  private Gson gson =
      new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

  @Override
  public Tweet deserialize(String topic, byte[] bytes) {
    if (bytes == null) return null;
    return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), Tweet.class);
  }
}
