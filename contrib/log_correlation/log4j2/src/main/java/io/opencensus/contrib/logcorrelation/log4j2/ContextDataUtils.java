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

import io.opencensus.contrib.logcorrelation.log4j2.OpenCensusTraceContextDataInjector.SpanSelection;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.unsafe.ContextUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.util.TriConsumer;

// Implementation of the methods inherited from ContextDataInjector.
//
// This class uses "shareable" to mean that a method's return value can be passed to another
// thread.
final class ContextDataUtils {
  private ContextDataUtils() {}

  // The implementation of this method is based on the example in the Javadocs for
  // ContextDataInjector.injectContextData.
  static StringMap injectContextData(
      SpanSelection spanSelection, @Nullable List<Property> properties, StringMap reusable) {
    if (properties == null || properties.isEmpty()) {
      return shareableRawContextData(spanSelection);
    }
    // Context data has precedence over configuration properties.
    putProperties(properties, reusable);
    // TODO(sebright): The following line can be optimized. See
    //     https://github.com/census-instrumentation/opencensus-java/pull/1422/files#r216425494.
    reusable.putAll(nonShareableRawContextData(spanSelection));
    return reusable;
  }

  private static void putProperties(Collection<Property> properties, StringMap stringMap) {
    for (Property property : properties) {
      stringMap.putValue(property.getName(), property.getValue());
    }
  }

  private static StringMap shareableRawContextData(SpanSelection spanSelection) {
    SpanContext spanContext = shouldAddTracingDataToLogEvent(spanSelection);
    return spanContext == null
        ? getShareableContextData()
        : getShareableContextAndTracingData(spanContext);
  }

  static ReadOnlyStringMap nonShareableRawContextData(SpanSelection spanSelection) {
    SpanContext spanContext = shouldAddTracingDataToLogEvent(spanSelection);
    return spanContext == null
        ? getNonShareableContextData()
        : getShareableContextAndTracingData(spanContext);
  }

  // This method returns the current span context iff tracing data should be added to the LogEvent.
  // It avoids getting the current span when the feature is disabled, for efficiency.
  @Nullable
  private static SpanContext shouldAddTracingDataToLogEvent(SpanSelection spanSelection) {
    switch (spanSelection) {
      case NO_SPANS:
        return null;
      case SAMPLED_SPANS:
        SpanContext spanContext = getCurrentSpanContext();
        if (spanContext.getTraceOptions().isSampled()) {
          return spanContext;
        } else {
          return null;
        }
      case ALL_SPANS:
        return getCurrentSpanContext();
    }
    throw new AssertionError("Unknown spanSelection: " + spanSelection);
  }

  private static StringMap getShareableContextData() {
    ReadOnlyThreadContextMap context = ThreadContext.getThreadContextMap();

    // Return a new object, since StringMap is modifiable.
    return context == null
        ? new SortedArrayStringMap(ThreadContext.getImmutableContext())
        : new SortedArrayStringMap(context.getReadOnlyContextData());
  }

  private static ReadOnlyStringMap getNonShareableContextData() {
    ReadOnlyThreadContextMap context = ThreadContext.getThreadContextMap();
    if (context != null) {
      return context.getReadOnlyContextData();
    } else {
      Map<String, String> contextMap = ThreadContext.getImmutableContext();
      return contextMap.isEmpty()
          ? UnmodifiableReadOnlyStringMap.EMPTY
          : new UnmodifiableReadOnlyStringMap(contextMap);
    }
  }

  private static StringMap getShareableContextAndTracingData(SpanContext spanContext) {
    ReadOnlyThreadContextMap context = ThreadContext.getThreadContextMap();
    SortedArrayStringMap stringMap;
    if (context == null) {
      stringMap = new SortedArrayStringMap(ThreadContext.getImmutableContext());
    } else {
      StringMap contextData = context.getReadOnlyContextData();
      stringMap = new SortedArrayStringMap(contextData.size() + 3);
      stringMap.putAll(contextData);
    }
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

  @Immutable
  private static final class UnmodifiableReadOnlyStringMap implements ReadOnlyStringMap {
    private static final long serialVersionUID = 0L;

    static final ReadOnlyStringMap EMPTY =
        new UnmodifiableReadOnlyStringMap(Collections.<String, String>emptyMap());

    private final Map<String, String> map;

    UnmodifiableReadOnlyStringMap(Map<String, String> map) {
      this.map = map;
    }

    @Override
    public boolean containsKey(String key) {
      return map.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> void forEach(BiConsumer<String, ? super V> action) {
      for (Entry<String, String> entry : map.entrySet()) {
        action.accept(entry.getKey(), (V) entry.getValue());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {
      for (Entry<String, String> entry : map.entrySet()) {
        action.accept(entry.getKey(), (V) entry.getValue(), state);
      }
    }

    @Override
    @Nullable
    @SuppressWarnings({
      "unchecked",
      "TypeParameterUnusedInFormals" // This is an overridden method.
    })
    public <V> V getValue(String key) {
      return (V) map.get(key);
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public Map<String, String> toMap() {
      return map;
    }
  }
}
