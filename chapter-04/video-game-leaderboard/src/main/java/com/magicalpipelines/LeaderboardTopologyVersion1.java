package com.magicalpipelines;

import com.magicalpipelines.model.Player;
import com.magicalpipelines.model.Product;
import com.magicalpipelines.model.ScoreEvent;
import com.magicalpipelines.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

class LeaderboardTopologyVersion1 {

  /**
   * This is the initial version of the topology we start with in Chapter 4.
   *
   * <p>We develop it further as we progress through the chapter (see the buildFinal method in this
   * class.
   */
  public static Topology build() {
    StreamsBuilder builder = new StreamsBuilder();

    KStream<byte[], ScoreEvent> scoreEvents =
        builder.stream("score-events", Consumed.with(Serdes.ByteArray(), JsonSerdes.ScoreEvent()));

    KTable<String, Player> players =
        builder.table("players", Consumed.with(Serdes.String(), JsonSerdes.Player()));

    GlobalKTable<String, Product> products =
        builder.globalTable("products", Consumed.with(Serdes.String(), JsonSerdes.Product()));

    scoreEvents.print(Printed.<byte[], ScoreEvent>toSysOut().withLabel("score-events"));
    players.toStream().print(Printed.<String, Player>toSysOut().withLabel("players"));
    // there's no print or toStream().print() option for GlobalKTables

    return builder.build();
  }
}
