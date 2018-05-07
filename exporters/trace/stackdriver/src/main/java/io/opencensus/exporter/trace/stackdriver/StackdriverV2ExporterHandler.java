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
import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.TraceServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.devtools.cloudtrace.v2.AttributeValue;
import com.google.devtools.cloudtrace.v2.ProjectName;
import com.google.devtools.cloudtrace.v2.Span;
import com.google.devtools.cloudtrace.v2.Span.Attributes;
import com.google.devtools.cloudtrace.v2.Span.Link;
import com.google.devtools.cloudtrace.v2.Span.Links;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent.MessageEvent;
import com.google.devtools.cloudtrace.v2.SpanName;
import com.google.devtools.cloudtrace.v2.TruncatableString;
import com.google.protobuf.Int32Value;
import com.google.rpc.Status;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.OpenCensusLibraryInformation;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

// TODO(hailongwen): remove the usage of `NetworkEvent` in the future.
/** Exporter to Stackdriver Trace API v2. */
final class StackdriverV2ExporterHandler extends SpanExporter.Handler {
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySpampler = Samplers.probabilitySampler(0.0001);
  private static final String AGENT_LABEL_KEY = "g.co/agent";
  private static final String AGENT_LABEL_VALUE_STRING =
      "opencensus-java [" + OpenCensusLibraryInformation.VERSION + "]";
  private static final AttributeValue AGENT_LABEL_VALUE =
      AttributeValue.newBuilder()
          .setStringValue(toTruncatableStringProto(AGENT_LABEL_VALUE_STRING))
          .build();

  private static final ImmutableMap<String, String> HTTP_ATTRIBUTE_MAPPING =
      ImmutableMap.<String, String>builder()
          .put("http.host", "/http/host")
          .put("http.method", "/http/method")
          .put("http.path", "/http/path")
          .put("http.route", "/http/route")
          .put("http.user_agent", "/http/user_agent")
          .put("http.status_code", "/http/status_code")
          .build();

  private final String projectId;
  private final TraceServiceClient traceServiceClient;
  private final ProjectName projectName;

  @VisibleForTesting
  StackdriverV2ExporterHandler(String projectId, TraceServiceClient traceServiceClient) {
    this.projectId = checkNotNull(projectId, "projectId");
    this.traceServiceClient = traceServiceClient;
    projectName = ProjectName.newBuilder().setProject(projectId).build();

    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(Collections.singletonList("ExportStackdriverTraces"));
  }

  static StackdriverV2ExporterHandler createWithCredentials(
      Credentials credentials, String projectId) throws IOException {
    checkNotNull(credentials, "credentials");
    TraceServiceSettings traceServiceSettings =
        TraceServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build();
    return new StackdriverV2ExporterHandler(
        projectId, TraceServiceClient.create(traceServiceSettings));
  }

  static StackdriverV2ExporterHandler create(String projectId) throws IOException {
    return new StackdriverV2ExporterHandler(projectId, TraceServiceClient.create());
  }

  @VisibleForTesting
  Span generateSpan(SpanData spanData) {
    SpanContext context = spanData.getContext();
    final String traceIdHex = encodeTraceId(context.getTraceId());
    final String spanIdHex = encodeSpanId(context.getSpanId());
    SpanName spanName =
        SpanName.newBuilder().setProject(projectId).setTrace(traceIdHex).setSpan(spanIdHex).build();
    @SuppressWarnings("deprecation")
    Span.Builder spanBuilder =
        Span.newBuilder()
            .setName(spanName.toString())
            .setSpanId(encodeSpanId(context.getSpanId()))
            .setDisplayName(toTruncatableStringProto(spanData.getName()))
            .setStartTime(toTimestampProto(spanData.getStartTimestamp()))
            .setAttributes(toAttributesProto(spanData.getAttributes()))
            .setTimeEvents(
                toTimeEventsProto(spanData.getAnnotations(), spanData.getNetworkEvents()));
    io.opencensus.trace.Status status = spanData.getStatus();
    if (status != null) {
      spanBuilder.setStatus(toStatusProto(status));
    }
    Timestamp end = spanData.getEndTimestamp();
    if (end != null) {
      spanBuilder.setEndTime(toTimestampProto(end));
    }
    spanBuilder.setLinks(toLinksProto(spanData.getLinks()));
    Integer childSpanCount = spanData.getChildSpanCount();
    if (childSpanCount != null) {
      spanBuilder.setChildSpanCount(Int32Value.newBuilder().setValue(childSpanCount).build());
    }
    if (spanData.getParentSpanId() != null && spanData.getParentSpanId().isValid()) {
      spanBuilder.setParentSpanId(encodeSpanId(spanData.getParentSpanId()));
    }

    return spanBuilder.build();
  }

  private static String encodeSpanId(SpanId spanId) {
    return BaseEncoding.base16().lowerCase().encode(spanId.getBytes());
  }

  private static String encodeTraceId(TraceId traceId) {
    return BaseEncoding.base16().lowerCase().encode(traceId.getBytes());
  }

  @SuppressWarnings("deprecation")
  private static Span.TimeEvents toTimeEventsProto(
      TimedEvents<Annotation> annotationTimedEvents,
      TimedEvents<io.opencensus.trace.NetworkEvent> networkEventTimedEvents) {
    Span.TimeEvents.Builder timeEventsBuilder = Span.TimeEvents.newBuilder();
    timeEventsBuilder.setDroppedAnnotationsCount(annotationTimedEvents.getDroppedEventsCount());
    for (TimedEvent<Annotation> annotation : annotationTimedEvents.getEvents()) {
      timeEventsBuilder.addTimeEvent(toTimeAnnotationProto(annotation));
    }
    timeEventsBuilder.setDroppedMessageEventsCount(networkEventTimedEvents.getDroppedEventsCount());
    for (TimedEvent<io.opencensus.trace.NetworkEvent> networkEvent :
        networkEventTimedEvents.getEvents()) {
      timeEventsBuilder.addTimeEvent(toTimeMessageEventProto(networkEvent));
    }
    return timeEventsBuilder.build();
  }

  private static TimeEvent toTimeAnnotationProto(TimedEvent<Annotation> timedEvent) {
    TimeEvent.Builder timeEventBuilder =
        TimeEvent.newBuilder().setTime(toTimestampProto(timedEvent.getTimestamp()));
    Annotation annotation = timedEvent.getEvent();
    timeEventBuilder.setAnnotation(
        TimeEvent.Annotation.newBuilder()
            .setDescription(toTruncatableStringProto(annotation.getDescription()))
            .setAttributes(toAttributesBuilderProto(annotation.getAttributes(), 0))
            .build());
    return timeEventBuilder.build();
  }

  @SuppressWarnings("deprecation")
  private static TimeEvent toTimeMessageEventProto(
      TimedEvent<io.opencensus.trace.NetworkEvent> timedEvent) {
    TimeEvent.Builder timeEventBuilder =
        TimeEvent.newBuilder().setTime(toTimestampProto(timedEvent.getTimestamp()));
    io.opencensus.trace.NetworkEvent networkEvent = timedEvent.getEvent();
    timeEventBuilder.setMessageEvent(
        TimeEvent.MessageEvent.newBuilder()
            .setId(networkEvent.getMessageId())
            .setCompressedSizeBytes(networkEvent.getCompressedMessageSize())
            .setUncompressedSizeBytes(networkEvent.getUncompressedMessageSize())
            .setType(toMessageEventTypeProto(networkEvent))
            .build());
    return timeEventBuilder.build();
  }

  @SuppressWarnings("deprecation")
  private static TimeEvent.MessageEvent.Type toMessageEventTypeProto(
      io.opencensus.trace.NetworkEvent networkEvent) {
    if (networkEvent.getType() == io.opencensus.trace.NetworkEvent.Type.RECV) {
      return MessageEvent.Type.RECEIVED;
    } else {
      return MessageEvent.Type.SENT;
    }
  }

  // These are the attributes of the Span, where usually we may add more attributes like the agent.
  private static Attributes toAttributesProto(
      io.opencensus.trace.export.SpanData.Attributes attributes) {
    Attributes.Builder attributesBuilder =
        toAttributesBuilderProto(
            attributes.getAttributeMap(), attributes.getDroppedAttributesCount());
    attributesBuilder.putAttributeMap(AGENT_LABEL_KEY, AGENT_LABEL_VALUE);
    return attributesBuilder.build();
  }

  private static Attributes.Builder toAttributesBuilderProto(
      Map<String, io.opencensus.trace.AttributeValue> attributes, int droppedAttributesCount) {
    Attributes.Builder attributesBuilder =
        Attributes.newBuilder().setDroppedAttributesCount(droppedAttributesCount);
    for (Map.Entry<String, io.opencensus.trace.AttributeValue> label : attributes.entrySet()) {
      attributesBuilder.putAttributeMap(
          mapKey(label.getKey()), toAttributeValueProto(label.getValue()));
    }
    return attributesBuilder;
  }

  private static String mapKey(String key) {
    if (HTTP_ATTRIBUTE_MAPPING.containsKey(key)) {
      return HTTP_ATTRIBUTE_MAPPING.get(key);
    } else {
      return key;
    }
  }

  private static Status toStatusProto(io.opencensus.trace.Status status) {
    Status.Builder statusBuilder = Status.newBuilder().setCode(status.getCanonicalCode().value());
    if (status.getDescription() != null) {
      statusBuilder.setMessage(status.getDescription());
    }
    return statusBuilder.build();
  }

  private static TruncatableString toTruncatableStringProto(String string) {
    return TruncatableString.newBuilder().setValue(string).setTruncatedByteCount(0).build();
  }

  private static com.google.protobuf.Timestamp toTimestampProto(Timestamp timestamp) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(timestamp.getSeconds())
        .setNanos(timestamp.getNanos())
        .build();
  }

  private static AttributeValue toAttributeValueProto(
      io.opencensus.trace.AttributeValue attributeValue) {
    final AttributeValue.Builder attributeValueBuilder = AttributeValue.newBuilder();
    attributeValue.match(
        new Function<String, Void>() {
          @Override
          public Void apply(String stringValue) {
            attributeValueBuilder.setStringValue(toTruncatableStringProto(stringValue));
            return null;
          }
        },
        new Function<Boolean, Void>() {
          @Override
          public Void apply(Boolean booleanValue) {
            attributeValueBuilder.setBoolValue(booleanValue);
            return null;
          }
        },
        new Function<Long, Void>() {
          @Override
          public Void apply(Long longValue) {
            attributeValueBuilder.setIntValue(longValue);
            return null;
          }
        },
        Functions.</*@Nullable*/ Void>returnNull());
    return attributeValueBuilder.build();
  }

  private static Link.Type toLinkTypeProto(io.opencensus.trace.Link.Type type) {
    if (type == io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN) {
      return Link.Type.PARENT_LINKED_SPAN;
    } else {
      return Link.Type.CHILD_LINKED_SPAN;
    }
  }

  private static Link toLinkProto(io.opencensus.trace.Link link) {
    checkNotNull(link);
    return Link.newBuilder()
        .setTraceId(encodeTraceId(link.getTraceId()))
        .setSpanId(encodeSpanId(link.getSpanId()))
        .setType(toLinkTypeProto(link.getType()))
        .setAttributes(toAttributesBuilderProto(link.getAttributes(), 0))
        .build();
  }

  private static Links toLinksProto(io.opencensus.trace.export.SpanData.Links links) {
    final Links.Builder linksBuilder =
        Links.newBuilder().setDroppedLinksCount(links.getDroppedLinksCount());
    for (io.opencensus.trace.Link link : links.getLinks()) {
      linksBuilder.addLink(toLinkProto(link));
    }
    return linksBuilder.build();
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // Start a new span with explicit 1/10000 sampling probability to avoid the case when user
    // sets the default sampler to always sample and we get the gRPC span of the stackdriver
    // export call always sampled and go to an infinite loop.
    try (Scope scope =
        tracer
            .spanBuilder("ExportStackdriverTraces")
            .setSampler(probabilitySpampler)
            .setRecordEvents(true)
            .startScopedSpan()) {
      List<Span> spans = new ArrayList<>(spanDataList.size());
      for (SpanData spanData : spanDataList) {
        spans.add(generateSpan(spanData));
      }
      // Sync call because it is already called for a batch of data, and on a separate thread.
      // TODO(bdrutu): Consider to make this async in the future.
      traceServiceClient.batchWriteSpans(projectName, spans);
    }
  }
}
