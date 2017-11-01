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

package io.opencensus.contrib.agent.bootstrap;

import com.google.errorprone.annotations.MustBeClosed;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import java.io.Closeable;

/**
 * {@code TraceTrampoline} provides methods for creating and manipulating trace spans from
 * instrumented bytecode.
 *
 * <p>{@code TraceTrampoline} avoids tight coupling with the concrete trace API through the
 * {@link TraceStrategy} interface.
 *
 * <p>Both {@link TraceTrampoline} and {@link TraceStrategy} are loaded by the bootstrap
 * classloader so that they can be used from classes loaded by the bootstrap classloader.
 * A concrete implementation of {@link TraceStrategy} will be loaded by the system classloader.
 * This allows for using the same trace API as the instrumented application.
 *
 * <p>{@code TraceTrampoline} is implemented as a static class to allow for easy and fast use from
 * instrumented bytecode. We cannot use dependency injection for the instrumented bytecode.
 */
public final class TraceTrampoline {

  // Not synchronized to avoid any synchronization costs after initialization.
  // The agent is responsible for initializing this once (through #setTraceStrategy) before any
  // other method of this class is called.
  private static TraceStrategy traceStrategy;

  private TraceTrampoline() {
  }

  /**
   * Sets the concrete strategy for creating and manipulating trace spans.
   *
   * <p>NB: The agent is responsible for setting the trace strategy once before any other method
   * of this class is called.
   *
   * @param traceStrategy the concrete strategy for creating and manipulating trace spans
   */
  public static void setTraceStrategy(TraceStrategy traceStrategy) {
    if (TraceTrampoline.traceStrategy != null) {
      throw new IllegalStateException("traceStrategy was already set");
    }

    if (traceStrategy == null) {
      throw new NullPointerException("traceStrategy");
    }

    TraceTrampoline.traceStrategy = traceStrategy;
  }

  /**
   * Starts a new span and sets it as the current span.
   *
   * @param spanName the name of the returned {@link Span}
   * @return an object that defines a scope where the newly created {@code Span} will be set to the
   *     current Context
   *
   * @see Tracer#spanBuilder(java.lang.String)
   * @see SpanBuilder#startScopedSpan()
   */
  @MustBeClosed
  public static Closeable startScopedSpan(String spanName) {
    return traceStrategy.startScopedSpan(spanName);
  }
}
