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
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;
import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.TraceServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
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
import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.rpc.Status;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.OpenCensusLibraryInformation;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.contrib.resource.util.ResourceUtils;
import io.opencensus.resource.Resource;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Exporter to Stackdriver Trace API v2. */
final class StackdriverV2ExporterHandler extends SpanExporter.Handler {

  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);
  private static final String AGENT_LABEL_KEY = "g.co/agent";
  private static final String AGENT_LABEL_VALUE_STRING =
      "opencensus-java [" + OpenCensusLibraryInformation.VERSION + "]";
  private static final String SERVER_PREFIX = "Recv.";
  private static final String CLIENT_PREFIX = "Sent.";
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

  // Only initialize once.
  private static final Map<String, AttributeValue> RESOURCE_LABELS =
      getResourceLabels(ResourceUtils.detectResource());

  // Constant functions for AttributeValue.
  private static final Function<String, /*@Nullable*/ AttributeValue> stringAttributeValueFunction =
      new Function<String, /*@Nullable*/ AttributeValue>() {
        @Override
        public AttributeValue apply(String stringValue) {
          return AttributeValue.newBuilder()
              .setStringValue(toTruncatableStringProto(stringValue))
              .build();
        }
      };
  private static final Function<Boolean, /*@Nullable*/ AttributeValue>
      booleanAttributeValueFunction =
          new Function<Boolean, /*@Nullable*/ AttributeValue>() {
            @Override
            public AttributeValue apply(Boolean booleanValue) {
              return AttributeValue.newBuilder().setBoolValue(booleanValue).build();
            }
          };
  private static final Function<Long, /*@Nullable*/ AttributeValue> longAttributeValueFunction =
      new Function<Long, /*@Nullable*/ AttributeValue>() {
        @Override
        public AttributeValue apply(Long longValue) {
          return AttributeValue.newBuilder().setIntValue(longValue).build();
        }
      };
  private static final Function<Double, /*@Nullable*/ AttributeValue> doubleAttributeValueFunction =
      new Function<Double, /*@Nullable*/ AttributeValue>() {
        @Override
        public AttributeValue apply(Double doubleValue) {
          AttributeValue.Builder attributeValueBuilder = AttributeValue.newBuilder();
          // TODO: set double value if Stackdriver Trace support it in the future.
          attributeValueBuilder.setStringValue(
              toTruncatableStringProto(String.valueOf(doubleValue)));
          return attributeValueBuilder.build();
        }
      };
  private static final String EXPORT_STACKDRIVER_TRACES = "ExportStackdriverTraces";
  private static final EndSpanOptions END_SPAN_OPTIONS =
      EndSpanOptions.builder().setSampleToLocalSpanStore(true).build();

  private final Map<String, AttributeValue> fixedAttributes;
  private final String projectId;
  private final TraceServiceClient traceServiceClient;
  private final ProjectName projectName;

  private StackdriverV2ExporterHandler(
      String projectId,
      TraceServiceClient traceServiceClient,
      Map<String, io.opencensus.trace.AttributeValue> fixedAttributes) {
    this.projectId = projectId;
    this.traceServiceClient = traceServiceClient;
    this.fixedAttributes = new HashMap<>();
    for (Map.Entry<String, io.opencensus.trace.AttributeValue> label : fixedAttributes.entrySet()) {
      AttributeValue value = toAttributeValueProto(label.getValue());
      if (value != null) {
        this.fixedAttributes.put(label.getKey(), value);
      }
    }
    projectName = ProjectName.of(this.projectId);
  }

  static StackdriverV2ExporterHandler createWithStub(
      String projectId,
      TraceServiceClient traceServiceClient,
      Map<String, io.opencensus.trace.AttributeValue> fixedAttributes) {
    return new StackdriverV2ExporterHandler(projectId, traceServiceClient, fixedAttributes);
  }

  static StackdriverV2ExporterHandler createWithCredentials(
      String projectId,
      Credentials credentials,
      Map<String, io.opencensus.trace.AttributeValue> fixedAttributes,
      @javax.annotation.Nullable Duration deadline)
      throws IOException {
    TraceServiceSettings.Builder builder =
        TraceServiceSettings.newBuilder()
            .setCredentialsProvider(
                FixedCredentialsProvider.create(checkNotNull(credentials, "credentials")));
    if (deadline != null) {
      // We only use the batchWriteSpans API in this exporter.
      builder
          .batchWriteSpansSettings()
          .setSimpleTimeoutNoRetries(org.threeten.bp.Duration.ofMillis(deadline.toMillis()));
    } else {
      /*
       * Default retry settings for Stackdriver Trace client is:
       * settings =
       *   RetrySettings.newBuilder()
       *       .setInitialRetryDelay(Duration.ofMillis(100L))
       *       .setRetryDelayMultiplier(1.2)
       *       .setMaxRetryDelay(Duration.ofMillis(1000L))
       *       .setInitialRpcTimeout(Duration.ofMillis(30000L))
       *       .setRpcTimeoutMultiplier(1.5)
       *       .setMaxRpcTimeout(Duration.ofMillis(60000L))
       *       .setTotalTimeout(Duration.ofMillis(120000L))
       *       .build();
       *
       * Override the default settings with settings that don't retry.
       */
      builder
          .batchWriteSpansSettings()
          .setRetryableCodes()
          // RetrySettings.newBuilder().build() returns settings with no retries.
          .setRetrySettings(RetrySettings.newBuilder().build());
    }
    return new StackdriverV2ExporterHandler(
        projectId, TraceServiceClient.create(builder.build()), fixedAttributes);
  }

  @VisibleForTesting
  Span generateSpan(
      SpanData spanData,
      Map<String, AttributeValue> resourceLabels,
      Map<String, AttributeValue> fixedAttributes) {
    SpanContext context = spanData.getContext();
    final String spanIdHex = context.getSpanId().toLowerBase16();
    SpanName spanName =
        SpanName.newBuilder()
            .setProject(projectId)
            .setTrace(context.getTraceId().toLowerBase16())
            .setSpan(spanIdHex)
            .build();
    Span.Builder spanBuilder =
        Span.newBuilder()
            .setName(spanName.toString())
            .setSpanId(spanIdHex)
            .setDisplayName(
                toTruncatableStringProto(toDisplayName(spanData.getName(), spanData.getKind())))
            .setStartTime(toTimestampProto(spanData.getStartTimestamp()))
            .setAttributes(
                toAttributesProto(spanData.getAttributes(), resourceLabels, fixedAttributes))
            .setTimeEvents(
                toTimeEventsProto(spanData.getAnnotations(), spanData.getMessageEvents()));
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
      spanBuilder.setParentSpanId(spanData.getParentSpanId().toLowerBase16());
    }
    /*@Nullable*/ Boolean hasRemoteParent = spanData.getHasRemoteParent();
    if (hasRemoteParent != null) {
      spanBuilder.setSameProcessAsParentSpan(BoolValue.of(!hasRemoteParent));
    }
    return spanBuilder.build();
  }

  private static Span.TimeEvents toTimeEventsProto(
      TimedEvents<Annotation> annotationTimedEvents,
      TimedEvents<io.opencensus.trace.MessageEvent> messageEventTimedEvents) {
    Span.TimeEvents.Builder timeEventsBuilder = Span.TimeEvents.newBuilder();
    timeEventsBuilder.setDroppedAnnotationsCount(annotationTimedEvents.getDroppedEventsCount());
    for (TimedEvent<Annotation> annotation : annotationTimedEvents.getEvents()) {
      timeEventsBuilder.addTimeEvent(toTimeAnnotationProto(annotation));
    }
    timeEventsBuilder.setDroppedMessageEventsCount(messageEventTimedEvents.getDroppedEventsCount());
    for (TimedEvent<io.opencensus.trace.MessageEvent> networkEvent :
        messageEventTimedEvents.getEvents()) {
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

  private static TimeEvent toTimeMessageEventProto(
      TimedEvent<io.opencensus.trace.MessageEvent> timedEvent) {
    TimeEvent.Builder timeEventBuilder =
        TimeEvent.newBuilder().setTime(toTimestampProto(timedEvent.getTimestamp()));
    io.opencensus.trace.MessageEvent messageEvent = timedEvent.getEvent();
    timeEventBuilder.setMessageEvent(
        TimeEvent.MessageEvent.newBuilder()
            .setId(messageEvent.getMessageId())
            .setCompressedSizeBytes(messageEvent.getCompressedMessageSize())
            .setUncompressedSizeBytes(messageEvent.getUncompressedMessageSize())
            .setType(toMessageEventTypeProto(messageEvent))
            .build());
    return timeEventBuilder.build();
  }

  private static TimeEvent.MessageEvent.Type toMessageEventTypeProto(
      io.opencensus.trace.MessageEvent messageEvent) {
    if (messageEvent.getType() == Type.RECEIVED) {
      return MessageEvent.Type.RECEIVED;
    } else {
      return MessageEvent.Type.SENT;
    }
  }

  // These are the attributes of the Span, where usually we may add more attributes like the agent.
  private static Attributes toAttributesProto(
      io.opencensus.trace.export.SpanData.Attributes attributes,
      Map<String, AttributeValue> resourceLabels,
      Map<String, AttributeValue> fixedAttributes) {
    Attributes.Builder attributesBuilder =
        toAttributesBuilderProto(
            attributes.getAttributeMap(), attributes.getDroppedAttributesCount());
    attributesBuilder.putAttributeMap(AGENT_LABEL_KEY, AGENT_LABEL_VALUE);
    for (Entry<String, AttributeValue> entry : resourceLabels.entrySet()) {
      attributesBuilder.putAttributeMap(entry.getKey(), entry.getValue());
    }
    for (Entry<String, AttributeValue> entry : fixedAttributes.entrySet()) {
      attributesBuilder.putAttributeMap(entry.getKey(), entry.getValue());
    }
    return attributesBuilder.build();
  }

  private static Attributes.Builder toAttributesBuilderProto(
      Map<String, io.opencensus.trace.AttributeValue> attributes, int droppedAttributesCount) {
    Attributes.Builder attributesBuilder =
        Attributes.newBuilder().setDroppedAttributesCount(droppedAttributesCount);
    for (Map.Entry<String, io.opencensus.trace.AttributeValue> label : attributes.entrySet()) {
      AttributeValue value = toAttributeValueProto(label.getValue());
      if (value != null) {
        attributesBuilder.putAttributeMap(mapKey(label.getKey()), value);
      }
    }
    return attributesBuilder;
  }

  @VisibleForTesting
  static Map<String, AttributeValue> getResourceLabels(
      @javax.annotation.Nullable Resource resource) {
    if (resource == null) {
      return Collections.emptyMap();
    }
    Map<String, AttributeValue> resourceLabels = new LinkedHashMap<String, AttributeValue>();
    for (Map.Entry<String, String> entry : resource.getLabels().entrySet()) {
      putToResourceAttributeMap(resourceLabels, entry.getKey(), entry.getValue());
    }
    return Collections.unmodifiableMap(resourceLabels);
  }

  private static void putToResourceAttributeMap(
      Map<String, AttributeValue> map, String attributeName, String attributeValue) {
    map.put(createResourceLabelKey(attributeName), toStringAttributeValueProto(attributeValue));
  }

  @VisibleForTesting
  static String createResourceLabelKey(String resourceAttribute) {
    return "g.co/r/" + resourceAttribute;
  }

  @VisibleForTesting
  static AttributeValue toStringAttributeValueProto(String value) {
    return AttributeValue.newBuilder().setStringValue(toTruncatableStringProto(value)).build();
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

  @javax.annotation.Nullable
  private static AttributeValue toAttributeValueProto(
      io.opencensus.trace.AttributeValue attributeValue) {
    return attributeValue.match(
        stringAttributeValueFunction,
        booleanAttributeValueFunction,
        longAttributeValueFunction,
        doubleAttributeValueFunction,
        Functions.</*@Nullable*/ AttributeValue>returnNull());
  }

  private static Link.Type toLinkTypeProto(io.opencensus.trace.Link.Type type) {
    if (type == io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN) {
      return Link.Type.PARENT_LINKED_SPAN;
    } else {
      return Link.Type.CHILD_LINKED_SPAN;
    }
  }

  private static String toDisplayName(String spanName, @javax.annotation.Nullable Kind spanKind) {
    if (spanKind == Kind.SERVER && !spanName.startsWith(SERVER_PREFIX)) {
      return SERVER_PREFIX + spanName;
    }

    if (spanKind == Kind.CLIENT && !spanName.startsWith(CLIENT_PREFIX)) {
      return CLIENT_PREFIX + spanName;
    }

    return spanName;
  }

  private static Link toLinkProto(io.opencensus.trace.Link link) {
    checkNotNull(link);
    return Link.newBuilder()
        .setTraceId(link.getTraceId().toLowerBase16())
        .setSpanId(link.getSpanId().toLowerBase16())
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
    io.opencensus.trace.Span span =
        tracer
            .spanBuilder(EXPORT_STACKDRIVER_TRACES)
            .setSampler(probabilitySampler)
            .setRecordEvents(true)
            .startSpan();
    Scope scope = tracer.withSpan(span);
    try {
      List<Span> spans = new ArrayList<>(spanDataList.size());
      for (SpanData spanData : spanDataList) {
        spans.add(generateSpan(spanData, RESOURCE_LABELS, fixedAttributes));
      }
      // Sync call because it is already called for a batch of data, and on a separate thread.
      // TODO(bdrutu): Consider to make this async in the future.
      traceServiceClient.batchWriteSpans(projectName, spans);
    } finally {
      scope.close();
      span.end(END_SPAN_OPTIONS);
    }
  }
}
