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

package io.opencensus.implcore.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.StartEndHandlerImpl;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.RunningSpanStore.Filter;
import java.util.EnumSet;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link RunningSpanStoreImpl}. */
@RunWith(JUnit4.class)
public class RunningSpanStoreImplTest {

  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private final Random random = new Random(1234);
  private final SpanExporterImpl sampledSpansServiceExporter = SpanExporterImpl.create(4, 1000);
  private final RunningSpanStoreImpl activeSpansExporter = new RunningSpanStoreImpl();
  private final StartEndHandler startEndHandler =
      new StartEndHandlerImpl(
          sampledSpansServiceExporter, activeSpansExporter, null, new SimpleEventQueue());
  private EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);

  private final SpanImpl createSpan(String spanName) {
    final SpanContext spanContext =
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT);
    return SpanImpl.startSpan(
        spanContext,
        recordSpanOptions,
        spanName,
        SpanId.generateRandomId(random),
        false,
        TraceParams.DEFAULT,
        startEndHandler,
        null,
        MillisClock.getInstance());
  }

  @Test
  public void getSummary_SpansWithDifferentNames() {
    final SpanImpl span1 = createSpan(SPAN_NAME_1);
    final SpanImpl span2 = createSpan(SPAN_NAME_2);
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(2);
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_1)
                .getNumRunningSpans())
        .isEqualTo(1);
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_2)
                .getNumRunningSpans())
        .isEqualTo(1);
    span1.end();
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(1);
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().get(SPAN_NAME_1)).isNull();
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_2)
                .getNumRunningSpans())
        .isEqualTo(1);
    span2.end();
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(0);
  }

  @Test
  public void getSummary_SpansWithSameName() {
    final SpanImpl span1 = createSpan(SPAN_NAME_1);
    final SpanImpl span2 = createSpan(SPAN_NAME_1);
    final SpanImpl span3 = createSpan(SPAN_NAME_1);
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(1);
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_1)
                .getNumRunningSpans())
        .isEqualTo(3);
    span1.end();
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(1);
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_1)
                .getNumRunningSpans())
        .isEqualTo(2);
    span2.end();
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(1);
    assertThat(
            activeSpansExporter
                .getSummary()
                .getPerSpanNameSummary()
                .get(SPAN_NAME_1)
                .getNumRunningSpans())
        .isEqualTo(1);
    span3.end();
    assertThat(activeSpansExporter.getSummary().getPerSpanNameSummary().size()).isEqualTo(0);
  }

  @Test
  public void getActiveSpans_SpansWithDifferentNames() {
    SpanImpl span1 = createSpan(SPAN_NAME_1);
    SpanImpl span2 = createSpan(SPAN_NAME_2);
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_1, 0)))
        .containsExactly(span1.toSpanData());
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_1, 2)))
        .containsExactly(span1.toSpanData());
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_2, 0)))
        .containsExactly(span2.toSpanData());
    span1.end();
    span2.end();
  }

  @Test
  public void getActiveSpans_SpansWithSameName() {
    SpanImpl span1 = createSpan(SPAN_NAME_1);
    SpanImpl span2 = createSpan(SPAN_NAME_1);
    SpanImpl span3 = createSpan(SPAN_NAME_1);
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_1, 0)))
        .containsExactly(span1.toSpanData(), span2.toSpanData(), span3.toSpanData());
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_1, 2)).size())
        .isEqualTo(2);
    assertThat(activeSpansExporter.getRunningSpans(Filter.create(SPAN_NAME_1, 2)))
        .containsAnyOf(span1.toSpanData(), span2.toSpanData(), span3.toSpanData());
    span1.end();
    span2.end();
    span3.end();
  }
}
