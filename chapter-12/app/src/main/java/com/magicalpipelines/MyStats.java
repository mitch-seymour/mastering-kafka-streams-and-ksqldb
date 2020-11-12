package com.magicalpipelines;

import io.opencensus.stats.*;
import io.opencensus.stats.Measure.MeasureLong;
import java.util.Collections;

public class MyStats {
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  private static final MeasureLong MESSAGES_KEPT =
      MeasureLong.create(
          "myapp_messages_kept",
          "the number of messages kept after the initial filtering step",
          "messages");

  private static final MeasureLong MESSAGES_RECEIVED =
      MeasureLong.create(
          "myapp_messages_received",
          "the number of messages consumed prior to any filtering",
          "messages");

  // Views definition
  private static final View[] views =
      new View[] {
        View.create(
            View.Name.create(MESSAGES_KEPT.getName()),
            MESSAGES_KEPT.getDescription(),
            MESSAGES_KEPT,
            Aggregation.Sum.create(),
            Collections.emptyList()),
        View.create(
            View.Name.create(MESSAGES_RECEIVED.getName()),
            MESSAGES_RECEIVED.getDescription(),
            MESSAGES_RECEIVED,
            Aggregation.Sum.create(),
            Collections.emptyList()),
      };

  private static volatile boolean viewsRegistered = false;

  public static void registerAllViews() {
    if (!viewsRegistered) {
      viewsRegistered = true;
      for (View view : views) Stats.getViewManager().registerView(view);
    }
  }

  public static void markMessagesKept() {
    statsRecorder.newMeasureMap().put(MESSAGES_KEPT, 1).record();
  }

  public static void markMessagesReceived() {
    statsRecorder.newMeasureMap().put(MESSAGES_RECEIVED, 1).record();
  }
}
