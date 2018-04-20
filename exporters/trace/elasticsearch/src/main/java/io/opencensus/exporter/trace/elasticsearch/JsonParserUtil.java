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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.io.BaseEncoding;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
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

/**
 * Util parsers trace conversion to Elasticsearch friendly json for {@link
 * ElasticsearchTraceExporter}.
 *
 * @since 0.13
 */
public class JsonParserUtil {

  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

  private static final Function<Object, String> returnString =
      new Function<Object, String>() {
        @Override
        public String apply(Object input) {
          return input.toString();
        }
      };

  private String encodeTraceId(TraceId traceId) {
    return BaseEncoding.base16().lowerCase().encode(traceId.getBytes(), 0, 8);
  }

  private String encodeSpanId(SpanId spanId) {
    return BaseEncoding.base16().lowerCase().encode(spanId.getBytes());
  }

  private String toSpanName(SpanData spanData) {
    return spanData.getName();
  }

  private long toMillis(Timestamp timestamp) {
    return SECONDS.toMillis(timestamp.getSeconds()) + NANOSECONDS.toMillis(timestamp.getNanos());
  }

  private long toMillis(Timestamp start, Timestamp end) {
    Duration duration = end.subtractTimestamp(start);
    return SECONDS.toMillis(duration.getSeconds()) + NANOSECONDS.toMillis(duration.getNanos());
  }

  private Date toDate(Timestamp timestamp) {
    return new Date(toMillis(timestamp));
  }

  private String formatDate(Timestamp timestamp) {
    return dateFormat.format(toDate(timestamp));
  }

  private String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        returnString, returnString, returnString, Functions.<String>returnNull());
  }

  private String toSpanType(SpanData spanData) {
    if (spanData.getParentSpanId() == null || Boolean.TRUE.equals(spanData.getHasRemoteParent())) {
      return "ENTRY";
    }
    if (spanData.getName().startsWith("Sent.")) {
      return "EXIT";
    }
    return "INTERMEDIATE";
  }

  /**
   * Registers the {@code ElasticsearchTraceExporter}.
   *
   * @param appName the name of app to include in traces.
   * @param spanDataList Collection of {@code SpanData} to be converted to elasticsearch friendly
   *     json.
   * @return Collection of {@code SpanData} converted to JSON to be indexed.
   * @since 0.13
   */
  public List<String> convertToJson(String appName, Collection<SpanData> spanDataList) {
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
      sb.append("\"dateStarted\":\"").append(formatDate(startTimestamp)).append("\",");
      sb.append("\"dateEnded\":\"").append(formatDate(endTimestamp)).append('"');
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
}
