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
   * Register a new service handler that allows the library to
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
   * @param name the name of the exporter. Must be unique for each service.
   * @param serviceHandler the service handler that is called for each ended sampled span. If {@code
   *     null} the previous exporter with the same name will be removed.
   */
  public abstract void registerServiceHandler(String name, @Nullable ServiceHandler serviceHandler);

  /**
   * An abstract class that allows different tracing services to export recorded data for sampled
   * spans in their own format.
   *
   * <p>To export data this MUST be register to to the TraceExporter using {@link
   * #registerServiceHandler(String, ServiceHandler)}.
   */
  public abstract static class ServiceHandler {

    /**
     * It is called every time after a sampled (see {@link TraceOptions#isSampled()}) {@link Span}
     * is ended.
     *
     * <p>This may be called from a different thread than the one that called {@link Span#end()}.
     *
     * <p>Implementation SHOULD not block the calling thread and execute the export on a different
     * thread if possible.
     *
     * @param spanData the immutable representation of all data collected by the {@code Span}.
     */
    public abstract void export(SpanData spanData);
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
  }
}
