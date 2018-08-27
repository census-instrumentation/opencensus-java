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

package io.opencensus.exporter.trace.zipkin;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.Attributes;
import io.opencensus.trace.export.SpanData.Links;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import zipkin2.Endpoint;
import zipkin2.Span;

/** Unit tests for {@link ZipkinExporterHandler}. */
@RunWith(JUnit4.class)
public class ZipkinExporterHandlerTest {
  private static final Endpoint localEndpoint =
      Endpoint.newBuilder().serviceName("tweetiebird").build();
  private static final String TRACE_ID = "d239036e7d5cec116b562147388b35bf";
  private static final String SPAN_ID = "9cc1e3049173be09";
  private static final String PARENT_SPAN_ID = "8b03ab423da481c5";
  private static final Map<String, AttributeValue> attributes = Collections.emptyMap();
  private static final List<TimedEvent<Annotation>> annotations = Collections.emptyList();
  private static final List<TimedEvent<MessageEvent>> messageEvents =
      ImmutableList.of(
          TimedEvent.create(
              Timestamp.create(1505855799, 433901068),
              MessageEvent.builder(Type.RECEIVED, 0).setCompressedMessageSize(7).build()),
          TimedEvent.create(
              Timestamp.create(1505855799, 459486280),
              MessageEvent.builder(Type.SENT, 0).setCompressedMessageSize(13).build()));

  @Test
  public void generateSpan_NoKindAndRemoteParent() {
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build()),
            // TODO SpanId.fromLowerBase16
            SpanId.fromLowerBase16(PARENT_SPAN_ID),
            true, /* hasRemoteParent */
            "Recv.helloworld.Greeter.SayHello", /* name */
            null, /* kind */
            Timestamp.create(1505855794, 194009601) /* startTimestamp */,
            Attributes.create(attributes, 0 /* droppedAttributesCount */),
            TimedEvents.create(annotations, 0 /* droppedEventsCount */),
            TimedEvents.create(messageEvents, 0 /* droppedEventsCount */),
            Links.create(Collections.<Link>emptyList(), 0 /* droppedLinksCount */),
            null, /* childSpanCount */
            Status.OK,
            Timestamp.create(1505855799, 465726528) /* endTimestamp */);

    assertThat(ZipkinExporterHandler.generateSpan(data, localEndpoint))
        .isEqualTo(
            Span.newBuilder()
                .traceId(TRACE_ID)
                .parentId(PARENT_SPAN_ID)
                .id(SPAN_ID)
                .kind(Span.Kind.SERVER)
                .name(data.getName())
                .timestamp(1505855794000000L + 194009601L / 1000)
                .duration(
                    (1505855799000000L + 465726528L / 1000)
                        - (1505855794000000L + 194009601L / 1000))
                .localEndpoint(localEndpoint)
                .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
                .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
                .putTag("census.status_code", "OK")
                .build());
  }

  @Test
  public void generateSpan_ServerKind() {
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build()),
            // TODO SpanId.fromLowerBase16
            SpanId.fromLowerBase16(PARENT_SPAN_ID),
            true, /* hasRemoteParent */
            "Recv.helloworld.Greeter.SayHello", /* name */
            Kind.SERVER, /* kind */
            Timestamp.create(1505855794, 194009601) /* startTimestamp */,
            Attributes.create(attributes, 0 /* droppedAttributesCount */),
            TimedEvents.create(annotations, 0 /* droppedEventsCount */),
            TimedEvents.create(messageEvents, 0 /* droppedEventsCount */),
            Links.create(Collections.<Link>emptyList(), 0 /* droppedLinksCount */),
            null, /* childSpanCount */
            Status.OK,
            Timestamp.create(1505855799, 465726528) /* endTimestamp */);

    assertThat(ZipkinExporterHandler.generateSpan(data, localEndpoint))
        .isEqualTo(
            Span.newBuilder()
                .traceId(TRACE_ID)
                .parentId(PARENT_SPAN_ID)
                .id(SPAN_ID)
                .kind(Span.Kind.SERVER)
                .name(data.getName())
                .timestamp(1505855794000000L + 194009601L / 1000)
                .duration(
                    (1505855799000000L + 465726528L / 1000)
                        - (1505855794000000L + 194009601L / 1000))
                .localEndpoint(localEndpoint)
                .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
                .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
                .putTag("census.status_code", "OK")
                .build());
  }

  @Test
  public void generateSpan_ClientKind() {
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build()),
            // TODO SpanId.fromLowerBase16
            SpanId.fromLowerBase16(PARENT_SPAN_ID),
            true, /* hasRemoteParent */
            "Sent.helloworld.Greeter.SayHello", /* name */
            Kind.CLIENT, /* kind */
            Timestamp.create(1505855794, 194009601) /* startTimestamp */,
            Attributes.create(attributes, 0 /* droppedAttributesCount */),
            TimedEvents.create(annotations, 0 /* droppedEventsCount */),
            TimedEvents.create(messageEvents, 0 /* droppedEventsCount */),
            Links.create(Collections.<Link>emptyList(), 0 /* droppedLinksCount */),
            null, /* childSpanCount */
            Status.OK,
            Timestamp.create(1505855799, 465726528) /* endTimestamp */);

    assertThat(ZipkinExporterHandler.generateSpan(data, localEndpoint))
        .isEqualTo(
            Span.newBuilder()
                .traceId(TRACE_ID)
                .parentId(PARENT_SPAN_ID)
                .id(SPAN_ID)
                .kind(Span.Kind.CLIENT)
                .name(data.getName())
                .timestamp(1505855794000000L + 194009601L / 1000)
                .duration(
                    (1505855799000000L + 465726528L / 1000)
                        - (1505855794000000L + 194009601L / 1000))
                .localEndpoint(localEndpoint)
                .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
                .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
                .putTag("census.status_code", "OK")
                .build());
  }

  @Test
  public void generateSpan_WithAttributes() {
    Map<String, AttributeValue> attributeMap = new HashMap<String, AttributeValue>();
    attributeMap.put("string", AttributeValue.stringAttributeValue("string value"));
    attributeMap.put("boolean", AttributeValue.booleanAttributeValue(false));
    attributeMap.put("long", AttributeValue.longAttributeValue(9999L));
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build()),
            // TODO SpanId.fromLowerBase16
            SpanId.fromLowerBase16(PARENT_SPAN_ID),
            true, /* hasRemoteParent */
            "Sent.helloworld.Greeter.SayHello", /* name */
            Kind.CLIENT, /* kind */
            Timestamp.create(1505855794, 194009601) /* startTimestamp */,
            Attributes.create(attributeMap, 0 /* droppedAttributesCount */),
            TimedEvents.create(annotations, 0 /* droppedEventsCount */),
            TimedEvents.create(messageEvents, 0 /* droppedEventsCount */),
            Links.create(Collections.<Link>emptyList(), 0 /* droppedLinksCount */),
            null, /* childSpanCount */
            Status.OK,
            Timestamp.create(1505855799, 465726528) /* endTimestamp */);

    assertThat(ZipkinExporterHandler.generateSpan(data, localEndpoint))
        .isEqualTo(
            Span.newBuilder()
                .traceId(TRACE_ID)
                .parentId(PARENT_SPAN_ID)
                .id(SPAN_ID)
                .kind(Span.Kind.CLIENT)
                .name(data.getName())
                .timestamp(1505855794000000L + 194009601L / 1000)
                .duration(
                    (1505855799000000L + 465726528L / 1000)
                        - (1505855794000000L + 194009601L / 1000))
                .localEndpoint(localEndpoint)
                .addAnnotation(1505855799000000L + 433901068L / 1000, "RECEIVED")
                .addAnnotation(1505855799000000L + 459486280L / 1000, "SENT")
                .putTag("census.status_code", "OK")
                .putTag("string", "string value")
                .putTag("boolean", "false")
                .putTag("long", "9999")
                .build());
  }
}
