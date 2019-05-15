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

package io.opencensus.implcore.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
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

/** Unit tests for {@link RecordEventsSpanImpl}. */
@RunWith(JUnit4.class)
public class RecordEventsSpanImplTest {
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
  private final Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
  private final Map<String, AttributeValue> expectedAttributes =
      new HashMap<String, AttributeValue>();
  @Mock private StartEndHandler startEndHandler;
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    attributes.put("MyLongAttributeKey", AttributeValue.longAttributeValue(123L));
    attributes.put("MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(false));
    expectedAttributes.putAll(attributes);
    expectedAttributes.put(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
  }

  @Test
  public void noEventsRecordedAfterEnd() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    span.end();
    // Check that adding trace events after Span#end() does not throw any exception and are not
    // recorded.
    span.putAttributes(attributes);
    span.putAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    span.addNetworkEvent(
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setUncompressedMessageSize(3).build());
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
  public void deprecatedAddAttributesStillWorks() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    span.addAttributes(attributes);
    span.end();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getAttributes().getAttributeMap()).isEqualTo(attributes);
  }

  @Test
  public void toSpanData_ActiveSpan() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            true,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    span.putAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    span.putAttributes(attributes);
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    testClock.advanceTime(Duration.create(0, 100));
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setUncompressedMessageSize(3).build();
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
    assertThat(spanData.getAttributes().getAttributeMap()).isEqualTo(expectedAttributes);
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
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    span.putAttribute(
        "MySingleStringAttributeKey",
        AttributeValue.stringAttributeValue("MySingleStringAttributeValue"));
    span.putAttributes(attributes);
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(Annotation.fromDescription(ANNOTATION_DESCRIPTION));
    testClock.advanceTime(Duration.create(0, 100));
    span.addAnnotation(ANNOTATION_DESCRIPTION, attributes);
    testClock.advanceTime(Duration.create(0, 100));
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setUncompressedMessageSize(3).build();
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
    assertThat(spanData.getAttributes().getAttributeMap()).isEqualTo(expectedAttributes);
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
  public void status_ViaSetStatus() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    testClock.advanceTime(Duration.create(0, 100));
    assertThat(span.getStatus()).isEqualTo(Status.OK);
    span.setStatus(Status.CANCELLED);
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
    span.end();
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
  }

  @Test
  public void status_ViaEndSpanOptions() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    testClock.advanceTime(Duration.create(0, 100));
    assertThat(span.getStatus()).isEqualTo(Status.OK);
    span.setStatus(Status.CANCELLED);
    assertThat(span.getStatus()).isEqualTo(Status.CANCELLED);
    span.end(EndSpanOptions.builder().setStatus(Status.ABORTED).build());
    assertThat(span.getStatus()).isEqualTo(Status.ABORTED);
  }

  @Test
  public void droppingAttributes() {
    final int maxNumberOfAttributes = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(maxNumberOfAttributes).build();
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      span.putAttributes(attributes);
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
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    for (int i = 0; i < 2 * maxNumberOfAttributes; i++) {
      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("MyStringAttributeKey" + i, AttributeValue.longAttributeValue(i));
      span.putAttributes(attributes);
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
      span.putAttributes(attributes);
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
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
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
          .isEqualTo(timestamp.addNanos(100L * (maxNumberOfAnnotations + i)));
      assertThat(spanData.getAnnotations().getEvents().get(i).getEvent()).isEqualTo(annotation);
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getAnnotations().getDroppedEventsCount()).isEqualTo(maxNumberOfAnnotations);
    assertThat(spanData.getAnnotations().getEvents().size()).isEqualTo(maxNumberOfAnnotations);
    for (int i = 0; i < maxNumberOfAnnotations; i++) {
      assertThat(spanData.getAnnotations().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100L * (maxNumberOfAnnotations + i)));
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
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            traceParams,
            startEndHandler,
            timestampConverter,
            testClock);
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setUncompressedMessageSize(3).build();
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
          .isEqualTo(timestamp.addNanos(100L * (maxNumberOfNetworkEvents + i)));
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getEvent()).isEqualTo(networkEvent);
    }
    span.end();
    spanData = span.toSpanData();
    assertThat(spanData.getNetworkEvents().getDroppedEventsCount())
        .isEqualTo(maxNumberOfNetworkEvents);
    assertThat(spanData.getNetworkEvents().getEvents().size()).isEqualTo(maxNumberOfNetworkEvents);
    for (int i = 0; i < maxNumberOfNetworkEvents; i++) {
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getTimestamp())
          .isEqualTo(timestamp.addNanos(100L * (maxNumberOfNetworkEvents + i)));
      assertThat(spanData.getNetworkEvents().getEvents().get(i).getEvent()).isEqualTo(networkEvent);
    }
  }

  @Test
  public void droppingLinks() {
    final int maxNumberOfLinks = 8;
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfLinks(maxNumberOfLinks).build();
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
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

  @Test
  public void sampleToLocalSpanStore() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    span.end(EndSpanOptions.builder().setSampleToLocalSpanStore(true).build());
    Mockito.verify(startEndHandler, Mockito.times(1)).onEnd(span);
    assertThat(span.getSampleToLocalSpanStore()).isTrue();
    span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    span.end();
    Mockito.verify(startEndHandler, Mockito.times(1)).onEnd(span);
    assertThat(span.getSampleToLocalSpanStore()).isFalse();
  }

  @Test
  public void sampleToLocalSpanStore_RunningSpan() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    exception.expect(IllegalStateException.class);
    exception.expectMessage("Running span does not have the SampleToLocalSpanStore set.");
    span.getSampleToLocalSpanStore();
  }

  @Test
  public void getSpanKind() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.SERVER,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    assertThat(span.getKind()).isEqualTo(Kind.SERVER);
  }

  @Test
  public void startEndTimes() {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            Kind.SERVER,
            parentSpanId,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);

    Timestamp start = Timestamp.create(1234, 5678);
    Timestamp end = Timestamp.create(4567, 7890);

    span.setStartTime(start);
    span.end(EndSpanOptions.DEFAULT, end);

    assertThat(span.getStartNanoTime()).isEqualTo(timestampConverter.convertTimestamp(start));
    assertThat(span.getEndNanoTime()).isEqualTo(timestampConverter.convertTimestamp(end));
  }
}
