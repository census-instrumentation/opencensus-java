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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanImpl.StartEndHandler;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.internal.RandomHandler;
import io.opencensus.trace.samplers.Samplers;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SpanBuilderImpl}. */
@RunWith(JUnit4.class)
public class SpanBuilderImplTest {
  private static final String SPAN_NAME = "MySpanName";
  private SpanBuilderImpl.Options spanBuilderOptions;
  private TraceParams alwaysSampleTraceParams =
      TraceParams.DEFAULT.toBuilder().setSampler(Samplers.alwaysSample()).build();
  private final TestClock testClock = TestClock.create();
  private final RandomHandler randomHandler = new FakeRandomHandler();
  @Mock private StartEndHandler startEndHandler;
  @Mock private TraceConfig traceConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    spanBuilderOptions =
        new SpanBuilderImpl.Options(randomHandler, startEndHandler, testClock, traceConfig);
    when(traceConfig.getActiveTraceParams()).thenReturn(alwaysSampleTraceParams);
  }

  @Test
  public void startSpanNullParent() {
    SpanImpl span =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions).startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
    assertThat(spanData.getStartTimestamp()).isEqualTo(testClock.now());
    assertThat(spanData.getDisplayName()).isEqualTo(SPAN_NAME);
  }

  @Test
  public void startSpanNullParentWithRecordEvents() {
    SpanImpl span =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .setRecordEvents(true)
            .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void startSpanNullParentNoRecordOptions() {
    Span span =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isFalse();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startChildSpan() {
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions).startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    Span childSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, rootSpan, spanBuilderOptions).startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(((SpanImpl) childSpan).toSpanData().getParentSpanId())
        .isEqualTo(rootSpan.getContext().getSpanId());
    assertThat(((SpanImpl) childSpan).getTimestampConverter())
        .isEqualTo(((SpanImpl) rootSpan).getTimestampConverter());
  }

  @Test
  public void startRemoteSpan_NullParent() {
    SpanImpl span =
        SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, null, spanBuilderOptions).startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isFalse();
  }

  @Test
  public void startRemoteSpanInvalidParent() {
    SpanImpl span =
        SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, SpanContext.INVALID, spanBuilderOptions)
            .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
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
    SpanImpl span =
        SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, spanContext, spanBuilderOptions)
            .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getContext().getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(spanData.getHasRemoteParent()).isTrue();
  }

  private static final class FakeRandomHandler extends RandomHandler {
    private final Random random;

    FakeRandomHandler() {
      this.random = new Random(1234);
    }

    @Override
    public Random current() {
      return random;
    }
  }
}
