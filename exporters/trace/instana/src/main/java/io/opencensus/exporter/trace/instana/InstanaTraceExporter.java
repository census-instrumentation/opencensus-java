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

package io.opencensus.exporter.trace.instana;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Instana.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   InstanaTraceExporter.createAndRegister("http://localhost:42699/com.instana.plugin.generic.trace");
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.12
 */
public final class InstanaTraceExporter {

  private static final String REGISTER_NAME = InstanaTraceExporter.class.getName();
  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  private InstanaTraceExporter() {}

  /**
   * Creates and registers the Instana Trace exporter to the OpenCensus library. Only one Instana
   * exporter can be registered at any point.
   *
   * @param agentEndpoint Ex http://localhost:42699/com.instana.plugin.generic.trace
   * @throws MalformedURLException if the agentEndpoint is not a valid http url.
   * @throws IllegalStateException if a Instana exporter is already registered.
   * @since 0.12
   */
  public static void createAndRegister(String agentEndpoint) throws MalformedURLException {
    synchronized (monitor) {
      checkState(handler == null, "Instana exporter is already registered.");
      Handler newHandler = new InstanaExporterHandler(new URL(agentEndpoint));
      handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  /**
   * Registers the {@code InstanaTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  /**
   * Unregisters the Instana Trace exporter from the OpenCensus library.
   *
   * @throws IllegalStateException if a Instana exporter is not registered.
   * @since 0.12
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null, "Instana exporter is not registered.");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@code InstanaTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }
}
