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

package io.opencensus.contrib.logcorrelation.stackdriver;

import com.google.cloud.ServiceOptions;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.LoggingEnhancer;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.unsafe.ContextUtils;
import java.util.logging.LogManager;
import javax.annotation.Nullable;

/**
 * Stackdriver {@link LoggingEnhancer} that adds OpenCensus tracing data to log entries.
 *
 * @since 0.17
 */
public final class OpenCensusTraceLoggingEnhancer implements LoggingEnhancer {

  /**
   * Name of the property that overrides the default project ID (overrides the value returned by
   * {@code com.google.cloud.ServiceOptions.getDefaultProjectId()}). The name is {@value}.
   *
   * @since 0.17
   */
  public static final String PROJECT_ID_PROPERTY_NAME =
      "io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.projectId";

  @Nullable private final String projectId;

  // This field caches the prefix used for the LogEntry.trace field and is derived from projectId.
  private final String tracePrefix;

  /**
   * Constructor to be called by reflection, e.g., by a google-cloud-java {@code LoggingHandler} or
   * google-cloud-logging-logback {@code LoggingAppender}.
   *
   * <p>This constructor looks up the project ID from the environment. It uses the default project
   * ID (the value returned by {@code com.google.cloud.ServiceOptions.getDefaultProjectId()}),
   * unless the ID is overridden by the property {@value #PROJECT_ID_PROPERTY_NAME}. The property
   * can be specified with a {@link java.util.logging} property or a system property, with
   * preference given to the logging property.
   *
   * @since 0.17
   */
  public OpenCensusTraceLoggingEnhancer() {
    this(lookUpProjectId());
  }

  // visible for testing
  OpenCensusTraceLoggingEnhancer(@Nullable String projectId) {
    this.projectId = projectId;
    this.tracePrefix = "projects/" + (projectId == null ? "" : projectId) + "/traces/";
  }

  @Nullable
  private static String lookUpProjectId() {
    String projectIdProperty = lookUpProperty(PROJECT_ID_PROPERTY_NAME);
    return projectIdProperty == null || projectIdProperty.isEmpty()
        ? ServiceOptions.getDefaultProjectId()
        : projectIdProperty;
  }

  // An OpenCensusTraceLoggingEnhancer property can be set with a logging property or a system
  // property.
  @Nullable
  private static String lookUpProperty(String name) {
    String property = LogManager.getLogManager().getProperty(name);
    return property == null || property.isEmpty() ? System.getProperty(name) : property;
  }

  // visible for testing
  @Nullable
  String getProjectId() {
    return projectId;
  }

  // This method avoids getting the current span when the feature is disabled, for efficiency.
  @Override
  public void enhanceLogEntry(LogEntry.Builder builder) {
    addTracingData(tracePrefix, getCurrentSpanContext(), builder);
  }

  private static SpanContext getCurrentSpanContext() {
    Span span = ContextUtils.CONTEXT_SPAN_KEY.get();
    return span == null ? SpanContext.INVALID : span.getContext();
  }

  private static void addTracingData(
      String tracePrefix, SpanContext span, LogEntry.Builder builder) {
    builder.setTrace(formatTraceId(tracePrefix, span.getTraceId()));
    builder.setSpanId(span.getSpanId().toLowerBase16());

    // TODO(sebright): Add the sampling decision once google-cloud-logging supports it.
  }

  private static String formatTraceId(String tracePrefix, TraceId traceId) {
    return tracePrefix + traceId.toLowerBase16();
  }
}
