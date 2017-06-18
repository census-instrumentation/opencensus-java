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
import static org.mockito.Mockito.verify;

import io.grpc.Context;
import io.opencensus.common.NonThrowingCloseable;
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
  private static final Tracer noopTracer = Tracing.getTracer();
  private static final String SPAN_NAME = "MySpanName";
  @Rule public ExpectedException thrown = ExpectedException.none();
  @Mock private Tracer tracer;
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
    NonThrowingCloseable ws = noopTracer.withSpan(span);
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
    NonThrowingCloseable ws = noopTracer.withSpan(span);
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
    assertThat(noopTracer.spanBuilder(null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(noopTracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    assertThat(noopTracer.spanBuilder(null, null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(noopTracer.spanBuilder(null, SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(null, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWitRemoteParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(null, SPAN_NAME).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startSpanWithValidParentFromContext() {
    NonThrowingCloseable ws = tracer.withSpan(span);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
      assertThat(tracer.spanBuilder(SPAN_NAME)).isNull();
      verify(tracer).spanBuilder(span, SPAN_NAME);
    } finally {
      ws.close();
    }
  }

  @Test
  public void startSpanWithInvalidParentFromContext() {
    NonThrowingCloseable ws = tracer.withSpan(BlankSpan.INSTANCE);
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
      assertThat(tracer.spanBuilder(SPAN_NAME)).isNull();
      verify(tracer).spanBuilder(BlankSpan.INSTANCE, SPAN_NAME);
    } finally {
      ws.close();
    }
  }
}
