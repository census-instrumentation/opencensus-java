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

package io.opencensus.exporter.trace.stackdriver;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.contrib.monitoredresource.util.ResourceType.AWS_EC2_INSTANCE;
import static io.opencensus.contrib.monitoredresource.util.ResourceType.GCP_GCE_INSTANCE;
import static io.opencensus.contrib.monitoredresource.util.ResourceType.GCP_GKE_CONTAINER;
import static io.opencensus.exporter.trace.stackdriver.StackdriverV2ExporterHandler.createResourceLabelKey;
import static io.opencensus.exporter.trace.stackdriver.StackdriverV2ExporterHandler.toStringAttributeValueProto;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.cloudtrace.v2.AttributeValue;
import com.google.devtools.cloudtrace.v2.Span;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent.MessageEvent;
import com.google.devtools.cloudtrace.v2.StackTrace;
import com.google.devtools.cloudtrace.v2.TruncatableString;
import com.google.protobuf.Int32Value;
import io.opencensus.common.Timestamp;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for proto conversions in {@link StackdriverV2ExporterHandler}. */
@RunWith(JUnit4.class)
public final class StackdriverV2ExporterHandlerProtoTest {

  private static final Credentials FAKE_CREDENTIALS =
      GoogleCredentials.newBuilder().setAccessToken(new AccessToken("fake", new Date(100))).build();
  // OpenCensus constants
  private static final Timestamp startTimestamp = Timestamp.create(123, 456);
  private static final Timestamp eventTimestamp1 = Timestamp.create(123, 457);
  private static final Timestamp eventTimestamp2 = Timestamp.create(123, 458);
  private static final Timestamp eventTimestamp3 = Timestamp.create(123, 459);
  private static final Timestamp endTimestamp = Timestamp.create(123, 460);

  private static final String PROJECT_ID = "PROJECT_ID";
  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
  private static final String SPAN_ID = "24aa0b2d371f48c9";
  private static final String PARENT_SPAN_ID = "71da8d631536f5f1";
  private static final String SPAN_NAME = "MySpanName";
  private static final String SD_SPAN_NAME =
      String.format("projects/%s/traces/%s/spans/%s", PROJECT_ID, TRACE_ID, SPAN_ID);
  private static final String ANNOTATION_TEXT = "MyAnnotationText";
  private static final String ATTRIBUTE_KEY_1 = "MyAttributeKey1";
  private static final String ATTRIBUTE_KEY_2 = "MyAttributeKey2";

  private static final int DROPPED_ATTRIBUTES_COUNT = 1;
  private static final int DROPPED_ANNOTATIONS_COUNT = 2;
  private static final int DROPPED_NETWORKEVENTS_COUNT = 3;
  private static final int DROPPED_LINKS_COUNT = 4;
  private static final int CHILD_SPAN_COUNT = 13;

  private static final Annotation annotation = Annotation.fromDescription(ANNOTATION_TEXT);
  private static final io.opencensus.trace.MessageEvent recvMessageEvent =
      io.opencensus.trace.MessageEvent.builder(io.opencensus.trace.MessageEvent.Type.RECEIVED, 1)
          .build();
  private static final io.opencensus.trace.MessageEvent sentMessageEvent =
      io.opencensus.trace.MessageEvent.builder(io.opencensus.trace.MessageEvent.Type.SENT, 1)
          .build();
  private static final Status status = Status.DEADLINE_EXCEEDED.withDescription("TooSlow");
  private static final SpanId parentSpanId = SpanId.fromLowerBase16(PARENT_SPAN_ID);
  private static final SpanId spanId = SpanId.fromLowerBase16(SPAN_ID);
  private static final TraceId traceId = TraceId.fromLowerBase16(TRACE_ID);
  private static final TraceOptions traceOptions = TraceOptions.DEFAULT;
  private static final SpanContext spanContext = SpanContext.create(traceId, spanId, traceOptions);

  private static final List<TimedEvent<Annotation>> annotationsList =
      ImmutableList.of(
          SpanData.TimedEvent.create(eventTimestamp1, annotation),
          SpanData.TimedEvent.create(eventTimestamp3, annotation));
  private static final List<TimedEvent<io.opencensus.trace.MessageEvent>> networkEventsList =
      ImmutableList.of(
          SpanData.TimedEvent.create(eventTimestamp1, recvMessageEvent),
          SpanData.TimedEvent.create(eventTimestamp2, sentMessageEvent));
  private static final List<Link> linksList =
      ImmutableList.of(Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN));

  private static final SpanData.Attributes attributes =
      SpanData.Attributes.create(
          ImmutableMap.of(
              ATTRIBUTE_KEY_1,
              io.opencensus.trace.AttributeValue.longAttributeValue(10L),
              ATTRIBUTE_KEY_2,
              io.opencensus.trace.AttributeValue.booleanAttributeValue(true)),
          DROPPED_ATTRIBUTES_COUNT);
  private static final TimedEvents<Annotation> annotations =
      TimedEvents.create(annotationsList, DROPPED_ANNOTATIONS_COUNT);
  private static final TimedEvents<io.opencensus.trace.MessageEvent> messageEvents =
      TimedEvents.create(networkEventsList, DROPPED_NETWORKEVENTS_COUNT);
  private static final SpanData.Links links = SpanData.Links.create(linksList, DROPPED_LINKS_COUNT);
  private static final Map<String, AttributeValue> EMPTY_RESOURCE_LABELS = Collections.emptyMap();
  private static final AwsEc2InstanceMonitoredResource AWS_EC2_INSTANCE_MONITORED_RESOURCE =
      AwsEc2InstanceMonitoredResource.create("my-project", "my-instance", "us-east-1");
  private static final GcpGceInstanceMonitoredResource GCP_GCE_INSTANCE_MONITORED_RESOURCE =
      GcpGceInstanceMonitoredResource.create("my-project", "my-instance", "us-east1");
  private static final GcpGkeContainerMonitoredResource GCP_GKE_CONTAINER_MONITORED_RESOURCE =
      GcpGkeContainerMonitoredResource.create(
          "my-project", "cluster", "container", "namespace", "my-instance", "pod", "us-east1");
  private static final ImmutableMap<String, AttributeValue> AWS_RESOURCE_LABELS =
      ImmutableMap.of(
          createResourceLabelKey(AWS_EC2_INSTANCE, "aws_account"),
          toStringAttributeValueProto("my-project"),
          createResourceLabelKey(AWS_EC2_INSTANCE, "instance_id"),
          toStringAttributeValueProto("my-instance"),
          createResourceLabelKey(AWS_EC2_INSTANCE, "region"),
          toStringAttributeValueProto("aws:us-east-1"));
  private static final ImmutableMap<String, AttributeValue> GCE_RESOURCE_LABELS =
      ImmutableMap.of(
          createResourceLabelKey(GCP_GCE_INSTANCE, "project_id"),
          toStringAttributeValueProto("my-project"),
          createResourceLabelKey(GCP_GCE_INSTANCE, "instance_id"),
          toStringAttributeValueProto("my-instance"),
          createResourceLabelKey(GCP_GCE_INSTANCE, "zone"),
          toStringAttributeValueProto("us-east1"));
  private static final ImmutableMap<String, AttributeValue> GKE_RESOURCE_LABELS =
      ImmutableMap.<String, AttributeValue>builder()
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "project_id"),
              toStringAttributeValueProto("my-project"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "cluster_name"),
              toStringAttributeValueProto("cluster"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "container_name"),
              toStringAttributeValueProto("container"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "namespace_name"),
              toStringAttributeValueProto("namespace"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "instance_id"),
              toStringAttributeValueProto("my-instance"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "pod_name"),
              toStringAttributeValueProto("pod"))
          .put(
              createResourceLabelKey(GCP_GKE_CONTAINER, "location"),
              toStringAttributeValueProto("us-east1"))
          .build();

  private StackdriverV2ExporterHandler handler;

  @Before
  public void setUp() throws IOException {
    handler = StackdriverV2ExporterHandler.createWithCredentials(FAKE_CREDENTIALS, PROJECT_ID);
  }

  @Test
  public void generateSpan() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            SPAN_NAME,
            null,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    TimeEvent annotationTimeEvent1 =
        TimeEvent.newBuilder()
            .setAnnotation(
                TimeEvent.Annotation.newBuilder()
                    .setDescription(
                        TruncatableString.newBuilder().setValue(ANNOTATION_TEXT).build())
                    .setAttributes(Span.Attributes.newBuilder().build())
                    .build())
            .setTime(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(eventTimestamp1.getSeconds())
                    .setNanos(eventTimestamp1.getNanos())
                    .build())
            .build();
    TimeEvent annotationTimeEvent2 =
        TimeEvent.newBuilder()
            .setAnnotation(
                TimeEvent.Annotation.newBuilder()
                    .setDescription(
                        TruncatableString.newBuilder().setValue(ANNOTATION_TEXT).build())
                    .setAttributes(Span.Attributes.newBuilder().build())
                    .build())
            .setTime(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(eventTimestamp3.getSeconds())
                    .setNanos(eventTimestamp3.getNanos())
                    .build())
            .build();

    TimeEvent sentTimeEvent =
        TimeEvent.newBuilder()
            .setMessageEvent(
                TimeEvent.MessageEvent.newBuilder()
                    .setType(MessageEvent.Type.SENT)
                    .setId(sentMessageEvent.getMessageId()))
            .setTime(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(eventTimestamp2.getSeconds())
                    .setNanos(eventTimestamp2.getNanos())
                    .build())
            .build();
    TimeEvent recvTimeEvent =
        TimeEvent.newBuilder()
            .setMessageEvent(
                TimeEvent.MessageEvent.newBuilder()
                    .setType(MessageEvent.Type.RECEIVED)
                    .setId(recvMessageEvent.getMessageId()))
            .setTime(
                com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(eventTimestamp1.getSeconds())
                    .setNanos(eventTimestamp1.getNanos())
                    .build())
            .build();

    Span.Links spanLinks =
        Span.Links.newBuilder()
            .setDroppedLinksCount(DROPPED_LINKS_COUNT)
            .addLink(
                Span.Link.newBuilder()
                    .setType(Span.Link.Type.CHILD_LINKED_SPAN)
                    .setTraceId(TRACE_ID)
                    .setSpanId(SPAN_ID)
                    .setAttributes(Span.Attributes.newBuilder().build())
                    .build())
            .build();

    com.google.rpc.Status spanStatus =
        com.google.rpc.Status.newBuilder()
            .setCode(com.google.rpc.Code.DEADLINE_EXCEEDED.getNumber())
            .setMessage("TooSlow")
            .build();

    com.google.protobuf.Timestamp startTime =
        com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(startTimestamp.getSeconds())
            .setNanos(startTimestamp.getNanos())
            .build();
    com.google.protobuf.Timestamp endTime =
        com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(endTimestamp.getSeconds())
            .setNanos(endTimestamp.getNanos())
            .build();

    Span span = handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS);
    assertThat(span.getName()).isEqualTo(SD_SPAN_NAME);
    assertThat(span.getSpanId()).isEqualTo(SPAN_ID);
    assertThat(span.getParentSpanId()).isEqualTo(PARENT_SPAN_ID);
    assertThat(span.getDisplayName())
        .isEqualTo(TruncatableString.newBuilder().setValue(SPAN_NAME).build());
    assertThat(span.getStartTime()).isEqualTo(startTime);
    assertThat(span.getEndTime()).isEqualTo(endTime);
    assertThat(span.getAttributes().getDroppedAttributesCount())
        .isEqualTo(DROPPED_ATTRIBUTES_COUNT);
    // The generated attributes map contains more values (e.g. agent). We only test what we added.
    assertThat(span.getAttributes().getAttributeMapMap())
        .containsEntry(ATTRIBUTE_KEY_1, AttributeValue.newBuilder().setIntValue(10L).build());
    assertThat(span.getAttributes().getAttributeMapMap())
        .containsEntry(ATTRIBUTE_KEY_2, AttributeValue.newBuilder().setBoolValue(true).build());
    // TODO(@Hailong): add stack trace test in the future.
    assertThat(span.getStackTrace()).isEqualTo(StackTrace.newBuilder().build());
    assertThat(span.getTimeEvents().getDroppedMessageEventsCount())
        .isEqualTo(DROPPED_NETWORKEVENTS_COUNT);
    assertThat(span.getTimeEvents().getDroppedAnnotationsCount())
        .isEqualTo(DROPPED_ANNOTATIONS_COUNT);
    assertThat(span.getTimeEvents().getTimeEventList())
        .containsAllOf(annotationTimeEvent1, annotationTimeEvent2, sentTimeEvent, recvTimeEvent);
    assertThat(span.getLinks()).isEqualTo(spanLinks);
    assertThat(span.getStatus()).isEqualTo(spanStatus);
    assertThat(span.getSameProcessAsParentSpan())
        .isEqualTo(com.google.protobuf.BoolValue.newBuilder().build());
    assertThat(span.getChildSpanCount())
        .isEqualTo(Int32Value.newBuilder().setValue(CHILD_SPAN_COUNT).build());
  }

  @Test
  public void getResourceLabels_AwsEc2ResourceLabels() {
    testGetResourceLabels(AWS_EC2_INSTANCE_MONITORED_RESOURCE, AWS_RESOURCE_LABELS);
  }

  @Test
  public void getResourceLabels_GceResourceLabels() {
    testGetResourceLabels(GCP_GCE_INSTANCE_MONITORED_RESOURCE, GCE_RESOURCE_LABELS);
  }

  @Test
  public void getResourceLabels_GkeResourceLabels() {
    testGetResourceLabels(GCP_GKE_CONTAINER_MONITORED_RESOURCE, GKE_RESOURCE_LABELS);
  }

  private static void testGetResourceLabels(
      MonitoredResource resource, Map<String, AttributeValue> expectedLabels) {
    Map<String, AttributeValue> actualLabels =
        StackdriverV2ExporterHandler.getResourceLabels(resource);
    assertThat(actualLabels).containsExactlyEntriesIn(expectedLabels);
  }

  @Test
  public void generateSpan_WithResourceLabels() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            SPAN_NAME,
            null,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    Span span = handler.generateSpan(spanData, AWS_RESOURCE_LABELS);
    Map<String, AttributeValue> attributeMap = span.getAttributes().getAttributeMapMap();
    assertThat(attributeMap.entrySet()).containsAllIn(AWS_RESOURCE_LABELS.entrySet());
  }

  @Test
  public void mapHttpAttributes() {
    Map<String, io.opencensus.trace.AttributeValue> attributesMap =
        new HashMap<String, io.opencensus.trace.AttributeValue>();

    attributesMap.put("http.host", io.opencensus.trace.AttributeValue.stringAttributeValue("host"));
    attributesMap.put(
        "http.method", io.opencensus.trace.AttributeValue.stringAttributeValue("method"));
    attributesMap.put("http.path", io.opencensus.trace.AttributeValue.stringAttributeValue("path"));
    attributesMap.put(
        "http.route", io.opencensus.trace.AttributeValue.stringAttributeValue("route"));
    attributesMap.put(
        "http.user_agent", io.opencensus.trace.AttributeValue.stringAttributeValue("user_agent"));
    attributesMap.put(
        "http.status_code", io.opencensus.trace.AttributeValue.longAttributeValue(200L));
    SpanData.Attributes httpAttributes = SpanData.Attributes.create(attributesMap, 0);

    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            SPAN_NAME,
            startTimestamp,
            httpAttributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);

    Span span = handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS);
    Map<String, AttributeValue> attributes = span.getAttributes().getAttributeMapMap();

    assertThat(attributes).containsEntry("/http/host", toStringAttributeValueProto("host"));
    assertThat(attributes).containsEntry("/http/method", toStringAttributeValueProto("method"));
    assertThat(attributes).containsEntry("/http/path", toStringAttributeValueProto("path"));
    assertThat(attributes).containsEntry("/http/route", toStringAttributeValueProto("route"));
    assertThat(attributes)
        .containsEntry("/http/user_agent", toStringAttributeValueProto("user_agent"));
    assertThat(attributes)
        .containsEntry("/http/status_code", AttributeValue.newBuilder().setIntValue(200L).build());
  }

  @Test
  public void generateSpanName_ForServer() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            SPAN_NAME,
            Kind.SERVER,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    assertThat(handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS).getDisplayName().getValue())
        .isEqualTo("Recv." + SPAN_NAME);
  }

  @Test
  public void generateSpanName_ForServerWithRecv() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            "Recv." + SPAN_NAME,
            Kind.SERVER,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    assertThat(handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS).getDisplayName().getValue())
        .isEqualTo("Recv." + SPAN_NAME);
  }

  @Test
  public void generateSpanName_ForClient() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            SPAN_NAME,
            Kind.CLIENT,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    assertThat(handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS).getDisplayName().getValue())
        .isEqualTo("Sent." + SPAN_NAME);
  }

  @Test
  public void generateSpanName_ForClientWithSent() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ true,
            "Sent." + SPAN_NAME,
            Kind.CLIENT,
            startTimestamp,
            attributes,
            annotations,
            messageEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    assertThat(handler.generateSpan(spanData, EMPTY_RESOURCE_LABELS).getDisplayName().getValue())
        .isEqualTo("Sent." + SPAN_NAME);
  }
}
