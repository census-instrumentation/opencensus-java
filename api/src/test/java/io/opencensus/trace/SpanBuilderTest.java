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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.common.Scope;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.samplers.Samplers;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SpanBuilder}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Java-6 does not support try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class SpanBuilderTest {
  private Tracer tracer = Tracing.getTracer();
  @Mock private SpanBuilder spanBuilder;
  @Mock private Span span;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(spanBuilder.startSpan()).thenReturn(span);
  }

  @Test
  public void startScopedSpan() {
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    Scope scope = spanBuilder.startScopedSpan();
    try {
      assertThat(tracer.getCurrentSpan()).isSameAs(span);
    } finally {
      scope.close();
    }
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startSpanAndRun() {
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    spanBuilder.startSpanAndRun(
        new Runnable() {
          @Override
          public void run() {
            assertThat(tracer.getCurrentSpan()).isSameAs(span);
          }
        });
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void startSpanAndCall() throws Exception {
    final Object ret = new Object();
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    assertThat(
            spanBuilder.startSpanAndCall(
                new Callable<Object>() {
                  @Override
                  public Object call() throws Exception {
                    assertThat(tracer.getCurrentSpan()).isSameAs(span);
                    return ret;
                  }
                }))
        .isEqualTo(ret);
    verify(span).end(EndSpanOptions.DEFAULT);
    assertThat(tracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void doNotCrash_NoopImplementation() throws Exception {
    SpanBuilder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setParentLinks(Collections.<Span>emptyList());
    spanBuilder.setRecordEvents(true);
    spanBuilder.setSampler(Samplers.alwaysSample());
    spanBuilder.setSpanKind(Kind.SERVER);
    assertThat(spanBuilder.startSpan()).isSameAs(BlankSpan.INSTANCE);
  }
}
