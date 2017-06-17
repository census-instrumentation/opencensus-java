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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.ViewDescriptor.DistributionViewDescriptor;
import io.opencensus.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * The aggregated data for a particular {@link ViewDescriptor}.
 */
@Immutable
public abstract class View {
  /**
   * The {@link ViewDescriptor} associated with this {@link View}.
   */
  public abstract ViewDescriptor getViewDescriptor();

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<DistributionView, T> p0,
      Function<IntervalView, T> p1);

  // Prevents this class from being subclassed anywhere else.
  private View() {
  }

  /**
   * A {@link View} for distribution-based aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionView extends View {
    /**
     * Constructs a new {@link DistributionView}.
     */
    public static DistributionView create(DistributionViewDescriptor distributionViewDescriptor,
        List<DistributionAggregation> distributionAggregations, Timestamp start, Timestamp end) {
      return new AutoValue_View_DistributionView(
          distributionViewDescriptor,
          Collections.unmodifiableList(
              new ArrayList<DistributionAggregation>(distributionAggregations)),
          start,
          end);
    }

    @Override
    public abstract DistributionViewDescriptor getViewDescriptor();

    /**
     * The {@link DistributionAggregation}s associated with this {@link DistributionView}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<DistributionAggregation> getDistributionAggregations();

    /**
     * Returns start timestamp for this aggregation.
     */
    public abstract Timestamp getStart();

    /**
     * Returns end timestamp for this aggregation.
     */
    public abstract Timestamp getEnd();

    @Override
    public final <T> T match(
        Function<DistributionView, T> p0,
        Function<IntervalView, T> p1) {
      return p0.apply(this);
    }
  }

  /**
   * A {@link View} for interval-base aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class IntervalView extends View {
    /**
     * Constructs a new {@link IntervalView}.
     */
    public static IntervalView create(IntervalViewDescriptor intervalViewDescriptor,
        List<IntervalAggregation> intervalAggregations) {
      return new AutoValue_View_IntervalView(
          intervalViewDescriptor,
          Collections.unmodifiableList(new ArrayList<IntervalAggregation>(intervalAggregations)));
    }

    @Override
    public abstract IntervalViewDescriptor getViewDescriptor();

    /**
     * The {@link IntervalAggregation}s associated with this {@link IntervalView}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<IntervalAggregation> getIntervalAggregations();

    @Override
    public final <T> T match(
        Function<DistributionView, T> p0,
        Function<IntervalView, T> p1) {
      return p1.apply(this);
    }
  }
}
