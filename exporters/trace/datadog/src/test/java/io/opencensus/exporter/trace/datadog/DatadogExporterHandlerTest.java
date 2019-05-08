/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.trace.datadog;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.trace.datadog.DatadogTraceConfiguration.DEFAULT_DEADLINE;

import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.export.SpanData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DatadogExporterHandler}. */
@RunWith(JUnit4.class)
public class DatadogExporterHandlerTest {
  private static final String TRACE_ID = "d239036e7d5cec116b562147388b35bf";
  private static final String SPAN_ID = "9cc1e3049173be09";
  private static final String PARENT_SPAN_ID = "8b03ab423da481c5";
  private static final Map<String, AttributeValue> attributes =
      ImmutableMap.of(
          "http.url", AttributeValue.stringAttributeValue("http://localhost/foo"),
          "resource", AttributeValue.stringAttributeValue("/foo"));
  private static final List<SpanData.TimedEvent<Annotation>> annotations = Collections.emptyList();
  private static final List<SpanData.TimedEvent<MessageEvent>> messageEvents =
      Collections.emptyList();

  private DatadogExporterHandler handler;

  @Before
  public void setup() throws Exception {
    this.handler =
        new DatadogExporterHandler("http://localhost", "service", "web", DEFAULT_DEADLINE);
  }

  @Test
  public void testJsonConversion() {
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build(),
                Tracestate.builder().build()),
            SpanId.fromLowerBase16(PARENT_SPAN_ID),
            /* hasRemoteParent= */ true,
            "SpanName",
            /* kind= */ null,
            /* startTimestamp= */ Timestamp.create(1505855794, 194009601),
            SpanData.Attributes.create(attributes, 0),
            SpanData.TimedEvents.create(annotations, 0),
            SpanData.TimedEvents.create(messageEvents, 0),
            SpanData.Links.create(Collections.emptyList(), 0),
            /* childSpanCount= */ null,
            Status.OK,
            /* endTimestamp= */ Timestamp.create(1505855799, 465726528));

    final String expected =
        "[["
            + "{"
            + "\"trace_id\":3298601478987650031,"
            + "\"span_id\":7151185124527981047,"
            + "\"name\":\"SpanName\","
            + "\"resource\":\"/foo\","
            + "\"service\":\"service\","
            + "\"type\":\"web\","
            + "\"start\":1505855794194009601,"
            + "\"duration\":5271716927,"
            + "\"parent_id\":8429705776517054011,"
            + "\"error\":0,"
            + "\"meta\":{"
            + "\"resource\":\"/foo\","
            + "\"http.url\":\"http://localhost/foo\""
            + "}"
            + "}"
            + "]]";

    assertThat(handler.convertToJson(Collections.singletonList(data))).isEqualTo(expected);
  }

  @Test
  public void testNullableConversion() {
    SpanData data =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(TRACE_ID),
                SpanId.fromLowerBase16(SPAN_ID),
                TraceOptions.builder().setIsSampled(true).build(),
                Tracestate.builder().build()),
            /* parentSpanId= */ null,
            /* hasRemoteParent= */ false,
            "SpanName",
            /* kind= */ null,
            /* startTimestamp= */ Timestamp.create(1505855794, 194009601),
            SpanData.Attributes.create(attributes, 0),
            SpanData.TimedEvents.create(annotations, 0),
            SpanData.TimedEvents.create(messageEvents, 0),
            SpanData.Links.create(Collections.emptyList(), 0),
            /* childSpanCount= */ null,
            /* status= */ null,
            /* endTimestamp= */ null);

    final String expected =
        "[["
            + "{"
            + "\"trace_id\":3298601478987650031,"
            + "\"span_id\":7151185124527981047,"
            + "\"name\":\"SpanName\","
            + "\"resource\":\"/foo\","
            + "\"service\":\"service\","
            + "\"type\":\"web\","
            + "\"start\":1505855794194009601,"
            + "\"duration\":-1505855794194009601," // the tracer clock is set to 0 in tests
            + "\"error\":0,"
            + "\"meta\":{"
            + "\"resource\":\"/foo\","
            + "\"http.url\":\"http://localhost/foo\""
            + "}"
            + "}"
            + "]]";

    assertThat(handler.convertToJson(Collections.singletonList(data))).isEqualTo(expected);
  }
}
