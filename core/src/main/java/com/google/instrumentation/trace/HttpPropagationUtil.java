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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.BaseEncoding;

/**
 * This is a helper class for {@link SpanContext} propagation on the wire when using the HTTP
 * transport protocol.
 *
 * <p>HTTP header format:
 *
 * <ul>
 * <li>Header name: Trace-Context
 * <li>Header value: base16(&lt;version_id&gt;)&lt;version_format&gt;
 * <li>version_id: 1-byte representing the version id.
 * <li>For version_id = 0:
 *     <ul>
 *     <li>version_format: base16(&lt;trace-id&gt;&lt;span-id&gt;&lt;trace-options&gt;)
 *     <li>trace-id: 16-byte array representing the trace_id.
 *     <li>span-id: 8-byte array representing the span_id.
 *     <li>trace-options: 4-byte array representing the trace_options. It is in little-endian order,
 *         if represented as an int.
 *     <li>Valid value example:
 *         <ul>
 *         <li>"00404142434445464748494A4B4C4D4E4F616263646566676801000000"
 *         <li>version_id = 0;
 *         <li>trace_id = {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}
 *         <li>span_id = {97, 98, 99, 100, 101, 102, 103, 104};
 *         <li>trace_options = {1, 0, 0, 0} == 1;
 *         </ul>
 *     </ul>
 *
 * </ul>
 *
 * <p>All characters in the header value must be upper case and US-ASCII encoded.
 *
 * <p>It is strongly encouraged to use this format when using HTTP as a RPC transport.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracer.getTracer();
 * void onSendRequest() {
 *   try (NonThrowingCloseable ss = tracer.spanBuilder("Sent.MyRequest")) {
 *     String headerName = HttpPropagationUtil.HTTP_HEADER_NAME;
 *     String headerValue = HttpPropagationUtil.toHttpHeaderValue(span.context());
 *     headers.add(headerName, headerValue);
 *     // Send the HTTP request and wait for the response.
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage on the server:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracer.getTracer();
 * void onRequestReceived() {
 *   String headerName = HttpPropagationUtil.HTTP_HEADER_NAME;
 *   SpanContext spanContext = HttpPropagationUtil.fromHttpHeaderValue(headers.find(headerName));
 *   try (NonThrowingCloseable ss =
 *            tracer.spanBuilderWithRemoteParent(spanContext, "Recv.MyRequest").startScopedSpan() {
 *     // Handle request and send response back.
 *   }
 * }
 * }</pre>
 */
public final class HttpPropagationUtil {
  private static final byte VERSION_ID = 0;
  // The version_id size in bytes.
  private static final byte VERSION_ID_SIZE = 1;
  private static final int BINARY_VERSION_TRACE_ID_OFFSET = VERSION_ID_SIZE;
  private static final int BINARY_VERSION_SPAN_ID_OFFSET = VERSION_ID_SIZE + TraceId.SIZE;
  private static final int BINARY_VERSION_TRACE_OPTIONS_OFFSET =
      VERSION_ID_SIZE + TraceId.SIZE + SpanId.SIZE;
  private static final int BINARY_VERSION_FORMAT_LENGTH =
      VERSION_ID_SIZE + TraceId.SIZE + SpanId.SIZE + TraceOptions.SIZE;
  private static final int HEX_VERSION_FORMAT_LENGTH = 2 * BINARY_VERSION_FORMAT_LENGTH;

  /** The header name that must be used in the HTTP request for the tracing context. */
  public static final String HTTP_HEADER_NAME = "Trace-Context";

  // Disallow instances of this class.
  private HttpPropagationUtil() {}

  /**
   * Serializes a {@link SpanContext} using the HTTP standard format.
   *
   * @param spanContext the {@code SpanContext} to serialize.
   * @return the serialized US-ASCII encoded HTTP header value.
   * @throws NullPointerException if the {@code spanContext} is null.
   */
  public static String toHttpHeaderValue(SpanContext spanContext) {
    checkNotNull(spanContext, "spanContext");
    byte[] bytes = new byte[BINARY_VERSION_FORMAT_LENGTH];
    bytes[0] = VERSION_ID;
    spanContext.getTraceId().copyBytesTo(bytes, BINARY_VERSION_TRACE_ID_OFFSET);
    spanContext.getSpanId().copyBytesTo(bytes, BINARY_VERSION_SPAN_ID_OFFSET);
    spanContext.getTraceOptions().copyBytesTo(bytes, BINARY_VERSION_TRACE_OPTIONS_OFFSET);
    return BaseEncoding.base16().encode(bytes);
  }

  /**
   * Parses the {@link SpanContext} from the HTTP standard format.
   *
   * @param input a US-ASCII encoded buffer of characters from which the {@code SpanContext} will be
   *     parsed.
   * @return the parsed {@code SpanContext}.
   * @throws NullPointerException if the {@code input} is null.
   * @throws IllegalArgumentException if the {@code input} is invalid.
   */
  public static SpanContext fromHttpHeaderValue(CharSequence input) {
    checkNotNull(input, "input");
    checkArgument(
        input.length() == HEX_VERSION_FORMAT_LENGTH,
        "Invalid input size: expected %s, got %s",
        HEX_VERSION_FORMAT_LENGTH,
        input.length());
    byte[] bytes = BaseEncoding.base16().decode(input);
    checkArgument(bytes[0] == VERSION_ID, "Unsupported version.");
    return new SpanContext(
        TraceId.fromBytes(bytes, BINARY_VERSION_TRACE_ID_OFFSET),
        SpanId.fromBytes(bytes, BINARY_VERSION_SPAN_ID_OFFSET),
        TraceOptions.fromBytes(bytes, BINARY_VERSION_TRACE_OPTIONS_OFFSET));
  }
}
