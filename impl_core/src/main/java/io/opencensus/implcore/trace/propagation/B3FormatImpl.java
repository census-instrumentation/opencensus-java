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
import io.opencensus.trace.propagation.B3Format;
import java.text.ParseException;
import java.util.EnumMap;
import java.util.Map;

/** Implementation of the {@link B3Format}. */
@VisibleForTesting
public final class B3FormatImpl extends B3Format {
  // Used as the upper 8-bytes of the traceId when a 8-bytes traceId is received.
  private static final String UPPER_TRACE_ID = "0000000000000000";
  private static final String SAMPLED_VALUE = "1";
  private static final String FLAGS_VALUE = "1";

  @Override
  public Map<HeaderName, String> toHeaders(SpanContext spanContext) {
    checkNotNull(spanContext, "spanContext");
    Map<HeaderName, String> headers = new EnumMap<HeaderName, String>(HeaderName.class);
    headers.put(HeaderName.X_B3_TRACE_ID, spanContext.getTraceId().toLowerBase16());
    headers.put(HeaderName.X_B3_SPAN_ID, spanContext.getSpanId().toLowerBase16());
    if (spanContext.getTraceOptions().isSampled()) {
      headers.put(HeaderName.X_B3_SAMPLED, SAMPLED_VALUE);
    }
    return headers;
  }

  @Override
  public SpanContext fromHeaders(Map<HeaderName, String> headers) throws ParseException {
    checkNotNull(headers, "headers");
    try {
      TraceId traceId = TraceId.INVALID;
      String traceIdStr = headers.get(HeaderName.X_B3_TRACE_ID);
      if (traceIdStr != null) {
        if (traceIdStr.length() == TraceId.SIZE) {
          // This is an 8-byte traceID.
          traceId = TraceId.fromLowerBase16(UPPER_TRACE_ID + traceIdStr);
        } else {
          traceId = TraceId.fromLowerBase16(traceIdStr);
        }
      }
      SpanId spanId = SpanId.INVALID;
      String spanIdStr = headers.get(HeaderName.X_B3_SPAN_ID);
      if (spanIdStr != null) {
        spanId = SpanId.fromLowerBase16(spanIdStr);
      }
      TraceOptions traceOptions = TraceOptions.DEFAULT;
      if (SAMPLED_VALUE.equals(headers.get(HeaderName.X_B3_SAMPLED))
          || FLAGS_VALUE.equals(headers.get(HeaderName.X_B3_FLAGS))) {
        traceOptions = TraceOptions.builder().setIsSampled().build();
      }
      return SpanContext.create(traceId, spanId, traceOptions);
    } catch (IllegalArgumentException e) {
      throw new ParseException("Invalid input: " + e.toString(), 0);
    }
  }
}
