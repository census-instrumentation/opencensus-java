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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.contrib.http.util.HttpTraceUtil;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/** Base class for handling request on http client and server. */
abstract class AbstractHttpHandler<Q, P> {
  /** The {@link HttpExtractor} used to extract information from request/response. */
  @VisibleForTesting final HttpExtractor<Q, P> extractor;

  @VisibleForTesting final AtomicLong reqId = new AtomicLong();

  /** Constructor to allow access from same package subclasses only. */
  AbstractHttpHandler(HttpExtractor<Q, P> extractor) {
    checkNotNull(extractor, "extractor");
    this.extractor = extractor;
  }

  /**
   * A convenience to record a {@link MessageEvent} with given parameters.
   *
   * @param span the span which this {@code MessageEvent} will be added to.
   * @param id the id of the event.
   * @param type the {@code MessageEvent.Type} of the event.
   * @param uncompressedMessageSize size of the message before compressed (optional).
   * @param compressedMessageSize size of the message after compressed (optional).
   * @since 0.18
   */
  static void recordMessageEvent(
      Span span, long id, Type type, long uncompressedMessageSize, long compressedMessageSize) {
    MessageEvent messageEvent =
        MessageEvent.builder(type, id)
            .setUncompressedMessageSize(uncompressedMessageSize)
            .setCompressedMessageSize(compressedMessageSize)
            .build();
    span.addMessageEvent(messageEvent);
  }

  private static void putAttributeIfNotEmptyOrNull(Span span, String key, @Nullable String value) {
    if (value != null && !value.isEmpty()) {
      span.putAttribute(key, AttributeValue.stringAttributeValue(value));
    }
  }

  /**
   * Instrument an HTTP span after a message is sent. Typically called when last chunk of request or
   * response is sent.
   *
   * @param context request specific {@link HttpRequestContext}
   * @since 0.18
   */
  public final void handleMessageSent(HttpRequestContext context, long bytes) {
    checkNotNull(context, "context");
    context.sentMessageSize.addAndGet(bytes);
    if (context.span.getOptions().contains(Options.RECORD_EVENTS)) {
      // record compressed size
      recordMessageEvent(context.span, context.reqId, Type.SENT, bytes, 0L);
    }
  }

  /**
   * Instrument an HTTP span after a message is received. Typically called when last chunk of
   * request or response is received.
   *
   * @param context request specific {@link HttpRequestContext}
   * @since 0.18
   */
  public final void handleMessageReceived(HttpRequestContext context, long bytes) {
    checkNotNull(context, "context");
    context.receiveMessageSize.addAndGet(bytes);
    if (context.span.getOptions().contains(Options.RECORD_EVENTS)) {
      // record compressed size
      recordMessageEvent(context.span, context.reqId, Type.RECEIVED, bytes, 0L);
    }
  }

  void spanEnd(Span span, @Nullable P response, @Nullable Throwable error) {
    checkNotNull(span, "span");
    int statusCode = extractor.getStatusCode(response);
    if (span.getOptions().contains(Options.RECORD_EVENTS)) {
      span.putAttribute(
          HttpTraceAttributeConstants.HTTP_STATUS_CODE,
          AttributeValue.longAttributeValue(statusCode));
    }
    span.setStatus(HttpTraceUtil.parseResponseStatus(statusCode, error));
    span.end();
  }

  final String getSpanName(Q request, HttpExtractor<Q, P> extractor) {
    // default span name
    String path = extractor.getPath(request);
    if (path == null) {
      path = "/";
    }
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    return path;
  }

  final void addSpanRequestAttributes(Span span, Q request, HttpExtractor<Q, P> extractor) {
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_USER_AGENT, extractor.getUserAgent(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_HOST, extractor.getHost(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_METHOD, extractor.getMethod(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_PATH, extractor.getPath(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_ROUTE, extractor.getRoute(request));
    putAttributeIfNotEmptyOrNull(
        span, HttpTraceAttributeConstants.HTTP_URL, extractor.getUrl(request));
  }

  /**
   * Retrieves {@link Span} from the {@link HttpRequestContext}.
   *
   * @param context request specific {@link HttpRequestContext}
   * @return {@link Span} associated with the request.
   * @since 0.19
   */
  public Span getSpanFromContext(HttpRequestContext context) {
    checkNotNull(context, "context");
    return context.span;
  }

  HttpRequestContext getNewContext(Span span) {
    return new HttpRequestContext(span, reqId.addAndGet(1L));
  }
}
