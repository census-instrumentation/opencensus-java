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

import com.google.instrumentation.common.Function;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

import java.util.List;

/**
 * The aggregated data for a particular {@link ViewDescriptor}.
 */
public abstract class View {
  /**
   * The {@link ViewDescriptor} associated with this {@link View}.
   */
  public abstract ViewDescriptor getViewDescriptor();

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Visitor<T> visitor);

  // Prevents this class from being subclassed anywhere else.
  private View() {
  }

  /**
   * Visitor for the View type.
   */
  public interface Visitor<T> {
    T apply(DistributionView view);

    T apply(IntervalView view);
  }

  /**
   * Creates a Visitor with two {@link Function} objects. This method may be more convenient than
   * extending Visitor when lambdas are available.
   *
   * <p>For example, the following visitor counts the aggregations in a View:
   *
   * <pre>{@code
   *   int numAggregations = myView.match(View.createVisitor(
   *       d -> d.getDistributionAggregations().size(),
   *       i -> i.getIntervalAggregations().size()));
   *  }</pre>
   */
  public static <T> Visitor<T> createVisitor(
      final Function<DistributionView, T> f1, final Function<IntervalView, T> f2) {
    return new Visitor<T>() {
      @Override
      public T apply(DistributionView view) {
        return f1.apply(view);
      }

      @Override
      public T apply(IntervalView view) {
        return f2.apply(view);
      }
    };
  }

  /**
   * A {@link View} for distribution-based aggregations.
   */
  public static final class DistributionView extends View {
    /**
     * Constructs a new {@link DistributionView}.
     */
    public static DistributionView create(DistributionViewDescriptor distributionViewDescriptor,
        List<DistributionAggregation> distributionAggregations, Timestamp start, Timestamp end) {
      return new DistributionView(distributionViewDescriptor, distributionAggregations, start, end);
    }

    /**
     * The {@link DistributionAggregation}s associated with this {@link DistributionView}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public List<DistributionAggregation> getDistributionAggregations() {
      return distributionAggregations;
    }

    /**
     * Returns start timestamp for this aggregation.
     */
    public Timestamp getStart() {
      return start;
    }

    /**
     * Returns end timestamp for this aggregation.
     */
    public Timestamp getEnd() {
      return end;
    }

    @Override
    public DistributionViewDescriptor getViewDescriptor() {
      return distributionViewDescriptor;
    }

    @Override
    public <T> T match(Visitor<T> visitor) {
      return visitor.apply(this);
    }

    private final DistributionViewDescriptor distributionViewDescriptor;
    private final List<DistributionAggregation> distributionAggregations;
    private final Timestamp start;
    private final Timestamp end;

    private DistributionView(DistributionViewDescriptor distributionViewDescriptor,
        List<DistributionAggregation> distributionAggregations, Timestamp start, Timestamp end) {
      this.distributionViewDescriptor = distributionViewDescriptor;
      this.distributionAggregations = distributionAggregations;
      this.start = start;
      this.end = end;
    }
  }

  /**
   * A {@link View} for interval-base aggregations.
   */
  public static final class IntervalView extends View {
    /**
     * Constructs a new {@link IntervalView}.
     */
    public static IntervalView create(IntervalViewDescriptor intervalViewDescriptor,
        List<IntervalAggregation> intervalAggregations) {
      return new IntervalView(intervalViewDescriptor, intervalAggregations);
    }

    /**
     * The {@link IntervalAggregation}s associated with this {@link IntervalView}.
     *
     * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public List<IntervalAggregation> getIntervalAggregations() {
      return intervalAggregations;
    }

    @Override
    public IntervalViewDescriptor getViewDescriptor() {
      return intervalViewDescriptor;
    }

    @Override
    public <T> T match(Visitor<T> visitor) {
      return visitor.apply(this);
    }

    private final IntervalViewDescriptor intervalViewDescriptor;
    private final List<IntervalAggregation> intervalAggregations;

    private IntervalView(IntervalViewDescriptor intervalViewDescriptor,
        List<IntervalAggregation> intervalAggregations) {
      this.intervalViewDescriptor = intervalViewDescriptor;
      this.intervalAggregations = intervalAggregations;
    }
  }
}
