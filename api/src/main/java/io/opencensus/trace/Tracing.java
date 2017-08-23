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

package io.opencensus.trace;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Clock;
import io.opencensus.internal.Provider;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class that manages a global instance of the {@link TraceComponent}. */
public final class Tracing {
  private static final Logger logger = Logger.getLogger(Tracing.class.getName());
  private static final TraceComponent traceComponent =
      loadTraceComponent(TraceComponent.class.getClassLoader());

  /**
   * Returns the global {@link Tracer}.
   *
   * @return the global {@code Tracer}.
   */
  public static Tracer getTracer() {
    return traceComponent.getTracer();
  }

  /**
   * Returns the global {@link PropagationComponent}.
   *
   * @return the global {@code PropagationComponent}.
   */
  public static PropagationComponent getPropagationComponent() {
    return traceComponent.getPropagationComponent();
  }

  /**
   * Returns the global {@link Clock}.
   *
   * @return the global {@code Clock}.
   */
  public static Clock getClock() {
    return traceComponent.getClock();
  }

  /**
   * Returns the global {@link ExportComponent}.
   *
   * @return the global {@code ExportComponent}.
   */
  public static ExportComponent getExportComponent() {
    return traceComponent.getExportComponent();
  }

  /**
   * Returns the global {@link TraceConfig}.
   *
   * @return the global {@code TraceConfig}.
   */
  public static TraceConfig getTraceConfig() {
    return traceComponent.getTraceConfig();
  }

  // Any provider that may be used for TraceComponent can be added here.
  @VisibleForTesting
  static TraceComponent loadTraceComponent(ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.impl.trace.TraceComponentImpl", true, classLoader),
          TraceComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for TraceComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.impllite.trace.TraceComponentImplLite", true, classLoader),
          TraceComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for TraceComponent, now using "
              + "default implementation for TraceComponent.",
          e);
    }
    return TraceComponent.getNoopTraceComponent();
  }

  // No instance of this class.
  private Tracing() {}
}
