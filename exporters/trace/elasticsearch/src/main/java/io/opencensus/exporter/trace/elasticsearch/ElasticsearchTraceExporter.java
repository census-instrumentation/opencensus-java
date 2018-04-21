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

package io.opencensus.exporter.trace.elasticsearch;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Scope;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.DatatypeConverter;

/**
 * An OpenCensus span exporter implementation which exports data to Elasticsearch.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * ElasticsearchConfiguration elasticsearchConfiguration
 *     = new ElasticsearchConfiguration("sample-app","username", "password","http://localhost:9200",
 *        "trace_index","trace_type");
 *    ElasticsearchTraceExporter.createAndRegister(elasticsearchConfiguration);
 *   ... // Do work.
 * }</pre>
 *
 * @since 0.13
 */
public class ElasticsearchTraceExporter {

  private static final String REGISTER_NAME = ElasticsearchTraceExporter.class.getName();
  private static final String CONTENT_TYPE = "application/json";
  private static final String REQUEST_METHOD = "POST";
  private static final int CONNECTION_TIMEOUT = 6000;
  private static final Object monitor = new Object();
  private static final JsonParserUtil jsonParserUtil = new JsonParserUtil();
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  public ElasticsearchTraceExporter() {}

  /**
   * Creates and registers the ElasticsearchTraceExporter to the OpenCensus.
   *
   * @param elasticsearchConfiguration {@link ElasticsearchConfiguration}
   * @throws MalformedURLException when the Elasticsearch url is invalid.
   * @throws IllegalStateException when required parameters in the configuration are missing. eg:
   *     Elasticsearch type and Elasticsearch index.
   * @since 0.13
   */
  public static void createAndRegister(ElasticsearchConfiguration elasticsearchConfiguration)
      throws Exception {
    synchronized (monitor) {
      checkState(handler == null, "Elasticsearch exporter already registered.");
      checkState(
          elasticsearchConfiguration.getElasticsearchIndex() != null,
          "Elasticsearch Index not specified");
      checkState(
          elasticsearchConfiguration.getElasticsearchType() != null,
          "Elasticsearch type not specified");
      checkState(
          elasticsearchConfiguration.getElasticsearchUrl() != null,
          "Elasticsearch URL not specified");
      registerElasticsearchHandler(new ElasticsearchTraceHandler(elasticsearchConfiguration));
    }
  }

  private static void registerElasticsearchHandler(SpanExporter.Handler elasticsearchHandler) {
    synchronized (monitor) {
      handler = elasticsearchHandler;
      register(Tracing.getExportComponent().getSpanExporter(), handler);
    }
  }

  /**
   * Registers the {@code ElasticsearchTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} registered.
   * @since 0.13
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  /**
   * Unregisters the {@code ElasticsearchTraceExporter} from OpenCensus
   *
   * @throws IllegalStateException if it's already unregistered.
   * @since 0.13
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(
          handler != null, "Can't unregister Elasticsearch Exporter which is not registered. ");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@code ElasticsearchTraceExporter} from OpenCensus.
   *
   * @param spanExporter the instance of the {@code SpanExporter} to be unregistered.
   * @since 0.13
   */
  @VisibleForTesting
  public static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  static final class ElasticsearchTraceHandler extends SpanExporter.Handler {

    private final ElasticsearchConfiguration elasticsearchConfiguration;
    private String appName;
    private URL indexUrl;

    @VisibleForTesting
    public ElasticsearchTraceHandler(ElasticsearchConfiguration elasticsearchConfiguration)
        throws MalformedURLException {

      this.elasticsearchConfiguration = elasticsearchConfiguration;
      StringBuilder sb = new StringBuilder();
      sb.append(elasticsearchConfiguration.getElasticsearchUrl()).append("/");
      sb.append(elasticsearchConfiguration.getElasticsearchIndex()).append("/");
      sb.append(elasticsearchConfiguration.getElasticsearchType()).append("/");
      indexUrl = new URL(sb.toString());
      appName = elasticsearchConfiguration.getAppName();
    }

    /**
     * Handles exporting of traces in {@code ElasticsearchTraceExporter}.
     *
     * @param spanDataList Collection of {@code SpanData} to be exported.
     * @since 0.13
     */
    @Override
    public void export(Collection<SpanData> spanDataList) {
      Scope scope =
          tracer
              .spanBuilder("ExportElasticsearchTraces")
              .setSampler(probabilitySampler)
              .startScopedSpan();
      try {
        List<String> jsonList = jsonParserUtil.convertToJson(appName, spanDataList);
        if (jsonList.isEmpty()) {
          return;
        }
        for (String json : jsonList) {

          OutputStream outputStream = null;
          InputStream inputStream = null;

          try {
            HttpURLConnection connection = (HttpURLConnection) indexUrl.openConnection();
            if (elasticsearchConfiguration.getUserName() != null
                && !elasticsearchConfiguration.getUserName().isEmpty()) {
              String parameters =
                  DatatypeConverter.printBase64Binary(
                      (elasticsearchConfiguration.getUserName()
                              + ":"
                              + elasticsearchConfiguration.getPassword())
                          .getBytes("UTF-8"));
              connection.setRequestProperty("Authorization", "Basic " + parameters);
            }
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            outputStream = connection.getOutputStream();
            outputStream.write(json.getBytes(Charset.defaultCharset()));
            outputStream.flush();
            inputStream = connection.getInputStream();
            if (connection.getResponseCode() != 200) {
              tracer
                  .getCurrentSpan()
                  .setStatus(
                      Status.UNKNOWN.withDescription("Response " + connection.getResponseCode()));
            }
          } catch (IOException e) {
            tracer
                .getCurrentSpan()
                .setStatus(
                    Status.UNKNOWN.withDescription(
                        e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            // dropping span batch
          } finally {
            if (inputStream != null) {
              try {
                inputStream.close();
              } catch (IOException e) {
                // ignore
              }
            }
            if (outputStream != null) {
              try {
                outputStream.close();
              } catch (IOException e) {
                // ignore
              }
            }
          }
        }
      } finally {
        scope.close();
      }
    }
  }
}
