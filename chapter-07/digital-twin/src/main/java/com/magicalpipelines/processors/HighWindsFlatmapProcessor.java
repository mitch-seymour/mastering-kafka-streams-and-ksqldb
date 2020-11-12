package com.magicalpipelines.processors;

import com.magicalpipelines.model.TurbineState;
import com.magicalpipelines.model.TurbineState.Power;
import com.magicalpipelines.model.TurbineState.Type;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighWindsFlatmapProcessor implements Processor<String, TurbineState> {
  private static final Logger log = LoggerFactory.getLogger(HighWindsFlatmapProcessor.class);

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    // keep the processor context locally because we need it in punctuate() and commit()
    this.context = context;
  }

  @Override
  public void process(String key, TurbineState reported) {
    // always forward the original, reported state
    context.forward(key, reported);

    // also forward a new desired, shutdown state if the wind speeds are too high
    if (reported.getWindSpeedMph() > 65 && reported.getPower() == Power.ON) {
      log.info("high winds detected. sending shutdown signal");
      TurbineState desired = TurbineState.clone(reported);
      desired.setPower(Power.OFF);
      desired.setType(Type.DESIRED);
      // forward the record to all downstream processors
      context.forward(key, desired);

      // if you wanted to forward to a specific downstream processor,
      // you could use to org.apache.kafka.streams.processor.To, as
      // shown below
      // context.forward(key, desired, To.child("some-child-node"));
    }
  }

  @Override
  public void close() {
    // nothing to do
  }
}
