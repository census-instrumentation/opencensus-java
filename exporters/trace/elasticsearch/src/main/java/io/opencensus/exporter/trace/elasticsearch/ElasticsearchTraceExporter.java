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
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
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

  private static final Logger logger = Logger.getLogger(ElasticsearchTraceExporter.class.getName());
  private static final String REGISTER_NAME = ElasticsearchTraceExporter.class.getName();
  private static final String ES_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
  private static final String CONTENT_TYPE = "application/json";
  private static final String REQUEST_METHOD = "POST";
  private static final int CONNECTION_TIMEOUT = 6000;
  private static final Object monitor = new Object();
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(ES_DATE_FORMAT);
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);
  private static final Function<Object, String> RETURN_STRING =
      new Function<Object, String>() {
        @Override
        public String apply(Object input) {
          return input.toString();
        }
      };
  private static String appName;
  private static URL indexUrl;
  @GuardedBy("monitor")
  @Nullable
  private static SpanExporter.Handler handler = null;

  /**
   * Creates and registers the ElasticsearchTraceExporter to the OpenCensus.
   *
   * @param elasticsearchConfiguration {@link ElasticsearchConfiguration}
   * @throws MalformedURLException when the Elasticsearch url is invalid.
   * @throws IllegalStateException when required parameters in the configuration are missing. eg:
   * Elasticsearch type and Elasticsearch index.
   * @since 0.13
   */
  public static void createAndRegister(ElasticsearchConfiguration elasticsearchConfiguration)
      throws Exception {
    synchronized (monitor) {
      checkState(handler == null, "Elasticsearch exporter already registered.");
      checkState(elasticsearchConfiguration.getElasticsearchIndex() != null,
          "Elasticsearch Index not specified");
      checkState(elasticsearchConfiguration.getElasticsearchType() != null,
          "Elasticsearch type not specified");
      checkState(elasticsearchConfiguration.getElasticsearchUrl() != null,
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
  static void register(SpanExporter spanExporter, SpanExporter.Handler handler) {
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
      checkState(handler != null,
          "Can't unregister Elasticsearch Exporter which is not registered. ");
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

    static List<String> convertToJson(Collection<SpanData> spanDataList) {
      List<String> spanJson = new ArrayList<String>();
      StringBuilder sb = new StringBuilder();
      for (final SpanData span : spanDataList) {
        final SpanContext spanContext = span.getContext();
        final SpanId parentSpanId = span.getParentSpanId();
        final Timestamp startTimestamp = span.getStartTimestamp();
        final Timestamp endTimestamp = span.getEndTimestamp();
        final Status status = span.getStatus();
        if (status == null || endTimestamp == null) {
          continue;
        }
        sb.append('{');
        sb.append("\"appName\":\"").append(appName).append("\",");
        sb.append("\"spanId\":\"").append(encodeSpanId(spanContext.getSpanId())).append("\",");
        sb.append("\"traceId\":\"").append(encodeTraceId(spanContext.getTraceId())).append("\",");
        if (parentSpanId != null) {
          sb.append("\"parentId\":\"").append(encodeSpanId(parentSpanId)).append("\",");
        }
        sb.append("\"timestamp\":").append(toMillis(startTimestamp)).append(',');
        sb.append("\"duration\":").append(toMillis(startTimestamp, endTimestamp)).append(',');
        sb.append("\"name\":\"").append(toSpanName(span)).append("\",");
        sb.append("\"type\":\"").append(toSpanType(span)).append("\",");
        sb.append("\"dateStarted\":\"").append(dateFormat.format(toDate(startTimestamp)))
            .append("\",");
        sb.append("\"dateEnded\":\"").append(dateFormat.format(toDate(endTimestamp))).append('"');
        if (!status.isOk()) {
          sb.append(",\"error\":").append("true");
        }
        Map<String, AttributeValue> attributeMap = span.getAttributes().getAttributeMap();
        if (attributeMap.size() > 0) {
          StringBuilder dataSb = new StringBuilder();
          dataSb.append('{');
          for (Entry<String, AttributeValue> entry : attributeMap.entrySet()) {
            if (dataSb.length() > 1) {
              dataSb.append(',');
            }
            dataSb
                .append("\"")
                .append(entry.getKey())
                .append("\":\"")
                .append(attributeValueToString(entry.getValue()))
                .append("\"");
          }
          dataSb.append('}');
          sb.append(",\"data\":").append(dataSb);
        }
        sb.append('}');
        spanJson.add(sb.toString());
      }
      return spanJson;
    }

    private static String encodeTraceId(TraceId traceId) {
      return BaseEncoding.base16().lowerCase().encode(traceId.getBytes(), 0, 8);
    }

    private static String encodeSpanId(SpanId spanId) {
      return BaseEncoding.base16().lowerCase().encode(spanId.getBytes());
    }

    private static String toSpanName(SpanData spanData) {
      return spanData.getName();
    }

    private static long toMillis(Timestamp timestamp) {
      return SECONDS.toMillis(timestamp.getSeconds()) + NANOSECONDS.toMillis(timestamp.getNanos());
    }

    private static long toMillis(Timestamp start, Timestamp end) {
      Duration duration = end.subtractTimestamp(start);
      return SECONDS.toMillis(duration.getSeconds()) + NANOSECONDS.toMillis(duration.getNanos());
    }

    private static Date toDate(Timestamp timestamp) {
      return new Date(toMillis(timestamp));
    }

    private static String attributeValueToString(AttributeValue attributeValue) {
      return attributeValue.match(
          RETURN_STRING, RETURN_STRING, RETURN_STRING, Functions.<String>returnNull());
    }

    private static String toSpanType(SpanData spanData) {
      if (spanData.getParentSpanId() == null || Boolean.TRUE
          .equals(spanData.getHasRemoteParent())) {
        return "ENTRY";
      }
      if (spanData.getName().startsWith("Sent.")) {
        return "EXIT";
      }
      return "INTERMEDIATE";
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
          tracer.spanBuilder("ExportElasticsearchTraces").setSampler(probabilitySampler)
              .startScopedSpan();
      try {
        List<String> jsonList = convertToJson(spanDataList);
        if (jsonList == null || jsonList.isEmpty()) {
          return;
        }
        for (String json : jsonList) {

          OutputStream outputStream = null;
          InputStream inputStream = null;

          try {
            HttpURLConnection connection = (HttpURLConnection) indexUrl.openConnection();
            if (elasticsearchConfiguration.getUserName() != null && !elasticsearchConfiguration
                .getUserName().isEmpty()) {
              String parameters = DatatypeConverter
                  .printBase64Binary((elasticsearchConfiguration.getUserName()
                      + ":" + elasticsearchConfiguration.getPassword()).getBytes("UTF-8"));
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
