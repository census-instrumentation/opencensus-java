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

import com.google.common.primitives.UnsignedLongs;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
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
 * TRACE_ID/SPAN_ID[;o=SAMPLED]
 * </pre>
 *
 * <ul>
 *   <li>TRACE_ID is a 32-character hex value;
 *   <li>SPAN_ID is a decimal representation of a unsigned long;
 *   <li>SAMPLED must be 1 for sampled requests, 0 otherwise.
 * </ul>
 *
 * <p>Valid values:
 * <ul>
 *   <li>"105445aa7843bc8bf206b120001000/123;o=1"
 *   <li>"105445aa7843bc8bf206b120001000/123"
 *   <li>"105445aa7843bc8bf206b120001000/123;o=0"
 * </ul>
 */
final class CloudTraceFormat extends TextFormat {
  private static final String HEADER_NAME = "X-Cloud-Trace-Context";
  private static final List<String> FIELDS = Collections.singletonList(HEADER_NAME);
  private static final char SPAN_ID_DELIMITER = '/';
  private static final String TRACE_OPTION_DELIMITER = ";o=";
  private static final String SAMPLED = "1";
  private static final String NOT_SAMPLED = "0";

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
    // TODO: implement this.
    return SpanContext.INVALID;
  }

  // Using big-endian encoding.
  private SpanId longToSpanId(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(SpanId.SIZE);
    buffer.putLong(x);
    return SpanId.fromBytes(buffer.array());
  }

  // Using big-endian encoding.
  private long spanIdToLong(SpanId spanId) {
    ByteBuffer buffer = ByteBuffer.allocate(SpanId.SIZE);
    buffer.put(spanId.getBytes());
    return buffer.getLong(0);
  }
}
