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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.instrumentation.common.NonThrowingCloseable;
import com.google.instrumentation.common.Timestamp;
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
  @Mock private ContextSpanHandler contextSpanHandler;
  @Mock private Span span;
  @Mock private SpanFactory spanFactory;
  @Mock private NonThrowingCloseable withSpan;

  private static final String SPAN_NAME = "MySpanName";
  private Random random;
  private SpanContext spanContext;
  private SpanBuilder spanBuilder;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    random = new Random(1234);
    spanContext =
        new SpanContext(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.getDefault());
    spanBuilder =
        new SpanBuilder(
            spanFactory, contextSpanHandler, spanContext, false /* hasRemoteParent */, SPAN_NAME);
  }

  @Test
  public void startScopedSpanRoot() {
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = spanBuilder.becomeRoot().startScopedSpan()) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startScopedSpanRootWithOptions() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    startSpanOptions.setSampler(Samplers.neverSample());
    startSpanOptions.setStartTime(Timestamp.fromMillis(1234567L));
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), same(SPAN_NAME), eq(startSpanOptions)))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss =
        spanBuilder
            .becomeRoot()
            .setSampler(Samplers.neverSample())
            .setStartTime(Timestamp.fromMillis(1234567L))
            .startScopedSpan()) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpan() {
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span rootSpan = spanBuilder.becomeRoot().startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpan_WithNullParent() {
    spanBuilder =
        new SpanBuilder(
            spanFactory, contextSpanHandler, null, false /* hasRemoteParent */, SPAN_NAME);
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span rootSpan = spanBuilder.startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpanWithOptions() {
    List<Span> parentList = Arrays.<Span>asList(BlankSpan.INSTANCE);
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    startSpanOptions.setParentLinks(parentList);
    startSpanOptions.setSampler(Samplers.neverSample());
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), same(SPAN_NAME), eq(startSpanOptions)))
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
            same(spanContext), eq(false), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span childSpan = spanBuilder.startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpanWithOptions() {
    StartSpanOptions startSpanOptions = new StartSpanOptions();
    startSpanOptions.setSampler(Samplers.neverSample());
    startSpanOptions.setRecordEvents(true);
    when(spanFactory.startSpan(same(spanContext), eq(false), same(SPAN_NAME), eq(startSpanOptions)))
        .thenReturn(span);
    Span childSpan =
        spanBuilder.setSampler(Samplers.neverSample()).setRecordEvents(true).startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent() {
    spanBuilder =
        new SpanBuilder(
            spanFactory, contextSpanHandler, spanContext, true /* hasRemoteParent */, SPAN_NAME);
    when(spanFactory.startSpan(
            same(spanContext), eq(true), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent_WithNullParent() {
    spanBuilder =
        new SpanBuilder(
            spanFactory, contextSpanHandler, null, true /* hasRemoteParent */, SPAN_NAME);
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(true), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }
}
