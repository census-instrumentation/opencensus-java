/*
 * Copyright 2016-17, OpenCensus Authors
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

public class DisruptorTestConfig implements DisruptorConfig {

  @Override
  public <P> Disruptor<P> createDisruptor(
      EventFactory<P> mandatoryEventFactory,
      int defaultBufferSize,
      ThreadFactory defaultThreadFactory,
      ProducerType defaultProducerType,
      WaitStrategy defaultWaitStrategy) {

    return new Disruptor<>(
        mandatoryEventFactory, 128, defaultThreadFactory, defaultProducerType, defaultWaitStrategy);
  }
}
