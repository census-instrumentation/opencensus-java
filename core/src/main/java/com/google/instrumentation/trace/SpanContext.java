/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Formatter;
import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate
 * to child {@link Span}s and across process boundaries. It contains the identifiers (a {@link
 * TraceId trace_id} and span_id) associated with the {@link Span} and a set of {@link TraceOptions
 * options}.
 */
@Immutable
public final class SpanContext {
  private static final long INVALID_SPAN_ID = 0;

  private static final SpanContext INVALID_CONTEXT =
      new SpanContext(TraceId.getInvalid(), INVALID_SPAN_ID, TraceOptions.getDefault());

  private final TraceId traceId;
  private final long spanId;
  private final TraceOptions traceOptions;

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceId the trace identifier of the span context.
   * @param spanId the span identifier of the span context.
   * @param traceOptions the trace options for the span context.
   */
  SpanContext(TraceId traceId, long spanId, TraceOptions traceOptions) {
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceOptions = traceOptions;
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   */
  public TraceId getTraceId() {
    return traceId;
  }

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   */
  public long getSpanId() {
    return spanId;
  }

  /**
   * Returns the trace options associated with this {@code SpanContext}.
   *
   * @return the trace options associated with this {@code SpanContext}.
   */
  public TraceOptions getTraceOptions() {
    return traceOptions;
  }

  /**
   * Returns true if this {@code SpanContext} is valid.
   *
   * @return true if this {@code SpanContext} is valid.
   */
  public boolean isValid() {
    return traceId.isValid() && spanId != INVALID_SPAN_ID;
  }

  /**
   * Returns an invalid {@code SpanContext}.
   *
   * @return an invalid {@code SpanContext}.
   */
  public static SpanContext getInvalid() {
    return INVALID_CONTEXT;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof SpanContext)) {
      return false;
    }

    SpanContext that = (SpanContext) obj;
    return traceId.equals(that.traceId)
        && spanId == that.spanId
        && traceOptions.equals(that.traceOptions);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(traceId, spanId, traceOptions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("traceId", traceId)
        .add("spanId", new Formatter(Locale.US).format("%016x", spanId).toString())
        .add("traceOptions", traceOptions)
        .toString();
  }
}
