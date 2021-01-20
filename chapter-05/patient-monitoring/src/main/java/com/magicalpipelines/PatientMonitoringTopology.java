package com.magicalpipelines;

import com.magicalpipelines.model.BodyTemp;
import com.magicalpipelines.model.CombinedVitals;
import com.magicalpipelines.model.Pulse;
import com.magicalpipelines.serialization.json.JsonSerdes;
import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PatientMonitoringTopology {
  private static final Logger log = LoggerFactory.getLogger(PatientMonitoringTopology.class);

  public static Topology build() {
    // the builder is used to construct the topology
    StreamsBuilder builder = new StreamsBuilder();

    // the following topology steps are numbered. these numbers correlate with
    // the topology design shown in the book (Chapter 5: Windows and Time)

    // 1.1
    Consumed<String, Pulse> pulseConsumerOptions =
        Consumed.with(Serdes.String(), JsonSerdes.Pulse())
            .withTimestampExtractor(new VitalTimestampExtractor());

    KStream<String, Pulse> pulseEvents =
        // register the pulse-events stream
        builder.stream("pulse-events", pulseConsumerOptions);

    // 1.2
    Consumed<String, BodyTemp> bodyTempConsumerOptions =
        Consumed.with(Serdes.String(), JsonSerdes.BodyTemp())
            .withTimestampExtractor(new VitalTimestampExtractor());

    KStream<String, BodyTemp> tempEvents =
        // register the body-temp-events stream
        builder.stream("body-temp-events", bodyTempConsumerOptions);

    // turn pulse into a rate (bpm)
    TimeWindows tumblingWindow =
        TimeWindows.of(Duration.ofSeconds(60)).grace(Duration.ofSeconds(5));

    /*!
     * Examples of other windows (not needed for the tutorial) are commented
     * out below
     *
     * TimeWindows hoppingWindow =
     *     TimeWindows.of(Duration.ofSeconds(5)).advanceBy(Duration.ofSeconds(4));
     *
     * SessionWindows sessionWindow = SessionWindows.with(Duration.ofSeconds(5));
     *
     * JoinWindows joinWindow = JoinWindows.of(Duration.ofSeconds(5));
     *
     * SlidingWindows slidingWindow =
     *     SlidingWindows.withTimeDifferenceAndGrace(Duration.ofSeconds(5), Duration.ofSeconds(0));
     */

    KTable<Windowed<String>, Long> pulseCounts =
        pulseEvents
            // 2
            .groupByKey()
            // 3.1 - windowed aggregation
            .windowedBy(tumblingWindow)
            // 3.2 - windowed aggregation
            .count(Materialized.as("pulse-counts"))
            // 4
            .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded().shutDownWhenFull()));

    // 5.1
    // filter for any pulse that exceeds our threshold
    KStream<String, Long> highPulse =
        pulseCounts
            .toStream()
            // this peek operator is not included in the book, but was added
            // to this code example so you could view some additional information
            // when running the application locally :)
            .peek(
                (key, value) -> {
                  String id = new String(key.key());
                  Long start = key.window().start();
                  Long end = key.window().end();
                  log.info(
                      "Patient {} had a heart rate of {} between {} and {}", id, value, start, end);
                })
            // 5.1
            .filter((key, value) -> value >= 100)
            // 6
            .map(
                (windowedKey, value) -> {
                  return KeyValue.pair(windowedKey.key(), value);
                });

    // 5.2
    // filter for any temperature reading that exceeds our threshold
    KStream<String, BodyTemp> highTemp =
        tempEvents.filter(
            (key, value) ->
                value != null && value.getTemperature() != null && value.getTemperature() > 100.4);

    // looking for step 6? it's chained right after 5.1

    // 7
    StreamJoined<String, Long, BodyTemp> joinParams =
        StreamJoined.with(Serdes.String(), Serdes.Long(), JsonSerdes.BodyTemp());

    JoinWindows joinWindows =
        JoinWindows
            // timestamps must be 1 minute apart
            .of(Duration.ofSeconds(60))
            // tolerate late arriving data for up to 10 seconds
            .grace(Duration.ofSeconds(10));

    ValueJoiner<Long, BodyTemp, CombinedVitals> valueJoiner =
        (pulseRate, bodyTemp) -> new CombinedVitals(pulseRate.intValue(), bodyTemp);

    KStream<String, CombinedVitals> vitalsJoined =
        highPulse.join(highTemp, valueJoiner, joinWindows, joinParams);

    // 8
    vitalsJoined.to("alerts", Produced.with(Serdes.String(), JsonSerdes.CombinedVitals()));

    // debug only
    pulseCounts
        .toStream()
        .print(Printed.<Windowed<String>, Long>toSysOut().withLabel("pulse-counts"));
    highPulse.print(Printed.<String, Long>toSysOut().withLabel("high-pulse"));
    highTemp.print(Printed.<String, BodyTemp>toSysOut().withLabel("high-temp"));
    vitalsJoined.print(Printed.<String, CombinedVitals>toSysOut().withLabel("vitals-joined"));

    return builder.build();
  }
}
