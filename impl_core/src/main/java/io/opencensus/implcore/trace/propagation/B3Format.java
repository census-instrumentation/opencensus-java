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
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>b3-propagation</a>.
 */
final class B3Format extends TextFormat {
  @VisibleForTesting static final String X_B3_TRACE_ID = "X─B3─TraceId";
  @VisibleForTesting static final String X_B3_SPAN_ID = "X─B3─SpanId";
  @VisibleForTesting static final String X_B3_PARENT_SPAN_ID = "X─B3─ParentSpanId";
  @VisibleForTesting static final String X_B3_SAMPLED = "X─B3─Sampled";
  @VisibleForTesting static final String X_B3_FLAGS = "X-B3-Flags";
  private static final List<String> FIELDS =
      Collections.unmodifiableList(
          Arrays.asList(
              X_B3_TRACE_ID, X_B3_SPAN_ID, X_B3_PARENT_SPAN_ID, X_B3_SAMPLED, X_B3_FLAGS));

  // Used as the upper TraceId.SIZE hex characters of the traceID. B3-propagation used to send
  // TraceId.SIZE hex characters (8-bytes traceId) in the past.
  private static final String UPPER_TRACE_ID = "0000000000000000";
  // Sampled value via the X_B3_SAMPLED header.
  private static final String SAMPLED_VALUE = "1";
  // "Debug" sampled value.
  private static final String FLAGS_VALUE = "1";

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    checkNotNull(spanContext, "spanContext");
    checkNotNull(setter, "setter");
    checkNotNull(carrier, "carrier");
    setter.put(carrier, X_B3_TRACE_ID, spanContext.getTraceId().toLowerBase16());
    setter.put(carrier, X_B3_SPAN_ID, spanContext.getSpanId().toLowerBase16());
    if (spanContext.getTraceOptions().isSampled()) {
      setter.put(carrier, X_B3_SAMPLED, SAMPLED_VALUE);
    }
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) throws SpanContextParseException {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");
    try {
      TraceId traceId;
      String traceIdStr = getter.get(carrier, X_B3_TRACE_ID);
      if (traceIdStr != null) {
        if (traceIdStr.length() == TraceId.SIZE) {
          // This is an 8-byte traceID.
          traceIdStr = UPPER_TRACE_ID + traceIdStr;
        }
        traceId = TraceId.fromLowerBase16(traceIdStr);
      } else {
        throw new SpanContextParseException("Missing X_B3_TRACE_ID.");
      }
      SpanId spanId;
      String spanIdStr = getter.get(carrier, X_B3_SPAN_ID);
      if (spanIdStr != null) {
        spanId = SpanId.fromLowerBase16(spanIdStr);
      } else {
        throw new SpanContextParseException("Missing X_B3_SPAN_ID.");
      }
      TraceOptions traceOptions = TraceOptions.DEFAULT;
      if (SAMPLED_VALUE.equals(getter.get(carrier, X_B3_SAMPLED))
          || FLAGS_VALUE.equals(getter.get(carrier, X_B3_FLAGS))) {
        traceOptions = TraceOptions.builder().setIsSampled(true).build();
      }
      return SpanContext.create(traceId, spanId, traceOptions);
    } catch (IllegalArgumentException e) {
      throw new SpanContextParseException("Invalid input.", e);
    }
  }
}
