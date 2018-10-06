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

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.uber.jaeger.exceptions.SenderException;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.thriftjava.Log;
import com.uber.jaeger.thriftjava.Process;
import com.uber.jaeger.thriftjava.Span;
import com.uber.jaeger.thriftjava.SpanRef;
import com.uber.jaeger.thriftjava.SpanRefType;
import com.uber.jaeger.thriftjava.Tag;
import com.uber.jaeger.thriftjava.TagType;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.export.SpanData;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JaegerExporterHandlerTest {
  private static final byte FF = (byte) 0xFF;

  private final HttpSender mockSender = mock(HttpSender.class);
  private final Process process = new Process("test");
  private final JaegerExporterHandler handler = new JaegerExporterHandler(mockSender, process);

  @Captor private ArgumentCaptor<List<Span>> captor;

  @Test
  public void exportShouldConvertFromSpanDataToJaegerThriftSpan() throws SenderException {
    final long startTime = 1519629870001L;
    final long endTime = 1519630148002L;
    final SpanData spanData =
        SpanData.create(
            sampleSpanContext(),
            SpanId.fromBytes(new byte[] {(byte) 0x7F, FF, FF, FF, FF, FF, FF, FF}),
            true,
            "test",
            Kind.SERVER,
            Timestamp.fromMillis(startTime),
            SpanData.Attributes.create(sampleAttributes(), 0),
            SpanData.TimedEvents.create(singletonList(sampleAnnotation()), 0),
            SpanData.TimedEvents.create(singletonList(sampleMessageEvent()), 0),
            SpanData.Links.create(sampleLinks(), 0),
            0,
            Status.OK,
            Timestamp.fromMillis(endTime));

    handler.export(singletonList(spanData));

    verify(mockSender).send(eq(process), captor.capture());
    List<Span> spans = captor.getValue();

    assertThat(spans.size()).isEqualTo(1);
    Span span = spans.get(0);

    assertThat(span.operationName).isEqualTo("test");
    assertThat(span.spanId).isEqualTo(256L);
    assertThat(span.traceIdHigh).isEqualTo(-72057594037927936L);
    assertThat(span.traceIdLow).isEqualTo(1L);
    assertThat(span.parentSpanId).isEqualTo(Long.MAX_VALUE);
    assertThat(span.flags).isEqualTo(1);
    assertThat(span.startTime).isEqualTo(MILLISECONDS.toMicros(startTime));
    assertThat(span.duration).isEqualTo(MILLISECONDS.toMicros(endTime - startTime));

    assertThat(span.tags.size()).isEqualTo(4);
    assertThat(span.tags)
        .containsExactly(
            new Tag("BOOL", TagType.BOOL).setVBool(false),
            new Tag("LONG", TagType.LONG).setVLong(Long.MAX_VALUE),
            new Tag("span.kind", TagType.STRING).setVStr("server"),
            new Tag("STRING", TagType.STRING)
                .setVStr(
                    "Judge of a man by his questions rather than by his answers. -- Voltaire"));

    assertThat(span.logs.size()).isEqualTo(2);
    Log log = span.logs.get(0);
    assertThat(log.timestamp).isEqualTo(1519629872987654L);
    assertThat(log.fields.size()).isEqualTo(4);
    assertThat(log.fields)
        .containsExactly(
            new Tag("annotation.description", TagType.STRING).setVStr("annotation #1"),
            new Tag("bool", TagType.BOOL).setVBool(true),
            new Tag("long", TagType.LONG).setVLong(1337L),
            new Tag("string", TagType.STRING)
                .setVStr("Kind words do not cost much. Yet they accomplish much. -- Pascal"));
    log = span.logs.get(1);
    assertThat(log.timestamp).isEqualTo(1519629871123456L);
    assertThat(log.fields.size()).isEqualTo(4);
    assertThat(log.fields)
        .containsExactly(
            new Tag("message_event.type", TagType.STRING).setVStr("sent"),
            new Tag("message_event.id", TagType.LONG).setVLong(42L),
            new Tag("message_event.compressed_size", TagType.LONG).setVLong(69),
            new Tag("message_event.uncompressed_size", TagType.LONG).setVLong(96));

    assertThat(span.references.size()).isEqualTo(1);
    SpanRef reference = span.references.get(0);
    assertThat(reference.traceIdHigh).isEqualTo(-1L);
    assertThat(reference.traceIdLow).isEqualTo(-256L);
    assertThat(reference.spanId).isEqualTo(512L);
    assertThat(reference.refType).isEqualTo(SpanRefType.CHILD_OF);
  }

  private static SpanContext sampleSpanContext() {
    return SpanContext.create(
        TraceId.fromBytes(new byte[] {FF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}),
        SpanId.fromBytes(new byte[] {0, 0, 0, 0, 0, 0, 1, 0}),
        TraceOptions.builder().setIsSampled(true).build(),
        Tracestate.builder().build());
  }

  private static ImmutableMap<String, AttributeValue> sampleAttributes() {
    return ImmutableMap.of(
        "BOOL", AttributeValue.booleanAttributeValue(false),
        "LONG", AttributeValue.longAttributeValue(Long.MAX_VALUE),
        "STRING",
            AttributeValue.stringAttributeValue(
                "Judge of a man by his questions rather than by his answers. -- Voltaire"));
  }

  private static SpanData.TimedEvent<Annotation> sampleAnnotation() {
    return SpanData.TimedEvent.create(
        Timestamp.create(1519629872L, 987654321),
        Annotation.fromDescriptionAndAttributes(
            "annotation #1",
            ImmutableMap.of(
                "bool", AttributeValue.booleanAttributeValue(true),
                "long", AttributeValue.longAttributeValue(1337L),
                "string",
                    AttributeValue.stringAttributeValue(
                        "Kind words do not cost much. Yet they accomplish much. -- Pascal"))));
  }

  private static SpanData.TimedEvent<MessageEvent> sampleMessageEvent() {
    return SpanData.TimedEvent.create(
        Timestamp.create(1519629871L, 123456789),
        MessageEvent.builder(MessageEvent.Type.SENT, 42L)
            .setCompressedMessageSize(69)
            .setUncompressedMessageSize(96)
            .build());
  }

  private static List<Link> sampleLinks() {
    return Lists.newArrayList(
        Link.fromSpanContext(
            SpanContext.create(
                TraceId.fromBytes(
                    new byte[] {FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, FF, 0}),
                SpanId.fromBytes(new byte[] {0, 0, 0, 0, 0, 0, 2, 0}),
                TraceOptions.builder().setIsSampled(false).build(),
                Tracestate.builder().build()),
            Link.Type.CHILD_LINKED_SPAN,
            ImmutableMap.of(
                "Bool", AttributeValue.booleanAttributeValue(true),
                "Long", AttributeValue.longAttributeValue(299792458L),
                "String",
                    AttributeValue.stringAttributeValue(
                        "Man is condemned to be free; because once thrown into the world, "
                            + "he is responsible for everything he does. -- Sartre"))));
  }
}
