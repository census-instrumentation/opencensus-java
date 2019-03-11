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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.opencensus.common.Duration;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.export.SpanData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * Util to parse {@link SpanData} to json for {@link ElasticsearchTraceExporter}.
 *
 * @since 0.20.0
 */
final class JsonConversionUtils {

  private static final String ELASTICSEARCH_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

  private JsonConversionUtils() {}

  private static String encodeTraceId(TraceId traceId) {
    return traceId.toLowerBase16();
  }

  private static String encodeSpanId(SpanId spanId) {
    return spanId.toLowerBase16();
  }

  private static String toSpanName(SpanData spanData) {
    return spanData.getName();
  }

  private static long toMillis(Timestamp timestamp) {
    return SECONDS.toMillis(timestamp.getSeconds()) + NANOSECONDS.toMillis(timestamp.getNanos());
  }

  private static long toMillis(Timestamp start, Timestamp end) {
    Duration duration = end.subtractTimestamp(start);
    return duration.toMillis();
  }

  private static Date toDate(Timestamp timestamp) {
    return new Date(toMillis(timestamp));
  }

  private static String formatDate(Timestamp timestamp) {
    return new SimpleDateFormat(ELASTICSEARCH_DATE_PATTERN).format(toDate(timestamp));
  }

  @Nullable
  @SuppressWarnings("nullness")
  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnConstant(""));
  }

  private static String toSpanKind(SpanData spanData) {
    if (spanData.getKind() == Kind.SERVER
        || (spanData.getKind() == null && Boolean.TRUE.equals(spanData.getHasRemoteParent()))) {
      return Span.Kind.SERVER.name();
    }
    if (spanData.getKind() == Kind.CLIENT) {
      return Span.Kind.CLIENT.name();
    }
    return "";
  }

  /**
   * Converts a collection of {@link SpanData} to a Collection of json string.
   *
   * @param appName the name of app to include in traces.
   * @param spanDataList Collection of {@code SpanData} to be converted to json.
   * @return Collection of {@code SpanData} converted to JSON to be indexed.
   */
  static List<String> convertToJson(String appName, Collection<SpanData> spanDataList) {
    List<String> spanJson = new ArrayList<String>();
    if (spanDataList == null) {
      return spanJson;
    }
    StringBuilder sb = new StringBuilder();
    for (final SpanData span : spanDataList) {
      final SpanContext spanContext = span.getContext();
      final SpanId parentSpanId = span.getParentSpanId();
      final Timestamp startTimestamp = span.getStartTimestamp();
      final Timestamp endTimestamp = span.getEndTimestamp();
      final Status status = span.getStatus();
      if (endTimestamp == null) {
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
      sb.append("\"kind\":\"").append(toSpanKind(span)).append("\",");
      sb.append("\"dateStarted\":\"").append(formatDate(startTimestamp)).append("\",");
      sb.append("\"dateEnded\":\"").append(formatDate(endTimestamp)).append('"');
      if (status == null) {
        sb.append(",\"status\":").append("\"ok\"");
      } else if (!status.isOk()) {
        sb.append(",\"error\":").append("true");
      }
      Map<String, AttributeValue> attributeMap = span.getAttributes().getAttributeMap();
      if (attributeMap.size() > 0) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (Entry<String, AttributeValue> entry : attributeMap.entrySet()) {
          if (builder.length() > 1) {
            builder.append(',');
          }
          builder
              .append("\"")
              .append(entry.getKey())
              .append("\":\"")
              .append(attributeValueToString(entry.getValue()))
              .append("\"");
        }
        builder.append('}');
        sb.append(",\"data\":").append(builder);
      }
      sb.append('}');
      spanJson.add(sb.toString());
    }
    return spanJson;
  }
}
