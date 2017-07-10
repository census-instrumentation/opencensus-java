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
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * The aggregated data for a particular {@link View}.
 */
@Immutable
public abstract class ViewData {
  /**
   * The {@link View} associated with this {@link ViewData}.
   */
  public abstract View getView();

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super DistributionViewData, T> p0,
      Function<? super IntervalViewData, T> p1);

  // Prevents this class from being subclassed anywhere else.
  private ViewData() {
  }

  /**
   * A {@link ViewData} for distribution-based aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionViewData extends ViewData {
    /**
     * Constructs a new {@link DistributionViewData}.
     */
    public static DistributionViewData create(DistributionView distributionView,
        List<DistributionAggregate> distributionAggregates, Timestamp start, Timestamp end) {
      return new AutoValue_ViewData_DistributionViewData(
          distributionView,
          Collections.unmodifiableList(
              new ArrayList<DistributionAggregate>(distributionAggregates)),
          start,
          end);
    }

    @Override
    public abstract DistributionView getView();

    /**
     * The {@link DistributionAggregate}s associated with this {@link DistributionViewData}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<DistributionAggregate> getDistributionAggregates();

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
        Function<? super DistributionViewData, T> p0,
        Function<? super IntervalViewData, T> p1) {
      return p0.apply(this);
    }
  }

  /**
   * A {@link ViewData} for interval-base aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class IntervalViewData extends ViewData {
    /**
     * Constructs a new {@link IntervalViewData}.
     */
    public static IntervalViewData create(IntervalView intervalView,
        List<IntervalAggregate> intervalAggregates) {
      return new AutoValue_ViewData_IntervalViewData(
          intervalView,
          Collections.unmodifiableList(new ArrayList<IntervalAggregate>(intervalAggregates)));
    }

    @Override
    public abstract IntervalView getView();

    /**
     * The {@link IntervalAggregate}s associated with this {@link IntervalViewData}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<IntervalAggregate> getIntervalAggregates();

    @Override
    public final <T> T match(
        Function<? super DistributionViewData, T> p0,
        Function<? super IntervalViewData, T> p1) {
      return p1.apply(this);
    }
  }
}
