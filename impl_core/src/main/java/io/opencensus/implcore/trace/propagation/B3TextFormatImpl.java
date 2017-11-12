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

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class B3TextFormatImpl extends TextFormat {

  /**
   * 128 or 64-bit trace ID lower-hex encoded into 32 or 16 characters (required)
   */
  static final String TRACE_ID_NAME = "X-B3-TraceId";
  /**
   * 64-bit span ID lower-hex encoded into 16 characters (required)
   */
  static final String SPAN_ID_NAME = "X-B3-SpanId";
  /**
   * 64-bit parent span ID lower-hex encoded into 16 characters (absent on root span)
   */
  static final String PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId";
  /**
   * "1" means report this span to the tracing system, "0" means do not. (absent means defer the
   * decision to the receiver of this header).
   */
  static final String SAMPLED_NAME = "X-B3-Sampled";
  /**
   * "1" implies sampled and is a request to override collection-tier sampling policy.
   */
  static final String FLAGS_NAME = "X-B3-Flags";

  static final List<String> FIELDS = Collections.unmodifiableList(
      Arrays.asList(TRACE_ID_NAME, SPAN_ID_NAME, PARENT_SPAN_ID_NAME, SAMPLED_NAME, FLAGS_NAME)
  );

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  /**
   * Replaces keys in the carrier, notably excluding {@link #PARENT_SPAN_ID_NAME} and {@link
   * #FLAGS_NAME} which aren't available.
   */
  @Override
  public <C> void putContext(SpanContext spanContext, C carrier, Setter<C> setter) {
    setter.put(carrier, TRACE_ID_NAME, spanContext.getTraceId().toString());
    setter.put(carrier, SPAN_ID_NAME, spanContext.getSpanId().toString());
    if (spanContext.getTraceOptions().isSampled()) {
      setter.put(carrier, SAMPLED_NAME, "1");
    } else {
      setter.put(carrier, SAMPLED_NAME, "0");
    }
  }
}
