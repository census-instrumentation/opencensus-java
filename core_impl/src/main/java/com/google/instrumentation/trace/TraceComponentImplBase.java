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

/** Base implementation of the {@link TraceComponent}. */
public class TraceComponentImplBase extends TraceComponent {
  private static final Tracer tracer = Tracer.getNoopTracer();
  private static final BinaryPropagationHandler binaryPropagationHandler =
      BinaryPropagationHandlerImpl.INSTANCE;
  private final Clock clock;
  private final TraceExporter traceExporter = TraceExporter.getNoopTraceExporter();

  TraceComponentImplBase(Clock clock) {
    this.clock = clock;
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
}
