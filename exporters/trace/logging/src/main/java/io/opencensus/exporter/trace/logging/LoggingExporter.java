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
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 */
@ThreadSafe
@Deprecated
public final class LoggingExporter {
  private static final Logger logger = Logger.getLogger(LoggingExporter.class.getName());
  private static final String REGISTER_NAME = LoggingExporter.class.getName();
  private static final LoggingExporterHandler HANDLER = new LoggingExporterHandler();

  private LoggingExporter() {}

  /** Registers the Logging exporter to the OpenCensus library. */
  public static void register() {
    register(Tracing.getExportComponent().getSpanExporter());
  }

  /**
   * Registers the {@code LoggingHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter) {
    spanExporter.registerHandler(REGISTER_NAME, HANDLER);
  }

  /** Unregisters the Logging exporter from the OpenCensus library. */
  public static void unregister() {
    unregister(Tracing.getExportComponent().getSpanExporter());
  }

  /**
   * Unregisters the {@code LoggingHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  @VisibleForTesting
  static final class LoggingExporterHandler extends Handler {
    @Override
    public void export(Collection<SpanData> spanDataList) {
      // TODO(bdrutu): Use JSON as a standard format for logging SpanData and define this to be
      // compatible between languages.
      for (SpanData spanData : spanDataList) {
        logger.log(Level.INFO, spanData.toString());
      }
    }
  }
}
