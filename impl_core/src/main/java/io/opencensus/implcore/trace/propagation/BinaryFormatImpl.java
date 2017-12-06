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

package io.opencensus.implcore.trace.propagation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;

/**
 * Implementation of the {@link BinaryFormat}.
 *
 * <p>BinaryFormat format:
 *
 * <ul>
 *   <li>Binary value: &lt;version_id&gt;&lt;version_format&gt;
 *   <li>version_id: 1-byte representing the version id.
 *   <li>For version_id = 0:
 *       <ul>
 *         <li>version_format: &lt;field&gt;&lt;field&gt;
 *         <li>field_format: &lt;field_id&gt;&lt;field_format&gt;
 *         <li>Fields:
 *             <ul>
 *               <li>TraceId: (field_id = 0, len = 16, default = &#34;0000000000000000&#34;) -
 *                   16-byte array representing the trace_id.
 *               <li>SpanId: (field_id = 1, len = 8, default = &#34;00000000&#34;) - 8-byte array
 *                   representing the span_id.
 *               <li>TraceOptions: (field_id = 2, len = 1, default = &#34;0&#34;) - 1-byte array
 *                   representing the trace_options.
 *             </ul>
 *         <li>Fields MUST be encoded using the field id order (smaller to higher).
 *         <li>Valid value example:
 *             <ul>
 *               <li>{0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97,
 *                   98, 99, 100, 101, 102, 103, 104, 2, 1}
 *               <li>version_id = 0;
 *               <li>trace_id = {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}
 *               <li>span_id = {97, 98, 99, 100, 101, 102, 103, 104};
 *               <li>trace_options = {1};
 *             </ul>
 *       </ul>
 * </ul>
 */
@VisibleForTesting
public final class BinaryFormatImpl extends BinaryFormat {
  private static final byte VERSION_ID = 0;
  private static final int VERSION_ID_OFFSET = 0;
  // The version_id/field_id size in bytes.
  private static final byte ID_SIZE = 1;
  private static final byte TRACE_ID_FIELD_ID = 0;
  private static final int TRACE_ID_FIELD_ID_OFFSET = VERSION_ID_OFFSET + ID_SIZE;
  private static final int TRACE_ID_OFFSET = TRACE_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte SPAN_ID_FIELD_ID = 1;
  private static final int SPAN_ID_FIELD_ID_OFFSET = TRACE_ID_OFFSET + TraceId.SIZE;
  private static final int SPAN_ID_OFFSET = SPAN_ID_FIELD_ID_OFFSET + ID_SIZE;
  private static final byte TRACE_OPTION_FIELD_ID = 2;
  private static final int TRACE_OPTION_FIELD_ID_OFFSET = SPAN_ID_OFFSET + SpanId.SIZE;
  private static final int TRACE_OPTIONS_OFFSET = TRACE_OPTION_FIELD_ID_OFFSET + ID_SIZE;
  private static final int FORMAT_LENGTH =
      4 * ID_SIZE + TraceId.SIZE + SpanId.SIZE + TraceOptions.SIZE;

  @Override
  public byte[] toByteArray(SpanContext spanContext) {
    checkNotNull(spanContext, "spanContext");
    byte[] bytes = new byte[FORMAT_LENGTH];
    bytes[VERSION_ID_OFFSET] = VERSION_ID;
    bytes[TRACE_ID_FIELD_ID_OFFSET] = TRACE_ID_FIELD_ID;
    spanContext.getTraceId().copyBytesTo(bytes, TRACE_ID_OFFSET);
    bytes[SPAN_ID_FIELD_ID_OFFSET] = SPAN_ID_FIELD_ID;
    spanContext.getSpanId().copyBytesTo(bytes, SPAN_ID_OFFSET);
    bytes[TRACE_OPTION_FIELD_ID_OFFSET] = TRACE_OPTION_FIELD_ID;
    spanContext.getTraceOptions().copyBytesTo(bytes, TRACE_OPTIONS_OFFSET);
    return bytes;
  }

  @Override
  public SpanContext fromByteArray(byte[] bytes) throws SpanContextParseException {
    checkNotNull(bytes, "bytes");
    if (bytes.length == 0 || bytes[0] != VERSION_ID) {
      throw new SpanContextParseException("Unsupported version.");
    }
    TraceId traceId = TraceId.INVALID;
    SpanId spanId = SpanId.INVALID;
    TraceOptions traceOptions = TraceOptions.DEFAULT;
    int pos = 1;
    try {
      if (bytes.length > pos && bytes[pos] == TRACE_ID_FIELD_ID) {
        traceId = TraceId.fromBytes(bytes, pos + ID_SIZE);
        pos += ID_SIZE + TraceId.SIZE;
      }
      if (bytes.length > pos && bytes[pos] == SPAN_ID_FIELD_ID) {
        spanId = SpanId.fromBytes(bytes, pos + ID_SIZE);
        pos += ID_SIZE + SpanId.SIZE;
      }
      if (bytes.length > pos && bytes[pos] == TRACE_OPTION_FIELD_ID) {
        traceOptions = TraceOptions.fromBytes(bytes, pos + ID_SIZE);
      }
      return SpanContext.create(traceId, spanId, traceOptions);
    } catch (IndexOutOfBoundsException e) {
      throw new SpanContextParseException("Invalid input.", e);
    }
  }
}
