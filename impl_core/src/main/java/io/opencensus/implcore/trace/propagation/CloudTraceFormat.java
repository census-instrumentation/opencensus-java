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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.primitives.UnsignedLongs;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the "X-Cloud-Trace-Context" format, defined by the Google Cloud Trace.
 *
 * <p>The supported format is the following:
 *
 * <pre>
 * &lt;TRACE_ID&gt;/&lt;SPAN_ID&gt;[;o=&lt;TRACE_OPTIONS&gt;]
 * </pre>
 *
 * <ul>
 *   <li>TRACE_ID is a 32-character hex value;
 *   <li>SPAN_ID is a decimal representation of a unsigned long;
 *   <li>TRACE_OPTIONS is a hex representation of a 32-bit unsigned integer. The least significant
 *       bit defines whether a request is traced (1 - enabled, 0 - disabled). Behaviors of other
 *       bits are currently undefined. This value is optional. Although upstream service may leave
 *       this value unset to leave the sampling decision up to downstream client, this utility will
 *       always default it to 0 if absent.
 * </ul>
 *
 * <p>Valid values:
 *
 * <ul>
 *   <li>"105445aa7843bc8bf206b120001000/123;o=1"
 *   <li>"105445aa7843bc8bf206b120001000/123"
 *   <li>"105445aa7843bc8bf206b120001000/123;o=0"
 * </ul>
 */
final class CloudTraceFormat extends TextFormat {
  static final String HEADER_NAME = "X-Cloud-Trace-Context";
  static final List<String> FIELDS = Collections.singletonList(HEADER_NAME);
  static final char SPAN_ID_DELIMITER = '/';
  static final String TRACE_OPTION_DELIMITER = ";o=";
  static final String SAMPLED = "1";
  static final String NOT_SAMPLED = "0";
  static final int TRACE_ID_SIZE = 2 * TraceId.SIZE;
  static final int TRACE_OPTION_DELIMITER_SIZE = TRACE_OPTION_DELIMITER.length();

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    checkNotNull(spanContext, "spanContext");
    checkNotNull(setter, "setter");
    checkNotNull(carrier, "carrier");
    StringBuilder builder =
        new StringBuilder()
            .append(spanContext.getTraceId().toLowerBase16())
            .append(SPAN_ID_DELIMITER)
            .append(UnsignedLongs.toString(spanIdToLong(spanContext.getSpanId())))
            .append(TRACE_OPTION_DELIMITER)
            .append(spanContext.getTraceOptions().isSampled() ? SAMPLED : NOT_SAMPLED);

    setter.put(carrier, HEADER_NAME, builder.toString());
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) throws SpanContextParseException {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");
    try {
      String headerStr = getter.get(carrier, HEADER_NAME);
      if (headerStr == null) {
        throw new SpanContextParseException("Missing " + HEADER_NAME);
      }

      int delimiterPos = headerStr.indexOf(SPAN_ID_DELIMITER);
      int traceOptionsPos = headerStr.indexOf(TRACE_OPTION_DELIMITER);
      checkArgument(delimiterPos == TRACE_ID_SIZE, "Invalid TRACE_ID size");

      TraceId traceId = TraceId.fromLowerBase16(headerStr.substring(0, TRACE_ID_SIZE));
      String spanIdStr =
          headerStr.substring(
              TRACE_ID_SIZE + 1, traceOptionsPos < 0 ? headerStr.length() : traceOptionsPos);
      SpanId spanId = longToSpanId(UnsignedLongs.parseUnsignedLong(spanIdStr, 10));
      TraceOptions traceOptions = TraceOptions.DEFAULT;
      if (traceOptionsPos > 0) {
        String traceOptionsStr = headerStr.substring(traceOptionsPos + TRACE_OPTION_DELIMITER_SIZE);
        if (traceOptionsStr.isEmpty()) {
          throw new SpanContextParseException("Invalid TRACE_OPTIONS");
        }
        if (SAMPLED.equals(traceOptionsStr)) {
          traceOptions = TraceOptions.builder().setIsSampled(true).build();
        }
      }
      return SpanContext.create(traceId, spanId, traceOptions);
    } catch (IllegalArgumentException e) {
      throw new SpanContextParseException("Invalid input", e);
    }
  }

  // Using big-endian encoding.
  private static SpanId longToSpanId(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(SpanId.SIZE);
    buffer.putLong(x);
    return SpanId.fromBytes(buffer.array());
  }

  // Using big-endian encoding.
  private static long spanIdToLong(SpanId spanId) {
    ByteBuffer buffer = ByteBuffer.allocate(SpanId.SIZE);
    buffer.put(spanId.getBytes());
    return buffer.getLong(0);
  }
}
