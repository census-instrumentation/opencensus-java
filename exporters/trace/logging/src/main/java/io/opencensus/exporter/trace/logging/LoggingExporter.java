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

package io.opencensus.exporter.trace.logging;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.export.SpanExporter;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An OpenCensus span exporter implementation which logs all data.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   LoggingExporter.register();
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @deprecated Deprecated due to inconsistent naming. Use {@link LoggingTraceExporter}.
 * @since 0.6
 */
@ThreadSafe
@Deprecated
public final class LoggingExporter {
  private LoggingExporter() {}

  /**
   * Registers the Logging exporter to the OpenCensus library.
   *
   * @since 0.6
   */
  public static void register() {
    LoggingTraceExporter.register();
  }

  /**
   * Registers the {@code LoggingHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter) {
    LoggingTraceExporter.register(spanExporter);
  }

  /**
   * Unregisters the Logging exporter from the OpenCensus library.
   *
   * @since 0.6
   */
  public static void unregister() {
    LoggingTraceExporter.unregister();
  }

  /**
   * Unregisters the {@code LoggingHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    LoggingTraceExporter.unregister(spanExporter);
  }
}
