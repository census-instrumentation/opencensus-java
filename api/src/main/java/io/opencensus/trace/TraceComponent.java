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

package io.opencensus.trace;

import io.opencensus.common.Clock;
import io.opencensus.internal.ZeroTimeClock;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;

/**
 * Class that holds the implementation instances for {@link Tracer}, {@link
 * PropagationComponent}, {@link Clock}, {@link ExportComponent} and {@link TraceConfig}.
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
   * Returns the {@link PropagationComponent} with the provided implementation. If no
   * implementation is provided then no-op implementation will be used.
   *
   * @return the {@code PropagationComponent} implementation.
   */
  public abstract PropagationComponent getPropagationComponent();

  /**
   * Returns the {@link Clock} with the provided implementation.
   *
   * @return the {@code Clock} implementation.
   */
  public abstract Clock getClock();

  /**
   * Returns the {@link ExportComponent} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link ExportComponent} implementation.
   */
  public abstract ExportComponent getExportComponent();

  /**
   * Returns the {@link TraceConfig} with the provided implementation. If no implementation is
   * provided then no-op implementations will be used.
   *
   * @return the {@link TraceConfig} implementation.
   */
  public abstract TraceConfig getTraceConfig();

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
    public PropagationComponent getPropagationComponent() {
      return PropagationComponent.getNoopPropagationComponent();
    }

    @Override
    public Clock getClock() {
      return ZeroTimeClock.getInstance();
    }

    @Override
    public ExportComponent getExportComponent() {
      return ExportComponent.getNoopExportComponent();
    }

    @Override
    public TraceConfig getTraceConfig() {
      return TraceConfig.getNoopTraceConfig();
    }

    private NoopTraceComponent() {}
  }
}
