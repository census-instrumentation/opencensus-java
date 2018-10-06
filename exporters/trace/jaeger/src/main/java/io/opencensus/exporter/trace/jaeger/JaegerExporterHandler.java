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

package io.opencensus.exporter.trace.jaeger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.errorprone.annotations.MustBeClosed;
import com.uber.jaeger.exceptions.SenderException;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.thriftjava.Log;
import com.uber.jaeger.thriftjava.Process;
import com.uber.jaeger.thriftjava.Span;
import com.uber.jaeger.thriftjava.SpanRef;
import com.uber.jaeger.thriftjava.SpanRefType;
import com.uber.jaeger.thriftjava.Tag;
import com.uber.jaeger.thriftjava.TagType;
import io.opencensus.common.Function;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
final class JaegerExporterHandler extends SpanExporter.Handler {
  private static final String EXPORT_SPAN_NAME = "ExportJaegerTraces";
  private static final String SPAN_KIND = "span.kind";
  private static final String DESCRIPTION = "annotation.description";
  private static final String MESSAGE_EVENT_TYPE = "message_event.type";
  private static final String MESSAGE_EVENT_ID = "message_event.id";
  private static final String MESSAGE_EVENT_COMPRESSED_SIZE = "message_event.compressed_size";
  private static final String MESSAGE_EVENT_UNCOMPRESSED_SIZE = "message_event.uncompressed_size";

  private static final Logger logger = Logger.getLogger(JaegerExporterHandler.class.getName());

  /**
   * Sampler with low probability used during the export in order to avoid the case when user sets
   * the default sampler to always sample and we get the Thrift span of the Jaeger export call
   * always sampled and go to an infinite loop.
   */
  private static final Sampler lowProbabilitySampler = Samplers.probabilitySampler(0.0001);

  private static final Tracer tracer = Tracing.getTracer();

  private static final Function<? super String, Tag> stringAttributeConverter =
      new Function<String, Tag>() {
        @Override
        public Tag apply(final String value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.STRING);
          tag.setVStr(value);
          return tag;
        }
      };

  private static final Function<? super Boolean, Tag> booleanAttributeConverter =
      new Function<Boolean, Tag>() {
        @Override
        public Tag apply(final Boolean value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.BOOL);
          tag.setVBool(value);
          return tag;
        }
      };

  private static final Function<? super Double, Tag> doubleAttributeConverter =
      new Function<Double, Tag>() {
        @Override
        public Tag apply(final Double value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.DOUBLE);
          tag.setVDouble(value);
          return tag;
        }
      };

  private static final Function<? super Long, Tag> longAttributeConverter =
      new Function<Long, Tag>() {
        @Override
        public Tag apply(final Long value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.LONG);
          tag.setVLong(value);
          return tag;
        }
      };

  private static final Function<Object, Tag> defaultAttributeConverter =
      new Function<Object, Tag>() {
        @Override
        public Tag apply(final Object value) {
          final Tag tag = new Tag();
          tag.setVType(TagType.STRING);
          tag.setVStr(value.toString());
          return tag;
        }
      };

  // Re-usable buffers to avoid too much memory allocation during conversions.
  // N.B.: these make instances of this class thread-unsafe, hence the above
  // @NotThreadSafe annotation.
  private final byte[] spanIdBuffer = new byte[SpanId.SIZE];
  private final byte[] traceIdBuffer = new byte[TraceId.SIZE];
  private final byte[] optionsBuffer = new byte[Integer.SIZE / Byte.SIZE];

  private final HttpSender sender;
  private final Process process;

  JaegerExporterHandler(final HttpSender sender, final Process process) {
    this.sender = checkNotNull(sender, "Jaeger sender must NOT be null.");
    this.process = checkNotNull(process, "Process sending traces must NOT be null.");
  }

  @Override
  public void export(final Collection<SpanData> spanDataList) {
    final Scope exportScope = newExportScope();
    try {
      doExport(spanDataList);
    } catch (SenderException e) {
      tracer
          .getCurrentSpan() // exportScope above.
          .setStatus(Status.UNKNOWN.withDescription(getMessageOrDefault(e)));
      logger.log(Level.WARNING, "Failed to export traces to Jaeger: " + e);
    } finally {
      exportScope.close();
    }
  }

  @MustBeClosed
  private static Scope newExportScope() {
    // Start a new span with explicit sampler (with low probability) to avoid the case when user
    // sets the default sampler to always sample and we get the Thrift span of the Jaeger
    // export call always sampled and go to an infinite loop.
    return tracer.spanBuilder(EXPORT_SPAN_NAME).setSampler(lowProbabilitySampler).startScopedSpan();
  }

  private void doExport(final Collection<SpanData> spanDataList) throws SenderException {
    final List<Span> spans = spanDataToJaegerThriftSpans(spanDataList);
    sender.send(process, spans);
  }

  private static String getMessageOrDefault(final SenderException e) {
    return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
  }

  private List<Span> spanDataToJaegerThriftSpans(final Collection<SpanData> spanDataList) {
    final List<Span> spans = Lists.newArrayListWithExpectedSize(spanDataList.size());
    for (final SpanData spanData : spanDataList) {
      spans.add(spanDataToJaegerThriftSpan(spanData));
    }
    return spans;
  }

  private Span spanDataToJaegerThriftSpan(final SpanData spanData) {
    final long startTimeInMicros = timestampToMicros(spanData.getStartTimestamp());
    final long endTimeInMicros = timestampToMicros(spanData.getEndTimestamp());

    final SpanContext context = spanData.getContext();
    copyToBuffer(context.getTraceId());

    return new com.uber.jaeger.thriftjava.Span(
            traceIdLow(),
            traceIdHigh(),
            spanIdToLong(context.getSpanId()),
            spanIdToLong(spanData.getParentSpanId()),
            spanData.getName(),
            optionsToFlags(context.getTraceOptions()),
            startTimeInMicros,
            endTimeInMicros - startTimeInMicros)
        .setReferences(linksToReferences(spanData.getLinks().getLinks()))
        .setTags(
            attributesToTags(
                spanData.getAttributes().getAttributeMap(), spanKindToTag(spanData.getKind())))
        .setLogs(
            timedEventsToLogs(
                spanData.getAnnotations().getEvents(), spanData.getMessageEvents().getEvents()));
  }

  private void copyToBuffer(final TraceId traceId) {
    // Attempt to minimise allocations, since TraceId#getBytes currently creates a defensive copy:
    traceId.copyBytesTo(traceIdBuffer, 0);
  }

  private long traceIdHigh() {
    return Longs.fromBytes(
        traceIdBuffer[0],
        traceIdBuffer[1],
        traceIdBuffer[2],
        traceIdBuffer[3],
        traceIdBuffer[4],
        traceIdBuffer[5],
        traceIdBuffer[6],
        traceIdBuffer[7]);
  }

  private long traceIdLow() {
    return Longs.fromBytes(
        traceIdBuffer[8],
        traceIdBuffer[9],
        traceIdBuffer[10],
        traceIdBuffer[11],
        traceIdBuffer[12],
        traceIdBuffer[13],
        traceIdBuffer[14],
        traceIdBuffer[15]);
  }

  private long spanIdToLong(final @Nullable SpanId spanId) {
    if (spanId == null) {
      return 0L;
    }
    // Attempt to minimise allocations, since SpanId#getBytes currently creates a defensive copy:
    spanId.copyBytesTo(spanIdBuffer, 0);
    return Longs.fromByteArray(spanIdBuffer);
  }

  private int optionsToFlags(final TraceOptions traceOptions) {
    // Attempt to minimise allocations, since TraceOptions#getBytes currently creates a defensive
    // copy:
    traceOptions.copyBytesTo(optionsBuffer, optionsBuffer.length - 1);
    return Ints.fromByteArray(optionsBuffer);
  }

  private List<SpanRef> linksToReferences(final List<Link> links) {
    final List<SpanRef> spanRefs = Lists.newArrayListWithExpectedSize(links.size());
    for (final Link link : links) {
      copyToBuffer(link.getTraceId());
      spanRefs.add(
          new SpanRef(
              linkTypeToRefType(link.getType()),
              traceIdLow(),
              traceIdHigh(),
              spanIdToLong(link.getSpanId())));
    }
    return spanRefs;
  }

  private static long timestampToMicros(final @Nullable Timestamp timestamp) {
    return (timestamp == null)
        ? 0L
        : SECONDS.toMicros(timestamp.getSeconds()) + NANOSECONDS.toMicros(timestamp.getNanos());
  }

  private static SpanRefType linkTypeToRefType(final Link.Type type) {
    switch (type) {
      case CHILD_LINKED_SPAN:
        return SpanRefType.CHILD_OF;
      case PARENT_LINKED_SPAN:
        return SpanRefType.FOLLOWS_FROM;
    }
    throw new UnsupportedOperationException(
        format("Failed to convert link type [%s] to a Jaeger SpanRefType.", type));
  }

  private static List<Tag> attributesToTags(
      final Map<String, AttributeValue> attributes, @Nullable final Tag extraTag) {
    final List<Tag> tags = Lists.newArrayListWithExpectedSize(attributes.size() + 1);
    for (final Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      final Tag tag =
          entry
              .getValue()
              .match(
                  stringAttributeConverter,
                  booleanAttributeConverter,
                  longAttributeConverter,
                  doubleAttributeConverter,
                  defaultAttributeConverter);
      tag.setKey(entry.getKey());
      tags.add(tag);
    }
    if (extraTag != null) {
      tags.add(extraTag);
    }
    return tags;
  }

  private static List<Log> timedEventsToLogs(
      final List<SpanData.TimedEvent<Annotation>> annotations,
      final List<SpanData.TimedEvent<MessageEvent>> messageEvents) {
    final List<Log> logs =
        Lists.newArrayListWithExpectedSize(annotations.size() + messageEvents.size());
    for (final SpanData.TimedEvent<Annotation> event : annotations) {
      final long timestampsInMicros = timestampToMicros(event.getTimestamp());
      logs.add(
          new Log(
              timestampsInMicros,
              attributesToTags(
                  event.getEvent().getAttributes(),
                  descriptionToTag(event.getEvent().getDescription()))));
    }
    for (final SpanData.TimedEvent<MessageEvent> event : messageEvents) {
      final long timestampsInMicros = timestampToMicros(event.getTimestamp());
      final Tag tagMessageType =
          new Tag(MESSAGE_EVENT_TYPE, TagType.STRING)
              .setVStr(event.getEvent().getType().name().toLowerCase(Locale.US));
      final Tag tagMessageId =
          new Tag(MESSAGE_EVENT_ID, TagType.LONG).setVLong(event.getEvent().getMessageId());
      final Tag tagCompressedSize =
          new Tag(MESSAGE_EVENT_COMPRESSED_SIZE, TagType.LONG)
              .setVLong(event.getEvent().getCompressedMessageSize());
      final Tag tagUncompressedSize =
          new Tag(MESSAGE_EVENT_UNCOMPRESSED_SIZE, TagType.LONG)
              .setVLong(event.getEvent().getUncompressedMessageSize());
      logs.add(
          new Log(
              timestampsInMicros,
              Arrays.asList(tagMessageType, tagMessageId, tagCompressedSize, tagUncompressedSize)));
    }
    return logs;
  }

  private static Tag descriptionToTag(final String description) {
    final Tag tag = new Tag(DESCRIPTION, TagType.STRING);
    tag.setVStr(description);
    return tag;
  }

  private static @Nullable Tag spanKindToTag(@Nullable final io.opencensus.trace.Span.Kind kind) {
    if (kind == null) {
      return null;
    }
    final Tag tag = new Tag(SPAN_KIND, TagType.STRING);
    tag.setVStr(kind.name().toLowerCase(Locale.US));
    return tag;
  }
}
