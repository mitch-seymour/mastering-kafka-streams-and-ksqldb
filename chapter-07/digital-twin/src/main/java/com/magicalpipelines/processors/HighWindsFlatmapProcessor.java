package com.magicalpipelines.processors;

import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Power;
import com.magicalpipelines.model.TurbineState.Type;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighWindsFlatmapProcessor
    implements Processor<String, TurbineState, String, TurbineState> {
  private static final Logger log = LoggerFactory.getLogger(HighWindsFlatmapProcessor.class);

  private ProcessorContext<String, TurbineState> context;

  @Override
  public void init(ProcessorContext<String, TurbineState> context) {
    // keep the processor context locally because we need it in punctuate() and commit()
    this.context = context;
  }

  @Override
  public void process(Record<String, TurbineState> record) {
    TurbineState reported = record.value();
    // always forward the original, reported state
    context.forward(record);

    // also forward a new desired, shutdown state if the wind speeds are too high
    if (reported.getWindSpeedMph() > 65 && reported.getPower() == Power.ON) {
      log.info("high winds detected. sending shutdown signal");
      TurbineState desired = TurbineState.clone(reported);
      desired.setPower(Power.OFF);
      desired.setType(Type.DESIRED);
      // forward the record to all downstream processors
      Record<String, TurbineState> newRecord =
          new Record<>(record.key(), desired, record.timestamp());
      context.forward(newRecord);

      // if you wanted to forward to a specific downstream processor,
      // e.g. a processor named "some-child-node", you could use the
      // following code instead
      // context.forward(newRecord, "some-child-node");
    }
  }

  @Override
  public void close() {
    // nothing to do
  }
}
