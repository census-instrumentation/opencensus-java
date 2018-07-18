package io.opencensus.impl.internal;

import java.util.concurrent.ThreadFactory;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public interface DisruptorConfigSPI {

  /**
   * Interface permitting user customized creation of the LMAX Disruptor.
   *
   * @param mandatoryEventFactory event factory which must be supplied to the disruptor
   * @param defaultBufferSize recommended buffer size, must be a power of 2
   * @param defaultThreadFactory recommended thread factory
   * @param defaultProducerType recommended claim strategy - only select SINGLE when your
   * application constrains the publisher implementations
   * @param defaultWaitStrategy recommended wait strategy: SleepingWaitStrategy.
   * BlockingWaitStrategy is available for CPU constrained applications
   * @param <P> privately visible disruptor event type
   * @return a new Disruptor instance
   */
  <P> Disruptor<P> createDisruptor(
      EventFactory<P> mandatoryEventFactory,
      int defaultBufferSize,
      ThreadFactory defaultThreadFactory,
      ProducerType defaultProducerType,
      WaitStrategy defaultWaitStrategy);
}
