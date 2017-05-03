/*
 * Copyright 2016, Google Inc.
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
import com.google.instrumentation.internal.ZeroTimeClock;

/**
 * Class that holds the implementation instances for {@link Tracer} and {@link
 * BinaryPropagationHandler}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 */
public abstract class TraceComponent {
  private static final NoopTraceComponent noopTraceComponent = new NoopTraceComponent();

  /**
   * Returns the {@link Tracer} with the provided implementations. If no implementation is provided
   * then no-op implementations will be used.
   *
   * @return the {@code Tracer} implementation.
   */
  public abstract Tracer getTracer();

  /**
   * Returns the {@link BinaryPropagationHandler} with the provided implementations. If no
   * implementation is provided then no-op implementation will be used.
   *
   * @return the {@code BinaryPropagationHandler} implementation.
   */
  public abstract BinaryPropagationHandler getBinaryPropagationHandler();

  /**
   * Returns the {@link Clock} with the provided implementation.
   *
   * @return the {@code Clock} implementation.
   */
  public abstract Clock getClock();

  /**
   * Returns the {@link TraceExporter} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link TraceExporter} implementation.
   */
  public abstract TraceExporter getTraceExporter();

  /**
   * Returns the {@link TraceConfig} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link TraceConfig} implementation.
   */
  public abstract TraceConfig getTraceConfig();

  // Disallow external overrides until we define the final API.
  TraceComponent() {}

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   */
  static TraceComponent getNoopTraceComponent() {
    return noopTraceComponent;
  }

  private static final class NoopTraceComponent extends TraceComponent {
    @Override
    public Tracer getTracer() {
      return Tracer.getNoopTracer();
    }

    @Override
    public BinaryPropagationHandler getBinaryPropagationHandler() {
      return BinaryPropagationHandler.getNoopBinaryPropagationHandler();
    }

    @Override
    public Clock getClock() {
      return ZeroTimeClock.getInstance();
    }

    @Override
    public TraceExporter getTraceExporter() {
      return TraceExporter.getNoopTraceExporter();
    }

    @Override
    public TraceConfig getTraceConfig() {
      return TraceConfig.getNoopTraceConfig();
    }

    private NoopTraceComponent() {}
  }
}
