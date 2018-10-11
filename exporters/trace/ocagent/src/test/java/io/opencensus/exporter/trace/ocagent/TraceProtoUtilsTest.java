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

package io.opencensus.exporter.trace.ocagent;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.trace.ocagent.TraceProtoUtils.toByteString;
import static io.opencensus.exporter.trace.ocagent.TraceProtoUtils.toTruncatableStringProto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt32Value;
import io.opencensus.common.Timestamp;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.ConstantSampler;
import io.opencensus.proto.trace.v1.ProbabilitySampler;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.Span.SpanKind;
import io.opencensus.proto.trace.v1.Span.TimeEvent;
import io.opencensus.proto.trace.v1.Span.TimeEvent.MessageEvent;
import io.opencensus.proto.trace.v1.TraceConfig;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Link;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import io.opencensus.trace.samplers.Samplers;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link TraceProtoUtils}. */
@RunWith(JUnit4.class)
public class TraceProtoUtilsTest {

  @Mock private io.opencensus.trace.config.TraceConfig mockTraceConfig;

  private static final TraceParams DEFAULT_PARAMS = TraceParams.DEFAULT;

  private static final Timestamp startTimestamp = Timestamp.create(123, 456);
  private static final Timestamp eventTimestamp1 = Timestamp.create(123, 457);
  private static final Timestamp eventTimestamp2 = Timestamp.create(123, 458);
  private static final Timestamp eventTimestamp3 = Timestamp.create(123, 459);
  private static final Timestamp endTimestamp = Timestamp.create(123, 460);

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
  private static final String SPAN_ID = "24aa0b2d371f48c9";
  private static final String PARENT_SPAN_ID = "71da8d631536f5f1";
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_TEXT = "MyAnnotationText";
  private static final String ATTRIBUTE_KEY_1 = "MyAttributeKey1";
  private static final String ATTRIBUTE_KEY_2 = "MyAttributeKey2";

  private static final String FIRST_KEY = "key_1";
  private static final String SECOND_KEY = "key_2";
  private static final String FIRST_VALUE = "value.1";
  private static final String SECOND_VALUE = "value.2";
  private static final Tracestate multiValueTracestate =
      Tracestate.builder().set(FIRST_KEY, FIRST_VALUE).set(SECOND_KEY, SECOND_VALUE).build();

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
  private static final SpanContext spanContext =
      SpanContext.create(traceId, spanId, traceOptions, multiValueTracestate);

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

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockTraceConfig.getActiveTraceParams()).thenReturn(DEFAULT_PARAMS);
    Mockito.doNothing()
        .when(mockTraceConfig)
        .updateActiveTraceParams(Mockito.any(TraceParams.class));
  }

  @Test
  public void toSpanProto() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            /* hasRemoteParent= */ false,
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
    TimeEvent annotationTimeEvent1 =
        TimeEvent.newBuilder()
            .setAnnotation(
                TimeEvent.Annotation.newBuilder()
                    .setDescription(toTruncatableStringProto(ANNOTATION_TEXT))
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
                    .setDescription(toTruncatableStringProto(ANNOTATION_TEXT))
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
                    .setTraceId(toByteString(traceId.getBytes()))
                    .setSpanId(toByteString(spanId.getBytes()))
                    .setAttributes(Span.Attributes.newBuilder().build())
                    .build())
            .build();

    io.opencensus.proto.trace.v1.Status spanStatus =
        io.opencensus.proto.trace.v1.Status.newBuilder()
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

    Span span = TraceProtoUtils.toSpanProto(spanData);
    assertThat(span.getName()).isEqualTo(toTruncatableStringProto(SPAN_NAME));
    assertThat(span.getTraceId()).isEqualTo(toByteString(traceId.getBytes()));
    assertThat(span.getSpanId()).isEqualTo(toByteString(spanId.getBytes()));
    assertThat(span.getParentSpanId()).isEqualTo(toByteString(parentSpanId.getBytes()));
    assertThat(span.getStartTime()).isEqualTo(startTime);
    assertThat(span.getEndTime()).isEqualTo(endTime);
    assertThat(span.getKind()).isEqualTo(SpanKind.CLIENT);
    assertThat(span.getAttributes().getDroppedAttributesCount())
        .isEqualTo(DROPPED_ATTRIBUTES_COUNT);
    // The generated attributes map contains more values (e.g. agent). We only test what we added.
    assertThat(span.getAttributes().getAttributeMapMap())
        .containsEntry(ATTRIBUTE_KEY_1, AttributeValue.newBuilder().setIntValue(10L).build());
    assertThat(span.getAttributes().getAttributeMapMap())
        .containsEntry(ATTRIBUTE_KEY_2, AttributeValue.newBuilder().setBoolValue(true).build());
    assertThat(span.getTimeEvents().getDroppedMessageEventsCount())
        .isEqualTo(DROPPED_NETWORKEVENTS_COUNT);
    assertThat(span.getTimeEvents().getDroppedAnnotationsCount())
        .isEqualTo(DROPPED_ANNOTATIONS_COUNT);
    assertThat(span.getTimeEvents().getTimeEventList())
        .containsAllOf(annotationTimeEvent1, annotationTimeEvent2, sentTimeEvent, recvTimeEvent);
    assertThat(span.getLinks()).isEqualTo(spanLinks);
    assertThat(span.getStatus()).isEqualTo(spanStatus);
    assertThat(span.getSameProcessAsParentSpan()).isEqualTo(BoolValue.of(true));
    assertThat(span.getChildSpanCount())
        .isEqualTo(UInt32Value.newBuilder().setValue(CHILD_SPAN_COUNT).build());
  }

  @Test
  public void toTraceConfigProto_AlwaysSampler() {
    assertThat(TraceProtoUtils.toTraceConfigProto(getTraceParams(Samplers.alwaysSample())))
        .isEqualTo(
            TraceConfig.newBuilder()
                .setConstantSampler(ConstantSampler.newBuilder().setDecision(true).build())
                .build());
  }

  @Test
  public void toTraceConfigProto_NeverSampler() {
    assertThat(TraceProtoUtils.toTraceConfigProto(getTraceParams(Samplers.neverSample())))
        .isEqualTo(
            TraceConfig.newBuilder()
                .setConstantSampler(ConstantSampler.newBuilder().setDecision(false).build())
                .build());
  }

  @Test
  public void toTraceConfigProto_ProbabilitySampler() {
    assertThat(TraceProtoUtils.toTraceConfigProto(getTraceParams(Samplers.probabilitySampler(0.5))))
        .isEqualTo(
            TraceConfig.newBuilder()
                .setProbabilitySampler(
                    ProbabilitySampler.newBuilder().setSamplingProbability(0.5).build())
                .build());
  }

  @Test
  public void fromTraceConfigProto_AlwaysSampler() {
    TraceConfig traceConfig =
        TraceConfig.newBuilder()
            .setConstantSampler(ConstantSampler.newBuilder().setDecision(true).build())
            .build();
    assertThat(TraceProtoUtils.fromTraceConfigProto(traceConfig, DEFAULT_PARAMS).getSampler())
        .isEqualTo(Samplers.alwaysSample());
  }

  @Test
  public void fromTraceConfigProto_NeverSampler() {
    TraceConfig traceConfig =
        TraceConfig.newBuilder()
            .setConstantSampler(ConstantSampler.newBuilder().setDecision(false).build())
            .build();
    assertThat(TraceProtoUtils.fromTraceConfigProto(traceConfig, DEFAULT_PARAMS).getSampler())
        .isEqualTo(Samplers.neverSample());
  }

  @Test
  public void fromTraceConfigProto_ProbabilitySampler() {
    TraceConfig traceConfig =
        TraceConfig.newBuilder()
            .setProbabilitySampler(
                ProbabilitySampler.newBuilder().setSamplingProbability(0.01).build())
            .build();
    assertThat(TraceProtoUtils.fromTraceConfigProto(traceConfig, DEFAULT_PARAMS).getSampler())
        .isEqualTo(Samplers.probabilitySampler(0.01));
  }

  @Test
  public void getCurrentTraceConfig() {
    TraceConfig configProto = TraceProtoUtils.toTraceConfigProto(DEFAULT_PARAMS);
    assertThat(TraceProtoUtils.getCurrentTraceConfig(mockTraceConfig)).isEqualTo(configProto);
    Mockito.verify(mockTraceConfig, Mockito.times(1)).getActiveTraceParams();
  }

  @Test
  public void applyUpdatedConfig() {
    TraceConfig configProto =
        TraceConfig.newBuilder()
            .setProbabilitySampler(
                ProbabilitySampler.newBuilder().setSamplingProbability(0.01).build())
            .build();
    UpdatedLibraryConfig updatedLibraryConfig =
        UpdatedLibraryConfig.newBuilder().setConfig(configProto).build();
    TraceProtoUtils.applyUpdatedConfig(updatedLibraryConfig, mockTraceConfig);
    TraceParams expectedParams =
        DEFAULT_PARAMS.toBuilder().setSampler(Samplers.probabilitySampler(0.01)).build();
    Mockito.verify(mockTraceConfig, Mockito.times(1)).getActiveTraceParams();
    Mockito.verify(mockTraceConfig, Mockito.times(1))
        .updateActiveTraceParams(Mockito.eq(expectedParams));
  }

  private static TraceParams getTraceParams(Sampler sampler) {
    return DEFAULT_PARAMS.toBuilder().setSampler(sampler).build();
  }
}
