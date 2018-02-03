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

package io.opencensus.exporter.trace.instana;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.NetworkEvent.Type;
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
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InstanaExporterHandler}. */
@RunWith(JUnit4.class)
public class InstanaExporterHandlerTest {

  @Test
  public void generateSpan() {
    String traceId = "d239036e7d5cec116b562147388b35bf";
    String spanId = "9cc1e3049173be09";
    String parentId = "8b03ab423da481c5";
    Map<String, AttributeValue> attributes =
        Collections.singletonMap(
            "http.url", AttributeValue.stringAttributeValue("http://localhost/foo"));
    List<TimedEvent<Annotation>> annotations = Collections.emptyList();
    List<TimedEvent<NetworkEvent>> networkEvents =
        ImmutableList.of(
            TimedEvent.create(
                Timestamp.create(1505855799, 433901068),
                NetworkEvent.builder(Type.RECV, 0).setCompressedMessageSize(7).build()),
            TimedEvent.create(
                Timestamp.create(1505855799, 459486280),
                NetworkEvent.builder(Type.SENT, 0).setCompressedMessageSize(13).build()));
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(traceId),
                SpanId.fromLowerBase16(spanId),
                TraceOptions.fromBytes(new byte[] {1} /* sampled */)),
            SpanId.fromLowerBase16(parentId),
            true, /* hasRemoteParent */
            "SpanName", /* name */
            Timestamp.create(1505855794, 194009601) /* startTimestamp */,
            Attributes.create(attributes, 0 /* droppedAttributesCount */),
            TimedEvents.create(annotations, 0 /* droppedEventsCount */),
            TimedEvents.create(networkEvents, 0 /* droppedEventsCount */),
            Links.create(Collections.<Link>emptyList(), 0 /* droppedLinksCount */),
            null, /* childSpanCount */
            Status.OK,
            Timestamp.create(1505855799, 465726528) /* endTimestamp */);

    assertThat(InstanaExporterHandler.convertToJson(Collections.singletonList(data)))
        .isEqualTo(
            "["
                + "{"
                + "\"spanId\":\"9cc1e3049173be09\","
                + "\"traceId\":\"d239036e7d5cec11\","
                + "\"parentId\":\"8b03ab423da481c5\","
                + "\"timestamp\":1505855794194,"
                + "\"duration\":5271,"
                + "\"name\":\"SpanName\","
                + "\"type\":\"ENTRY\","
                + "\"data\":"
                + "{\"http.url\":\"http://localhost/foo\"}"
                + "}"
                + "]");
  }
}
