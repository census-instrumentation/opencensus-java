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

package io.opencensus.exporter.trace.instana;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.io.BaseEncoding;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span.Kind;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/*
 * Exports to an Instana agent acting as proxy to the Instana backend (and handling authentication)
 * Uses the Trace SDK documented:
 * https://github.com/instana/instana-java-sdk#instana-trace-webservice
 *
 * Currently does a blocking export using HttpUrlConnection.
 * Also uses a StringBuilder to build JSON.
 * Both can be improved should 3rd party library usage not be a concern.
 *
 * Major TODO is the limitation of Instana to only suport 64bit trace ids, which will be resolved.
 * Until then it is crossing fingers and treating it as 50% sampler :).
 */
final class InstanaExporterHandler extends SpanExporter.Handler {

  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySpampler = Samplers.probabilitySampler(0.0001);
  private final URL agentEndpoint;

  InstanaExporterHandler(URL agentEndpoint) {
    this.agentEndpoint = agentEndpoint;
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

  private static String toSpanType(SpanData spanData) {
    if (spanData.getKind() == Kind.SERVER
        || (spanData.getKind() == null
            && (spanData.getParentSpanId() == null
                || Boolean.TRUE.equals(spanData.getHasRemoteParent())))) {
      return "ENTRY";
    }

    // This is a hack because the Span API did not have SpanKind.
    if (spanData.getKind() == Kind.CLIENT
        || (spanData.getKind() == null && spanData.getName().startsWith("Sent."))) {
      return "EXIT";
    }

    return "INTERMEDIATE";
  }

  private static long toMillis(Timestamp timestamp) {
    return SECONDS.toMillis(timestamp.getSeconds()) + NANOSECONDS.toMillis(timestamp.getNanos());
  }

  private static long toMillis(Timestamp start, Timestamp end) {
    Duration duration = end.subtractTimestamp(start);
    return SECONDS.toMillis(duration.getSeconds()) + NANOSECONDS.toMillis(duration.getNanos());
  }

  // The return type needs to be nullable when this function is used as an argument to 'match' in
  // attributeValueToString, because 'match' doesn't allow covariant return types.
  private static final Function<Object, /*@Nullable*/ String> RETURN_STRING =
      new Function<Object, /*@Nullable*/ String>() {
        @Override
        public String apply(Object input) {
          return input.toString();
        }
      };

  @javax.annotation.Nullable
  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        RETURN_STRING, RETURN_STRING, RETURN_STRING, Functions.</*@Nullable*/ String>returnNull());
  }

  static String convertToJson(Collection<SpanData> spanDataList) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (final SpanData span : spanDataList) {
      final SpanContext spanContext = span.getContext();
      final SpanId parentSpanId = span.getParentSpanId();
      final Timestamp startTimestamp = span.getStartTimestamp();
      final Timestamp endTimestamp = span.getEndTimestamp();
      final Status status = span.getStatus();
      if (status == null || endTimestamp == null) {
        continue;
      }
      if (sb.length() > 1) {
        sb.append(',');
      }
      sb.append('{');
      sb.append("\"spanId\":\"").append(encodeSpanId(spanContext.getSpanId())).append("\",");
      sb.append("\"traceId\":\"").append(encodeTraceId(spanContext.getTraceId())).append("\",");
      if (parentSpanId != null) {
        sb.append("\"parentId\":\"").append(encodeSpanId(parentSpanId)).append("\",");
      }
      sb.append("\"timestamp\":").append(toMillis(startTimestamp)).append(',');
      sb.append("\"duration\":").append(toMillis(startTimestamp, endTimestamp)).append(',');
      sb.append("\"name\":\"").append(toSpanName(span)).append("\",");
      sb.append("\"type\":\"").append(toSpanType(span)).append('"');
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
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // Start a new span with explicit 1/10000 sampling probability to avoid the case when user
    // sets the default sampler to always sample and we get the gRPC span of the instana
    // export call always sampled and go to an infinite loop.
    Scope scope =
        tracer.spanBuilder("ExportInstanaTraces").setSampler(probabilitySpampler).startScopedSpan();
    try {
      String json = convertToJson(spanDataList);

      OutputStream outputStream = null;
      InputStream inputStream = null;
      try {
        HttpURLConnection connection = (HttpURLConnection) agentEndpoint.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
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
    } finally {
      scope.close();
    }
  }
}
