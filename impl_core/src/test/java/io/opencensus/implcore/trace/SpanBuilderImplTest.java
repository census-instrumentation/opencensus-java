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
import static org.mockito.Mockito.when;

import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.internal.RandomHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.samplers.Samplers;
import java.util.Collections;
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
  private final TraceParams alwaysSampleTraceParams =
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
  public void startSpan_CreatesTheCorrectSpanImplInstance() {
    assertThat(
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setSampler(Samplers.alwaysSample())
                .startSpan())
        .isInstanceOf(RecordEventsSpanImpl.class);
    assertThat(
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setRecordEvents(true)
                .setSampler(Samplers.neverSample())
                .startSpan())
        .isInstanceOf(RecordEventsSpanImpl.class);
    assertThat(
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setSampler(Samplers.neverSample())
                .startSpan())
        .isInstanceOf(NoRecordEventsSpanImpl.class);
  }

  @Test
  public void setSpanKind_NotNull() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setSpanKind(Kind.CLIENT)
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getKind()).isEqualTo(Kind.CLIENT);
    assertThat(span.toSpanData().getKind()).isEqualTo(Kind.CLIENT);
  }

  @Test
  public void setSpanKind_DefaultNull() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getKind()).isNull();
    assertThat(span.toSpanData().getKind()).isNull();
  }

  @Test
  public void startSpanNullParent() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isNull();
    assertThat(spanData.getStartTimestamp()).isEqualTo(testClock.now());
    assertThat(spanData.getName()).isEqualTo(SPAN_NAME);
  }

  @Test
  public void startSpanNullParentWithRecordEvents() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setSampler(Samplers.neverSample())
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isFalse();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isNull();
  }

  @Test
  public void startSpanIncreaseNumberOfChildren() {
    RecordEventsSpanImpl parent =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(parent.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(parent.toSpanData().getChildSpanCount()).isEqualTo(0);
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, parent, spanBuilderOptions)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span.toSpanData().getChildSpanCount()).isEqualTo(0);
    assertThat(parent.toSpanData().getChildSpanCount()).isEqualTo(1);
    span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithParent(SPAN_NAME, parent, spanBuilderOptions)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(span.toSpanData().getChildSpanCount()).isEqualTo(0);
    assertThat(parent.toSpanData().getChildSpanCount()).isEqualTo(2);
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
    assertThat(((RecordEventsSpanImpl) rootSpan).toSpanData().getHasRemoteParent()).isNull();
    Span childSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, rootSpan, spanBuilderOptions).startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(((RecordEventsSpanImpl) childSpan).toSpanData().getParentSpanId())
        .isEqualTo(rootSpan.getContext().getSpanId());
    assertThat(((RecordEventsSpanImpl) childSpan).toSpanData().getHasRemoteParent()).isFalse();
    assertThat(((RecordEventsSpanImpl) childSpan).getTimestampConverter())
        .isEqualTo(((RecordEventsSpanImpl) rootSpan).getTimestampConverter());
  }

  @Test
  public void startRemoteSpan_NullParent() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, null, spanBuilderOptions)
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isNull();
  }

  @Test
  public void startRemoteSpanInvalidParent() {
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithRemoteParent(
                    SPAN_NAME, SpanContext.INVALID, spanBuilderOptions)
                .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getOptions().contains(Options.RECORD_EVENTS)).isTrue();
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isNull();
    assertThat(spanData.getHasRemoteParent()).isNull();
  }

  @Test
  public void startRemoteSpan() {
    SpanContext spanContext =
        SpanContext.create(
            TraceId.generateRandomId(randomHandler.current()),
            SpanId.generateRandomId(randomHandler.current()),
            TraceOptions.DEFAULT);
    RecordEventsSpanImpl span =
        (RecordEventsSpanImpl)
            SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, spanContext, spanBuilderOptions)
                .setRecordEvents(true)
                .startSpan();
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(span.getContext().getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(span.getContext().getTraceOptions().isSampled()).isTrue();
    SpanData spanData = span.toSpanData();
    assertThat(spanData.getParentSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(spanData.getHasRemoteParent()).isTrue();
  }

  @Test
  public void startRootSpan_WithSpecifiedSampler() {
    // Apply given sampler before default sampler for root spans.
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startRootSpan_WithoutSpecifiedSampler() {
    // Apply default sampler (always true in the tests) for root spans.
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions).startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
  }

  @Test
  public void startRemoteChildSpan_WithSpecifiedSampler() {
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    // Apply given sampler before default sampler for spans with remote parent.
    Span childSpan =
        SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, rootSpan.getContext(), spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startRemoteChildSpan_WithoutSpecifiedSampler() {
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isFalse();
    // Apply default sampler (always true in the tests) for spans with remote parent.
    Span childSpan =
        SpanBuilderImpl.createWithRemoteParent(SPAN_NAME, rootSpan.getContext(), spanBuilderOptions)
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isTrue();
  }

  @Test
  public void startChildSpan_WithSpecifiedSampler() {
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    // Apply the given sampler for child spans.
    Span childSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, rootSpan, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startChildSpan_WithoutSpecifiedSampler() {
    Span rootSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(rootSpan.getContext().isValid()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isFalse();
    // Don't apply the default sampler (always true) for child spans.
    Span childSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, rootSpan, spanBuilderOptions).startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(rootSpan.getContext().getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isFalse();
  }

  @Test
  public void startChildSpan_SampledLinkedParent() {
    Span rootSpanUnsampled =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.neverSample())
            .startSpan();
    assertThat(rootSpanUnsampled.getContext().getTraceOptions().isSampled()).isFalse();
    Span rootSpanSampled =
        SpanBuilderImpl.createWithParent(SPAN_NAME, null, spanBuilderOptions)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(rootSpanSampled.getContext().getTraceOptions().isSampled()).isTrue();
    // Sampled because the linked parent is sampled.
    Span childSpan =
        SpanBuilderImpl.createWithParent(SPAN_NAME, rootSpanUnsampled, spanBuilderOptions)
            .setParentLinks(Collections.singletonList(rootSpanSampled))
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId())
        .isEqualTo(rootSpanUnsampled.getContext().getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isTrue();
  }

  @Test
  public void startRemoteChildSpan_WithProbabilitySamplerDefaultSampler() {
    when(traceConfig.getActiveTraceParams()).thenReturn(TraceParams.DEFAULT);
    // This traceId will not be sampled by the ProbabilitySampler because the first 8 bytes as long
    // is not less than probability * Long.MAX_VALUE;
    TraceId traceId =
        TraceId.fromBytes(
            new byte[] {
              (byte) 0x8F,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0
            });

    // If parent is sampled then the remote child must be sampled.
    Span childSpan =
        SpanBuilderImpl.createWithRemoteParent(
                SPAN_NAME,
                SpanContext.create(
                    traceId,
                    SpanId.generateRandomId(randomHandler.current()),
                    TraceOptions.builder().setIsSampled(true).build()),
                spanBuilderOptions)
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(traceId);
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isTrue();
    childSpan.end();

    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(TraceParams.DEFAULT);

    // If parent is not sampled then the remote child must be not sampled.
    childSpan =
        SpanBuilderImpl.createWithRemoteParent(
                SPAN_NAME,
                SpanContext.create(
                    traceId,
                    SpanId.generateRandomId(randomHandler.current()),
                    TraceOptions.DEFAULT),
                spanBuilderOptions)
            .startSpan();
    assertThat(childSpan.getContext().isValid()).isTrue();
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(traceId);
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isFalse();
    childSpan.end();
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
