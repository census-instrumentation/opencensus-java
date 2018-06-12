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
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

/**
 * Stackdriver {@link LoggingEnhancer} that adds OpenCensus tracing data to log entries.
 *
 * <p>This feature is currently experimental.
 */

// TODO(sebright): Allow configuring the LoggingEnhancer with Java properties. One property should
// control the types of spans to correlate, with these three choices:
// - off
// - only add tracing data for sampled spans
// - add tracing data for all spans
@ExperimentalApi
public final class OpenCensusTraceLoggingEnhancer implements LoggingEnhancer {
  private static final String SAMPLED_KEY = "sampled";

  private static final Tracer tracer = Tracing.getTracer();

  /** Constructor to be called by Stackdriver logging. */
  public OpenCensusTraceLoggingEnhancer() {}

  @Override
  public void enhanceLogEntry(LogEntry.Builder builder) {
    SpanContext span = tracer.getCurrentSpan().getContext();
    builder.setTrace(formatTraceId(span.getTraceId()));
    builder.setSpanId(span.getSpanId().toLowerBase16());

    // TODO(sebright): Find the correct way to add the sampling decision.
    builder.addLabel(SAMPLED_KEY, Boolean.toString(span.getTraceOptions().isSampled()));
  }

  private static String formatTraceId(TraceId traceId) {
    // TODO(sebright): Cache the project ID.
    // TODO(sebright): Add a way to override the project ID in the logging configuration.
    String projectId = ServiceOptions.getDefaultProjectId();
    return "projects/" + projectId + "/traces/" + traceId.toLowerBase16();
  }
}
