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
import com.google.devtools.cloudtrace.v2.AttributeValue;
import com.google.devtools.cloudtrace.v2.AttributeValue.Builder;
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
import io.opencensus.contrib.monitoredresource.util.MonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResourceUtils;
import io.opencensus.contrib.monitoredresource.util.ResourceType;
import io.opencensus.trace.Annotation;
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

  @javax.annotation.Nullable
  private static final MonitoredResource RESOURCE = MonitoredResourceUtils.getDefaultResource();

  // Only initialize once.
  private static final Map<String, AttributeValue> RESOURCE_LABELS = getResourceLabels(RESOURCE);

  // Constant functions for AttributeValue.
  private static final Function<String, AttributeValue> STRING_ATTRIBUTE_VALUE_FUNCTION =
      new Function<String, AttributeValue>() {
        @Override
        public AttributeValue apply(String stringValue) {
          Builder attributeValueBuilder = AttributeValue.newBuilder();
          attributeValueBuilder.setStringValue(toTruncatableStringProto(stringValue));
          return attributeValueBuilder.build();
        }
      };
  private static final Function<Boolean, AttributeValue> BOOLEAN_ATTRIBUTE_VALUE_FUNCTION =
      new Function<Boolean, AttributeValue>() {
        @Override
        public AttributeValue apply(Boolean booleanValue) {
          Builder attributeValueBuilder = AttributeValue.newBuilder();
          attributeValueBuilder.setBoolValue(booleanValue);
          return attributeValueBuilder.build();
        }
      };
  private static final Function<Long, AttributeValue> LONG_ATTRIBUTE_VALUE_FUNCTION =
      new Function<Long, AttributeValue>() {
        @Override
        public AttributeValue apply(Long longValue) {
          Builder attributeValueBuilder = AttributeValue.newBuilder();
          attributeValueBuilder.setIntValue(longValue);
          return attributeValueBuilder.build();
        }
      };

  private final String projectId;
  private final TraceServiceClient traceServiceClient;
  private final ProjectName projectName;

  @VisibleForTesting
  StackdriverV2ExporterHandler(String projectId, TraceServiceClient traceServiceClient) {
    this.projectId = checkNotNull(projectId, "projectId");
    this.traceServiceClient = traceServiceClient;
    projectName = ProjectName.of(this.projectId);

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

  @VisibleForTesting
  Span generateSpan(SpanData spanData, Map<String, AttributeValue> resourceLabels) {
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
            .setAttributes(toAttributesProto(spanData.getAttributes(), resourceLabels))
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
      Map<String, AttributeValue> resourceLabels) {
    Attributes.Builder attributesBuilder =
        toAttributesBuilderProto(
            attributes.getAttributeMap(), attributes.getDroppedAttributesCount());
    attributesBuilder.putAttributeMap(AGENT_LABEL_KEY, AGENT_LABEL_VALUE);
    for (Entry<String, AttributeValue> entry : resourceLabels.entrySet()) {
      attributesBuilder.putAttributeMap(entry.getKey(), entry.getValue());
    }
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

  @VisibleForTesting
  static Map<String, AttributeValue> getResourceLabels(
      @javax.annotation.Nullable MonitoredResource resource) {
    if (resource == null) {
      return Collections.emptyMap();
    }
    Map<String, AttributeValue> resourceLabels = new HashMap<String, AttributeValue>();
    ResourceType resourceType = resource.getResourceType();
    switch (resourceType) {
      case AWS_EC2_INSTANCE:
        AwsEc2InstanceMonitoredResource awsEc2InstanceMonitoredResource =
            (AwsEc2InstanceMonitoredResource) resource;
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "aws_account",
            awsEc2InstanceMonitoredResource.getAccount());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "instance_id",
            awsEc2InstanceMonitoredResource.getInstanceId());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "region",
            "aws:" + awsEc2InstanceMonitoredResource.getRegion());
        return Collections.unmodifiableMap(resourceLabels);
      case GCP_GCE_INSTANCE:
        GcpGceInstanceMonitoredResource gcpGceInstanceMonitoredResource =
            (GcpGceInstanceMonitoredResource) resource;
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "project_id",
            gcpGceInstanceMonitoredResource.getAccount());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "instance_id",
            gcpGceInstanceMonitoredResource.getInstanceId());
        putToResourceAttributeMap(
            resourceLabels, resourceType, "zone", gcpGceInstanceMonitoredResource.getZone());
        return Collections.unmodifiableMap(resourceLabels);
      case GCP_GKE_CONTAINER:
        GcpGkeContainerMonitoredResource gcpGkeContainerMonitoredResource =
            (GcpGkeContainerMonitoredResource) resource;
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "project_id",
            gcpGkeContainerMonitoredResource.getAccount());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "instance_id",
            gcpGkeContainerMonitoredResource.getInstanceId());
        putToResourceAttributeMap(
            resourceLabels, resourceType, "zone", gcpGkeContainerMonitoredResource.getZone());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "cluster_name",
            gcpGkeContainerMonitoredResource.getClusterName());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "container_name",
            gcpGkeContainerMonitoredResource.getContainerName());
        putToResourceAttributeMap(
            resourceLabels,
            resourceType,
            "namespace_id",
            gcpGkeContainerMonitoredResource.getNamespaceId());
        putToResourceAttributeMap(
            resourceLabels, resourceType, "pod_id", gcpGkeContainerMonitoredResource.getPodId());
        return Collections.unmodifiableMap(resourceLabels);
    }
    return Collections.emptyMap();
  }

  private static void putToResourceAttributeMap(
      Map<String, AttributeValue> map,
      ResourceType resourceType,
      String attributeName,
      String attributeValue) {
    map.put(
        createResourceLabelKey(resourceType, attributeName),
        toStringAttributeValueProto(attributeValue));
  }

  @VisibleForTesting
  static String createResourceLabelKey(ResourceType resourceType, String resourceAttribute) {
    return String.format("g.co/r/%s/%s", mapToStringResourceType(resourceType), resourceAttribute);
  }

  private static String mapToStringResourceType(ResourceType resourceType) {
    switch (resourceType) {
      case GCP_GCE_INSTANCE:
        return "gce_instance";
      case GCP_GKE_CONTAINER:
        return "gke_container";
      case AWS_EC2_INSTANCE:
        return "aws_ec2_instance";
    }
    throw new IllegalArgumentException("Unknown resource type.");
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

  private static AttributeValue toAttributeValueProto(
      io.opencensus.trace.AttributeValue attributeValue) {
    return attributeValue.match(
        STRING_ATTRIBUTE_VALUE_FUNCTION,
        BOOLEAN_ATTRIBUTE_VALUE_FUNCTION,
        LONG_ATTRIBUTE_VALUE_FUNCTION,
        Functions.</*@Nullable*/ AttributeValue>returnNull());
  }

  private static Link.Type toLinkTypeProto(io.opencensus.trace.Link.Type type) {
    if (type == io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN) {
      return Link.Type.PARENT_LINKED_SPAN;
    } else {
      return Link.Type.CHILD_LINKED_SPAN;
    }
  }

  private static String toDisplayName(String spanName, Kind spanKind) {
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
    try (Scope scope =
        tracer
            .spanBuilder("ExportStackdriverTraces")
            .setSampler(probabilitySampler)
            .setRecordEvents(true)
            .startScopedSpan()) {
      List<Span> spans = new ArrayList<>(spanDataList.size());
      for (SpanData spanData : spanDataList) {
        spans.add(generateSpan(spanData, RESOURCE_LABELS));
      }
      // Sync call because it is already called for a batch of data, and on a separate thread.
      // TODO(bdrutu): Consider to make this async in the future.
      traceServiceClient.batchWriteSpans(projectName, spans);
    }
  }
}
