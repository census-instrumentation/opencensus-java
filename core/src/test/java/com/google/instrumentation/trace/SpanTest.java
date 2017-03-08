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
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;
import java.util.EnumSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Unit tests for {@link Span}. */
@RunWith(JUnit4.class)
public class SpanTest {
  private static final SpanContext spanContext =
      new SpanContext(
          TraceId.generateRandomId(new SecureRandom()),
          SpanId.generateRandomId(new SecureRandom()),
          TraceOptions.getDefault());
  private static final EnumSet<Span.Options> spanOptions = EnumSet.of(Span.Options.RECORD_EVENTS);

  @Test(expected = NullPointerException.class)
  public void newSpan_WithNullContext() {
    new NoopSpan(null, null);
  }

  @Test
  public void getOptions_WhenNullOptions() {
    Span span = new NoopSpan(spanContext, null);
    assertThat(span.getOptions()).isEmpty();
  }

  @Test
  public void getContextAndOptions() {
    Span span = new NoopSpan(spanContext, spanOptions);
    assertThat(span.getContext()).isEqualTo(spanContext);
    assertThat(span.getOptions()).isEqualTo(spanOptions);
  }

  @Test
  public void endCallsEndWithDefaultOptions() {
    Span span = Mockito.spy(new NoopSpan(spanContext, spanOptions));
    span.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void closeCallsEndWithDefaultOptions() {
    Span span = Mockito.spy(new NoopSpan(spanContext, spanOptions));
    span.close();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  // No-op implementation of the Span for testing only.
  private static class NoopSpan extends Span {
    private NoopSpan(SpanContext context, EnumSet<Span.Options> options) {
      super(context, options);
    }

    @Override
    public void addLabels(Labels labels) {}

    @Override
    public void addAnnotation(String description) {}

    @Override
    public void addAnnotation(String description, Labels labels) {}

    @Override
    public void addNetworkEvent(NetworkEvent networkEvent) {}

    @Override
    public void addChildLink(Span childLink) {}

    @Override
    public void end(EndSpanOptions options) {}
  }
}
