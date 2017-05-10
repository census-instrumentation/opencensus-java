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

package com.google.instrumentation.trace;

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.common.EventQueue;

/** Base implementation of the {@link TraceComponent}. */
class TraceComponentImplBase extends TraceComponent {
  private static final int TRACE_EXPORTER_BUFFER_SIZE = 32;
  // Enforces that trace exporter exports data at least once every 2 seconds.
  private static final long TRACE_EXPORTER_SCHEDULE_DELAY_MS = 2000;
  private final BinaryPropagationHandler binaryPropagationHandler =
      new BinaryPropagationHandlerImpl();
  private final Clock clock;
  private final TraceExporter traceExporter;
  private final TraceConfig traceConfig = new TraceConfigImpl();
  private final Tracer tracer;

  TraceComponentImplBase(Clock clock, RandomHandler randomHandler, EventQueue eventQueue) {
    this.clock = clock;
    TraceExporterImpl traceExporterImpl = TraceExporterImpl.create(TRACE_EXPORTER_BUFFER_SIZE,
        TRACE_EXPORTER_SCHEDULE_DELAY_MS, eventQueue);
    traceExporter = traceExporterImpl;
    tracer = new TracerImpl(randomHandler, traceExporterImpl, clock, traceConfig);
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
