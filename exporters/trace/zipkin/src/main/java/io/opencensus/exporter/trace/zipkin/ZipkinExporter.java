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

package io.opencensus.exporter.trace.zipkin;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

/**
 * An OpenCensus span exporter implementation which exports data to Zipkin.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   ZipkinExporter.createAndRegister("http://127.0.0.1:9411/api/v2/spans", "myservicename");
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @deprecated Deprecated due to inconsistent naming. Use {@link ZipkinTraceExporter}.
 */
@Deprecated
public final class ZipkinExporter {

  private ZipkinExporter() {}

  /**
   * Creates and registers the Zipkin Trace exporter to the OpenCensus library. Only one Zipkin
   * exporter can be registered at any point.
   *
   * @param v2Url Ex http://127.0.0.1:9411/api/v2/spans
   * @param serviceName the {@link Span#localServiceName() local service name} of the process.
   * @throws IllegalStateException if a Zipkin exporter is already registered.
   */
  public static void createAndRegister(String v2Url, String serviceName) {
    ZipkinTraceExporter.createAndRegister(v2Url, serviceName);
  }

  /**
   * Creates and registers the Zipkin Trace exporter to the OpenCensus library. Only one Zipkin
   * exporter can be registered at any point.
   *
   * @param encoder Usually {@link SpanBytesEncoder#JSON_V2}
   * @param sender Often, but not necessarily an http sender. This could be Kafka or SQS.
   * @param serviceName the {@link Span#localServiceName() local service name} of the process.
   * @throws IllegalStateException if a Zipkin exporter is already registered.
   */
  public static void createAndRegister(
      SpanBytesEncoder encoder, Sender sender, String serviceName) {
    ZipkinTraceExporter.createAndRegister(encoder, sender, serviceName);
  }

  /**
   * Registers the {@code ZipkinExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    ZipkinTraceExporter.register(spanExporter, handler);
  }

  /**
   * Unregisters the Zipkin Trace exporter from the OpenCensus library.
   *
   * @throws IllegalStateException if a Zipkin exporter is not registered.
   */
  public static void unregister() {
    ZipkinTraceExporter.unregister();
  }

  /**
   * Unregisters the {@code ZipkinExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    ZipkinTraceExporter.unregister(spanExporter);
  }
}
