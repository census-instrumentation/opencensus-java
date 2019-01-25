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

package io.opencensus.exporter.trace.datadog;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.opencensus.common.Functions;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

@SuppressWarnings({
  // This library is not supposed to be Android or Java 7 compatible.
  "AndroidJdkLibsChecker",
  "Java7ApiChecker"
})
final class DatadogExporterHandler extends SpanExporter.Handler {

  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySpampler = Samplers.probabilitySampler(0.0001);
  private static final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();

  private final URL agentEndpoint;
  private final String service;
  private final String type;

  DatadogExporterHandler(final String agentEndpoint, final String service, final String type)
      throws MalformedURLException {
    this.agentEndpoint = new URL(agentEndpoint);
    this.service = service;
    this.type = type;
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.returnToString(),
        Functions.throwIllegalArgumentException());
  }

  private static Map<String, String> attributesToMeta(
      final Map<String, AttributeValue> attributes) {
    final HashMap<String, String> result = new HashMap<>();
    attributes.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .forEach(entry -> result.put(entry.getKey(), attributeValueToString(entry.getValue())));
    return result;
  }

  private static long convertSpanId(final SpanId spanId) {
    final byte[] bytes = spanId.getBytes();
    long result = 0;
    for (int i = 0; i < Long.SIZE / Byte.SIZE; i++) {
      result <<= Byte.SIZE;
      result |= (bytes[i] & 0xff);
    }
    if (result < 0) {
      return -result;
    }
    return result;
  }

  private static long timestampToNanos(final Timestamp timestamp) {
    return TimeUnit.SECONDS.toNanos(timestamp.getSeconds()) + timestamp.getNanos();
  }

  private static Integer errorCode(@Nullable final Status status) {
    if (status == null || status.equals(Status.OK) || status.equals(Status.ALREADY_EXISTS)) {
      return 0;
    }

    return 1;
  }

  String convertToJson(Collection<SpanData> spanDataList) {
    final ArrayList<DatadogSpan> datadogSpans = new ArrayList<>();
    for (SpanData sd : spanDataList) {
      SpanContext sc = sd.getContext();

      final long startTime = timestampToNanos(sd.getStartTimestamp());
      final Timestamp endTimestamp =
          Optional.ofNullable(sd.getEndTimestamp()).orElseGet(() -> Tracing.getClock().now());
      final long endTime = timestampToNanos(endTimestamp);
      final long duration = endTime - startTime;

      final Long parentId =
          Optional.ofNullable(sd.getParentSpanId())
              .map(DatadogExporterHandler::convertSpanId)
              .orElse(null);

      final Map<String, AttributeValue> attributes = sd.getAttributes().getAttributeMap();
      final Map<String, String> meta =
          attributes.isEmpty() ? new HashMap<>() : attributesToMeta(attributes);

      final String resource = meta.getOrDefault("resource", "UNKNOWN");

      final DatadogSpan span =
          new DatadogSpan(
              sc.getTraceId().getLowerLong(),
              convertSpanId(sc.getSpanId()),
              sd.getName(),
              resource,
              this.service,
              this.type,
              startTime,
              duration,
              parentId,
              errorCode(sd.getStatus()),
              meta);
      datadogSpans.add(span);
    }

    final Collection<List<DatadogSpan>> traces =
        datadogSpans.stream()
            .collect(Collectors.groupingBy(DatadogSpan::getTraceId, Collectors.toList()))
            .values();

    return gson.toJson(traces);
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // Start a new span with explicit 1/10000 sampling probability to avoid the case when user
    // sets the default sampler to always sample and we get the gRPC span of the datadog
    // export call always sampled and go to an infinite loop.
    try (Scope ss =
        tracer
            .spanBuilder("ExportDatadogTraces")
            .setSampler(probabilitySpampler)
            .startScopedSpan()) {

      final String data = convertToJson(spanDataList);

      final HttpURLConnection connection = (HttpURLConnection) agentEndpoint.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data.getBytes(Charset.defaultCharset()));
      outputStream.flush();
      outputStream.close();
      if (connection.getResponseCode() != 200) {
        tracer
            .getCurrentSpan()
            .setStatus(Status.UNKNOWN.withDescription("Response " + connection.getResponseCode()));
      }
    } catch (IOException e) {
      tracer
          .getCurrentSpan()
          .setStatus(
              Status.UNKNOWN.withDescription(
                  e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
      // drop span batch
    }
  }
}
