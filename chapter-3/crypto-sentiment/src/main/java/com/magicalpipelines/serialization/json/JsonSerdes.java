package com.magicalpipelines.serialization.json;

import com.magicalpipelines.serialization.Tweet;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {
  public static Serde<Tweet> Tweet() {
    return Serdes.serdeFrom(new TweetSerializer(), new TweetDeserializer());
  }
}
