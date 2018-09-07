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
import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.unsafe.ContextUtils;
import java.util.logging.LogManager;
import javax.annotation.Nullable;

/**
 * Stackdriver {@link LoggingEnhancer} that adds OpenCensus tracing data to log entries.
 *
 * <p>This feature is currently experimental.
 *
 * @since 0.15
 */
@ExperimentalApi
public final class OpenCensusTraceLoggingEnhancer implements LoggingEnhancer {
  private static final String SAMPLED_LABEL_KEY = "opencensusTraceSampled";
  private static final SpanSelection DEFAULT_SPAN_SELECTION = SpanSelection.ALL_SPANS;

  /**
   * Name of the property that overrides the default project ID (overrides the value returned by
   * {@code com.google.cloud.ServiceOptions.getDefaultProjectId()}). The name is {@value}.
   *
   * @since 0.15
   */
  public static final String PROJECT_ID_PROPERTY_NAME =
      "io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.projectId";

  /**
   * Name of the property that defines the {@link SpanSelection}. The name is {@value}.
   *
   * @since 0.15
   */
  public static final String SPAN_SELECTION_PROPERTY_NAME =
      "io.opencensus.contrib.logcorrelation.stackdriver."
          + "OpenCensusTraceLoggingEnhancer.spanSelection";

  private final String projectId;
  private final SpanSelection spanSelection;

  // This field caches the prefix used for the LogEntry.trace field and is derived from projectId.
  private final String tracePrefix;

  /**
   * How to decide whether to add tracing data from the current span to a log entry.
   *
   * @since 0.15
   */
  public enum SpanSelection {

    /**
     * Never add tracing data to log entries. This constant disables the log correlation feature.
     *
     * @since 0.15
     */
    NO_SPANS,

    /**
     * Add tracing data to a log entry iff the current span is sampled.
     *
     * @since 0.15
     */
    SAMPLED_SPANS,

    /**
     * Always add tracing data to log entries, even when the current span is not sampled. This is
     * the default.
     *
     * @since 0.15
     */
    ALL_SPANS
  }

  /**
   * Constructor to be called by reflection, e.g., by a google-cloud-java {@code LoggingHandler} or
   * google-cloud-logging-logback {@code LoggingAppender}.
   *
   * <p>This constructor looks up the project ID and {@link SpanSelection SpanSelection} from the
   * environment. It uses the default project ID (the value returned by {@code
   * com.google.cloud.ServiceOptions.getDefaultProjectId()}), unless the ID is overridden by the
   * property {@value #PROJECT_ID_PROPERTY_NAME}. It looks up the {@code SpanSelection} using the
   * property {@value #SPAN_SELECTION_PROPERTY_NAME}. Each property can be specified with a {@link
   * java.util.logging} property or a system property, with preference given to the logging
   * property.
   *
   * @since 0.15
   */
  public OpenCensusTraceLoggingEnhancer() {
    this(lookUpProjectId(), lookUpSpanSelectionProperty());
  }

  /**
   * Constructs a {@code OpenCensusTraceLoggingEnhancer} with the given project ID and {@code
   * SpanSelection}.
   *
   * @param projectId the project ID for this instance.
   * @param spanSelection the {@code SpanSelection} for this instance.
   * @since 0.15
   */
  public OpenCensusTraceLoggingEnhancer(@Nullable String projectId, SpanSelection spanSelection) {
    this.projectId = projectId == null ? "" : projectId;
    this.spanSelection = spanSelection;
    this.tracePrefix = "projects/" + this.projectId + "/traces/";
  }

  private static String lookUpProjectId() {
    String projectIdProperty = lookUpProperty(PROJECT_ID_PROPERTY_NAME);
    return projectIdProperty == null || projectIdProperty.isEmpty()
        ? ServiceOptions.getDefaultProjectId()
        : projectIdProperty;
  }

  private static SpanSelection lookUpSpanSelectionProperty() {
    String spanSelectionProperty = lookUpProperty(SPAN_SELECTION_PROPERTY_NAME);
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

  // An OpenCensusTraceLoggingEnhancer property can be set with a logging property or a system
  // property.
  @Nullable
  private static String lookUpProperty(String name) {
    String property = LogManager.getLogManager().getProperty(name);
    return property == null || property.isEmpty() ? System.getProperty(name) : property;
  }

  /**
   * Returns the project ID setting for this instance.
   *
   * @return the project ID setting for this instance.
   * @since 0.15
   */
  public String getProjectId() {
    return projectId;
  }

  /**
   * Returns the {@code SpanSelection} setting for this instance.
   *
   * @return the {@code SpanSelection} setting for this instance.
   * @since 0.15
   */
  public SpanSelection getSpanSelection() {
    return spanSelection;
  }

  // This method avoids getting the current span when the feature is disabled, for efficiency.
  @Override
  public void enhanceLogEntry(LogEntry.Builder builder) {
    switch (spanSelection) {
      case NO_SPANS:
        return;
      case SAMPLED_SPANS:
        SpanContext span = getCurrentSpanContext();
        if (span.getTraceOptions().isSampled()) {
          addTracingData(tracePrefix, span, builder);
        }
        return;
      case ALL_SPANS:
        addTracingData(tracePrefix, getCurrentSpanContext(), builder);
        return;
    }
    throw new AssertionError("Unknown spanSelection: " + spanSelection);
  }

  private static SpanContext getCurrentSpanContext() {
    Span span = ContextUtils.CONTEXT_SPAN_KEY.get();
    return span == null ? SpanContext.INVALID : span.getContext();
  }

  private static void addTracingData(
      String tracePrefix, SpanContext span, LogEntry.Builder builder) {
    builder.setTrace(formatTraceId(tracePrefix, span.getTraceId()));
    builder.setSpanId(span.getSpanId().toLowerBase16());

    // TODO(sebright): Find the correct way to add the sampling decision.
    builder.addLabel(SAMPLED_LABEL_KEY, Boolean.toString(span.getTraceOptions().isSampled()));
  }

  private static String formatTraceId(String tracePrefix, TraceId traceId) {
    return tracePrefix + traceId.toLowerBase16();
  }
}
