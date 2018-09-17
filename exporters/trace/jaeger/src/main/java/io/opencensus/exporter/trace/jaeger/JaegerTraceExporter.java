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

package io.opencensus.exporter.trace.jaeger;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.thriftjava.Process;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Jaeger. Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   JaegerTraceExporter.createAndRegister("http://127.0.0.1:14268/api/traces", "myservicename");
 *     ... // Do work.
 *   }
 * }</pre>
 *
 * @since 0.13
 */
public final class JaegerTraceExporter {
  private static final String REGISTER_NAME = JaegerTraceExporter.class.getName();
  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static SpanExporter.Handler handler = null;

  // Make constructor private to hide it from the API and therefore avoid users calling it.
  private JaegerTraceExporter() {}

  /**
   * Creates and registers the Jaeger Trace exporter to the OpenCensus library. Only one Jaeger
   * exporter can be registered at any point.
   *
   * @param thriftEndpoint the Thrift endpoint of your Jaeger instance, e.g.:
   *     "http://127.0.0.1:14268/api/traces"
   * @param serviceName the local service name of the process.
   * @throws IllegalStateException if a Jaeger exporter is already registered.
   * @since 0.13
   */
  public static void createAndRegister(final String thriftEndpoint, final String serviceName) {
    synchronized (monitor) {
      checkState(handler == null, "Jaeger exporter is already registered.");
      final SpanExporter.Handler newHandler = newHandler(thriftEndpoint, serviceName);
      JaegerTraceExporter.handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  /**
   * Creates and registers the Jaeger Trace exporter to the OpenCensus library using the provided
   * HttpSender. Only one Jaeger exporter can be registered at any point.
   *
   * @param httpSender the pre-configured HttpSender to use with the exporter
   * @param serviceName the local service name of the process.
   * @throws IllegalStateException if a Jaeger exporter is already registered.
   * @since 0.17
   */
  public static void createWithSender(final HttpSender httpSender, final String serviceName) {
    synchronized (monitor) {
      checkState(handler == null, "Jaeger exporter is already registered.");
      final SpanExporter.Handler newHandler = newHandlerWithSender(httpSender, serviceName);
      JaegerTraceExporter.handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  private static SpanExporter.Handler newHandler(
      final String thriftEndpoint, final String serviceName) {
    final HttpSender sender = new HttpSender(thriftEndpoint);
    final Process process = new Process(serviceName);
    return new JaegerExporterHandler(sender, process);
  }

  private static SpanExporter.Handler newHandlerWithSender(
      final HttpSender sender, final String serviceName) {
    final Process process = new Process(serviceName);
    return new JaegerExporterHandler(sender, process);
  }

  /**
   * Registers the {@link JaegerTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(final SpanExporter spanExporter, final SpanExporter.Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  /**
   * Unregisters the {@link JaegerTraceExporter} from the OpenCensus library.
   *
   * @throws IllegalStateException if a Jaeger exporter is not registered.
   * @since 0.13
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null, "Jaeger exporter is not registered.");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@link JaegerTraceExporter}.
   *
   * @param spanExporter the instance of the {@link SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(final SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }
}
