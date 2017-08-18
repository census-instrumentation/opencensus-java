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
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

import io.grpc.Context;
import io.opencensus.common.Scope;
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
// Need to suppress warnings for MustBeClosed because Java-6 does not support try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class TracerTest {
  private static final Tracer noopTracer = Tracer.getNoopTracer();
  private static final String SPAN_NAME = "MySpanName";
  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private Tracer tracer;
  @Mock private SpanBuilder spanBuilder;
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(noopTracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void withSpan_NullSpan() {
    noopTracer.withSpan(null);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    Scope ws = noopTracer.withSpan(span);
    try {
      assertThat(noopTracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ws.close();
    }
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void propagationViaRunnable() {
    Runnable runnable;
    Scope ws = noopTracer.withSpan(span);
    try {
      assertThat(noopTracer.getCurrentSpan()).isSameAs(span);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(noopTracer.getCurrentSpan()).isSameAs(span);
                    }
                  });
    } finally {
      ws.close();
    }
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    // When we run the runnable we will have the span in the current Context.
    runnable.run();
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithName_NullName() {
    noopTracer.spanBuilder(null);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(noopTracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    noopTracer.spanBuilderWithExplicitParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(noopTracer.spanBuilderWithExplicitParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    noopTracer.spanBuilderWithRemoteParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent_NullParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, SpanContext.INVALID).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startSpanWithParentFromContext() {
    Scope ws = tracer.withSpan(span);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
      when(tracer.spanBuilderWithExplicitParent(same(SPAN_NAME), same(span)))
          .thenReturn(spanBuilder);
      assertThat(tracer.spanBuilder(SPAN_NAME)).isSameAs(spanBuilder);
    } finally {
      ws.close();
    }
  }

  @Test
  public void startSpanWithInvalidParentFromContext() {
    Scope ws = tracer.withSpan(BlankSpan.INSTANCE);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
      when(tracer.spanBuilderWithExplicitParent(same(SPAN_NAME), same(BlankSpan.INSTANCE)))
          .thenReturn(spanBuilder);
      assertThat(tracer.spanBuilder(SPAN_NAME)).isSameAs(spanBuilder);
    } finally {
      ws.close();
    }
  }
}
