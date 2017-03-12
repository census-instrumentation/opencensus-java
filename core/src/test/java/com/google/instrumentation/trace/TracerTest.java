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
import java.util.Random;
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

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

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
  public void spanBuilderWithName_NullName() {
    assertThat(tracer.spanBuilder(null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(tracer.spanBuilder("MySpanName").startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    assertThat(tracer.spanBuilder(null, null).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(tracer.spanBuilder(null, "MySpanName").startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    assertThat(tracer.spanBuilderWithRemoteParent(null, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWitRemoteParent() {
    assertThat(tracer.spanBuilderWithRemoteParent(null, "MySpanName").startSpan())
        .isSameAs(BlankSpan.INSTANCE);
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
  public void startScopedSpanRoot() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.getCurrentSpan()).thenReturn(null);
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), eq("MySpanName"), eq(new StartSpanOptions())))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss =
        mockTracer.spanBuilder("MySpanName").becomeRoot().startScopedSpan()) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startScopedSpanChild() {
    Tracer mockTracer = newTracerWithMocks();
    when(contextSpanHandler.getCurrentSpan()).thenReturn(BlankSpan.INSTANCE);
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE.getContext()),
            eq(false),
            eq("MySpanName"),
            eq(new StartSpanOptions())))
        .thenReturn(span);
    when(contextSpanHandler.withSpan(same(span))).thenReturn(withSpan);
    try (NonThrowingCloseable ss = mockTracer.spanBuilder("MySpanName").startScopedSpan()) {}
    verify(withSpan).close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpan() {
    Tracer mockTracer = newTracerWithMocks();
    when(spanFactory.startSpan(
            isNull(SpanContext.class), eq(false), eq("MySpanName"), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span rootSpan =
        mockTracer.spanBuilder(BlankSpan.INSTANCE, "MySpanName").becomeRoot().startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpan() {
    Tracer mockTracer = newTracerWithMocks();
    when(spanFactory.startSpan(
            same(BlankSpan.INSTANCE.getContext()),
            eq(false),
            eq("MySpanName"),
            eq(new StartSpanOptions())))
        .thenReturn(span);
    Span childSpan = mockTracer.spanBuilder(BlankSpan.INSTANCE, "MySpanName").startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent() {
    Random random = new Random(1234);
    Tracer mockTracer = newTracerWithMocks();
    SpanContext spanContext =
        new SpanContext(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT);
    when(spanFactory.startSpan(
            same(spanContext), eq(true), eq("MySpanName"), eq(new StartSpanOptions())))
        .thenReturn(span);
    Span remoteChildSpan =
        mockTracer.spanBuilderWithRemoteParent(spanContext, "MySpanName").startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  private Tracer newTracerWithMocks() {
    MockitoAnnotations.initMocks(this);
    return new Tracer(contextSpanHandler, spanFactory);
  }
}
