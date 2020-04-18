package com.magicalpipelines.serialization.json;

import com.google.gson.Gson;
import com.magicalpipelines.serialization.Tweet;
import java.nio.charset.Charset;
import org.apache.kafka.common.serialization.Serializer;

class TweetSerializer implements Serializer<Tweet> {
  private Gson gson = new Gson();

  public TweetSerializer() {}

  @Override
  public byte[] serialize(String topic, Tweet tweet) {
    if (tweet == null) return null;
    return gson.toJson(tweet).getBytes(Charset.forName("UTF-8"));
  }
}
