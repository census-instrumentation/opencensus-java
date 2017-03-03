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
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final Tracer tracer = Tracer.getTracer();

  @Mock private ContextSpanHandler contextSpanHandler;
  @Mock private SpanFactory spanFactory;
  @Mock private Span span;
  @Mock private NonThrowingCloseable withSpan;

  @Test(expected = NullPointerException.class)
  public void withSpan_NullSpan() {
    try (NonThrowingCloseable ws = tracer.withSpan(null)) {}
  }

  @Test
  public void defaultWithSpan() {
    try (NonThrowingCloseable ss = tracer.withSpan(BlankSpan.INSTANCE)) {
      assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
    }
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithName_NullName() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan(null)) {}
  }

  @Test
  public void defaultStartScopedSpanWithName() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan("MySpanName")) {
      assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
    }
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithNameAndOptions_NullName() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan(null, StartSpanOptions.getDefault())) {}
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithNameAndOptions_NullOptions() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan("MySpanName", null)) {}
  }

  @Test
  public void defaultStartScopedSpanWithNameAndOptions() {
    try (NonThrowingCloseable ss =
        tracer.startScopedSpan("MySpanName", StartSpanOptions.getDefault())) {
      assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
    }
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithParentAndName_NullName() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan((Span) null, null)) {}
  }

  @Test
  public void defaultStartScopedSpanWithParentAndName() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan(null, "MySpanName")) {
      assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
    }
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithParentAndNameAndOptions_NullName() {
    try (NonThrowingCloseable ss =
        tracer.startScopedSpan(null, null, StartSpanOptions.getDefault())) {}
  }

  @Test(expected = NullPointerException.class)
  public void startScopedSpanWithParentAndNameAndOptions_NullOptions() {
    try (NonThrowingCloseable ss = tracer.startScopedSpan(null, "MySpanName", null)) {}
  }

  @Test
  public void defaultStartScopedSpanWithParentAndNameAndOptions() {
    try (NonThrowingCloseable ss =
        tracer.startScopedSpan(null, "MySpanName", StartSpanOptions.getDefault())) {
      assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
    }
  }

  @Test(expected = NullPointerException.class)
  public void startSpan_NullName() {
    tracer.startSpan(null, null, StartSpanOptions.getDefault());
  }

  @Test(expected = NullPointerException.class)
  public void startSpan_NullOptions() {
    tracer.startSpan(null, "MySpanName", null);
  }

  @Test
  public void defaultStartSpan() {
    assertThat(tracer.startSpan(null, "MySpanName", StartSpanOptions.getDefault()))
        .isEqualTo(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void startSpanWithRemoteParent_NullName() {
    tracer.startSpanWithRemoteParent(null, null, StartSpanOptions.getDefault());
  }

  @Test(expected = NullPointerException.class)
  public void startSpanWithRemoteParent_NullOptions() {
    tracer.startSpanWithRemoteParent(null, "MySpanName", null);
  }

  @Test
  public void defaultStartSpanWitRemoteParent() {
    assertThat(tracer.startSpanWithRemoteParent(null, "MySpanName", StartSpanOptions.getDefault()))
        .isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void loadContextSpanHandler_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    Tracer.loadContextSpanHandler(
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) {
            throw toThrow;
          }
        });
  }

  @Test
  public void loadContextSpanHandler_IgnoresMissingClasses() {
    assertThat(
            Tracer.loadContextSpanHandler(
                    new ClassLoader() {
                      @Override
                      public Class<?> loadClass(String name) throws ClassNotFoundException {
                        throw new ClassNotFoundException();
                      }
                    })
                .getClass()
                .getName())
        .isEqualTo("com.google.instrumentation.trace.Tracer$NoopContextSpanHandler");
  }

  @Test
  public void loadSpanFactory_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    Tracer.loadSpanFactory(
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) {
            throw toThrow;
          }
        });
  }

  @Test
  public void loadSpanFactory_IgnoresMissingClasses() {
    assertThat(
            Tracer.loadSpanFactory(
                    new ClassLoader() {
                      @Override
                      public Class<?> loadClass(String name) throws ClassNotFoundException {
                        throw new ClassNotFoundException();
                      }
                    })
                .getClass()
                .getName())
        .isEqualTo("com.google.instrumentation.trace.Tracer$NoopSpanFactory");
  }

  @Test
  public void getCurrentSpan() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.getCurrentSpan()).thenReturn(span);
    assertThat(mockTracer.getCurrentSpan()).isEqualTo(span);
  }

  @Test
  public void withSpan() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = mockTracer.withSpan(span)) {}
    verify(withSpan).close();
  }

  @Test
  public void startScopedSpanWithName() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.getCurrentSpan()).thenReturn(null);
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE), eq("MySpanName"), same(StartSpanOptions.getDefault())))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = mockTracer.startScopedSpan("MySpanName")) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.getDefault()));
  }

  @Test
  public void startScopedSpanWithNameAndOptions() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.getCurrentSpan()).thenReturn(null);
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = mockTracer.startScopedSpan("MySpanName", startSpanOptions)) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.getDefault()));
  }

  @Test
  public void startScopedSpanWithParentAndName() {
    Tracer mockTracer = newTracerWithMocks();
    when(spanFactory.startSpan(same(span), eq("MySpanName"), same(StartSpanOptions.getDefault())))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = mockTracer.startScopedSpan(span, "MySpanName")) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.getDefault()));
  }

  @Test
  public void startScopedSpanWithParentAndNameAndOptions() {
    Tracer mockTracer = newTracerWithMocks();
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpan(same(span), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss =
        mockTracer.startScopedSpan(span, "MySpanName", startSpanOptions)) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.getDefault()));
  }

  @Test
  public void startChildSpan() {
    Tracer mockTracer = newTracerWithMocks();
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpan(same(span), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    assertThat(mockTracer.startSpan(span, "MySpanName", startSpanOptions)).isEqualTo(span);
  }

  @Test
  public void startRootSpan() {
    Tracer mockTracer = newTracerWithMocks();
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpan(isNull(Span.class), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    assertThat(mockTracer.startSpan(null, "MySpanName", startSpanOptions)).isEqualTo(span);
  }

  @Test
  public void startSpanWitRemoteParent() {
    Tracer mockTracer = newTracerWithMocks();
    SpanContext spanContext = new SpanContext(new TraceId(10, 20), 30, TraceOptions.getDefault());
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpanWithRemoteParent(
            same(spanContext), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    assertThat(mockTracer.startSpanWithRemoteParent(spanContext, "MySpanName", startSpanOptions))
        .isEqualTo(span);
  }

  @Test
  public void startSpanWitRemoteParent_WithNullParent() {
    Tracer mockTracer = newTracerWithMocks();
    StartSpanOptions startSpanOptions = StartSpanOptions.builder().build();
    when(spanFactory.startSpanWithRemoteParent(
            isNull(SpanContext.class), eq("MySpanName"), same(startSpanOptions)))
        .thenReturn(span);
    assertThat(mockTracer.startSpanWithRemoteParent(null, "MySpanName", startSpanOptions))
        .isEqualTo(span);
  }

  private Tracer newTracerWithMocks() {
    MockitoAnnotations.initMocks(this);
    return new Tracer(contextSpanHandler, spanFactory);
  }
}
