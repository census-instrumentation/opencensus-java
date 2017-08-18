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

package io.opencensus.implcore.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SpanImpl}. */
@RunWith(JUnit4.class)
public class SpanImplTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final SpanId parentSpanId = SpanId.generateRandomId(random);
  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  private final TestClock testClock = TestClock.create(timestamp);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  private final EnumSet<Options> noRecordSpanOptions = EnumSet.noneOf(Options.class);
  private final EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  private final Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
  @Mock private StartEndHandler startEndHandler;
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    attributes.put("MyLongAttributeKey", AttributeValue.longAttributeValue(123L));
    attributes.put("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(false));
  }

  @Test
  public void toSpanData_NoRecordEvents() {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            noRecordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    // Check that adding trace events after Span#end() does not throw any exception.
    span.addAttributes(attributes);
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    span.addNetworkEvent(NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build());
    span.addLink(Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN));
    span.end();
    exception.expect(IllegalStateException.class);
    span.toSpanData();
  }

  @Test
  public void noEventsRecordedAfterEnd() {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    span.end();
    // Check that adding trace events after Span#end() does not throw any exception and are not
    // recorded.
    span.addAttributes(attributes);
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    span.addNetworkEvent(NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build());
    span.addLink(Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN));
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getStartTimestamp()).isEqualTo(timestamp);
    assertThat(spanData.getAttributes().getAttributeMap()).isEmpty();
    assertThat(spanData.getAnnotations().getEvents()).isEmpty();
    assertThat(spanData.getNetworkEvents().getEvents()).isEmpty();
    assertThat(spanData.getLinks().getLinks()).isEmpty();
    assertThat(spanData.getStatus()).isEqualTo(Status.OK);
    assertThat(spanData.getEndTimestamp()).isEqualTo(timestamp);
  }

  @Test
  public void toSpanData_ActiveSpan() {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            true,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    span.addAttributes(attributes);
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    testClock.advanceTime(Duration.create(0, 100));
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build();
    span.addNetworkEvent(networkEvent);
    testClock.advanceTime(Duration.create(0, 100));
    Link link = Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN);
    span.addLink(link);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isTrue();
    assertThat(spanData.getAttributes().getDroppedAttributesCount()).isEqualTo(0);
    assertThat(spanData.getAttributes().getAttributeMap()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations().getDroppedEventsCount()).isEqualTo(0);
    assertThat(spanData.getAnnotations().getEvents().size()).isEqualTo(2);
    assertThat(spanData.getAnnotations().getEvents().get(0).getTimestamp())
        .isEqualTo(timestamp.addNanos(100));
    assertThat(spanData.getAnnotations().getEvents().get(0).getEvent())
        .isEqualTo(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    assertThat(spanData.getAnnotations().getEvents().get(1).getTimestamp())
        .isEqualTo(timestamp.addNanos(200));
    assertThat(spanData.getAnnotations().getEvents().get(1).getEvent())
        .isEqualTo(Annotation.fromDescriptionAndAttributes(ANNOTATION_DESCRIPTION, attributes));
    assertThat(spanData.getNetworkEvents().getDroppedEventsCount()).isEqualTo(0);
    assertThat(spanData.getNetworkEvents().getEvents().size()).isEqualTo(1);
    assertThat(spanData.getNetworkEvents().getEvents().get(0).getTimestamp())
        .isEqualTo(timestamp.addNanos(300));
    assertThat(spanData.getNetworkEvents().getEvents().get(0).getEvent()).isEqualTo(networkEvent);
    assertThat(spanData.getLinks().getDroppedLinksCount()).isEqualTo(0);
    assertThat(spanData.getLinks().getLinks().size()).isEqualTo(1);
    assertThat(spanData.getLinks().getLinks().get(0)).isEqualTo(link);
    assertThat(spanData.getStartTimestamp()).isEqualTo(timestamp);
    assertThat(spanData.getStatus()).isNull();
    assertThat(spanData.getEndTimestamp()).isNull();
  }

  @Test
  public void toSpanData_EndedSpan() {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    span.addAttributes(attributes);
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    testClock.advanceTime(Duration.create(0, 100));
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build();
    span.addNetworkEvent(networkEvent);
    Link link = Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN);
    span.addLink(link);
    testClock.advanceTime(Duration.create(0, 100));
    span.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    Mockito.verify(startEndHandler, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isFalse();
    assertThat(spanData.getAttributes().getDroppedAttributesCount()).isEqualTo(0);
    assertThat(spanData.getAttributes().getAttributeMap()).isEqualTo(attributes);
    assertThat(spanData.getAnnotations().getDroppedEventsCount()).isEqualTo(0);
    assertThat(spanData.getAnnotations().getEvents().size()).isEqualTo(2);
    assertThat(spanData.getAnnotations().getEvents().get(0).getTimestamp())
        .isEqualTo(timestamp.addNanos(100));
    assertThat(spanData.getAnnotations().getEvents().get(0).getEvent())
        .isEqualTo(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    assertThat(spanData.getAnnotations().getEvents().get(1).getTimestamp())
        .isEqualTo(timestamp.addNanos(200));
    assertThat(spanData.getAnnotations().getEvents().get(1).getEvent())
        .isEqualTo(Annotation.fromDescriptionAndAttributes(ANNOTATION_DESCRIPTION, attributes));
    assertThat(spanData.getNetworkEvents().getDroppedEventsCount()).isEqualTo(0);
    assertThat(spanData.getNetworkEvents().getEvents().size()).isEqualTo(1);
    assertThat(spanData.getNetworkEvents().getEvents().get(0).getTimestamp())
        .isEqualTo(timestamp.addNanos(300));
    assertThat(spanData.getNetworkEvents().getEvents().get(0).getEvent()).isEqualTo(networkEvent);
    assertThat(spanData.getLinks().getDroppedLinksCount()).isEqualTo(0);
    assertThat(spanData.getLinks().getLinks().size()).isEqualTo(1);
    assertThat(spanData.getLinks().getLinks().get(0)).isEqualTo(link);
    assertThat(spanData.getStartTimestamp()).isEqualTo(timestamp);
    assertThat(spanData.getStatus()).isEqualTo(Status.CANCELLED);
    assertThat(spanData.getEndTimestamp()).isEqualTo(timestamp.addNanos(400));
  }

  @Test
  public void droppingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      span.addAttributes(attributes);
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().getDroppedAttributesCount())
        .isEqualTo(maxNumberOfAttributes);
    assertThat(spanData.getAttributes().getAttributeMap().size()).isEqualTo(maxNumberOfAttributes);
    for (int i = 0; i < maxNumberOfAttributes; i++) {
      assertThat(
              spanData
                  .getAttributes()
                  .getAttributeMap()
                  .get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
          .isEqualTo(AttributeValue.longAttributeValue(i + maxNumberOfAttributes));
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getAttributes().getDroppedAttributesCount())
        .isEqualTo(maxNumberOfAttributes);
    assertThat(spanData.getAttributes().getAttributeMap().size()).isEqualTo(maxNumberOfAttributes);
    for (int i = 0; i < maxNumberOfAttributes; i++) {
      assertThat(
              spanData
                  .getAttributes()
                  .getAttributeMap()
                  .get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
          .isEqualTo(AttributeValue.longAttributeValue(i + maxNumberOfAttributes));
    }
  }

  @Test
  public void droppingAndAddingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      span.addAttributes(attributes);
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().getDroppedAttributesCount())
        .isEqualTo(maxNumberOfAttributes);
    assertThat(spanData.getAttributes().getAttributeMap().size()).isEqualTo(maxNumberOfAttributes);
    for (int i = 0; i < maxNumberOfAttributes; i++) {
      assertThat(
              spanData
                  .getAttributes()
                  .getAttributeMap()
                  .get("MyStringAttributeKey" + (i + maxNumberOfAttributes)))
          .isEqualTo(AttributeValue.longAttributeValue(i + maxNumberOfAttributes));
    }
    for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      span.addAttributes(attributes);
    }
    spanData = span.toSpanData();
    assertThat(spanData.getAttributes().getDroppedAttributesCount())
        .isEqualTo(maxNumberOfAttributes * 3 / 2);
    assertThat(spanData.getAttributes().getAttributeMap().size()).isEqualTo(maxNumberOfAttributes);
    // Test that we still have in the attributes map the latest maxNumberOfAttributes / 2 entries.
    for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
      assertThat(
              spanData
                  .getAttributes()
                  .getAttributeMap()
                  .get("MyStringAttributeKey" + (i + maxNumberOfAttributes * 3 / 2)))
          .isEqualTo(AttributeValue.longAttributeValue(i + maxNumberOfAttributes * 3 / 2));
    }
    // Test that we have the newest re-added initial entries.
    for (int i = 0; i < maxNumberOfAttributes / 2; i++) {
      assertThat(spanData.getAttributes().getAttributeMap().get("MyStringAttributeKey" + i))
          .isEqualTo(AttributeValue.longAttributeValue(i));
    }
  }

  @Test
  public void droppingAnnotations() {
    final int maxNumberOfAnnotations = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfAnnotations(maxNumberOfAnnotations).build();
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    Annotation annotation = Annotation.fromDescription(ANNOTATION_DESCRIPTION);
    for (int i = 0; i < 2 * maxNumberOfAnnotations; i++) {
      span.addAnnotation(annotation);
      testClock.advanceTime(Duration.create(0, 100));
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAnnotations().getDroppedEventsCount()).isEqualTo(maxNumberOfAnnotations);
    assertThat(spanData.getAnnotations().getEvents().size()).isEqualTo(maxNumberOfAnnotations);
    for (int i = 0; i < maxNumberOfAnnotations; i++) {
      assertThat(spanData.getAnnotations().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100 * (maxNumberOfAnnotations + i)));
      assertThat(spanData.getAnnotations().getEvents().get(i).getEvent()).isEqualTo(annotation);
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getAnnotations().getDroppedEventsCount()).isEqualTo(maxNumberOfAnnotations);
    assertThat(spanData.getAnnotations().getEvents().size()).isEqualTo(maxNumberOfAnnotations);
    for (int i = 0; i < maxNumberOfAnnotations; i++) {
      assertThat(spanData.getAnnotations().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100 * (maxNumberOfAnnotations + i)));
      assertThat(spanData.getAnnotations().getEvents().get(i).getEvent()).isEqualTo(annotation);
    }
  }

  @Test
  public void droppingNetworkEvents() {
    final int maxNumberOfNetworkEvents = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT
            .toBuilder()
            .setMaxNumberOfNetworkEvents(maxNumberOfNetworkEvents)
            .build();
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build();
    for (int i = 0; i < 2 * maxNumberOfNetworkEvents; i++) {
      span.addNetworkEvent(networkEvent);
      testClock.advanceTime(Duration.create(0, 100));
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getNetworkEvents().getDroppedEventsCount())
        .isEqualTo(maxNumberOfNetworkEvents);
    assertThat(spanData.getNetworkEvents().getEvents().size()).isEqualTo(maxNumberOfNetworkEvents);
    for (int i = 0; i < maxNumberOfNetworkEvents; i++) {
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100 * (maxNumberOfNetworkEvents + i)));
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getEvent()).isEqualTo(networkEvent);
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getNetworkEvents().getDroppedEventsCount())
        .isEqualTo(maxNumberOfNetworkEvents);
    assertThat(spanData.getNetworkEvents().getEvents().size()).isEqualTo(maxNumberOfNetworkEvents);
    for (int i = 0; i < maxNumberOfNetworkEvents; i++) {
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100 * (maxNumberOfNetworkEvents + i)));
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getEvent()).isEqualTo(networkEvent);
    }
  }

  @Test
  public void droppingLinks() {
    final int maxNumberOfLinks = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfLinks(maxNumberOfLinks).build();
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    Link link = Link.fromSpanContext(spanContext, Link.Type.CHILD_LINKED_SPAN);
    for (int i = 0; i < 2 * maxNumberOfLinks; i++) {
      span.addLink(link);
    }
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getLinks().getDroppedLinksCount()).isEqualTo(maxNumberOfLinks);
    assertThat(spanData.getLinks().getLinks().size()).isEqualTo(maxNumberOfLinks);
    for (int i = 0; i < maxNumberOfLinks; i++) {
      assertThat(spanData.getLinks().getLinks().get(i)).isEqualTo(link);
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getLinks().getDroppedLinksCount()).isEqualTo(maxNumberOfLinks);
    assertThat(spanData.getLinks().getLinks().size()).isEqualTo(maxNumberOfLinks);
    for (int i = 0; i < maxNumberOfLinks; i++) {
      assertThat(spanData.getLinks().getLinks().get(i)).isEqualTo(link);
    }
  }
}
