package io.opencensus.impl.internal;

import java.util.concurrent.ThreadFactory;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorTestConfig implements DisruptorConfigSPI {

  @Override
  public <P> Disruptor<P> createDisruptor(
      EventFactory<P> mandatoryEventFactory,
      int defaultBufferSize,
      ThreadFactory defaultThreadFactory,
      ProducerType defaultProducerType,
      WaitStrategy defaultWaitStrategy) {

    return new Disruptor<>(mandatoryEventFactory, 128, defaultThreadFactory, defaultProducerType,
        defaultWaitStrategy);
  }
}
