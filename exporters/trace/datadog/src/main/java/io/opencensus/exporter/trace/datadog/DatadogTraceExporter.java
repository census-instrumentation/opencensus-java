/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.trace.datadog;

import static com.google.common.base.Preconditions.checkState;

import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import java.net.MalformedURLException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Datadog.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   DatadogTraceConfiguration config = DatadogTraceConfiguration.builder()
 * .setAgentEndpoint("http://localhost:8126/v0.3/traces")
 * .setService("myService")
 * .setType("web")
 * .build();
 * DatadogTraceExporter.createAndRegister(config);
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.19
 */
public final class DatadogTraceExporter {

  private static final Object monitor = new Object();
  private static final String REGISTER_NAME = DatadogTraceExporter.class.getName();

  @GuardedBy("monitor")
  @Nullable
  private static SpanExporter.Handler handler = null;

  private DatadogTraceExporter() {}

  /**
   * Creates and registers the Datadog Trace exporter to the OpenCensus library. Only one Datadog
   * exporter can be registered at any point.
   *
   * @param configuration the {@code DatadogTraceConfiguration} used to create the exporter.
   * @throws MalformedURLException if the agent URL is invalid.
   * @since 0.19
   */
  public static void createAndRegister(DatadogTraceConfiguration configuration)
      throws MalformedURLException {
    synchronized (monitor) {
      checkState(handler == null, "Datadog exporter is already registered.");

      String agentEndpoint = configuration.getAgentEndpoint();
      String service = configuration.getService();
      String type = configuration.getType();

      final DatadogExporterHandler exporterHandler =
          new DatadogExporterHandler(agentEndpoint, service, type);
      handler = exporterHandler;
      Tracing.getExportComponent()
          .getSpanExporter()
          .registerHandler(REGISTER_NAME, exporterHandler);
    }
  }

  /**
   * Unregisters the Datadog Trace exporter from the OpenCensus library.
   *
   * @throws IllegalStateException if a Datadog exporter is not registered.
   * @since 0.19
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null, "Datadog exporter is not registered.");
      Tracing.getExportComponent().getSpanExporter().unregisterHandler(REGISTER_NAME);
      handler = null;
    }
  }
}
