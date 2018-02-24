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

import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/**
 * A helper class to handle instrumentation in HTTP client/server.
 *
 * <p>This class provides basic operations, such as recording {@link MessageEvent} and handling
 * errors.
 *
 * @since 0.13
 */
public abstract class HttpHandler<Q, P> {

  /** The Open Census tracing component. */
  Tracer tracer;

  /** The {@link TextFormat} used in HTTP propagation. */
  TextFormat textFormat;

  /** The {@link HttpExtractor} used to extract information from request/response. */
  HttpExtractor<Q, P> extractor;

  private AtomicLong sentMessageEventId = new AtomicLong(0);
  private AtomicLong recvMessageEventId = new AtomicLong(0);

  /** Package-protected constructor to allow access from subclasses only. */
  HttpHandler(Tracer tracer, TextFormat textFormat, HttpExtractor<Q, P> extractor) {
    checkNotNull(tracer, "tracer");
    checkNotNull(textFormat, "textFormat");
    checkNotNull(extractor, "extractor");
    this.tracer = tracer;
    this.textFormat = textFormat;
    this.extractor = extractor;
  }

  /**
   * Returns the tracing component.
   *
   * @return the tracing component.
   * @since 0.13
   */
  public Tracer getTracer() {
    return this.tracer;
  }

  /**
   * Returns the {@link TextFormat} used in HTTP propagation.
   *
   * @return the {@code TextFormat} used in HTTP propagation.
   * @since 0.13
   */
  public TextFormat getTextFormat() {
    return this.textFormat;
  }

  /**
   * Returns the {@link HttpExtractor} used to extract information from the request/response.
   *
   * @return the {@code HttpExtractor} used to extract information from the request/response.
   * @since 0.13
   */
  public HttpExtractor<Q, P> getExtractor() {
    return this.extractor;
  }

  /**
   * Returns a span name determined by the given parameters.
   *
   * @param request the request entity.
   * @param method the qualified name of the method which handles the request sending procedure,
   *     which will be used when constructing the span name. Although user can obtain such
   *     information from the request itself, this parameter provides a more convenient way to
   *     specify it directly.
   * @return a span name determined by the given parameters.
   * @since 0.13
   */
  public abstract String getSpanName(Q request, String method);

  /**
   * Generate a sequential id for sent {@link MessageEvent}.
   *
   * @return next id for sent {@code MessageEvent}.
   * @since 0.13
   */
  public long nextSentMessageEventId() {
    return sentMessageEventId.getAndIncrement();
  }

  /**
   * Generate a sequential id for received {@link MessageEvent}.
   *
   * @return next id for received {@code MessageEvent}.
   * @since 0.13
   */
  public long nextRecvMessageEventId() {
    return recvMessageEventId.getAndIncrement();
  }

  /**
   * Records a {@link MessageEvent} with given parameters.
   *
   * @param span the span which this {@code MessageEvent} will be added to.
   * @param id the id of the event.
   * @param type the {@code MessageEvent.Type} of the event.
   * @param uncompressedMessageSize size of the message before compressed (optional).
   * @param compressedMessageSize size of the message after compressed (optional).
   * @since 0.13
   */
  public void recordMessageEvent(
      Span span, long id, Type type, long uncompressedMessageSize, long compressedMessageSize) {
    checkNotNull(span, "span");
    checkNotNull(type, "type");
    MessageEvent messageEvent =
        MessageEvent.builder(type, id)
            .setUncompressedMessageSize(uncompressedMessageSize)
            .setCompressedMessageSize(compressedMessageSize)
            .build();
    span.addMessageEvent(messageEvent);
  }

  /**
   * Parse OpenCensus Status from HTTP response.
   *
   * <p>This method provides a default implementation to map HTTP status code to Open Census Status.
   * The mapping is defined in <a
   * href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">Google API
   * canonical error code</a>.
   *
   * <p>Users can override this method to define their own parsing behaviors.
   *
   * @param response the HTTP response entity. It could be null if error is present.
   * @param error the error occured during response transmition. It could be null if response is
   *     present.
   * @return the corresponding OpenCensus {@code Status}.
   * @since 0.13
   */
  public Status parseResponseStatus(@Nullable P response, @Nullable Throwable error) {
    String message = null;
    Integer statusCode = extractor.getStatusCode(response);

    if (error != null) {
      message = error.getMessage();
      if (message == null) {
        message = error.getClass().getSimpleName();
      }
    }

    String description = String.format("statusCode:%s error:%s", statusCode, message);
    // set status according to response
    if (statusCode == null) {
      return Status.UNKNOWN.withDescription(description);
    } else {
      if (statusCode >= 200 && statusCode < 400) {
        return Status.OK;
      } else {
        // error code, try parse it
        switch (statusCode) {
          case 499:
            return Status.CANCELLED.withDescription(description);
          case 500:
            return Status.INTERNAL.withDescription(description); // Can also be UNKNOWN, DATA_LOSS
          case 400:
            return Status.INVALID_ARGUMENT.withDescription(
                description); // Can also be FAILED_PRECONDITION, OUT_OF_RANGE
          case 504:
            return Status.DEADLINE_EXCEEDED.withDescription(description);
          case 404:
            return Status.NOT_FOUND.withDescription(description);
          case 409:
            return Status.ALREADY_EXISTS.withDescription(description); // Can also be ABORTED
          case 403:
            return Status.PERMISSION_DENIED.withDescription(description);
          case 401:
            return Status.UNAUTHENTICATED.withDescription(description);
          case 429:
            return Status.RESOURCE_EXHAUSTED.withDescription(description);
          case 501:
            return Status.UNIMPLEMENTED.withDescription(description);
          case 503:
            return Status.UNAVAILABLE.withDescription(description);
          default:
            return Status.UNKNOWN.withDescription(description);
        }
      }
    }
  }
}
