/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.metrics;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.internal.DefaultVisibilityForTesting;
import io.opencensus.internal.Provider;
import io.opencensus.metrics.export.ExportComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

@ExperimentalApi
public final class Metrics {
  private static final Logger logger = Logger.getLogger(Metrics.class.getName());
  private static final MetricsComponent metricsComponent =
      loadMetricsComponent(MetricsComponent.class.getClassLoader());

  /**
   * Returns the global {@link ExportComponent}.
   *
   * @return the global {@code ExportComponent}.
   * @since 0.16
   */
  public static ExportComponent getExportComponent() {
    return metricsComponent.getExportComponent();
  }

  // Any provider that may be used for TraceComponent can be added here.
  @DefaultVisibilityForTesting
  static MetricsComponent loadMetricsComponent(@Nullable ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName(
              "io.opencensus.impl.metrics.MetricsComponentImpl", /*initialize=*/ true, classLoader),
          MetricsComponent.class);
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
          Class.forName(
              "io.opencensus.impllite.metrics.MetricsComponentImplLite",
              /*initialize=*/ true,
              classLoader),
          MetricsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for TraceComponent, now using "
              + "default implementation for TraceComponent.",
          e);
    }
    return MetricsComponent.newNoopMetricsComponent();
  }

  private Metrics() {}
}
