package com.magicalpipelines;

import io.opencensus.metrics.DerivedDoubleGauge;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.MetricOptions;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.Metrics;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that adds OpenCensus gauges for all Kafka Streams metrics. To use this class, add this to
 * your app's streaming properties, like so:
 *
 * <pre>{@code
 * streamingProps.put(METRIC_REPORTER_CLASSES_CONFIG, KafkaStreamsMetrics.class.getName());
 * }</pre>
 */
public class KafkaStreamsMetrics implements MetricsReporter {
  private static Logger logger = LoggerFactory.getLogger(KafkaStreamsMetrics.class);
  private static Map<String, KafkaMetric> metrics = new HashMap<>();

  /** Configures this class with the given key-value pairs. */
  public void configure(Map<String, ?> configs) {
    // We don't currently need any of the Kafka configuration values for our custom reporter
  }

  /**
   * This is called when the reporter is first registered to initially register all existing metrics
   *
   * @param metrics all currently existing metrics
   */
  @Override
  public void init(List<KafkaMetric> metrics) {
    // no init currently needed
  }

  /**
   * This is called whenever a metric is added by the underlying Kafka client libs.
   *
   * @param metric the metric that was updated
   */
  @Override
  public void metricChange(KafkaMetric metric) {
    MetricName metricName = metric.metricName();
    String metricId = getMetricId(metricName);
    if (metrics.containsKey(metricId)) {
      // remove the old KafkaMetric instance
      metrics.remove(metricId);
    }
    // add the new KafkaMetric instance
    metrics.put(metricId, metric);
    createGauge(metricId, metric.metricName());
  }

  /**
   * Called whenever a Kafka metric is removed.
   *
   * @param metric the metric that was removed
   */
  @Override
  public void metricRemoval(KafkaMetric metric) {
    metrics.remove(getMetricId(metric.metricName()));
  }

  /** Called when the metrics repository is closed. */
  @Override
  public void close() {
    metrics = new HashMap<>();
  }

  /**
   * Get a unique identifier for the supplied Kafka Metric
   *
   * @param metricName The Kafka metric name
   * @return A unique metric identifier
   */
  private String getMetricId(MetricName metricName) {
    String group = metricName.group().replace("-", "_");
    String name = metricName.name().replace("-", "_");
    return String.format("%s_%s", group, name);
  }

  /**
   * Create an OpenCensus gauge for a Kafka metric
   *
   * @param metricId A unique identifier for the Kafka metric
   * @param metricName The metric name
   */
  private void createGauge(String metricId, MetricName metricName) {
    logger.debug("Creating new gauge: " + metricId);
    MetricRegistry metricRegistry = Metrics.getMetricRegistry();
    /**
     * Label keys and values are used to uniquely identify metrics in OpenCensus. We could have also
     * used the metric ID here, but it will be easier to filter metrics if we create a label for the
     * metric group and name instead of an ID string
     */
    List<LabelKey> labelKeys =
        Arrays.asList(
            LabelKey.create("group", "Kafka metric group"),
            LabelKey.create("name", "Kafka metric name"));
    List<LabelValue> labelValues =
        Arrays.asList(
            // these values correspond to the keys above
            LabelValue.create(metricName.group()), LabelValue.create(metricName.name()));

    try {
      /**
       * We use a derived double gauge since we want to execute a callback everytime the gauge value
       * needs to be updated. We could also use a DoubleGauge, but this is a little more complicated
       * since we would need to track both the KafkaMetric object that the Kafka client libs give us
       * in the {@link KafkaStreamsMetrics#value()} callback, and also the DoublePoint object that
       * OpenCensus gives us when initializing the gauge
       */
      DerivedDoubleGauge gauge =
          metricRegistry.addDerivedDoubleGauge(
              metricId,
              MetricOptions.builder()
                  .setDescription(metricName.description())
                  .setUnit("")
                  .setLabelKeys(labelKeys)
                  .build());
      gauge.removeTimeSeries(labelValues);
      gauge.createTimeSeries(labelValues, metrics.get(metricId), this::getMetricValue);
    } catch (IllegalArgumentException e) {
      // This usually occurs if a different metric with the same name is already registered
      logger.debug("Gauge could not be created.", e);
    }
  }

  @SuppressWarnings("deprecation")
  /**
   * Get the current value of a Kafka metric
   *
   * @param metric The Kafka metric that we are getting the value for
   * @return a double representing the current value of this metric
   */
  private double getMetricValue(KafkaMetric metric) {
    /**
     * This method is deprecated but it's replacement, metric.metricValue(), is garbage. it returns
     * a generic Object instead of a Double, and this generic object is occassionally a String.
     * there's also not a good way to filter out metrics that return non-double values, so we'll
     * just stick to the deprecated method for now
     */
    if (metric == null) {
      return 0.0;
    }
    Double value = metric.value();
    if (Double.isNaN(value)) {
      return 0.0;
    }
    return value;
  }
}
