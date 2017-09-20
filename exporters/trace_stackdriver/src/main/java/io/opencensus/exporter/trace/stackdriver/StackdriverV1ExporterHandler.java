/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.exporter.trace.stackdriver;

import static com.google.api.client.util.Preconditions.checkNotNull;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.cloud.trace.v1.TraceServiceClient;
import com.google.cloud.trace.v1.TraceServiceSettings;
import com.google.common.io.BaseEncoding;
import com.google.devtools.cloudtrace.v1.PatchTracesRequest;
import com.google.devtools.cloudtrace.v1.Trace;
import com.google.devtools.cloudtrace.v1.TraceSpan;
import com.google.devtools.cloudtrace.v1.Traces;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.NetworkEvent.Type;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanExporter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

/**
 * Exporter to Stackdriver Trace API v1.
 *
 * <p>It contains helper methods to convert {@link SpanData} to Stackdriver Trace API v1 trace
 * messages.
 */
final class StackdriverV1ExporterHandler extends SpanExporter.Handler {

  private static final String STATUS_CODE = "g.co/status/code";
  private static final String STATUS_DESCRIPTION = "g.co/status/description";
  private static final String ANNOTATION_LABEL = "ANNOTATION-";
  private static final String NETWORK_EVENT_LABEL = "NETWORK-";

  private final String projectId;
  private final TraceServiceClient traceServiceClient;

  private StackdriverV1ExporterHandler(String projectId, TraceServiceClient traceServiceClient) {
    this.projectId = checkNotNull(projectId, "projectId");
    this.traceServiceClient = traceServiceClient;
  }

  static StackdriverV1ExporterHandler createWithCredentials(
      Credentials credentials, String projectId) throws IOException {
    checkNotNull(credentials, "credentials");
    TraceServiceSettings traceServiceSettings =
        TraceServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build();
    return new StackdriverV1ExporterHandler(
        projectId, TraceServiceClient.create(traceServiceSettings));
  }

  static StackdriverV1ExporterHandler create(String projectId) throws IOException {
    return new StackdriverV1ExporterHandler(projectId, TraceServiceClient.create());
  }

  // TODO(bdrutu): Add tests for this.
  private Trace generateTrace(SpanData spanData) {
    SpanContext context = spanData.getContext();
    TraceSpan.Builder spanBuilder =
        TraceSpan.newBuilder()
            .setSpanId(encodeSpanId(context.getSpanId()))
            .setKind(toSpanKindProto(spanData))
            .setName(spanData.getName())
            .setStartTime(toTimestampProto(spanData.getStartTimestamp()))
            .setEndTime(toTimestampProto(spanData.getEndTimestamp()));

    if (spanData.getParentSpanId() != null && spanData.getParentSpanId().isValid()) {
      spanBuilder.setParentSpanId(encodeSpanId(spanData.getParentSpanId()));
    }

    for (Map.Entry<String, AttributeValue> label :
        spanData.getAttributes().getAttributeMap().entrySet()) {
      spanBuilder.putLabels(label.getKey(), attributeValueToString(label.getValue()));
    }

    // Add Annotations as labels in the v1 API
    int seq = 0;
    for (TimedEvent<Annotation> annotation : spanData.getAnnotations().getEvents()) {
      spanBuilder.putLabels(
          ANNOTATION_LABEL + String.format("%03d", seq++),
          renderAnnotation(annotation, spanData.getStartTimestamp()));
    }

    // Add NetworkEvents as labels in the v1 API
    seq = 0;
    for (TimedEvent<NetworkEvent> networkEvent : spanData.getNetworkEvents().getEvents()) {
      spanBuilder.putLabels(
          NETWORK_EVENT_LABEL + String.format("%03d", seq++),
          renderNetworkEvents(networkEvent, spanData.getStartTimestamp()));
    }

    // Add Status as labels in the v1 API.
    spanBuilder.putLabels(STATUS_CODE, spanData.getStatus().getCanonicalCode().toString());
    if (spanData.getStatus().getDescription() != null) {
      spanBuilder.putLabels(STATUS_DESCRIPTION, spanData.getStatus().getDescription());
    }

    Trace.Builder traceBuilder =
        Trace.newBuilder()
            .setProjectId(projectId)
            .setTraceId(encodeTraceId(context.getTraceId()))
            .addSpans(spanBuilder.build());

    return traceBuilder.build();
  }

  private static long encodeSpanId(SpanId spanId) {
    return ByteBuffer.wrap(spanId.getBytes()).getLong();
  }

  private static String encodeTraceId(TraceId traceId) {
    return BaseEncoding.base16().lowerCase().encode(traceId.getBytes());
  }

  private static TraceSpan.SpanKind toSpanKindProto(SpanData spanData) {
    if (Boolean.TRUE.equals(spanData.getHasRemoteParent())) {
      return TraceSpan.SpanKind.RPC_SERVER;
    }

    // This is a hack because the v2 API does not have SpanKind. When switch to v2 this will be
    // fixed.
    if (spanData.getName().startsWith("Sent.")) {
      return TraceSpan.SpanKind.RPC_CLIENT;
    }

    return TraceSpan.SpanKind.SPAN_KIND_UNSPECIFIED;
  }

  private static String renderNetworkEvents(TimedEvent<NetworkEvent> timedEvent, Timestamp start) {
    StringBuilder stringBuilder = new StringBuilder();
    renderDelay(stringBuilder, timedEvent.getTimestamp().subtractTimestamp(start));
    NetworkEvent networkEvent = timedEvent.getEvent();
    if (networkEvent.getType() == Type.RECV) {
      stringBuilder.append("Received");
    } else if (networkEvent.getType() == Type.SENT) {
      stringBuilder.append("Sent");
    } else {
      stringBuilder.append("Unknown");
    }
    stringBuilder.append(" message_id=");
    stringBuilder.append(networkEvent.getMessageId());
    stringBuilder.append(" uncompressed_size=");
    stringBuilder.append(networkEvent.getUncompressedMessageSize());
    stringBuilder.append(" compressed_size=");
    stringBuilder.append(networkEvent.getCompressedMessageSize());
    if (networkEvent.getKernelTimestamp() != null) {
      stringBuilder.append(" kernel_timestamp=");
      stringBuilder.append(networkEvent.getKernelTimestamp().toString());
    }
    return stringBuilder.toString();
  }

  private static String renderAnnotation(TimedEvent<Annotation> timedEvent, Timestamp start) {
    StringBuilder stringBuilder = new StringBuilder();
    renderDelay(stringBuilder, timedEvent.getTimestamp().subtractTimestamp(start));
    Annotation annotation = timedEvent.getEvent();
    stringBuilder.append(annotation.getDescription());
    if (!annotation.getAttributes().isEmpty()) {
      stringBuilder.append(" ");
      stringBuilder.append(renderAttributes(annotation.getAttributes()));
    }
    return stringBuilder.toString();
  }

  private static void renderDelay(StringBuilder stringBuilder, Duration delay) {
    long delayUs = delay.getSeconds() * 1000000 + delay.getNanos() / 1000;
    stringBuilder.append("[@");
    stringBuilder.append(delayUs);
    stringBuilder.append(" us] ");
  }

  private static String renderAttributes(Map<String, AttributeValue> attributes) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Attributes:{");
    boolean first = true;
    for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      if (first) {
        first = false;
      } else {
        stringBuilder.append(", ");
      }
      stringBuilder.append(entry.getKey());
      stringBuilder.append("=");
      stringBuilder.append(attributeValueToString(entry.getValue()));
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  private static com.google.protobuf.Timestamp toTimestampProto(Timestamp timestamp) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(timestamp.getSeconds())
        .setNanos(timestamp.getNanos())
        .build();
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        new Function<String, String>() {
          @Override
          public String apply(String stringValue) {
            return stringValue;
          }
        },
        new Function<Boolean, String>() {
          @Override
          public String apply(Boolean booleanValue) {
            return booleanValue.toString();
          }
        },
        new Function<Long, String>() {
          @Override
          public String apply(Long longValue) {
            return longValue.toString();
          }
        },
        Functions.<String>returnNull());
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    Traces.Builder tracesBuilder = Traces.newBuilder();
    for (SpanData spanData : spanDataList) {
      tracesBuilder.addTraces(generateTrace(spanData));
    }
    traceServiceClient.patchTraces(
        PatchTracesRequest.newBuilder()
            .setProjectId(projectId)
            .setTraces(tracesBuilder.build())
            .build());
  }
}
