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
import io.grpc.Context;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link Tracer}. */
@RunWith(JUnit4.class)
public class TracerTest {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String SPAN_NAME = "MySpanName";
  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private SpanFactory spanFactory;
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void withSpan_NullSpan() {
    tracer.withSpan(null);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    NonThrowingCloseable ws = tracer.withSpan(span);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ws.close();
    }
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void propagationViaRunnable() {
    Runnable runnable = null;
    NonThrowingCloseable ws = tracer.withSpan(span);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(tracer.getCurrentSpan()).isSameAs(span);
                    }
                  });
    } finally {
      ws.close();
    }
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithName_NullName() {
    assertThat(tracer.spanBuilder(null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(tracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    assertThat(tracer.spanBuilder(null, null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(tracer.spanBuilder(null, SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    assertThat(tracer.spanBuilderWithRemoteParent(null, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWitRemoteParent() {
    assertThat(tracer.spanBuilderWithRemoteParent(null, SPAN_NAME).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startScopedSpanRoot() {
    Tracer mockTracer = new MockTracer(spanFactory);
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    NonThrowingCloseable ss = mockTracer.spanBuilder(SPAN_NAME).becomeRoot().startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startScopedSpanChild() {
    Tracer mockTracer = new MockTracer(spanFactory);
    NonThrowingCloseable ws = mockTracer.withSpan(BlankSpan.INSTANCE);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
      when(spanFactory.startSpan(
              same(BlankSpan.INSTANCE), same(SPAN_NAME), eq(new StartSpanOptions())))
          .thenReturn(span);
      NonThrowingCloseable ss = mockTracer.spanBuilder(SPAN_NAME).startScopedSpan();
      try {
        assertThat(tracer.getCurrentSpan()).isSameAs(span);
      } finally {
        ss.close();
      }
      assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    } finally {
      ws.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpan() {
    Tracer mockTracer = new MockTracer(spanFactory);
    when(spanFactory.startSpan(isNull(Span.class), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span rootSpan = mockTracer.spanBuilder(BlankSpan.INSTANCE, SPAN_NAME).becomeRoot().startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpan() {
    Tracer mockTracer = new MockTracer(spanFactory);
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span childSpan = mockTracer.spanBuilder(BlankSpan.INSTANCE, SPAN_NAME).startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent() {
    Random random = new Random(1234);
    Tracer mockTracer = new MockTracer(spanFactory);
    SpanContext spanContext =
        new SpanContext(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT);
    when(spanFactory.startSpanWithRemoteParent(
            same(spanContext), same(SPAN_NAME), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span remoteChildSpan =
        mockTracer.spanBuilderWithRemoteParent(spanContext, SPAN_NAME).startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  private static final class MockTracer extends Tracer {
    private MockTracer(SpanFactory spanFactory) {
      super(spanFactory);
    }
  }
}
