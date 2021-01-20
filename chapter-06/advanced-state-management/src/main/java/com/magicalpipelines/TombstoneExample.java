package com.magicalpipelines;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

class TombstoneExample {

  public static void main(String[] args) {
    Topology topology = getTopology();
    App.runTopology(topology);
  }

  public static Topology getTopology() {
    final String PATIENT_CHECKED_OUT = "check-out";

    StreamsBuilder builder = new StreamsBuilder();
    KStream<byte[], String> stream = builder.stream("patient-events");

    stream
        .groupByKey()
        .reduce(
            (value1, value2) -> {
              if (value2.equals(PATIENT_CHECKED_OUT)) {
                // create a tombstone
                System.out.println("Creating tombstone for " + value2);
                return null;
              }
              System.out.println("Returning aggregation for " + value2);
              return doSomething(value1, value2);
            });

    return builder.build();
  }

  public static String doSomething(String v1, String v2) {
    return "test";
  }
}
