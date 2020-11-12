package com.magicalpipelines;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.*;

class GreeterTopology {
  static final Logger log = LoggerFactory.getLogger(GreeterTopology.class);

  public static String generateGreeting(String user) {
    return String.format("Hello %s", user);
  }

  public static String generateGreetingSlow(String user) {
    try {
      // simulate some long running step
      TimeUnit.MILLISECONDS.sleep(100);
      return String.format("Hello %s", user);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Chapter 12 - simple test
   *
   * @return
   */
  public static Topology build() {
    StreamsBuilder builder = new StreamsBuilder();

    builder
        // stream data from the users topic
        .stream("users", Consumed.with(Serdes.Void(), Serdes.String()))
        // don't greet Randy. we're mad at Randy
        .filterNot((key, value) -> value.toLowerCase().equals("randy"))
        // generate greetings for everyone except Randy
        .mapValues(GreeterTopology::generateGreeting)
        // write all of the greetings to the `greetings` topic
        .to("greetings", Produced.with(Serdes.Void(), Serdes.String()));

    return builder.build();
  }

  /** Chapter 12 - add slow computation */
  public static Topology buildTopologyWithSlowComputation() {
    StreamsBuilder builder = new StreamsBuilder();

    builder
        // stream data from the users topic
        .stream("users", Consumed.with(Serdes.Void(), Serdes.String()))
        // don't greet Randy. we're mad at Randy
        .filterNot((key, value) -> value.toLowerCase().equals("randy"))
        // generate greetings for everyone except Randy
        .mapValues(GreeterTopology::generateGreetingSlow)
        // write all of the greetings to the `greetings` topic
        .to("greetings", Produced.with(Serdes.Void(), Serdes.String()));

    return builder.build();
  }

  /** Chapter 12 - add metrics */
  public static Topology buildWithMetrics() {
    StreamsBuilder builder = new StreamsBuilder();

    KStream<Void, String> stream =
        builder.stream("users", Consumed.with(Serdes.Void(), Serdes.String()));

    stream.foreach(
        (key, value) -> {
          MyStats.markMessagesReceived();
        });

    // don't greet Randy. we're mad at Randy
    KStream<Void, String> everyoneExceptRandy =
        stream.filterNot((key, value) -> value.toLowerCase().equals("randy"));

    KStream<Void, String> greetings =
        everyoneExceptRandy.mapValues(
            (key, value) -> {
              MyStats.markMessagesKept();
              String greeting = String.format("Hello %s", value);
              return greeting;
            });

    greetings.to("greetings", Produced.with(Serdes.Void(), Serdes.String()));

    return builder.build();
  }
}
