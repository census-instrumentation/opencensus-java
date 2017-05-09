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
import static org.mockito.Mockito.when;

import com.google.instrumentation.internal.TestClock;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.SpanImpl.StartEndHandler;
import com.google.instrumentation.trace.TraceConfig.TraceParams;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SpanFactoryImpl}. */
@RunWith(JUnit4.class)
public class SpanFactoryImplTest {
  private static final String SPAN_NAME = "MySpanName";
  private SpanFactoryImpl spanFactory;
  private TraceParams alwaysSampleTraceParams =
      TraceParams.DEFAULT.toBuilder().setSampler(Samplers.alwaysSample()).build();
  private final TestClock testClock = TestClock.create();
  private final RandomHandler randomHandler = new FakeRandomHandler();
  @Mock private StartEndHandler startEndHandler;
  @Mock private TraceConfig traceConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    spanFactory = new SpanFactoryImpl(randomHandler, startEndHandler, testClock, traceConfig);
  }

  @Test
  public void startSpanNullParent() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span = spanFactory.startSpan(null, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span instanceof SpanImpl).isTrue();
    SpanData spanData = ((SpanImpl) span).toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
    assertThat(spanData.getStartTimestamp()).isEqualTo(testClock.now());
    assertThat(spanData.getDisplayName()).isEqualTo(SPAN_NAME);
  }

  @Test
  public void startSpanNullParentWithRecordEvents() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    startSpanOptions.setSampler(Samplers.neverSample());
    startSpanOptions.setRecordEvents(true);
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span = spanFactory.startSpan(null, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
    assertThat(span instanceof SpanImpl).isTrue();
    SpanData spanData = ((SpanImpl) span).toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void startSpanNullParentNoRecordOptions() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    startSpanOptions.setSampler(Samplers.neverSample());
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span = spanFactory.startSpan(null, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isFalse();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startRemoteSpanNullParent() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span = spanFactory.startSpanWithRemoteParent(null, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span instanceof SpanImpl).isTrue();
    SpanData spanData = ((SpanImpl) span).toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void startChildSpan() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span rootSpan = spanFactory.startSpan(null, SPAN_NAME, startSpanOptions);
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    Span childSpan = spanFactory.startSpan(rootSpan, SPAN_NAME, startSpanOptions);
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(((SpanImpl) childSpan).toSpanData().getParentSpanId())
        .isEqualTo(rootSpan.getContext().getSpanId());
    assertThat(((SpanImpl) childSpan).getTimestampConverter())
        .isEqualTo(((SpanImpl) rootSpan).getTimestampConverter());
  }

  @Test
  public void startRemoteSpanInvalidParent() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span =
        spanFactory.startSpanWithRemoteParent(SpanContext.INVALID, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span instanceof SpanImpl).isTrue();
    SpanData spanData = ((SpanImpl) span).toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void startRemoteSpan() {
    SpanContext spanContext =
        SpanContext.create(
            TraceId.generateRandomId(randomHandler.current()),
            SpanId.generateRandomId(randomHandler.current()),
            TraceOptions.DEFAULT);
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
    Span span = spanFactory.startSpanWithRemoteParent(spanContext, SPAN_NAME, startSpanOptions);
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getContext().getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span instanceof SpanImpl).isTrue();
    SpanData spanData = ((SpanImpl) span).toSpanData();
    assertThat(spanData.getParentSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(spanData.getHasRemoteParent()).isTrue();
  }

  private static final class FakeRandomHandler extends RandomHandler {
    private final Random random;

    FakeRandomHandler() {
      this.random = new Random(1234);
    }

    @Override
    Random current() {
      return random;
    }
  }
}
