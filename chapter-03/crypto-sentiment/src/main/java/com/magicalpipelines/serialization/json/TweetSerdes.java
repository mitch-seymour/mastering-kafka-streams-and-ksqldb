package com.magicalpipelines.serialization.json;

import com.magicalpipelines.serialization.Tweet;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class TweetSerdes implements Serde<Tweet> {

  public Serializer<Tweet> serializer() {
    return new TweetSerializer();
  }

  public Deserializer<Tweet> deserializer() {
    return new TweetDeserializer();
  }
}
