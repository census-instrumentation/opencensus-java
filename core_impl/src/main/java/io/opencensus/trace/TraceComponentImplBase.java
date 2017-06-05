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

package io.opencensus.trace;

import io.opencensus.common.Clock;
import io.opencensus.common.EventQueue;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.SpanImpl.StartEndHandler;

/** Base implementation of the {@link TraceComponent}. */
class TraceComponentImplBase extends TraceComponent {
  private final BinaryPropagationHandler binaryPropagationHandler =
      new BinaryPropagationHandlerImpl();
  private final TraceExporterImpl traceExporter = new TraceExporterImpl();
  private final Clock clock;
  private final StartEndHandler startEndHandler;
  private final TraceConfig traceConfig = new TraceConfigImpl();
  private final Tracer tracer;

  TraceComponentImplBase(Clock clock, RandomHandler randomHandler, EventQueue eventQueue) {
    this.clock = clock;
    startEndHandler =
        new StartEndHandlerImpl(traceExporter.getSampledSpansServiceExporter(), eventQueue);
    tracer = new TracerImpl(randomHandler, startEndHandler, clock, traceConfig);
  }

  @Override
  public Tracer getTracer() {
    return tracer;
  }

  @Override
  public BinaryPropagationHandler getBinaryPropagationHandler() {
    return binaryPropagationHandler;
  }

  @Override
  public final Clock getClock() {
    return clock;
  }

  @Override
  public TraceExporter getTraceExporter() {
    return traceExporter;
  }

  @Override
  public TraceConfig getTraceConfig() {
    return traceConfig;
  }
}
