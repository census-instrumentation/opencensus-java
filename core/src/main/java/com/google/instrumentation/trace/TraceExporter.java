/*
 * Copyright 2017, Google Inc.
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The main exporting API for the trace library.
 *
 * <p>Implementation MUST ensure that all functions are thread safe.
 */
@ThreadSafe
public abstract class TraceExporter {
  private static final NoopTraceExporter noopTraceExporter = new NoopTraceExporter();

  /**
   * Registers a new service handler that is used by the library to export trace data.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *   Tracing.getTraceExporter().registerServiceHandler(
   *       "com.google.stackdriver", new StackdriverServiceHandler());
   *   // ...
   * }
   * }</pre>
   *
   * @param name the name of the service handler. Must be unique for each service.
   * @param serviceHandler the service handler that is called for each ended sampled span.
   */
  public abstract void registerServiceHandler(String name, ServiceHandler serviceHandler);

  /**
   * Unregisters the service handler with the provided name.
   *
   * @param name the name of the service handler that will be unregistered.
   */
  public abstract void unregisterServiceHandler(String name);

  /**
   * An abstract class that allows different tracing services to export recorded data for sampled
   * spans in their own format.
   *
   * <p>To export data this MUST be register to to the TraceExporter using {@link
   * #registerServiceHandler(String, ServiceHandler)}.
   */
  public abstract static class ServiceHandler {

    /**
     * Exports a list of sampled (see {@link TraceOptions#isSampled()}) {@link Span}s using the
     * immutable representation {@link SpanData}.
     *
     * <p>This may be called from a different thread than the one that called {@link Span#end()}.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param spanDataList a list of {@code SpanData} objects to be exported.
     */
    public abstract void export(List<SpanData> spanDataList);
  }

  /**
   * Implementation of the {@link ServiceHandler} which logs all the exported {@link SpanData}.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *   Tracing.getTraceExporter().registerServiceHandler(
   *       "com.google.instrumentation.LoggingServiceHandler", LoggingServiceHandler.getInstance());
   *   // ...
   * }
   * }</pre>
   */
  @ThreadSafe
  public static final class LoggingServiceHandler extends ServiceHandler {
    private static final Logger logger = Logger.getLogger(LoggingServiceHandler.class.getName());
    private static final String SERVICE_NAME =
        "com.google.instrumentation.trace.LoggingServiceHandler";
    private static final LoggingServiceHandler INSTANCE = new LoggingServiceHandler();

    /**
     * Registers the {@code LoggingServiceHandler} to the {@code TraceExporter}.
     *
     * @param traceExporter the instance of the {@code TraceExporter} where this service is
     *     registered.
     */
    public static void registerService(TraceExporter traceExporter) {
      traceExporter.registerServiceHandler(SERVICE_NAME, INSTANCE);
    }

    /**
     * Unregisters the {@code LoggingServiceHandler} from the {@code TraceExporter}.
     *
     * @param traceExporter the instance of the {@code TraceExporter} from where this service is
     *     unregistered.
     */
    public static void unregisterService(TraceExporter traceExporter) {
      traceExporter.unregisterServiceHandler(SERVICE_NAME);
    }

    @Override
    public void export(List<SpanData> spanDataList) {
      for (SpanData spanData : spanDataList) {
        logger.log(Level.INFO, spanData.toString());
      }
    }

    private LoggingServiceHandler() {}
  }

  /**
   * Returns the no-op implementation of the {@code TraceExporter}.
   *
   * @return the no-op implementation of the {@code TraceExporter}.
   */
  static TraceExporter getNoopTraceExporter() {
    return noopTraceExporter;
  }

  private static final class NoopTraceExporter extends TraceExporter {
    @Override
    public void registerServiceHandler(String name, @Nullable ServiceHandler serviceHandler) {}

    @Override
    public void unregisterServiceHandler(String name) {}
  }
}
