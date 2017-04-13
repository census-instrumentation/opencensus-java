/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.common.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanData}. */
@RunWith(JUnit4.class)
public class SpanDataTest {
  private static final Timestamp startTimestamp = Timestamp.create(123, 456);
  private static final Timestamp eventTimestamp1 = Timestamp.create(123, 457);
  private static final Timestamp eventTimestamp2 = Timestamp.create(123, 458);
  private static final Timestamp eventTimestamp3 = Timestamp.create(123, 459);
  private static final Timestamp endTimestamp = Timestamp.create(123, 460);
  private static final String DISPLAY_NAME = "MySpanDisplayName";
  private static final String ANNOTATION_TEXT = "MyAnnotationText";
  private static final Status status = Status.DEADLINE_EXCEEDED.withDescription("TooSlow");
  private Random random;
  private SpanContext spanContext;
  private SpanId parentSpanId;
  private final Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
  private final List<TimedEvent<Annotation>> annotations = new LinkedList<TimedEvent<Annotation>>();
  private final List<TimedEvent<NetworkEvent>> networkEvents =
      new LinkedList<TimedEvent<NetworkEvent>>();
  private final List<Link> links = new LinkedList<Link>();

  @Before
  public void setUp() {
    random = new Random(1234);
    spanContext =
        new SpanContext(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT);
    parentSpanId = SpanId.generateRandomId(random);
    annotations.add(
        new TimedEvent<Annotation>(eventTimestamp1, Annotation.fromDescription(ANNOTATION_TEXT)));
    annotations.add(
        new TimedEvent<Annotation>(eventTimestamp3, Annotation.fromDescription(ANNOTATION_TEXT)));
    networkEvents.add(
        new TimedEvent<NetworkEvent>(
            eventTimestamp1, NetworkEvent.builder(NetworkEvent.Type.RECV, 1).build()));
    networkEvents.add(
        new TimedEvent<NetworkEvent>(
            eventTimestamp2, NetworkEvent.builder(NetworkEvent.Type.SENT, 1).build()));
    attributes.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributes.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
  }

  @Test
  public void spanData_AllValues() {
    SpanData spanData =
        new SpanData(
            spanContext,
            parentSpanId,
            DISPLAY_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
            links,
            status,
            endTimestamp);
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getDisplayName()).isEqualTo(DISPLAY_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations()).isEqualTo(annotations);
    assertThat(spanData.getNetworkEvents()).isEqualTo(networkEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStatus()).isEqualTo(status);
    assertThat(spanData.getEndTimestamp()).isEqualTo(endTimestamp);
  }

  @Test
  public void spanData_RootActiveSpan() {
    SpanData spanData =
        new SpanData(
            spanContext,
            null,
            DISPLAY_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
            links,
            null,
            null);
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getDisplayName()).isEqualTo(DISPLAY_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations()).isEqualTo(annotations);
    assertThat(spanData.getNetworkEvents()).isEqualTo(networkEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getStatus()).isNull();
    assertThat(spanData.getEndTimestamp()).isNull();
  }

  @Test
  public void spanData_AllDataEmpty() {
    SpanData spanData =
        new SpanData(
            spanContext,
            parentSpanId,
            DISPLAY_NAME,
            startTimestamp,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<TimedEvent<Annotation>>emptyList(),
            Collections.<TimedEvent<NetworkEvent>>emptyList(),
            Collections.<Link>emptyList(),
            status,
            endTimestamp);
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getDisplayName()).isEqualTo(DISPLAY_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes().isEmpty()).isTrue();
    assertThat(spanData.getAnnotations().isEmpty()).isTrue();
    assertThat(spanData.getNetworkEvents().isEmpty()).isTrue();
    assertThat(spanData.getLinks().isEmpty()).isTrue();
    assertThat(spanData.getStatus()).isEqualTo(status);
    assertThat(spanData.getEndTimestamp()).isEqualTo(endTimestamp);
  }

  @Test
  public void spanData_ToString() {
    String spanDataString =
        new SpanData(
                spanContext,
                parentSpanId,
                DISPLAY_NAME,
                startTimestamp,
                attributes,
                annotations,
                networkEvents,
                links,
                status,
                endTimestamp)
            .toString();
    assertThat(spanDataString).contains(spanContext.toString());
    assertThat(spanDataString).contains(parentSpanId.toString());
    assertThat(spanDataString).contains(DISPLAY_NAME);
    assertThat(spanDataString).contains(startTimestamp.toString());
    assertThat(spanDataString).contains(attributes.toString());
    assertThat(spanDataString).contains(annotations.toString());
    assertThat(spanDataString).contains(networkEvents.toString());
    assertThat(spanDataString).contains(links.toString());
    assertThat(spanDataString).contains(status.toString());
    assertThat(spanDataString).contains(endTimestamp.toString());
  }
}
