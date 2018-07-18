/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.impl.internal;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;

public interface DisruptorConfig {

  /**
   * Interface permitting user customized creation of the LMAX Disruptor.
   *
   * @param mandatoryEventFactory event factory which must be supplied to the disruptor
   * @param defaultBufferSize recommended buffer size, must be a power of 2
   * @param defaultThreadFactory recommended thread factory
   * @param defaultProducerType recommended claim strategy - only select SINGLE when your
   *     application constrains the publisher implementations
   * @param defaultWaitStrategy recommended wait strategy: SleepingWaitStrategy.
   *     BlockingWaitStrategy is available for CPU constrained applications
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
