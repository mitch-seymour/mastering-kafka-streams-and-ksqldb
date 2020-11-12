package com.magicalpipelines.serialization.json;

import com.magicalpipelines.model.BodyTemp;
import com.magicalpipelines.model.CombinedVitals;
import com.magicalpipelines.model.Pulse;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {

  public static Serde<Pulse> Pulse() {
    JsonSerializer<Pulse> serializer = new JsonSerializer<>();
    JsonDeserializer<Pulse> deserializer = new JsonDeserializer<>(Pulse.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<BodyTemp> BodyTemp() {
    JsonSerializer<BodyTemp> serializer = new JsonSerializer<>();
    JsonDeserializer<BodyTemp> deserializer = new JsonDeserializer<>(BodyTemp.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<CombinedVitals> CombinedVitals() {
    JsonSerializer<CombinedVitals> serializer = new JsonSerializer<>();
    JsonDeserializer<CombinedVitals> deserializer = new JsonDeserializer<>(CombinedVitals.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }
}
