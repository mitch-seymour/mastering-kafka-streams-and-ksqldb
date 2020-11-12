package com.magicalpipelines;

import com.magicalpipelines.model.Player;
import com.magicalpipelines.model.Product;
import com.magicalpipelines.model.ScoreEvent;
import com.magicalpipelines.model.join.Enriched;
import com.magicalpipelines.model.join.ScoreWithPlayer;
import com.magicalpipelines.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.ValueJoiner;

class LeaderboardTopologyVersion2 {

  public static Topology build() {
    // the builder is used to construct the topology
    StreamsBuilder builder = new StreamsBuilder();

    // register the score events stream
    KStream<String, ScoreEvent> scoreEvents =
        builder
            .stream("score-events", Consumed.with(Serdes.ByteArray(), JsonSerdes.ScoreEvent()))
            // now marked for re-partitioning
            .selectKey((k, v) -> v.getPlayerId().toString());

    // create the sharded players table
    KTable<String, Player> players =
        builder.table("players", Consumed.with(Serdes.String(), JsonSerdes.Player()));

    // create the global product table
    GlobalKTable<String, Product> products =
        builder.globalTable("products", Consumed.with(Serdes.String(), JsonSerdes.Product()));

    // join params for scoreEvents -> players join
    Joined<String, ScoreEvent, Player> playerJoinParams =
        Joined.with(Serdes.String(), JsonSerdes.ScoreEvent(), JsonSerdes.Player());

    // join scoreEvents -> players
    ValueJoiner<ScoreEvent, Player, ScoreWithPlayer> scorePlayerJoiner =
        (score, player) -> new ScoreWithPlayer(score, player);
    KStream<String, ScoreWithPlayer> withPlayers =
        scoreEvents.join(players, scorePlayerJoiner, playerJoinParams);

    /**
     * map score-with-player records to products
     *
     * <p>Regarding the KeyValueMapper param types: - String is the key type for the score events
     * stream - ScoreWithPlayer is the value type for the score events stream - String is the lookup
     * key type
     */
    KeyValueMapper<String, ScoreWithPlayer, String> keyMapper =
        (leftKey, scoreWithPlayer) -> {
          return String.valueOf(scoreWithPlayer.getScoreEvent().getProductId());
        };

    // join the withPlayers stream to the product global ktable
    ValueJoiner<ScoreWithPlayer, Product, Enriched> productJoiner =
        (scoreWithPlayer, product) -> new Enriched(scoreWithPlayer, product);
    KStream<String, Enriched> withProducts = withPlayers.join(products, keyMapper, productJoiner);
    withProducts.print(Printed.<String, Enriched>toSysOut().withLabel("with-products"));

    /** Group the enriched product stream */
    KGroupedStream<String, Enriched> grouped =
        withProducts.groupBy(
            (key, value) -> value.getProductId().toString(),
            Grouped.with(Serdes.String(), JsonSerdes.Enriched()));
    // alternatively, use the following if you want to name the grouped repartition topic:
    // Grouped.with("grouped-enriched", Serdes.String(), JsonSerdes.Enriched()))

    /** The initial value of our aggregation will be a new HighScores instances */
    Initializer<HighScores> highScoresInitializer = HighScores::new;

    /** The logic for aggregating high scores is implemented in the HighScores.add method */
    Aggregator<String, Enriched, HighScores> highScoresAdder =
        (key, value, aggregate) -> aggregate.add(value);

    /** Perform the aggregation, and materialize the underlying state store for querying */
    KTable<String, HighScores> highScores =
        grouped.aggregate(highScoresInitializer, highScoresAdder);

    return builder.build();
  }
}
