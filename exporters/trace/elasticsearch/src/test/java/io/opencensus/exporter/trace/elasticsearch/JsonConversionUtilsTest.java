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

package io.opencensus.exporter.trace.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.Attributes;
import io.opencensus.trace.export.SpanData.Links;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test for {@link JsonConversionUtils}. */
@RunWith(JUnit4.class)
public class JsonConversionUtilsTest {

  private static final String SAMPLE_APP_NAME = "test-app";
  private static final String SAMPLE_TRACE_ID = "82bbc81f9999543682bbc81f99995436";
  private static final String SAMPLE_SPAN_ID = "82bbc81f99995436";
  private static final String SAMPLE_PARENT_SPAN_ID = "82bbc81f99995436";
  private static final Map<String, AttributeValue> attributes =
      ImmutableMap.of("data", AttributeValue.stringAttributeValue("d1"));
  private static final List<TimedEvent<Annotation>> annotations = Collections.emptyList();
  private static final List<TimedEvent<MessageEvent>> messageEvents =
      ImmutableList.of(
          TimedEvent.create(
              Timestamp.create(155096336, 469887399),
              MessageEvent.builder(Type.RECEIVED, 0).setCompressedMessageSize(7).build()),
          TimedEvent.create(
              Timestamp.create(155096336, 469887399),
              MessageEvent.builder(Type.SENT, 0).setCompressedMessageSize(13).build()));
  private static final TraceOptions SAMPLE_TRACE_OPTION =
      TraceOptions.builder().setIsSampled(true).build();
  private static final Tracestate SAMPLE_TRACE_STATE = Tracestate.builder().build();
  private List<SpanData> spanDataList;

  @Before
  public void setUp() {
    SpanData spanData =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(SAMPLE_TRACE_ID),
                SpanId.fromLowerBase16(SAMPLE_SPAN_ID),
                SAMPLE_TRACE_OPTION,
                SAMPLE_TRACE_STATE),
            SpanId.fromLowerBase16(SAMPLE_PARENT_SPAN_ID),
            true,
            "SpanName",
            null,
            Timestamp.create(155196336, 194009601),
            Attributes.create(attributes, 0),
            TimedEvents.create(annotations, 0),
            TimedEvents.create(messageEvents, 0),
            Links.create(Collections.<Link>emptyList(), 0),
            null,
            Status.OK,
            Timestamp.create(155296336, 465726528));

    spanDataList = new ArrayList<SpanData>();
    spanDataList.add(spanData);
  }

  @Test
  public void testConvertToJson() {
    List<String> json = JsonConversionUtils.convertToJson(SAMPLE_APP_NAME, spanDataList);
    Assert.assertEquals(json.size(), spanDataList.size());
    Assert.assertTrue(json.get(0).contains("\"appName\":\"" + SAMPLE_APP_NAME + "\""));
    Assert.assertTrue(json.get(0).contains("\"spanId\":\"" + SAMPLE_SPAN_ID + "\""));
  }

  @Test
  public void testConvertToJson_SpanDataListNull() {
    List<String> retList = JsonConversionUtils.convertToJson(null, null);
    assertEquals(0, retList.size());
    assertTrue(retList.isEmpty());
  }

  @Test
  public void testConvertToJson_endTimestampIsNull() {
    SpanData spanData =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(SAMPLE_TRACE_ID),
                SpanId.fromLowerBase16(SAMPLE_SPAN_ID),
                SAMPLE_TRACE_OPTION,
                SAMPLE_TRACE_STATE),
            SpanId.fromLowerBase16(SAMPLE_PARENT_SPAN_ID),
            true,
            "SpanName",
            null,
            Timestamp.create(155196336, 194009601),
            Attributes.create(attributes, 0),
            TimedEvents.create(annotations, 0),
            TimedEvents.create(messageEvents, 0),
            Links.create(Collections.<Link>emptyList(), 0),
            null,
            Status.OK,
            null);

    spanDataList = new ArrayList<SpanData>();
    spanDataList.add(spanData);

    List<String> json = JsonConversionUtils.convertToJson(null, spanDataList);
    assertTrue(json.isEmpty());
}

  @Test
  public void testConvertToJson_StatusIsNull() {
    SpanData spanData =
        SpanData.create(
            SpanContext.create(
                TraceId.fromLowerBase16(SAMPLE_TRACE_ID),
                SpanId.fromLowerBase16(SAMPLE_SPAN_ID),
                SAMPLE_TRACE_OPTION,
                SAMPLE_TRACE_STATE),
            SpanId.fromLowerBase16(SAMPLE_PARENT_SPAN_ID),
            true,
            "SpanName",
            null,
            Timestamp.create(155196336, 194009601),
            Attributes.create(attributes, 0),
            TimedEvents.create(annotations, 0),
            TimedEvents.create(messageEvents, 0),
            Links.create(Collections.<Link>emptyList(), 0),
            null,
            null,
            Timestamp.create(155296336, 465726528));

    spanDataList = new ArrayList<SpanData>();
    spanDataList.add(spanData);

    List<String> json = JsonConversionUtils.convertToJson(null, spanDataList);

    Assert.assertTrue(json.get(0).contains("\"status\":\"ok\""));
    } 
}
