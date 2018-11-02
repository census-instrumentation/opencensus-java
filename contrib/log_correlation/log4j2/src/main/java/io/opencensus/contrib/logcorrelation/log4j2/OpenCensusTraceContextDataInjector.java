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

import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;

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
 *   <li><code>%X{traceId}</code>
 *   <li><code>%X{spanId}</code>
 *   <li><code>%X{traceSampled}</code>
 * </ul>
 *
 * @since 0.16
 * @see <a
 *     href="https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/ContextDataInjector.html">org.apache.logging.log4j.core.ContextDataInjector</a>
 */
public final class OpenCensusTraceContextDataInjector implements ContextDataInjector {

  /**
   * Context key for the current trace ID. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String TRACE_ID_CONTEXT_KEY = "traceId";

  /**
   * Context key for the current span ID. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String SPAN_ID_CONTEXT_KEY = "spanId";

  /**
   * Context key for the sampling decision of the current span. The name is {@value}.
   *
   * @since 0.16
   */
  public static final String TRACE_SAMPLED_CONTEXT_KEY = "traceSampled";

  /**
   * Constructor to be called by Log4j.
   *
   * @since 0.16
   */
  public OpenCensusTraceContextDataInjector() {}

  // Note that this method must return an object that can be passed to another thread.
  @Override
  public StringMap injectContextData(@Nullable List<Property> properties, StringMap reusable) {
    return ContextDataUtils.injectContextData(properties, reusable);
  }

  // Note that this method does not need to return an object that can be passed to another thread.
  @Override
  public ReadOnlyStringMap rawContextData() {
    return ContextDataUtils.getContextAndTracingData();
  }
}
