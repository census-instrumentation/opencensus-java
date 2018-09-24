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

package io.opencensus.exporter.trace.ocagent;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The implementation of the OpenCensus Agent (OC-Agent) Trace Exporter.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   OcAgentTraceExporter.createAndRegister();
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public final class OcAgentTraceExporter {

  private static final Object monitor = new Object();
  private static final String REGISTER_NAME = OcAgentTraceExporter.class.getName();

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  private OcAgentTraceExporter() {}

  /**
   * Creates a {@code OcAgentTraceExporterHandler} with default configurations and registers it to
   * the OpenCensus library.
   *
   * @since 0.17
   */
  public static void createAndRegister() {
    synchronized (monitor) {
      checkState(handler == null, "OC-Agent exporter is already registered.");
      OcAgentTraceExporterHandler newHandler = new OcAgentTraceExporterHandler();
      registerInternal(newHandler);
    }
  }

  /**
   * Creates a {@code OcAgentTraceExporterHandler} with the given configurations and registers it to
   * the OpenCensus library.
   *
   * @param configuration the {@code OcAgentTraceExporterConfiguration}.
   * @since 0.17
   */
  public static void createAndRegister(OcAgentTraceExporterConfiguration configuration) {
    synchronized (monitor) {
      checkState(handler == null, "OC-Agent exporter is already registered.");
      OcAgentTraceExporterHandler newHandler =
          new OcAgentTraceExporterHandler(
              configuration.getHost(),
              configuration.getPort(),
              configuration.getServiceName(),
              configuration.getUseInsecure());
      registerInternal(newHandler);
    }
  }

  /**
   * Registers the {@code OcAgentTraceExporterHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  private static void registerInternal(Handler newHandler) {
    synchronized (monitor) {
      handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  /**
   * Unregisters the OC-Agent exporter from the OpenCensus library.
   *
   * @since 0.17
   */
  public static void unregister() {
    unregister(Tracing.getExportComponent().getSpanExporter());
  }

  /**
   * Unregisters the {@code OcAgentTraceExporterHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }
}
