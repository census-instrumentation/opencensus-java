/*
 * Copyright 2017, Google Inc.
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

package io.opencensus.trace.export;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import io.opencensus.trace.Span;
import java.util.Collection;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class allows users to access in-process information about all active spans.
 *
 * <p>The active spans tracking is available for all the spans with the option {@link
 * Span.Options#RECORD_EVENTS}. This functionality allows users to debug stuck operations or long
 * living operations.
 */
@ThreadSafe
public abstract class ActiveSpans {

  protected ActiveSpans() {}

  /**
   * Returns the number of active spans for every different span name.
   *
   * @return the number of active spans for every different span name.
   */
  public abstract Map<String, Integer> getNumberOfActiveSpans();

  /**
   * Returns a list of active spans that match the {@code Filter}.
   *
   * @param filter used to filter the returned spans.
   * @return a list of active spans that match the {@code Filter}.
   */
  public abstract Collection<SpanData> getActiveSpans(Filter filter);

  /**
   * Filter for active spans. Used to filter results returned by the {@link #getActiveSpans(Filter)}
   * request.
   */
  @AutoValue
  @Immutable
  public abstract static class Filter {

    Filter() {}

    /**
     * Returns a new instance of {@code Filter}.
     *
     * <p>Filters all the spans based on {@code spanName} and returns a maximum of {@code
     * maxSpansToReturn}.
     *
     * @param spanName the name of the span.
     * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
     * @return a new instance of {@code Filter}.
     * @throws NullPointerException if {@code spanName} is {@code null}.
     * @throws IllegalArgumentException if {@code maxSpansToReturn} is negative.
     */
    public static Filter create(String spanName, int maxSpansToReturn) {
      checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
      return new AutoValue_ActiveSpans_Filter(spanName, maxSpansToReturn);
    }

    /**
     * Returns the span name.
     *
     * @return the span name.
     */
    public abstract String getSpanName();

    /**
     * Returns the maximum number of spans to be returned. {@code 0} means all.
     *
     * @return the maximum number of spans to be returned.
     */
    public abstract int getMaxSpansToReturn();
  }
}
