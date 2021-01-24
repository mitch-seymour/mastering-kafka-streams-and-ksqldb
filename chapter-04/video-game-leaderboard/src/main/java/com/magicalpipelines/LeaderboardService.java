package com.magicalpipelines;

import com.magicalpipelines.model.join.Enriched;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LeaderboardService {
  private final HostInfo hostInfo;
  private final KafkaStreams streams;

  private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);

  LeaderboardService(HostInfo hostInfo, KafkaStreams streams) {
    this.hostInfo = hostInfo;
    this.streams = streams;
  }

  ReadOnlyKeyValueStore<String, HighScores> getStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            // state store name
            "leader-boards",
            // state store type
            QueryableStoreTypes.keyValueStore()));
  }

  void start() {
    Javalin app = Javalin.create().start(hostInfo.port());

    /** Local key-value store query: all entries */
    app.get("/leaderboard", this::getAll);

    /** Local key-value store query: approximate number of entries */
    app.get("/leaderboard/count", this::getCount);

    /** Local key-value store query: approximate number of entries */
    app.get("/leaderboard/count/local", this::getCountLocal);

    /** Local key-value store query: range scan (inclusive) */
    app.get("/leaderboard/:from/:to", this::getRange);

    /** Local key-value store query: point-lookup / single-key lookup */
    app.get("/leaderboard/:key", this::getKey);
  }

  void getAll(Context ctx) {
    Map<String, List<Enriched>> leaderboard = new HashMap<>();

    KeyValueIterator<String, HighScores> range = getStore().all();
    while (range.hasNext()) {
      KeyValue<String, HighScores> next = range.next();
      String game = next.key;
      HighScores highScores = next.value;
      leaderboard.put(game, highScores.toList());
    }
    // close the iterator to avoid memory leaks!
    range.close();
    // return a JSON response
    ctx.json(leaderboard);
  }

  void getRange(Context ctx) {
    String from = ctx.pathParam("from");
    String to = ctx.pathParam("to");

    Map<String, List<Enriched>> leaderboard = new HashMap<>();

    KeyValueIterator<String, HighScores> range = getStore().range(from, to);
    while (range.hasNext()) {
      KeyValue<String, HighScores> next = range.next();
      String game = next.key;
      HighScores highScores = next.value;
      leaderboard.put(game, highScores.toList());
    }
    // close the iterator to avoid memory leaks!
    range.close();
    // return a JSON response
    ctx.json(leaderboard);
  }

  void getCount(Context ctx) {
    long count = getStore().approximateNumEntries();

    for (StreamsMetadata metadata : streams.allMetadataForStore("leader-boards")) {
      if (!hostInfo.equals(metadata.hostInfo())) {
        continue;
      }
      count += fetchCountFromRemoteInstance(metadata.hostInfo().host(), metadata.hostInfo().port());
    }

    ctx.json(count);
  }

  long fetchCountFromRemoteInstance(String host, int port) {
    OkHttpClient client = new OkHttpClient();

    String url = String.format("http://%s:%d/leaderboard/count/local", host, port);
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      return Long.parseLong(response.body().string());
    } catch (Exception e) {
      // log error
      log.error("Could not get leaderboard count", e);
      return 0L;
    }
  }

  void getCountLocal(Context ctx) {
    long count = 0L;
    try {
      count = getStore().approximateNumEntries();
    } catch (Exception e) {
      log.error("Could not get local leaderboard count", e);
    } finally {
      ctx.result(String.valueOf(count));
    }
  }

  void getKey(Context ctx) {
    String productId = ctx.pathParam("key");

    // find out which host has the key
    KeyQueryMetadata metadata =
        streams.queryMetadataForKey("leader-boards", productId, Serdes.String().serializer());

    // the local instance has this key
    if (hostInfo.equals(metadata.activeHost())) {
      log.info("Querying local store for key");
      HighScores highScores = getStore().get(productId);

      if (highScores == null) {
        // game was not found
        ctx.status(404);
        return;
      }

      // game was found, so return the high scores
      ctx.json(highScores.toList());
      return;
    }

    // a remote instance has the key
    String remoteHost = metadata.activeHost().host();
    int remotePort = metadata.activeHost().port();
    String url =
        String.format(
            "http://%s:%d/leaderboard/%s",
            // params
            remoteHost, remotePort, productId);

    // issue the request
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      log.info("Querying remote store for key");
      ctx.result(response.body().string());
    } catch (Exception e) {
      ctx.status(500);
    }
  }
}
