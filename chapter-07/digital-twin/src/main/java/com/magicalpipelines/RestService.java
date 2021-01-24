package com.magicalpipelines;

import com.magicalpipelines.model.DigitalTwin;
import io.javalin.Javalin;
import io.javalin.http.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RestService {
  private final HostInfo hostInfo;
  private final KafkaStreams streams;

  private static final Logger log = LoggerFactory.getLogger(RestService.class);

  RestService(HostInfo hostInfo, KafkaStreams streams) {
    this.hostInfo = hostInfo;
    this.streams = streams;
  }

  ReadOnlyKeyValueStore<String, DigitalTwin> getStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            "digital-twin-store", QueryableStoreTypes.keyValueStore()));
  }

  void start() {
    Javalin app = Javalin.create().start(hostInfo.port());
    app.get("/devices/:id", this::getDevice);
  }

  void getDevice(Context ctx) {
    String deviceId = ctx.pathParam("id");

    // find out which host has the key
    KeyQueryMetadata metadata =
        streams.queryMetadataForKey("digital-twin-store", deviceId, Serdes.String().serializer());
    HostInfo activeHost = metadata.activeHost();

    // the local instance has this key
    if (hostInfo.equals(activeHost)) {
      log.info("Querying local store for key");
      DigitalTwin latestState = getStore().get(deviceId);

      if (latestState == null) {
        // digital twin record was not found
        ctx.status(404);
        return;
      }

      // digital twin record was found, so return it
      ctx.json(latestState);
      return;
    }

    // a remote instance has the key
    String url =
        String.format("http://%s:%s/devices/%s", activeHost.host(), activeHost.port(), deviceId);
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
