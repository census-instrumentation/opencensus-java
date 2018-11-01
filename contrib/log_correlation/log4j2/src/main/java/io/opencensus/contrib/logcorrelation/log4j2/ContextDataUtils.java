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

package io.opencensus.contrib.logcorrelation.log4j2;

import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.unsafe.ContextUtils;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

// Implementation of the methods inherited from ContextDataInjector.
final class ContextDataUtils {
  private ContextDataUtils() {}

  // The implementation of this method is based on the example in the Javadocs for
  // ContextDataInjector.injectContextData.
  static StringMap injectContextData(@Nullable List<Property> properties, StringMap reusable) {
    if (properties == null || properties.isEmpty()) {
      return getContextAndTracingData();
    }
    // Context data has precedence over configuration properties.
    putProperties(properties, reusable);
    // TODO(sebright): The following line can be optimized. See
    //     https://github.com/census-instrumentation/opencensus-java/pull/1422/files#r216425494.
    reusable.putAll(getContextAndTracingData());
    return reusable;
  }

  private static void putProperties(Collection<Property> properties, StringMap stringMap) {
    for (Property property : properties) {
      stringMap.putValue(property.getName(), property.getValue());
    }
  }

  static StringMap getContextAndTracingData() {
    SpanContext spanContext = getCurrentSpanContext();
    ReadOnlyThreadContextMap context = ThreadContext.getThreadContextMap();
    SortedArrayStringMap stringMap;
    if (context == null) {
      stringMap = new SortedArrayStringMap(ThreadContext.getImmutableContext());
    } else {
      StringMap contextData = context.getReadOnlyContextData();
      stringMap = new SortedArrayStringMap(contextData.size() + 3);
      stringMap.putAll(contextData);
    }
    // TODO(sebright): Move the calls to TraceId.toLowerBase16() and SpanId.toLowerBase16() out of
    // the critical path by wrapping the trace and span IDs in objects that call toLowerBase16() in
    // their toString() methods, after there is a fix for
    // https://github.com/census-instrumentation/opencensus-java/issues/1436.
    stringMap.putValue(
        OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY,
        spanContext.getTraceId().toLowerBase16());
    stringMap.putValue(
        OpenCensusTraceContextDataInjector.SPAN_ID_CONTEXT_KEY,
        spanContext.getSpanId().toLowerBase16());
    stringMap.putValue(
        OpenCensusTraceContextDataInjector.TRACE_SAMPLED_CONTEXT_KEY,
        spanContext.getTraceOptions().isSampled() ? "true" : "false");
    return stringMap;
  }

  private static SpanContext getCurrentSpanContext() {
    Span span = ContextUtils.CONTEXT_SPAN_KEY.get();
    return span == null ? SpanContext.INVALID : span.getContext();
  }
}
