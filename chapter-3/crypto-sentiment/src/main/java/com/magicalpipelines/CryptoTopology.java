package com.magicalpipelines;

import com.magicalpipelines.language.DummyClient;
import com.magicalpipelines.language.GcpClient;
import com.magicalpipelines.language.LanguageClient;
import com.magicalpipelines.model.EntitySentiment;
import com.magicalpipelines.serialization.Tweet;
import com.magicalpipelines.serialization.json.JsonSerdes;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

class CryptoTopology {
  private static final List<String> currencies = Arrays.asList("bitcoin", "ethereum");

  public static Topology build() {
    if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
      return build(new GcpClient());
    }

    return build(new DummyClient());
  }

  public static Topology build(LanguageClient languageClient) {
    // the builder is used to construct the topology
    StreamsBuilder builder = new StreamsBuilder();

    KStream<byte[], Tweet> stream =
        builder.stream("tweets", Consumed.with(Serdes.ByteArray(), JsonSerdes.Tweet()));

    // filter out retweets
    KStream<byte[], Tweet> filtered =
        stream.filterNot(
            (key, tweet) -> {
              return tweet.isRetweet();
            });

    // match all tweets that specify English as the source language
    Predicate<byte[], Tweet> englishTweets = (key, tweet) -> tweet.getLang().equals("en");

    // match all other tweets
    Predicate<byte[], Tweet> allOtherTweets = (key, tweet) -> true;

    // branch based on tweet language
    KStream<byte[], Tweet>[] branches = filtered.branch(englishTweets, allOtherTweets);

    // English tweets
    KStream<byte[], Tweet> englishStream = branches[0];
    englishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-english"));

    // non-English tweets
    KStream<byte[], Tweet> nonEnglishStream = branches[1];
    nonEnglishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-non-english"));

    // for non-English tweets, translate the tweet text first.
    KStream<byte[], Tweet> translatedStream =
        nonEnglishStream.mapValues(
            (tweet) -> {
              return languageClient.translate(tweet, "en");
            });

    // merge the two streams
    KStream<byte[], Tweet> merged = englishStream.merge(translatedStream);

    // enrich with sentiment and salience scores
    KStream<byte[], EntitySentiment> enriched =
        merged.flatMapValues(
            (tweet) -> {
              // perform entity-level sentiment analysis
              List<EntitySentiment> results = languageClient.getEntitySentiment(tweet);

              // get an iterator for iterating through the results
              Iterator<EntitySentiment> it = results.iterator();

              // remove all entity results that don't match a currency
              while (it.hasNext()) {
                EntitySentiment entitySentiment = it.next();
                if (!currencies.contains(entitySentiment.getEntity())) {
                  // no currency match
                  it.remove();
                }
              }

              return results;
            });

    enriched.to(
        "crypto-sentiment",
        Produced.with(Serdes.ByteArray(), AvroSerdes.get(EntitySentiment.class)));

    return builder.build();
  }
}
