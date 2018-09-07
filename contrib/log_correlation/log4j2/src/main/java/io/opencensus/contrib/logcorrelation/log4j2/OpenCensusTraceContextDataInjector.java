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

import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.unsafe.ContextUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * A Log4j {@link ContextDataInjector} that adds OpenCensus tracing data to log events.
 *
 * <p>This class adds the following key-value pairs:
 *
 * <ul>
 *   <li>{@value #TRACE_ID_CONTEXT_KEY} - the lowercase base16 encoding of the current trace ID
 *   <li>{@value #SPAN_ID_CONTEXT_KEY} - the lowercase base16 encoding of the current span ID
 *   <li>{@value #TRACE_SAMPLED_CONTEXT_KEY} - the sampling decision of the current span ({@code
 *       "true"} or {@code "false"})
 * </ul>
 *
 * <p>The tracing data can be accessed with {@link LogEvent#getContextData} or included in a {@link
 * Layout}. For example, the following patterns could be used to include the tracing data with a <a
 * href="https://logging.apache.org/log4j/2.x/manual/layouts.html#Pattern_Layout">Pattern
 * Layout</a>:
 *
 * <ul>
 *   <li><code>%X{opencensusTraceId}</code>
 *   <li><code>%X{opencensusSpanId}</code>
 *   <li><code>%X{opencensusTraceSampled}</code>
 * </ul>
 *
 * <p>This feature is currently experimental.
 *
 * @since 0.16
 * @see <a
 *     href="https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/ContextDataInjector.html">org.apache.logging.log4j.core.ContextDataInjector</a>
 */
@ExperimentalApi
public final class OpenCensusTraceContextDataInjector implements ContextDataInjector {
  private static final SpanSelection DEFAULT_SPAN_SELECTION = SpanSelection.ALL_SPANS;

  /**
   * Context key for the current trace ID. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String TRACE_ID_CONTEXT_KEY = "opencensusTraceId";

  /**
   * Context key for the current span ID. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String SPAN_ID_CONTEXT_KEY = "opencensusSpanId";

  /**
   * Context key for the sampling decision of the current span. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String TRACE_SAMPLED_CONTEXT_KEY = "opencensusTraceSampled";

  /**
   * Name of the property that defines the {@link SpanSelection}. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String SPAN_SELECTION_PROPERTY_NAME =
      "io.opencensus.contrib.logcorrelation.log4j2."
          + "OpenCensusTraceContextDataInjector.spanSelection";

  private final SpanSelection spanSelection;

  /**
   * How to decide whether to add tracing data from the current span to a log entry.
   *
   * @since 0.16
   */
  public enum SpanSelection {

    /**
     * Never add tracing data to log entries. This constant disables the log correlation feature.
     *
     * @since 0.16
     */
    NO_SPANS,

    /**
     * Add tracing data to a log entry iff the current span is sampled.
     *
     * @since 0.16
     */
    SAMPLED_SPANS,

    /**
     * Always add tracing data to log entries, even when the current span is not sampled. This is
     * the default.
     *
     * @since 0.16
     */
    ALL_SPANS
  }

  /**
   * Returns the {@code SpanSelection} setting for this instance.
   *
   * @return the {@code SpanSelection} setting for this instance.
   * @since 0.16
   */
  public SpanSelection getSpanSelection() {
    return spanSelection;
  }

  /**
   * Constructor to be called by Log4j.
   *
   * <p>This constructor looks up the {@link SpanSelection} using the system property {@link
   * #SPAN_SELECTION_PROPERTY_NAME}.
   *
   * @since 0.16
   */
  public OpenCensusTraceContextDataInjector() {
    this(lookUpSpanSelectionProperty());
  }

  private OpenCensusTraceContextDataInjector(SpanSelection spanSelection) {
    this.spanSelection = spanSelection;
  }

  private static SpanSelection lookUpSpanSelectionProperty() {
    String spanSelectionProperty = System.getProperty(SPAN_SELECTION_PROPERTY_NAME);
    return spanSelectionProperty == null || spanSelectionProperty.isEmpty()
        ? DEFAULT_SPAN_SELECTION
        : parseSpanSelection(spanSelectionProperty);
  }

  private static SpanSelection parseSpanSelection(String spanSelection) {
    try {
      return SpanSelection.valueOf(spanSelection);
    } catch (IllegalArgumentException e) {
      return DEFAULT_SPAN_SELECTION;
    }
  }

  // The implementation of this method is based on the example in the Javadocs for
  // ContextDataInjector.injectContextData.
  //
  // Note that this method must return an object that can be passed to another thread.
  @Override
  public StringMap injectContextData(@Nullable List<Property> properties, StringMap reusable) {
    if (properties == null || properties.isEmpty()) {
      return shareableRawContextData();
    }
    // Context data has precedence over configuration properties.
    putProperties(properties, reusable);
    reusable.putAll(rawContextData());
    return reusable;
  }

  private static void putProperties(Collection<Property> properties, StringMap stringMap) {
    for (Property property : properties) {
      stringMap.putValue(property.getName(), property.getValue());
    }
  }

  private StringMap shareableRawContextData() {
    SpanContext spanContext = shouldAddTracingDataToLogEvent();
    return spanContext == null
        ? getShareableContextData()
        : getShareableContextAndTracingData(spanContext);
  }

  // Note that this method does not need to return an object that can be passed to another thread.
  @Override
  public ReadOnlyStringMap rawContextData() {
    SpanContext spanContext = shouldAddTracingDataToLogEvent();
    return spanContext == null
        ? getNonShareableContextData()
        : getShareableContextAndTracingData(spanContext);
  }

  // This method returns the current span context iff tracing data should be added to the LogEvent.
  // It avoids getting the current span when the feature is disabled, for efficiency.
  @Nullable
  private SpanContext shouldAddTracingDataToLogEvent() {
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
    ReadOnlyThreadContextMap contextMap = ThreadContext.getThreadContextMap();

    // Return a new object, since StringMap is modifiable.
    return contextMap == null
        ? new SortedArrayStringMap()
        : new SortedArrayStringMap(contextMap.getReadOnlyContextData());
  }

  private static ReadOnlyStringMap getNonShareableContextData() {
    ReadOnlyThreadContextMap contextMap = ThreadContext.getThreadContextMap();
    return contextMap == null
        ? EmptyReadOnlyStringMap.INSTANCE
        : contextMap.getReadOnlyContextData();
  }

  private static StringMap getShareableContextAndTracingData(SpanContext spanContext) {
    ReadOnlyThreadContextMap contextMap = ThreadContext.getThreadContextMap();
    Map<String, String> map =
        contextMap == null ? new HashMap<String, String>() : contextMap.getCopy();
    map.put(TRACE_ID_CONTEXT_KEY, spanContext.getTraceId().toLowerBase16());
    map.put(SPAN_ID_CONTEXT_KEY, spanContext.getSpanId().toLowerBase16());
    map.put(
        TRACE_SAMPLED_CONTEXT_KEY, spanContext.getTraceOptions().isSampled() ? "true" : "false");
    return new SortedArrayStringMap(map);
  }

  private static SpanContext getCurrentSpanContext() {
    Span span = ContextUtils.CONTEXT_SPAN_KEY.get();
    return span == null ? SpanContext.INVALID : span.getContext();
  }

  @Immutable
  private static final class EmptyReadOnlyStringMap implements ReadOnlyStringMap {
    private static final long serialVersionUID = 0L;

    static final ReadOnlyStringMap INSTANCE = new EmptyReadOnlyStringMap();

    private EmptyReadOnlyStringMap() {}

    @Override
    public boolean containsKey(String key) {
      return false;
    }

    @Override
    public <V> void forEach(BiConsumer<String, ? super V> action) {}

    @Override
    public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {}

    @Override
    @Nullable
    @SuppressWarnings("TypeParameterUnusedInFormals") // This is an overridden method.
    public <V> V getValue(String key) {
      return null;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Map<String, String> toMap() {
      return Collections.<String, String>emptyMap();
    }
  }
}
