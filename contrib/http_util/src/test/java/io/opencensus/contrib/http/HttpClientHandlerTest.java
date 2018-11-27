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

package io.opencensus.contrib.http;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.common.Scope;
import io.opencensus.contrib.http.util.testing.FakeSpan;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link HttpClientHandler}. */
@RunWith(JUnit4.class)
public class HttpClientHandlerTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Random random = new Random();
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          null);
  private final Object request = new Object();
  private final Object carrier = new Object();
  private final Object response = new Object();
  @Mock private SpanBuilder spanBuilder;
  @Mock private Tracer tracer;
  @Mock private TextFormat textFormat;
  @Mock private TextFormat.Setter<Object> textFormatSetter;
  @Mock private HttpExtractor<Object, Object> extractor;
  private HttpClientHandler<Object, Object, Object> handler;
  @Spy private FakeSpan parentSpan = new FakeSpan(spanContext, null);
  private final FakeSpan childSpan = new FakeSpan(parentSpan.getContext(), null);
  @Captor private ArgumentCaptor<EndSpanOptions> optionsCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler =
        new HttpClientHandler<Object, Object, Object>(
            tracer, extractor, textFormat, textFormatSetter);
    when(tracer.spanBuilderWithExplicitParent(any(String.class), same(parentSpan)))
        .thenReturn(spanBuilder);
    when(spanBuilder.startSpan()).thenReturn(childSpan);
  }

  @Test
  public void constructorDisallowNullTextFormatSetter() {
    thrown.expect(NullPointerException.class);
    new HttpClientHandler<Object, Object, Object>(tracer, extractor, textFormat, null);
  }

  @Test
  public void handleStartWithoutSpanDisallowNullCarrier() {
    thrown.expect(NullPointerException.class);
    handler.handleStart(parentSpan, /*carrier=*/ null, request);
  }

  @Test
  public void handleStartDisallowNullRequest() {
    thrown.expect(NullPointerException.class);
    handler.handleStart(parentSpan, carrier, /*request=*/ null);
  }

  @Test
  public void handleStartShouldCreateChildSpanInCurrentContext() {
    Scope scope = tracer.withSpan(parentSpan);
    try {
      HttpContext context = handler.handleStart(null, carrier, request);
      verify(tracer).spanBuilderWithExplicitParent(any(String.class), same(parentSpan));
      assertThat(context.span).isEqualTo(childSpan);
    } finally {
      scope.close();
    }
  }

  @Test
  public void handleStartCreateChildSpanInSpecifiedContext() {
    // without scope
    HttpContext context = handler.handleStart(parentSpan, carrier, request);
    verify(tracer).spanBuilderWithExplicitParent(any(String.class), same(parentSpan));
    assertThat(context.span).isEqualTo(childSpan);
  }

  @Test
  public void handleStartShouldInjectCarrier() {
    handler.handleStart(parentSpan, carrier, request);
    verify(textFormat).inject(same(spanContext), same(carrier), same(textFormatSetter));
  }

  @Test
  public void handleEndDisallowNullContext() {
    thrown.expect(NullPointerException.class);
    handler.handleEnd(null, request, response, null);
  }

  @Test
  public void handleEndShouldEndSpan() {
    HttpContext context = new HttpContext(parentSpan, 1L);
    when(extractor.getStatusCode(any(Object.class))).thenReturn(0);
    handler.handleEnd(context, request, response, null);
    verify(parentSpan).end(optionsCaptor.capture());
    EndSpanOptions options = optionsCaptor.getValue();
    assertThat(options).isEqualTo(EndSpanOptions.DEFAULT);
  }
}
