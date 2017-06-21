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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.samplers.Samplers;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
  private FakeSpanBuilder spanBuilder;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    spanBuilder = Mockito.spy(FakeSpanBuilder.builder(BlankSpan.INSTANCE, SPAN_NAME));
  }

  @Test
  public void startScopedSpanRoot() {
    spanBuilder = Mockito.spy(FakeSpanBuilder.builder(null, SPAN_NAME));
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isNull();
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    NonThrowingCloseable ss = spanBuilder.startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startScopedSpanRootWithSampler() {
    spanBuilder = Mockito.spy(FakeSpanBuilder.builder(null, SPAN_NAME));
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isEqualTo(Samplers.neverSample());
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    NonThrowingCloseable ss = spanBuilder.setSampler(Samplers.neverSample()).startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      ss.close();
    }
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpan() {
    spanBuilder = Mockito.spy(FakeSpanBuilder.builder(null, SPAN_NAME));
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isNull();
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    Span rootSpan = spanBuilder.startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startRootSpanWithOptions() {
    spanBuilder = Mockito.spy(FakeSpanBuilder.builder(null, SPAN_NAME));
    final List<Span> parentList = Arrays.<Span>asList(BlankSpan.INSTANCE);
    when(spanBuilder.startSpan())
        .then(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isEqualTo(Samplers.neverSample());
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEqualTo(parentList);
                return span;
              }
            });
    Span rootSpan =
        spanBuilder.setSampler(Samplers.neverSample()).setParentLinks(parentList).startSpan();
    assertThat(rootSpan).isEqualTo(span);
    rootSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpan() {
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isSameAs(BlankSpan.INSTANCE);
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isNull();
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    Span childSpan = spanBuilder.startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startChildSpanWithOptions() {
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isSameAs(BlankSpan.INSTANCE);
                assertThat(spanBuilder.getRemoteParentSpanContext()).isNull();
                assertThat(spanBuilder.getSampler()).isEqualTo(Samplers.neverSample());
                assertThat(spanBuilder.getRecordEvents()).isTrue();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    Span childSpan =
        spanBuilder.setSampler(Samplers.neverSample()).setRecordEvents(true).startSpan();
    assertThat(childSpan).isEqualTo(span);
    childSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent() {
    spanBuilder = Mockito.spy(FakeSpanBuilder.builderWithRemoteParent(spanContext, SPAN_NAME));
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isSameAs(spanContext);
                assertThat(spanBuilder.getSampler()).isNull();
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void startSpanWitRemoteParent_WithInvalidParent() {
    spanBuilder =
        Mockito.spy(FakeSpanBuilder.builderWithRemoteParent(SpanContext.INVALID, SPAN_NAME));
    when(spanBuilder.startSpan())
        .thenAnswer(
            new Answer<Span>() {
              @Override
              public Span answer(InvocationOnMock invocation) throws Throwable {
                SpanBuilder spanBuilder = (SpanBuilder) invocation.getMock();
                assertThat(spanBuilder.getName()).isEqualTo(SPAN_NAME);
                assertThat(spanBuilder.getParentSpan()).isNull();
                assertThat(spanBuilder.getRemoteParentSpanContext()).isSameAs(SpanContext.INVALID);
                assertThat(spanBuilder.getSampler()).isNull();
                assertThat(spanBuilder.getRecordEvents()).isNull();
                assertThat(spanBuilder.getParentLinks()).isEmpty();
                return span;
              }
            });
    Span remoteChildSpan = spanBuilder.startSpan();
    assertThat(remoteChildSpan).isEqualTo(span);
    remoteChildSpan.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  private static class FakeSpanBuilder extends SpanBuilder {

    protected FakeSpanBuilder(
        @Nullable Span parentSpan, @Nullable SpanContext parentSpanContext, String name) {
      super(parentSpan, parentSpanContext, name);
    }

    static FakeSpanBuilder builder(Span parentSpan, String name) {
      return new FakeSpanBuilder(parentSpan, null, name);
    }

    static FakeSpanBuilder builderWithRemoteParent(SpanContext parentSpanContext, String name) {
      return new FakeSpanBuilder(null, parentSpanContext, name);
    }

    @Override
    public Span startSpan() {
      return null;
    }
  }
}
