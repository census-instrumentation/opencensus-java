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

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.internal.TestClock;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.SpanImpl.StartEndHandler;
import java.util.EnumSet;
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
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final SpanId parentSpanId = SpanId.generateRandomId(random);
  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  private final TestClock testClock = TestClock.create(timestamp);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  private EnumSet<Options> noRecordSpanOptions = EnumSet.noneOf(Options.class);
  private EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  @Mock private StartEndHandler startEndHandler;
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
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
            startEndHandler,
            timestampConverter,
            testClock);
    span.end();
    exception.expect(IllegalStateException.class);
    span.toSpanData();
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
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getDisplayName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isTrue();
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
            startEndHandler,
            timestampConverter,
            testClock);
    Mockito.verify(startEndHandler, Mockito.times(1)).onStart(span);
    testClock.advanceTime(Duration.create(0, 7777));
    span.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    Mockito.verify(startEndHandler, Mockito.times(1)).onEnd(span);
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getContext()).isEqualTo(spanContext);
    assertThat(spanData.getDisplayName()).isEqualTo(SPAN_NAME);
    assertThat(spanData.getParentSpanId()).isEqualTo(parentSpanId);
    assertThat(spanData.getHasRemoteParent()).isFalse();
    assertThat(spanData.getStartTimestamp()).isEqualTo(timestamp);
    assertThat(spanData.getStatus()).isEqualTo(Status.CANCELLED);
    assertThat(spanData.getEndTimestamp()).isEqualTo(timestamp.addNanos(7777));
  }
}
