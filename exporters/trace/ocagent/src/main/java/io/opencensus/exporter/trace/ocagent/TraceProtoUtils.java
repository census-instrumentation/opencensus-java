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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.UInt32Value;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.AttributeValue;
import io.opencensus.proto.trace.v1.ConstantSampler;
import io.opencensus.proto.trace.v1.ConstantSampler.ConstantDecision;
import io.opencensus.proto.trace.v1.ProbabilitySampler;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.proto.trace.v1.Span.Attributes;
import io.opencensus.proto.trace.v1.Span.Link;
import io.opencensus.proto.trace.v1.Span.Links;
import io.opencensus.proto.trace.v1.Span.SpanKind;
import io.opencensus.proto.trace.v1.Span.TimeEvent;
import io.opencensus.proto.trace.v1.Span.TimeEvent.MessageEvent;
import io.opencensus.proto.trace.v1.Span.Tracestate;
import io.opencensus.proto.trace.v1.Span.Tracestate.Entry;
import io.opencensus.proto.trace.v1.Status;
import io.opencensus.proto.trace.v1.TraceConfig;
import io.opencensus.proto.trace.v1.TruncatableString;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import io.opencensus.trace.samplers.Samplers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Utilities for converting the Tracing data models in OpenCensus Java to/from OpenCensus Proto. */
final class TraceProtoUtils {

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
          return AttributeValue.newBuilder().setDoubleValue(doubleValue).build();
        }
      };

  /**
   * Converts {@link SpanData} to {@link Span} proto.
   *
   * @param spanData the {@code SpanData}.
   * @return proto representation of {@code Span}.
   */
  static Span toSpanProto(SpanData spanData) {
    SpanContext spanContext = spanData.getContext();
    TraceId traceId = spanContext.getTraceId();
    SpanId spanId = spanContext.getSpanId();
    Span.Builder spanBuilder =
        Span.newBuilder()
            .setTraceId(toByteString(traceId.getBytes()))
            .setSpanId(toByteString(spanId.getBytes()))
            .setTracestate(toTracestateProto(spanContext.getTracestate()))
            .setName(toTruncatableStringProto(spanData.getName()))
            .setStartTime(toTimestampProto(spanData.getStartTimestamp()))
            .setAttributes(toAttributesProto(spanData.getAttributes()))
            .setTimeEvents(
                toTimeEventsProto(spanData.getAnnotations(), spanData.getMessageEvents()))
            .setLinks(toLinksProto(spanData.getLinks()));

    Kind kind = spanData.getKind();
    if (kind != null) {
      spanBuilder.setKind(toSpanKindProto(kind));
    }

    io.opencensus.trace.Status status = spanData.getStatus();
    if (status != null) {
      spanBuilder.setStatus(toStatusProto(status));
    }

    Timestamp end = spanData.getEndTimestamp();
    if (end != null) {
      spanBuilder.setEndTime(toTimestampProto(end));
    }

    Integer childSpanCount = spanData.getChildSpanCount();
    if (childSpanCount != null) {
      spanBuilder.setChildSpanCount(UInt32Value.newBuilder().setValue(childSpanCount).build());
    }

    Boolean hasRemoteParent = spanData.getHasRemoteParent();
    if (hasRemoteParent != null) {
      spanBuilder.setSameProcessAsParentSpan(BoolValue.of(!hasRemoteParent));
    }

    SpanId parentSpanId = spanData.getParentSpanId();
    if (parentSpanId != null && parentSpanId.isValid()) {
      spanBuilder.setParentSpanId(toByteString(parentSpanId.getBytes()));
    }

    return spanBuilder.build();
  }

  @VisibleForTesting
  static ByteString toByteString(byte[] bytes) {
    return ByteString.copyFrom(bytes);
  }

  private static Tracestate toTracestateProto(io.opencensus.trace.Tracestate tracestate) {
    return Tracestate.newBuilder().addAllEntries(toEntriesProto(tracestate.getEntries())).build();
  }

  private static List<Entry> toEntriesProto(List<io.opencensus.trace.Tracestate.Entry> entries) {
    List<Entry> entriesProto = new ArrayList<Entry>();
    for (io.opencensus.trace.Tracestate.Entry entry : entries) {
      entriesProto.add(
          Entry.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()).build());
    }
    return entriesProto;
  }

  private static SpanKind toSpanKindProto(Kind kind) {
    switch (kind) {
      case CLIENT:
        return SpanKind.CLIENT;
      case SERVER:
        return SpanKind.SERVER;
    }
    return SpanKind.UNRECOGNIZED;
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
            .setCompressedSize(messageEvent.getCompressedMessageSize())
            .setUncompressedSize(messageEvent.getUncompressedMessageSize())
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

  private static Attributes toAttributesProto(
      io.opencensus.trace.export.SpanData.Attributes attributes) {
    Attributes.Builder attributesBuilder =
        toAttributesBuilderProto(
            attributes.getAttributeMap(), attributes.getDroppedAttributesCount());
    return attributesBuilder.build();
  }

  private static Attributes.Builder toAttributesBuilderProto(
      Map<String, io.opencensus.trace.AttributeValue> attributes, int droppedAttributesCount) {
    Attributes.Builder attributesBuilder =
        Attributes.newBuilder().setDroppedAttributesCount(droppedAttributesCount);
    for (Map.Entry<String, io.opencensus.trace.AttributeValue> label : attributes.entrySet()) {
      AttributeValue value = toAttributeValueProto(label.getValue());
      if (value != null) {
        attributesBuilder.putAttributeMap(label.getKey(), value);
      }
    }
    return attributesBuilder;
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

  private static Status toStatusProto(io.opencensus.trace.Status status) {
    Status.Builder statusBuilder = Status.newBuilder().setCode(status.getCanonicalCode().value());
    if (status.getDescription() != null) {
      statusBuilder.setMessage(status.getDescription());
    }
    return statusBuilder.build();
  }

  @VisibleForTesting
  static TruncatableString toTruncatableStringProto(String string) {
    return TruncatableString.newBuilder().setValue(string).setTruncatedByteCount(0).build();
  }

  static com.google.protobuf.Timestamp toTimestampProto(Timestamp timestamp) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(timestamp.getSeconds())
        .setNanos(timestamp.getNanos())
        .build();
  }

  private static Link.Type toLinkTypeProto(io.opencensus.trace.Link.Type type) {
    if (type == io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN) {
      return Link.Type.PARENT_LINKED_SPAN;
    } else {
      return Link.Type.CHILD_LINKED_SPAN;
    }
  }

  private static Link toLinkProto(io.opencensus.trace.Link link) {
    return Link.newBuilder()
        .setTraceId(toByteString(link.getTraceId().getBytes()))
        .setSpanId(toByteString(link.getSpanId().getBytes()))
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

  /**
   * Converts {@link TraceParams} to {@link TraceConfig}.
   *
   * @param traceParams the {@code TraceParams}.
   * @return {@code TraceConfig}.
   */
  static TraceConfig toTraceConfigProto(TraceParams traceParams) {
    TraceConfig.Builder traceConfigProtoBuilder = TraceConfig.newBuilder();
    Sampler librarySampler = traceParams.getSampler();

    if (Samplers.alwaysSample().equals(librarySampler)) {
      traceConfigProtoBuilder.setConstantSampler(
          ConstantSampler.newBuilder().setDecision(ConstantDecision.ALWAYS_ON).build());
    } else if (Samplers.neverSample().equals(librarySampler)) {
      traceConfigProtoBuilder.setConstantSampler(
          ConstantSampler.newBuilder().setDecision(ConstantDecision.ALWAYS_OFF).build());
    } else {
      // TODO: consider exposing the sampling probability of ProbabilitySampler.
      double samplingProbability = parseSamplingProbability(librarySampler);
      traceConfigProtoBuilder.setProbabilitySampler(
          ProbabilitySampler.newBuilder().setSamplingProbability(samplingProbability).build());
    } // TODO: add support for RateLimitingSampler.

    return traceConfigProtoBuilder.build();
  }

  private static double parseSamplingProbability(Sampler sampler) {
    String description = sampler.getDescription();
    // description follows format "ProbabilitySampler{%.6f}", samplingProbability.
    int leftParenIndex = description.indexOf("{");
    int rightParenIndex = description.indexOf("}");
    return Double.parseDouble(description.substring(leftParenIndex + 1, rightParenIndex));
  }

  /**
   * Converts {@link TraceConfig} to {@link TraceParams}.
   *
   * @param traceConfigProto {@code TraceConfig}.
   * @param currentTraceParams current {@code TraceParams}.
   * @return updated {@code TraceParams}.
   * @since 0.17
   */
  static TraceParams fromTraceConfigProto(
      TraceConfig traceConfigProto, TraceParams currentTraceParams) {
    TraceParams.Builder builder = currentTraceParams.toBuilder();
    if (traceConfigProto.hasConstantSampler()) {
      ConstantSampler constantSampler = traceConfigProto.getConstantSampler();
      ConstantDecision decision = constantSampler.getDecision();
      if (ConstantDecision.ALWAYS_ON.equals(decision)) {
        builder.setSampler(Samplers.alwaysSample());
      } else if (ConstantDecision.ALWAYS_OFF.equals(decision)) {
        builder.setSampler(Samplers.neverSample());
      } // else if (ConstantDecision.ALWAYS_PARENT.equals(decision)) {
      // For ALWAYS_PARENT, don't need to update configs since in Java by default parent sampling
      // decision always takes precedence.
      // }
    } else if (traceConfigProto.hasProbabilitySampler()) {
      builder.setSampler(
          Samplers.probabilitySampler(
              traceConfigProto.getProbabilitySampler().getSamplingProbability()));
    } // TODO: add support for RateLimitingSampler.
    return builder.build();
  }

  // Creates a TraceConfig proto message with current TraceParams.
  static TraceConfig getCurrentTraceConfig(io.opencensus.trace.config.TraceConfig traceConfig) {
    TraceParams traceParams = traceConfig.getActiveTraceParams();
    return toTraceConfigProto(traceParams);
  }

  // Creates an updated TraceParams with the given UpdatedLibraryConfig message and current
  // TraceParams, then applies the updated TraceParams.
  static TraceParams getUpdatedTraceParams(
      UpdatedLibraryConfig config, io.opencensus.trace.config.TraceConfig traceConfig) {
    TraceParams currentParams = traceConfig.getActiveTraceParams();
    TraceConfig traceConfigProto = config.getConfig();
    return fromTraceConfigProto(traceConfigProto, currentParams);
  }

  private TraceProtoUtils() {}
}
