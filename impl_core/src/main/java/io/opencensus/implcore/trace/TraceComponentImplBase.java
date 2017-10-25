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

package io.opencensus.implcore.trace;

import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.config.TraceConfigImpl;
import io.opencensus.implcore.trace.export.ExportComponentImpl;
import io.opencensus.implcore.trace.internal.RandomHandler;
import io.opencensus.implcore.trace.propagation.PropagationComponentImpl;
import io.opencensus.trace.TraceComponent;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;

/**
 * Helper class to allow sharing the code for all the {@link TraceComponent} implementations. This
 * class cannot use inheritance because in version 0.5.* the constructor of the {@code
 * TraceComponent} is package protected.
 *
 * <p>This can be changed back to inheritance when version 0.5.* is no longer supported.
 */
public final class TraceComponentImplBase {
  private final ExportComponentImpl exportComponent;
  private final PropagationComponent propagationComponent = new PropagationComponentImpl();
  private final Clock clock;
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
      exportComponent = ExportComponentImpl.createWithoutInProcessStores(eventQueue);
    } else {
      exportComponent = ExportComponentImpl.createWithInProcessStores(eventQueue);
    }
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            exportComponent.getSpanExporter(),
            exportComponent.getRunningSpanStore(),
            exportComponent.getSampledSpanStore(),
            eventQueue);
    tracer = new TracerImpl(randomHandler, startEndHandler, clock, traceConfig);
  }

  public Tracer getTracer() {
    return tracer;
  }

  public PropagationComponent getPropagationComponent() {
    return propagationComponent;
  }

  public final Clock getClock() {
    return clock;
  }

  public ExportComponent getExportComponent() {
    return exportComponent;
  }

  public TraceConfig getTraceConfig() {
    return traceConfig;
  }
}
