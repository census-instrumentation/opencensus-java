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

import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.opencensus.common.Duration;
import io.opencensus.common.Scope;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class ElasticsearchTraceHandler extends SpanExporter.Handler {

  private final ElasticsearchTraceConfiguration elasticsearchTraceConfiguration;
  private final String appName;
  private final URL indexUrl;
  private final Duration deadline;
  private static final String CONTENT_TYPE = "application/json";
  private static final String REQUEST_METHOD = "POST";
  private static final int CONNECTION_TIMEOUT_MILLISECONDS = 6000;
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);

  ElasticsearchTraceHandler(ElasticsearchTraceConfiguration elasticsearchTraceConfiguration)
      throws MalformedURLException {

    this.elasticsearchTraceConfiguration = elasticsearchTraceConfiguration;
    StringBuilder sb = new StringBuilder();
    sb.append(elasticsearchTraceConfiguration.getElasticsearchUrl()).append("/");
    sb.append(elasticsearchTraceConfiguration.getElasticsearchIndex()).append("/");
    sb.append(elasticsearchTraceConfiguration.getElasticsearchType()).append("/");
    indexUrl = new URL(sb.toString());
    appName = elasticsearchTraceConfiguration.getAppName();
    deadline = elasticsearchTraceConfiguration.getDeadline();
  }

  /**
   * Handles exporting of traces in {@code ElasticsearchTraceExporter}.
   *
   * @param spanDataList Collection of {@code SpanData} to be exported.
   */
  @Override
  public void export(final Collection<SpanData> spanDataList) {
    Scope scope =
        tracer
            .spanBuilder("ExportElasticsearchTraces")
            .setSampler(probabilitySampler)
            .setRecordEvents(true)
            .startScopedSpan();
    try {
      TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
      timeLimiter.callWithTimeout(
          new Callable<Void>() {
            @Override
            public Void call() {
              doExport(spanDataList);
              return null;
            }
          },
          deadline.toMillis(),
          TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      handleException(e);
    } finally {
      scope.close();
    }
  }

  private void doExport(Collection<SpanData> spanDataList) {
    List<String> jsonList = JsonConversionUtils.convertToJson(appName, spanDataList);
    if (jsonList.isEmpty()) {
      return;
    }
    for (String json : jsonList) {

      OutputStream outputStream = null;
      InputStream inputStream = null;

      try {
        HttpURLConnection connection = (HttpURLConnection) indexUrl.openConnection();
        if (elasticsearchTraceConfiguration.getUserName() != null) {
          String parameters =
              BaseEncoding.base64()
                  .encode(
                      (elasticsearchTraceConfiguration.getUserName()
                              + ":"
                              + elasticsearchTraceConfiguration.getPassword())
                          .getBytes("UTF-8"));
          connection.setRequestProperty("Authorization", "Basic " + parameters);
        }
        connection.setRequestMethod(REQUEST_METHOD);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        outputStream = connection.getOutputStream();
        outputStream.write(json.getBytes(Charset.defaultCharset()));
        outputStream.flush();
        inputStream = connection.getInputStream();
        if (connection.getResponseCode() != 200) {
          handleException(new Exception("Response " + connection.getResponseCode()));
        }
      } catch (IOException e) {
        handleException(e);
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
  }

  private static void handleException(Exception e) {
    Status status = e instanceof TimeoutException ? Status.DEADLINE_EXCEEDED : Status.UNKNOWN;
    tracer
        .getCurrentSpan()
        .setStatus(
            status.withDescription(
                e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
  }
}
