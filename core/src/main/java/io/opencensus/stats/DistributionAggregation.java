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
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Describes data aggregations based on distributions.
 *
 * <p>A distribution aggregation may optionally contain a histogram of the values in the
 * population. The bucket boundaries for that histogram are described by
 * {@link BucketBoundaries}, which defines {@code BucketBoundaries.getBoundaries.size() + 1 (= N)}
 * buckets. The boundaries for bucket index i are:
 * <ul>
 * <li>[-infinity, bounds[i]) for i == 0
 * <li>[bounds[i-1], bounds[i]) for 0 &lt; i &lt; N-1
 * <li>[bounds[i-1], +infinity) for i == N-1
 * </ul>
 * i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
 * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
 *
 * <p>Note: If N = 1, there are no finite buckets and the single bucket is both the overflow
 * and underflow bucket.
 */
@Immutable
@AutoValue
public abstract class DistributionAggregation {
  /**
   * Constructs a new {@link DistributionAggregation} with the optional
   * histogram bucket boundaries.
   */
  public static DistributionAggregation create(List<Double> bucketBoundaries) {
    return createInternal(BucketBoundaries.create(bucketBoundaries));
  }

  /**
   * Constructs a new {@link DistributionAggregation} without the optional
   * histogram bucket boundaries.
   */
  public static DistributionAggregation create() {
    return createInternal(null);
  }

  private static DistributionAggregation createInternal(
      @Nullable BucketBoundaries bucketBoundaries) {
    return new AutoValue_DistributionAggregation(bucketBoundaries);
  }

  /**
   * The optional histogram bucket boundaries for a distribution.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  @Nullable
  public List<Double> getBucketBoundaries() {
    BucketBoundaries bucketBoundaries = getBucketBoundariesObject();
    return bucketBoundaries == null ? null : bucketBoundaries.getBoundaries();
  }

  @Nullable
  abstract BucketBoundaries getBucketBoundariesObject();
}
