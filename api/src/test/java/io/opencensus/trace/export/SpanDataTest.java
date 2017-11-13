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

package io.opencensus.trace.export;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.Link.Type;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.export.SpanData.Attributes;
import io.opencensus.trace.export.SpanData.Links;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_TEXT = "MyAnnotationText";
  private static final Annotation annotation = Annotation.fromDescription(ANNOTATION_TEXT);
  private static final NetworkEvent recvNetworkEvent =
      NetworkEvent.builder(NetworkEvent.Type.RECV, 1).build();
  private static final NetworkEvent sentNetworkEvent =
      NetworkEvent.builder(NetworkEvent.Type.SENT, 1).build();
  private static final Status status = Status.DEADLINE_EXCEEDED.withDescription("TooSlow");
  private static final int CHILD_SPAN_COUNT = 13;
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final SpanId parentSpanId = SpanId.generateRandomId(random);
  private final Map<String, AttributeValue> attributesMap = new HashMap<String, AttributeValue>();
  private final List<TimedEvent<Annotation>> annotationsList =
      new ArrayList<TimedEvent<Annotation>>();
  private final List<TimedEvent<NetworkEvent>> networkEventsList =
      new ArrayList<SpanData.TimedEvent<NetworkEvent>>();
  private final List<Link> linksList = new ArrayList<Link>();

  private Attributes attributes;
  private TimedEvents<Annotation> annotations;
  private TimedEvents<NetworkEvent> networkEvents;
  private Links links;

  @Before
  public void setUp() {
    attributesMap.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributesMap.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
    attributes = Attributes.create(attributesMap, 1);
    annotationsList.add(SpanData.TimedEvent.create(eventTimestamp1, annotation));
    annotationsList.add(SpanData.TimedEvent.create(eventTimestamp3, annotation));
    annotations = TimedEvents.create(annotationsList, 2);
    networkEventsList.add(SpanData.TimedEvent.create(eventTimestamp1, recvNetworkEvent));
    networkEventsList.add(SpanData.TimedEvent.create(eventTimestamp2, sentNetworkEvent));
    networkEvents = TimedEvents.create(networkEventsList, 3);
    linksList.add(Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN));
    links = Links.create(linksList, 0);
  }

  @Test
  public void spanData_AllValues() {
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
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isTrue();
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations()).isEqualTo(annotations);
    assertThat(spanData.getNetworkEvents()).isEqualTo(networkEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getChildSpanCount()).isEqualTo(CHILD_SPAN_COUNT);
    assertThat(spanData.getStatus()).isEqualTo(status);
    assertThat(spanData.getEndTimestamp()).isEqualTo(endTimestamp);
  }

  @Test
  public void spanData_RootActiveSpan() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            null,
            null,
            SPAN_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
            links,
            null,
            null,
            null);
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isNull();
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations()).isEqualTo(annotations);
    assertThat(spanData.getNetworkEvents()).isEqualTo(networkEvents);
    assertThat(spanData.getLinks()).isEqualTo(links);
    assertThat(spanData.getChildSpanCount()).isNull();
    assertThat(spanData.getStatus()).isNull();
    assertThat(spanData.getEndTimestamp()).isNull();
  }

  @Test
  public void spanData_AllDataEmpty() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            false,
            SPAN_NAME,
            startTimestamp,
            Attributes.create(Collections.<String, AttributeValue>emptyMap(), 0),
            TimedEvents.create(Collections.<SpanData.TimedEvent<Annotation>>emptyList(), 0),
            TimedEvents.create(Collections.<SpanData.TimedEvent<NetworkEvent>>emptyList(), 0),
            Links.create(Collections.<Link>emptyList(), 0),
            0,
            status,
            endTimestamp);
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isFalse();
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getStartTimestamp()).isEqualTo(startTimestamp);
    assertThat(spanData.getAttributes().getAttributeMap().isEmpty()).isTrue();
    assertThat(spanData.getAnnotations().getEvents().isEmpty()).isTrue();
    assertThat(spanData.getNetworkEvents().getEvents().isEmpty()).isTrue();
    assertThat(spanData.getLinks().getLinks().isEmpty()).isTrue();
    assertThat(spanData.getChildSpanCount()).isEqualTo(0);
    assertThat(spanData.getStatus()).isEqualTo(status);
    assertThat(spanData.getEndTimestamp()).isEqualTo(endTimestamp);
  }

  @Test
  public void spanDataEquals() {
    SpanData allSpanData1 =
        SpanData.create(
            spanContext,
            parentSpanId,
            false,
            SPAN_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    SpanData allSpanData2 =
        SpanData.create(
            spanContext,
            parentSpanId,
            false,
            SPAN_NAME,
            startTimestamp,
            attributes,
            annotations,
            networkEvents,
            links,
            CHILD_SPAN_COUNT,
            status,
            endTimestamp);
    SpanData emptySpanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            false,
            SPAN_NAME,
            startTimestamp,
            Attributes.create(Collections.<String, AttributeValue>emptyMap(), 0),
            TimedEvents.create(Collections.<SpanData.TimedEvent<Annotation>>emptyList(), 0),
            TimedEvents.create(Collections.<SpanData.TimedEvent<NetworkEvent>>emptyList(), 0),
            Links.create(Collections.<Link>emptyList(), 0),
            0,
            status,
            endTimestamp);
    new EqualsTester()
        .addEqualityGroup(allSpanData1, allSpanData2)
        .addEqualityGroup(emptySpanData)
        .testEquals();
  }

  @Test
  public void spanData_ToString() {
    String spanDataString =
        SpanData.create(
                spanContext,
                parentSpanId,
                false,
                SPAN_NAME,
                startTimestamp,
                attributes,
                annotations,
                networkEvents,
                links,
                CHILD_SPAN_COUNT,
                status,
                endTimestamp)
            .toString();
    assertThat(spanDataString).contains(spanContext.toString());
    assertThat(spanDataString).contains(parentSpanId.toString());
    assertThat(spanDataString).contains(SPAN_NAME);
    assertThat(spanDataString).contains(startTimestamp.toString());
    assertThat(spanDataString).contains(attributes.toString());
    assertThat(spanDataString).contains(annotations.toString());
    assertThat(spanDataString).contains(networkEvents.toString());
    assertThat(spanDataString).contains(links.toString());
    assertThat(spanDataString).contains(status.toString());
    assertThat(spanDataString).contains(endTimestamp.toString());
  }
}
