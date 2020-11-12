package com.magicalpipelines;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Properties;

public class AppConfig {
  private static final Config config = ConfigFactory.load();

  private static Properties toProperties(Config config) {
    Properties props = new Properties();
    config.entrySet().forEach(e -> props.setProperty(e.getKey(), config.getString(e.getKey())));
    return props;
  }

  public static Properties getKafkaStreamsConfig() {
    Config streamsConfig = config.getConfig("kafka.streams");
    Properties streamingProps = toProperties(streamsConfig);
    return streamingProps;
  }
}
