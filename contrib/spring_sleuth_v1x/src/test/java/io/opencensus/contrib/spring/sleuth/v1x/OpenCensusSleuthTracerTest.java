/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.spring.sleuth.v1x;

import static com.google.common.truth.Truth.assertThat;

import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.cloud.sleuth.DefaultSpanNamer;
import org.springframework.cloud.sleuth.NoOpSpanReporter;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.log.NoOpSpanLogger;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;

/** Unit tests for {@link OpenCensusSleuthTracer}. */
@RunWith(JUnit4.class)
public class OpenCensusSleuthTracerTest {
  private static final Tracer tracer =
      new OpenCensusSleuthTracer(
          new AlwaysSampler(),
          new Random(),
          new DefaultSpanNamer(),
          new NoOpSpanLogger(),
          new NoOpSpanReporter(),
          new TraceKeys());

  @After
  @Before
  public void verifyNotTracing() {
    assertThat(tracer.isTracing()).isFalse();
  }

  @Test
  public void testRootSpanAndClose() {
    Span root = tracer.createSpan("root");
    assertCurrentSpanIs(root);
    assertThat(root.getSavedSpan()).isNull();
    Span parent = tracer.close(root);
    assertThat(parent).isNull();
  }

  @Test
  public void testSpanStackAndClose() {
    Span[] spans = createSpansAndAssertCurrent(3);
    // pop the stack
    for (int i = spans.length - 1; i >= 0; i--) {
      assertCurrentSpanIs(spans[i]);
      Span parent = tracer.close(spans[i]);
      assertThat(parent).isEqualTo(spans[i].getSavedSpan());
    }
  }

  @Test
  public void testSpanStackAndCloseOutOfOrder() {
    Span[] spans = createSpansAndAssertCurrent(3);
    // try to close a non-current span
    tracer.close(spans[spans.length - 2]);
    assertCurrentSpanIs(spans[spans.length - 1]);
    // pop the stack
    for (int i = spans.length - 1; i >= 0; i--) {
      tracer.close(spans[i]);
    }
  }

  @Test
  public void testDetachNull() {
    Span parent = tracer.detach(null);
    assertThat(parent).isNull();
  }

  @Test
  public void testRootSpanAndDetach() {
    Span root = tracer.createSpan("root");
    assertCurrentSpanIs(root);
    assertThat(root.getSavedSpan()).isNull();
    Span parent = tracer.detach(root);
    assertThat(parent).isNull();
  }

  @Test
  public void testSpanStackAndDetach() {
    Span[] spans = createSpansAndAssertCurrent(3);
    Span parent = tracer.detach(spans[spans.length - 1]);
    assertThat(parent).isEqualTo(spans[spans.length - 2]);
  }

  @Test
  public void testSpanStackAndDetachOutOfOrder() {
    Span[] spans = createSpansAndAssertCurrent(3);
    // try to detach a non-current span
    tracer.detach(spans[spans.length - 2]);
    assertCurrentSpanIs(spans[spans.length - 1]);
    Span parent = tracer.detach(spans[spans.length - 1]);
    assertThat(parent).isEqualTo(spans[spans.length - 2]);
  }

  @Test
  public void testContinueNull() {
    Span span = tracer.continueSpan(null);
    assertThat(span).isNull();
  }

  @Test
  public void testRootSpanAndContinue() {
    Span root = tracer.createSpan("root");
    assertCurrentSpanIs(root);
    tracer.detach(root);
    Span span = tracer.continueSpan(root);
    assertThat(span).isEqualTo(root);
    tracer.detach(span);
  }

  @Test
  public void testSpanStackAndContinue() {
    Span[] spans = createSpansAndAssertCurrent(3);
    Span original = tracer.getCurrentSpan();
    assertThat(original).isEqualTo(spans[spans.length - 1]);
    Span parent = tracer.detach(original);
    assertThat(parent).isEqualTo(spans[spans.length - 2]);
    assertThat(tracer.getCurrentSpan()).isNull();

    Span continued = tracer.continueSpan(original);
    assertCurrentSpanIs(continued);
    assertThat(continued.getSavedSpan()).isEqualTo(parent);
    assertThat(continued).isEqualTo(original);
    tracer.detach(continued);
  }

  @Test
  public void testSpanStackAndCreateAndContinue() {
    createSpansAndAssertCurrent(3);
    Span original = tracer.getCurrentSpan();
    tracer.detach(original);
    Span root = tracer.createSpan("root");
    assertCurrentSpanIs(root);
    Span continued = tracer.continueSpan(original);
    assertCurrentSpanIs(continued);
    assertThat(continued.getSavedSpan()).isEqualTo(root);
    assertThat(continued).isEqualTo(original);
    assertThat(continued.getSavedSpan()).isNotEqualTo(original.getSavedSpan());
    tracer.detach(continued);
  }

  // Verifies span and associated saved span.
  private static void assertCurrentSpanIs(Span span) {
    assertThat(tracer.getCurrentSpan()).isEqualTo(span);
    assertThat(tracer.getCurrentSpan().getSavedSpan()).isEqualTo(span.getSavedSpan());

    assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(span);
    assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan().getSavedSpan())
        .isEqualTo(span.getSavedSpan());
  }

  private static Span[] createSpansAndAssertCurrent(int len) {
    Span[] spans = new Span[len];

    Span current = null;
    for (int i = 0; i < len; i++) {
      current = tracer.createSpan("span" + i, current);
      spans[i] = current;
      assertCurrentSpanIs(current);
    }
    return spans;
  }
}
