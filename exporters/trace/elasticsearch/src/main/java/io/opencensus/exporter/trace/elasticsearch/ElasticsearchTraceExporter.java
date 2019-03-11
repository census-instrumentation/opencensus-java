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

package io.opencensus.exporter.trace.elasticsearch;

import com.google.common.base.Preconditions;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.net.MalformedURLException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Elasticsearch.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) throws Exception{
 *
 *     ElasticsearchTraceConfiguration elasticsearchTraceConfiguration =
 *     ElasticsearchTraceConfiguration.builder().setAppName("sample-app").setElasticsearchUrl("http://localhost:9200")
 *     .setElasticsearchIndex("opencensus-index").setElasticsearchType("traces").build();
 *     ElasticsearchTraceExporter.createAndRegister(elasticsearchTraceConfiguration);
 *
 *     // Do work
 *
 * }
 *
 * }</pre>
 *
 * @since 0.20.0
 */
public final class ElasticsearchTraceExporter {

  private static final String REGISTERED_TRACE_EXPORTER_NAME =
      ElasticsearchTraceExporter.class.getName();
  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  private ElasticsearchTraceExporter() {}

  /**
   * Creates and registers the ElasticsearchTraceExporter to the OpenCensus.
   *
   * @param elasticsearchTraceConfiguration {@link ElasticsearchTraceConfiguration}
   * @throws MalformedURLException when the Elasticsearch url is invalid.
   * @throws IllegalStateException if ElasticsearchTraceExporter is already created.
   * @throws IllegalArgumentException when mandatory parameters in the configuration are missing.
   * @since 0.20.0
   */
  @SuppressWarnings("nullness")
  public static void createAndRegister(
      ElasticsearchTraceConfiguration elasticsearchTraceConfiguration)
      throws MalformedURLException {
    synchronized (monitor) {
      Preconditions.checkState(handler == null, "Elasticsearch exporter already registered.");
      Preconditions.checkArgument(
          elasticsearchTraceConfiguration != null, "Elasticsearch " + "configuration not set.");
      Preconditions.checkArgument(
          elasticsearchTraceConfiguration.getElasticsearchIndex() != null,
          "Elasticsearch index not specified");
      Preconditions.checkArgument(
          elasticsearchTraceConfiguration.getElasticsearchType() != null,
          "Elasticsearch type not specified");
      Preconditions.checkArgument(
          elasticsearchTraceConfiguration.getElasticsearchUrl() != null,
          "Elasticsearch URL not specified");
      handler = new ElasticsearchTraceHandler(elasticsearchTraceConfiguration);
      register(Tracing.getExportComponent().getSpanExporter(), handler);
    }
  }

  /**
   * Registers the ElasticsearchTraceExporter.
   *
   * @param spanExporter instance of the {@link SpanExporter} registered.
   * @param handler instance of the {@link Handler} registered.
   */
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTERED_TRACE_EXPORTER_NAME, handler);
  }

  /**
   * Unregisters the ElasticsearchTraceExporter from OpenCensus.
   *
   * @throws IllegalStateException if ElasticsearchTraceExporter is already unregistered.
   * @since 0.20.0
   */
  public static void unregister() {
    synchronized (monitor) {
      Preconditions.checkState(
          handler != null,
          "Can't unregister Elasticsearch Trace Exporter which is not registered.");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the ElasticsearchTraceExporter from OpenCensus.
   *
   * @param spanExporter the instance of the {@code SpanExporter} to be unregistered.
   * @since 0.20.0
   */
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTERED_TRACE_EXPORTER_NAME);
  }
}
