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

package io.opencensus.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.ParseException;

/**
 * This is a helper class for {@link SpanContext} propagation on the wire using binary encoding.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final BinaryPropagationHandler binaryPropagationHandler =
 *     Tracing.getBinaryPropagationHandler();
 * void onSendRequest() {
 *   try (NonThrowingCloseable ss = tracer.spanBuilder("Sent.MyRequest").startScopedSpan()) {
 *     byte[] binaryValue = binaryPropagationHandler.toBinaryValue(
 *         tracer.getCurrentContext().context());
 *     // Send the request including the binaryValue and wait for the response.
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage on the server:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final BinaryPropagationHandler binaryPropagationHandler =
 *     Tracing.getBinaryPropagationHandler();
 * void onRequestReceived() {
 *   // Get the binaryValue from the request.
 *   SpanContext spanContext = SpanContext.INVALID;
 *   try {
 *     if (binaryValue != null) {
 *       spanContext = binaryPropagationHandler.fromBinaryValue(binaryValue);
 *     }
 *   } catch (ParseException e) {
 *     // Maybe log the exception.
 *   }
 *   try (NonThrowingCloseable ss =
 *            tracer.spanBuilderWithRemoteParent(spanContext, "Recv.MyRequest").startScopedSpan()) {
 *     // Handle request and send response back.
 *   }
 * }
 * }</pre>
 */
public abstract class BinaryPropagationHandler {
  static final NoopBinaryPropagationHandler noopBinaryPropagationHandler =
      new NoopBinaryPropagationHandler();

  /**
   * Serializes a {@link SpanContext} using the binary format.
   *
   * @param spanContext the {@code SpanContext} to serialize.
   * @return the serialized binary value.
   * @throws NullPointerException if the {@code spanContext} is {@code null}.
   */
  public abstract byte[] toBinaryValue(SpanContext spanContext);

  /**
   * Parses the {@link SpanContext} from the binary format.
   *
   * @param bytes a binary encoded buffer from which the {@code SpanContext} will be parsed.
   * @return the parsed {@code SpanContext}.
   * @throws NullPointerException if the {@code input} is {@code null}.
   * @throws ParseException if the version is not supported or the input is invalid
   */
  public abstract SpanContext fromBinaryValue(byte[] bytes) throws ParseException;

  /**
   * Returns the no-op implementation of the {@code BinaryPropagationHandler}.
   *
   * @return the no-op implementation of the {@code BinaryPropagationHandler}.
   */
  static BinaryPropagationHandler getNoopBinaryPropagationHandler() {
    return noopBinaryPropagationHandler;
  }

  private static final class NoopBinaryPropagationHandler extends BinaryPropagationHandler {
    @Override
    public byte[] toBinaryValue(SpanContext spanContext) {
      checkNotNull(spanContext, "spanContext");
      return new byte[0];
    }

    @Override
    public SpanContext fromBinaryValue(byte[] bytes) throws ParseException {
      checkNotNull(bytes, "bytes");
      return SpanContext.INVALID;
    }

    private NoopBinaryPropagationHandler() {}
  }
}
