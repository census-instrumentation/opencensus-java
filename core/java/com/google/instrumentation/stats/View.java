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

package com.google.instrumentation.stats;

import com.google.instrumentation.common.Timestamp;
import java.util.List;

/**
 * Container for the {@link Aggregation}s for a particular {@link ViewDescriptor}.
 */
public final class View {
  /**
   * Creates a new {@link View}.
   */
  public static View create(
      ViewDescriptor viewDescriptor, List<Aggregation> aggregations, Timestamp start, Timestamp end) {
    return new View(viewDescriptor, aggregations, start, end);
  }

  /**
   * The {@link ViewDescriptor} asscoiated with this {@link View}.
   */
  public ViewDescriptor getViewDescriptor() {
    return viewDescriptor;
  }

  /**
   * The data for this View. Aggregations will have a unique set of {@link TagValue}s associated
   * with the {@link View}'s {@link TagKey}s.
   */
  public List<Aggregation> getAggregations() {
    return aggregations;
  }

  /**
   * Start {@link Timestamp} over which the Aggregations were accumulated.
   *
   * <p>Note: This value is not relevant/defined for {@link IntervalAggregations}, which are
   * always accumulated over a fixed time period.
   */
  public Timestamp getStart() {
    return start;
  }

  /**
   * End {@link Timestamp} over which the Aggregations were accumulated.
   *
   * <p>Note: This value is not relevant/defined for {@link IntervalAggregations}, which are
   * always accumulated over a fixed time period.
   */
  public Timestamp getEnd() {
    return end;
  }

  private final ViewDescriptor viewDescriptor;
  private final List<Aggregation> aggregations;
  private final Timestamp start;
  private final Timestamp end;

  private View(ViewDescriptor viewDescriptor, List<Aggregation> aggregations, Timestamp start, Timestamp end) {
    this.viewDescriptor = viewDescriptor;
    this.aggregations = aggregations;
    this.start = start;
    this.end = end;
  }
}
