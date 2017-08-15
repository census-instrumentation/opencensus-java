/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.impl.trace;

import io.opencensus.common.Clock;
import io.opencensus.impl.internal.EventQueue;
import io.opencensus.impl.internal.SimpleEventQueue;
import io.opencensus.impl.trace.SpanImpl.StartEndHandler;
import io.opencensus.impl.trace.config.TraceConfigImpl;
import io.opencensus.impl.trace.export.ExportComponentImpl;
import io.opencensus.impl.trace.internal.RandomHandler;
import io.opencensus.impl.trace.propagation.PropagationComponentImpl;
import io.opencensus.trace.TraceComponent;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;

/** Base implementation of the {@link TraceComponent}. */
public class TraceComponentImplBase extends TraceComponent {
  private final ExportComponentImpl exportComponent;
  private final PropagationComponent propagationComponent = new PropagationComponentImpl();
  private final Clock clock;
  private final StartEndHandler startEndHandler;
  private final TraceConfig traceConfig = new TraceConfigImpl();
  private final Tracer tracer;

  /**
   * Creates a new {@code TraceComponentImplBase}.
   *
   * @param clock the clock to use throughout tracing.
   * @param randomHandler the random number generator for generating trace and span IDs.
   * @param eventQueue the queue implementation.
   */
  public TraceComponentImplBase(Clock clock, RandomHandler randomHandler, EventQueue eventQueue) {
    this.clock = clock;
    // TODO(bdrutu): Add a config/argument for supportInProcessStores.
    if (eventQueue instanceof SimpleEventQueue) {
      exportComponent = ExportComponentImpl.createWithoutInProcessStores();
    } else {
      exportComponent = ExportComponentImpl.createWithInProcessStores();
    }
    startEndHandler =
        new StartEndHandlerImpl(
            exportComponent.getSpanExporter(),
            exportComponent.getRunningSpanStore(),
            exportComponent.getSampledSpanStore(),
            eventQueue);
    tracer = new TracerImpl(randomHandler, startEndHandler, clock, traceConfig);
  }

  @Override
  public Tracer getTracer() {
    return tracer;
  }

  @Override
  public PropagationComponent getPropagationComponent() {
    return propagationComponent;
  }

  @Override
  public final Clock getClock() {
    return clock;
  }

  @Override
  public ExportComponent getExportComponent() {
    return exportComponent;
  }

  @Override
  public TraceConfig getTraceConfig() {
    return traceConfig;
  }
}
