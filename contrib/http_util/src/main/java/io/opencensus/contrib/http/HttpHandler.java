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
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import javax.annotation.Nullable;

/**
 * Base class for {@link HttpClientHandler} and {@link HttpServerHandler}.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @since 0.13
 */
abstract class HttpHandler<Q, P> {

  /** The Open Census tracing component. */
  @VisibleForTesting final Tracer tracer;

  /** The {@link HttpExtractor} used to extract information from request/response. */
  @VisibleForTesting final HttpExtractor<Q, P> extractor;

  /** The {@link HttpSpanCustomizer} used to customize span behaviors. * */
  @VisibleForTesting final HttpSpanCustomizer<Q, P> customizer;

  /** Package-protected constructor to allow access from subclasses only. */
  HttpHandler(Tracer tracer, HttpExtractor<Q, P> extractor, HttpSpanCustomizer<Q, P> customizer) {
    checkNotNull(tracer, "tracer");
    checkNotNull(extractor, "extractor");
    checkNotNull(customizer, "customizer");
    this.tracer = tracer;
    this.extractor = extractor;
    this.customizer = customizer;
  }

  /**
   * A convenience to record a {@link MessageEvent} with given parameters.
   *
   * @param span the span which this {@code MessageEvent} will be added to.
   * @param id the id of the event.
   * @param type the {@code MessageEvent.Type} of the event.
   * @param uncompressedMessageSize size of the message before compressed (optional).
   * @param compressedMessageSize size of the message after compressed (optional).
   * @since 0.13
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

  /**
   * Instrument an HTTP span after a message is sent.
   *
   * @param span the span.
   * @param messageId an id for the message.
   * @param messageSize the size of the message.
   * @since 0.13
   */
  public void handleMessageSent(Span span, long messageId, long messageSize) {
    checkNotNull(span, "span");
    recordMessageEvent(span, messageId, Type.SENT, messageSize, 0L);
  }

  /**
   * Instrument an HTTP span after a message is received.
   *
   * @param span the span.
   * @param messageId an id for the message.
   * @param messageSize the size of the message.
   * @since 0.13
   */
  public void handleMessageReceived(Span span, long messageId, long messageSize) {
    checkNotNull(span, "span");
    // record message size
    recordMessageEvent(span, messageId, Type.RECEIVED, messageSize, 0L);
  }

  /**
   * Close an HTTP span.
   *
   * <p>This method will set status of the span and end it. Users could use {@link
   * HttpSpanCustomizer#customizeSpanEnd} to overwrite the status before the span is ended.
   *
   * @param response the HTTP response entity. {@code null} means invalid response.
   * @param error the error occurs when processing the response.
   * @param span the span.
   * @since 0.13
   */
  public void handleEnd(@Nullable P response, @Nullable Throwable error, Span span) {
    checkNotNull(span, "span");
    // user-customized handling.
    customizer.customizeSpanEnd(response, error, span, extractor);
    span.end();
  }
}
