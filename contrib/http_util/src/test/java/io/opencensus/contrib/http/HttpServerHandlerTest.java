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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.util.testing.FakeSpan;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.Tags;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Link.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link HttpServerHandler}. */
@RunWith(JUnit4.class)
public class HttpServerHandlerTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Random random = new Random();
  private final SpanContext spanContextRemote =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          null);
  private final SpanContext spanContextLocal =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          null);
  private final Object request = new Object();
  private final Object response = new Object();
  private final Object carrier = new Object();
  @Mock private SpanBuilder spanBuilderWithRemoteParent;
  @Mock private SpanBuilder spanBuilderWithLocalParent;
  @Mock private Tracer tracer;
  @Mock private TextFormat textFormat;
  @Mock private TextFormat.Getter<Object> textFormatGetter;
  @Mock private HttpExtractor<Object, Object> extractor;
  @Captor private ArgumentCaptor<Link> captor;
  @Captor private ArgumentCaptor<Kind> kindArgumentCaptor;
  @Captor private ArgumentCaptor<EndSpanOptions> optionsCaptor;
  private HttpServerHandler<Object, Object, Object> handler;
  private HttpServerHandler<Object, Object, Object> handlerForPublicEndpoint;
  // TODO(hailongwen): use `MockableSpan` instead.
  @Spy private FakeSpan spanWithLocalParent = new FakeSpan(spanContextLocal, null);
  @Spy private FakeSpan spanWithRemoteParent = new FakeSpan(spanContextRemote, null);
  private final TagContext tagContext = Tags.getTagger().getCurrentTagContext();
  private final HttpRequestContext context =
      new HttpRequestContext(spanWithLocalParent, tagContext);

  @Before
  public void setUp() throws SpanContextParseException {
    MockitoAnnotations.initMocks(this);
    handler =
        new HttpServerHandler<Object, Object, Object>(
            tracer, extractor, textFormat, textFormatGetter, false);
    handlerForPublicEndpoint =
        new HttpServerHandler<Object, Object, Object>(
            tracer, extractor, textFormat, textFormatGetter, true);

    when(tracer.spanBuilderWithRemoteParent(any(String.class), same(spanContextRemote)))
        .thenReturn(spanBuilderWithRemoteParent);
    when(tracer.spanBuilderWithExplicitParent(any(String.class), any(Span.class)))
        .thenReturn(spanBuilderWithLocalParent);
    when(spanBuilderWithRemoteParent.setSpanKind(any(Kind.class)))
        .thenReturn(spanBuilderWithRemoteParent);
    when(spanBuilderWithLocalParent.setSpanKind(any(Kind.class)))
        .thenReturn(spanBuilderWithLocalParent);
    when(spanBuilderWithRemoteParent.startSpan()).thenReturn(spanWithRemoteParent);
    when(spanBuilderWithLocalParent.startSpan()).thenReturn(spanWithLocalParent);

    when(textFormat.extract(same(carrier), same(textFormatGetter))).thenReturn(spanContextRemote);
  }

  @Test
  public void constructorDisallowNullTextFormatGetter() {
    thrown.expect(NullPointerException.class);
    new HttpServerHandler<Object, Object, Object>(tracer, extractor, textFormat, null, false);
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
    HttpRequestContext context = handler.handleStart(carrier, request);
    verify(tracer).spanBuilderWithRemoteParent(any(String.class), same(spanContextRemote));
    assertThat(context.span).isEqualTo(spanWithRemoteParent);
  }

  @Test
  public void handleStartShouldIgnoreContextParseException() throws Exception {
    when(textFormat.extract(same(carrier), same(textFormatGetter)))
        .thenThrow(new SpanContextParseException("test"));
    HttpRequestContext context = handler.handleStart(carrier, request);
    verify(tracer).spanBuilderWithExplicitParent(any(String.class), any(Span.class));
    assertThat(context.span).isEqualTo(spanWithLocalParent);
  }

  @Test
  public void handleStartShouldExtractFromCarrier() throws SpanContextParseException {
    handler.handleStart(carrier, request);
    verify(textFormat).extract(same(carrier), same(textFormatGetter));
  }

  @Test
  public void handleStartShouldSetKindToServer() throws SpanContextParseException {
    handler.handleStart(carrier, request);
    verify(spanBuilderWithRemoteParent).setSpanKind(kindArgumentCaptor.capture());

    Kind kind = kindArgumentCaptor.getValue();
    assertThat(kind).isEqualTo(Kind.SERVER);
  }

  @Test
  public void handleStartWithPublicEndpointShouldAddLink() throws Exception {
    handlerForPublicEndpoint.handleStart(carrier, request);
    verify(tracer).spanBuilderWithExplicitParent(any(String.class), any(Span.class));
    verify(spanWithLocalParent).addLink(captor.capture());

    Link link = captor.getValue();
    assertThat(link.getSpanId()).isEqualTo(spanContextRemote.getSpanId());
    assertThat(link.getTraceId()).isEqualTo(spanContextRemote.getTraceId());
    assertThat(link.getType()).isEqualTo(Type.PARENT_LINKED_SPAN);
  }

  @Test
  public void handleEndDisallowNullRequest() {
    thrown.expect(NullPointerException.class);
    handler.handleEnd(context, null, response, null);
  }

  @Test
  public void handleEndShouldEndSpan() {
    HttpRequestContext context = new HttpRequestContext(spanWithLocalParent, tagContext);
    when(extractor.getStatusCode(any(Object.class))).thenReturn(0);

    handler.handleEnd(context, carrier, response, null);
    verify(spanWithLocalParent).end(optionsCaptor.capture());
    EndSpanOptions options = optionsCaptor.getValue();
    assertThat(options).isEqualTo(EndSpanOptions.DEFAULT);
  }
}
