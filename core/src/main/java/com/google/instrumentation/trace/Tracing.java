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

import com.google.common.annotations.VisibleForTesting;
import com.google.instrumentation.common.Clock;
import com.google.instrumentation.internal.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class that manages a global instance of the {@link TraceComponent}. */
public final class Tracing {
  private static final Logger logger = Logger.getLogger(Tracer.class.getName());
  private static final TraceComponent traceComponent =
      loadTraceComponent(Provider.getCorrectClassLoader(TraceComponent.class));

  /**
   * Returns the global {@link Tracer}.
   *
   * @return the global {@code Tracer}.
   */
  public static Tracer getTracer() {
    return traceComponent.getTracer();
  }

  /**
   * Returns the global {@link BinaryPropagationHandler}.
   *
   * @return the global {@code BinaryPropagationHandler}.
   */
  public static BinaryPropagationHandler getBinaryPropagationHandler() {
    return traceComponent.getBinaryPropagationHandler();
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
   * Returns the global {@link TraceExporter}.
   *
   * @return the global {@code TraceExporter}.
   */
  public static TraceExporter getTraceExporter() {
    return traceComponent.getTraceExporter();
  }

  // Any provider that may be used for TraceComponent can be added here.
  @VisibleForTesting
  static TraceComponent loadTraceComponent(ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("com.google.instrumentation.trace.TraceComponentImpl", true, classLoader),
          TraceComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Using default implementation for TraceComponent.", e);
    }
    return TraceComponent.getNoopTraceComponent();
  }

  // No instance of this class.
  private Tracing() {}
}
