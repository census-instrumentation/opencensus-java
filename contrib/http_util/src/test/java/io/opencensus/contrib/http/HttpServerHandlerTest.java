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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.util.testing.FakeSpan;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link HttpServerHandler}. */
@RunWith(JUnit4.class)
public class HttpServerHandlerTest {

  @Mock private SpanBuilder spanBuilderWithRemoteParent;
  @Mock private SpanBuilder spanBuilderWithLocalParent;
  @Mock private Tracer tracer;
  @Mock private TextFormat textFormat;
  @Mock private TextFormat.Getter<Object> textFormatGetter;
  @Mock private HttpExtractor<Object, Object> extractor;
  @Mock private HttpSpanCustomizer<Object, Object> customizer;

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private HttpServerHandler<Object, Object, Object> handler;

  private final Random random = new Random();
  private final SpanContext spanContextRemote =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final SpanContext spanContextLocal =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);

  private final Object request = new Object();
  private final Object carrier = new Object();
  // TODO(hailongwen): use `MockableSpan` instead.
  @Spy private FakeSpan spanWithLocalParent = new FakeSpan(spanContextLocal);
  @Spy private FakeSpan spanWithRemoteParent = new FakeSpan(spanContextRemote);

  @Before
  public void setUp() throws SpanContextParseException {
    MockitoAnnotations.initMocks(this);
    handler =
        new HttpServerHandler<Object, Object, Object>(
            tracer, extractor, customizer, textFormat, textFormatGetter);

    when(tracer.spanBuilderWithRemoteParent(any(String.class), same(spanContextRemote)))
        .thenReturn(spanBuilderWithRemoteParent);
    when(tracer.spanBuilderWithExplicitParent(any(String.class), any(Span.class)))
        .thenReturn(spanBuilderWithLocalParent);
    when(spanBuilderWithRemoteParent.startSpan()).thenReturn(spanWithRemoteParent);
    when(spanBuilderWithLocalParent.startSpan()).thenReturn(spanWithLocalParent);

    when(textFormat.extract(same(carrier), same(textFormatGetter))).thenReturn(spanContextRemote);
    doCallRealMethod()
        .when(customizer)
        .customizeSpanBuilder(same(request), any(SpanBuilder.class), same(extractor));
  }

  @Test
  public void constructorDisallowNullTextFormatGetter() {
    thrown.expect(NullPointerException.class);
    new HttpServerHandler<Object, Object, Object>(tracer, extractor, customizer, textFormat, null);
  }

  @Test
  public void handleStartDisallowNullCarrier() {
    thrown.expect(NullPointerException.class);
    handler.handleStart(/*carrier=*/ null, request);
  }

  @Test
  public void handleStartDisallowNullRequest() {
    thrown.expect(NullPointerException.class);
    handler.handleStart(carrier, /*request=*/ null);
  }

  @Test
  public void handleStartShouldCreateChildSpanUnderParent() throws SpanContextParseException {
    handler.handleStart(carrier, request);
    verify(tracer).spanBuilderWithRemoteParent(any(String.class), same(spanContextRemote));
  }

  @Test
  public void handleStartShouldIgnoreContextParseException() throws Exception {
    when(textFormat.extract(same(carrier), same(textFormatGetter)))
        .thenThrow(new SpanContextParseException("test"));
    handler.handleStart(carrier, request);
    verify(tracer).spanBuilderWithExplicitParent(any(String.class), any(Span.class));
  }

  @Test
  public void handleStartShouldExtractFromCarrier() throws SpanContextParseException {
    handler.handleStart(carrier, request);
    verify(textFormat).extract(same(carrier), same(textFormatGetter));
  }

  @Test
  public void handleStartShouldInvokeCustomizer() {
    handler.handleStart(carrier, request);
    verify(customizer)
        .customizeSpanStart(same(request), same(spanWithRemoteParent), same(extractor));
  }
}
