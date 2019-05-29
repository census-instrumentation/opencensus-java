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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Unit tests for {@link Span}. */
@RunWith(JUnit4.class)
public class SpanTest {
  private Random random;
  private SpanContext spanContext;
  private SpanContext notSampledSpanContext;
  private EnumSet<Span.Options> spanOptions;

  @Before
  public void setUp() {
    random = new Random(1234);
    spanContext =
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.builder().setIsSampled(true).build());
    notSampledSpanContext =
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT);
    spanOptions = EnumSet.of(Span.Options.RECORD_EVENTS);
  }

  @Test(expected = NullPointerException.class)
  public void newSpan_WithNullContext() {
    new NoopSpan(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newSpan_SampledContextAndNullOptions() {
    new NoopSpan(spanContext, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void newSpan_SampledContextAndEmptyOptions() {
    new NoopSpan(spanContext, EnumSet.noneOf(Span.Options.class));
  }

  @Test
  public void getOptions_WhenNullOptions() {
    Span span = new NoopSpan(notSampledSpanContext, null);
    assertThat(span.getOptions()).isEmpty();
  }

  @Test
  public void getContextAndOptions() {
    Span span = new NoopSpan(spanContext, spanOptions);
    assertThat(span.getContext()).isEqualTo(spanContext);
    assertThat(span.getOptions()).isEqualTo(spanOptions);
  }

  @Test
  public void putAttributeCallsAddAttributesByDefault() {
    Span span = Mockito.spy(new NoopSpan(spanContext, spanOptions));
    span.putAttribute("MyKey", AttributeValue.booleanAttributeValue(true));
    span.end();
    verify(span)
        .putAttributes(
            eq(Collections.singletonMap("MyKey", AttributeValue.booleanAttributeValue(true))));
  }

  @Test
  public void endCallsEndWithDefaultOptions() {
    Span span = Mockito.spy(new NoopSpan(spanContext, spanOptions));
    span.end();
    verify(span).end(same(EndSpanOptions.DEFAULT));
  }

  @Test
  public void addMessageEventDefaultImplementation() {
    Span mockSpan = Mockito.mock(Span.class);
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.SENT, 123)
            .setUncompressedMessageSize(456)
            .setCompressedMessageSize(789)
            .build();
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.SENT, 123)
            .setUncompressedMessageSize(456)
            .setCompressedMessageSize(789)
            .build();
    Mockito.doCallRealMethod().when(mockSpan).addMessageEvent(messageEvent);
    mockSpan.addMessageEvent(messageEvent);
    verify(mockSpan).addNetworkEvent(eq(networkEvent));
  }
}
