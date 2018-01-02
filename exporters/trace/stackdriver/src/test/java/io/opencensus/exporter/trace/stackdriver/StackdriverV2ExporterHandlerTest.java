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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.stub.TraceServiceStub;
import com.google.devtools.cloudtrace.v2.AttributeValue;
import com.google.devtools.cloudtrace.v2.Span;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent;
import com.google.devtools.cloudtrace.v2.Span.TimeEvent.MessageEvent;
import com.google.devtools.cloudtrace.v2.StackTrace;
import com.google.devtools.cloudtrace.v2.TruncatableString;
import com.google.protobuf.Int32Value;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class StackdriverV2ExporterHandlerTest {
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
  private static final NetworkEvent recvNetworkEvent =
      NetworkEvent.builder(NetworkEvent.Type.RECV, 1).build();
  private static final NetworkEvent sentNetworkEvent =
      NetworkEvent.builder(NetworkEvent.Type.SENT, 1).build();
  private static final Status status = Status.DEADLINE_EXCEEDED.withDescription("TooSlow");
  private static final SpanId parentSpanId = SpanId.fromLowerBase16(PARENT_SPAN_ID);
  private static final SpanId spanId = SpanId.fromLowerBase16(SPAN_ID);
  private static final TraceId traceId = TraceId.fromLowerBase16(TRACE_ID);
  private static final TraceOptions traceOptions = TraceOptions.DEFAULT;
  private static final SpanContext spanContext = SpanContext.create(traceId, spanId, traceOptions);

  private final List<TimedEvent<Annotation>> annotationsList =
      new ArrayList<TimedEvent<Annotation>>();
  private final List<TimedEvent<NetworkEvent>> networkEventsList =
      new ArrayList<SpanData.TimedEvent<NetworkEvent>>();
  private final List<Link> linksList = new ArrayList<Link>();

  private SpanData.Attributes attributes;
  private TimedEvents<Annotation> annotations;
  private TimedEvents<NetworkEvent> networkEvents;
  private SpanData.Links links;

  // mock the service stub to provide a fake trace service.
  @Mock private TraceServiceStub traceServiceStub;
  private TraceServiceClient traceServiceClient;
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private StackdriverV2ExporterHandler handler;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    // TODO(@Hailong): this is a beta API and might change in the future.
    traceServiceClient = TraceServiceClient.create(traceServiceStub);
    handler = new StackdriverV2ExporterHandler(PROJECT_ID, traceServiceClient);

    Map<String, io.opencensus.trace.AttributeValue> attributesMap =
        new HashMap<String, io.opencensus.trace.AttributeValue>();
    attributesMap.put(ATTRIBUTE_KEY_1, io.opencensus.trace.AttributeValue.longAttributeValue(10L));
    attributesMap.put(
        ATTRIBUTE_KEY_2, io.opencensus.trace.AttributeValue.booleanAttributeValue(true));
    attributes = SpanData.Attributes.create(attributesMap, DROPPED_ATTRIBUTES_COUNT);

    annotationsList.add(SpanData.TimedEvent.create(eventTimestamp1, annotation));
    annotationsList.add(SpanData.TimedEvent.create(eventTimestamp3, annotation));
    annotations = TimedEvents.create(annotationsList, DROPPED_ANNOTATIONS_COUNT);

    networkEventsList.add(SpanData.TimedEvent.create(eventTimestamp1, recvNetworkEvent));
    networkEventsList.add(SpanData.TimedEvent.create(eventTimestamp2, sentNetworkEvent));
    networkEvents = TimedEvents.create(networkEventsList, DROPPED_NETWORKEVENTS_COUNT);

    linksList.add(Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN));
    links = SpanData.Links.create(linksList, DROPPED_LINKS_COUNT);
  }

  @Test
  public void generateSpan() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            true,
            SPAN_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
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
                    .setId(sentNetworkEvent.getMessageId()))
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
                    .setId(recvNetworkEvent.getMessageId()))
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

    Span span = handler.generateSpan(spanData);
    assertThat(span.getName()).isEqualTo(SD_SPAN_NAME);
    assertThat(span.getSpanId()).isEqualTo(SPAN_ID);
    assertThat(span.getParentSpanId()).isEqualTo(PARENT_SPAN_ID);
    assertThat(span.getDisplayName())
        .isEqualTo(TruncatableString.newBuilder().setValue(SPAN_NAME).build());
    assertThat(span.getStartTime()).isEqualTo(startTime);
    assertThat(span.getEndTime()).isEqualTo(endTime);
    assertThat(span.getAttributes().getDroppedAttributesCount())
        .isEqualTo(DROPPED_ATTRIBUTES_COUNT);
    // don't care about the agent attribute.
    assertThat(span.getAttributes().getAttributeMap())
        .containsEntry(ATTRIBUTE_KEY_1, AttributeValue.newBuilder().setIntValue(10L).build());
    assertThat(span.getAttributes().getAttributeMap())
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
  @SuppressWarnings("unchecked")
  public void export() {
    when(traceServiceStub.batchWriteSpansCallable())
        .thenThrow(new RuntimeException("TraceServiceStub called"));
    Collection<SpanData> spanDataList = Collections.<SpanData>emptyList();
    List<Span> spanList = Collections.<Span>emptyList();
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("TraceServiceStub called");
    handler.export(spanDataList);
  }
}
