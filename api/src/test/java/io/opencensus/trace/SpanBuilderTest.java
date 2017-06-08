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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.StartSpanOptions;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.internal.SpanFactory;
import io.opencensus.trace.samplers.Samplers;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link Tracer}. */
@RunWith(JUnit4.class)
public class SpanBuilderTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final Tracer tracer = Tracing.getTracer();
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  @Mock private Span span;
  @Mock private SpanFactory spanFactory;
  private SpanBuilder spanBuilder;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    spanBuilder = SpanBuilder.builder(spanFactory, BlankSpan.INSTANCE, SPAN_NAME);
  }

  @Test
  public void startScopedSpanRoot() {
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    NonThrowingCloseable ss = spanBuilder.becomeRoot().startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startScopedSpanRootWithOptions() {
    StartSpanOptions startSpanOptions =
        StartSpanOptions.builder().setSampler(Samplers.neverSample()).build();
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(startSpanOptions)))
        .thenReturn(span);
    NonThrowingCloseable ss =
        spanBuilder.becomeRoot().setSampler(Samplers.neverSample()).startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpan() {
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    Span rootSpan = spanBuilder.becomeRoot().startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpan_WithNullParent() {
    spanBuilder = SpanBuilder.builder(spanFactory, null, SPAN_NAME);
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    Span rootSpan = spanBuilder.startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpanWithOptions() {
    List<Span> parentList = Arrays.<Span>asList(BlankSpan.INSTANCE);
    StartSpanOptions startSpanOptions =
        StartSpanOptions.builder()
            .setSampler(Samplers.neverSample())
            .setParentLinks(parentList)
            .build();
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(startSpanOptions)))
        .thenReturn(span);
    Span rootSpan =
        spanBuilder
            .becomeRoot()
            .setSampler(Samplers.neverSample())
            .setParentLinks(parentList)
            .startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpan() {
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    Span childSpan = spanBuilder.startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpanWithOptions() {
    StartSpanOptions startSpanOptions =
        StartSpanOptions.builder().setSampler(Samplers.neverSample()).setRecordEvents(true).build();
    ;
    when(spanFactory.startSpan(same(BlankSpan.INSTANCE), same(SPAN_NAME), eq(startSpanOptions)))
        .thenReturn(span);
    Span childSpan =
        spanBuilder.setSampler(Samplers.neverSample()).setRecordEvents(true).startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent() {
    spanBuilder = SpanBuilder.builderWithRemoteParent(spanFactory, spanContext, SPAN_NAME);
    when(spanFactory.startSpanWithRemoteParent(
            same(spanContext), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent_WithNullParent() {
    spanBuilder = SpanBuilder.builderWithRemoteParent(spanFactory, null, SPAN_NAME);
    when(spanFactory.startSpanWithRemoteParent(
            isNull(SpanContext.class), same(SPAN_NAME), eq(StartSpanOptions.DEFAULT)))
        .thenReturn(span);
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }
}
