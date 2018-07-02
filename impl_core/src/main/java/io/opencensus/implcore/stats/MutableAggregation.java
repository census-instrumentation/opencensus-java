/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.stats;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData.DistributionData.Exemplar;
import io.opencensus.stats.BucketBoundaries;
import java.util.Map;

/** Mutable version of {@link Aggregation} that supports adding values. */
abstract class MutableAggregation {

  private MutableAggregation() {}

  // Tolerance for double comparison.
  private static final double TOLERANCE = 1e-6;

  /**
   * Put a new value into the MutableAggregation.
   *
   * @param value new value to be added to population
   * @param attachments the contextual information on an {@link Exemplar}
   * @param timestamp the timestamp when the value is recorded
   */
  abstract void add(double value, Map<String, String> attachments, Timestamp timestamp);

  // TODO(songya): remove this method once interval stats is completely removed.
  /**
   * Combine the internal values of this MutableAggregation and value of the given
   * MutableAggregation, with the given fraction. Then set the internal value of this
   * MutableAggregation to the combined value.
   *
   * @param other the other {@code MutableAggregation}. The type of this and other {@code
   *     MutableAggregation} must match.
   * @param fraction the fraction that the value in other {@code MutableAggregation} should
   *     contribute. Must be within [0.0, 1.0].
   */
  abstract void combine(MutableAggregation other, double fraction);

  /** Applies the given match function to the underlying data type. */
  abstract <T> T match(
      Function<? super MutableSum, T> p0,
      Function<? super MutableCount, T> p1,
      Function<? super MutableMean, T> p2,
      Function<? super MutableDistribution, T> p3,
      Function<? super MutableLastValue, T> p4);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  static final class MutableSum extends MutableAggregation {

    private double sum = 0.0;

    private MutableSum() {}

    /**
     * Construct a {@code MutableSum}.
     *
     * @return an empty {@code MutableSum}.
     */
    static MutableSum create() {
      return new MutableSum();
    }

    @Override
    void add(double value, Map<String, String> attachments, Timestamp timestamp) {
      sum += value;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableSum, "MutableSum expected.");
      this.sum += fraction * ((MutableSum) other).getSum();
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    double getSum() {
      return sum;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3,
        Function<? super MutableLastValue, T> p4) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  static final class MutableCount extends MutableAggregation {

    private long count = 0;

    private MutableCount() {}

    /**
     * Construct a {@code MutableCount}.
     *
     * @return an empty {@code MutableCount}.
     */
    static MutableCount create() {
      return new MutableCount();
    }

    @Override
    void add(double value, Map<String, String> attachments, Timestamp timestamp) {
      count++;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableCount, "MutableCount expected.");
      this.count += Math.round(fraction * ((MutableCount) other).getCount());
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3,
        Function<? super MutableLastValue, T> p4) {
      return p1.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  static final class MutableMean extends MutableAggregation {

    private double sum = 0.0;
    private long count = 0;

    private MutableMean() {}

    /**
     * Construct a {@code MutableMean}.
     *
     * @return an empty {@code MutableMean}.
     */
    static MutableMean create() {
      return new MutableMean();
    }

    @Override
    void add(double value, Map<String, String> attachments, Timestamp timestamp) {
      count++;
      sum += value;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableMean, "MutableMean expected.");
      MutableMean mutableMean = (MutableMean) other;
      this.count += Math.round(mutableMean.count * fraction);
      this.sum += mutableMean.sum * fraction;
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    double getMean() {
      return getCount() == 0 ? 0 : getSum() / getCount();
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    double getSum() {
      return sum;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3,
        Function<? super MutableLastValue, T> p4) {
      return p2.apply(this);
    }
  }

  /** Calculate distribution stats on aggregated {@code MeasureValue}s. */
  static final class MutableDistribution extends MutableAggregation {

    private double sum = 0.0;
    private double mean = 0.0;
    private long count = 0;
    private double sumOfSquaredDeviations = 0.0;

    // Initial "impossible" values, that will get reset as soon as first value is added.
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;
    private final Exemplar[] exemplars;

    private MutableDistribution(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      int buckets = bucketBoundaries.getBoundaries().size() + 1;
      this.bucketCounts = new long[buckets];
      // In the implementation, each histogram bucket can have up to one exemplar, and the exemplar
      // array is guaranteed to be in ascending order.
      // If there's no histogram, don't record exemplars.
      this.exemplars =
          bucketBoundaries.getBoundaries().isEmpty() ? new Exemplar[0] : new Exemplar[buckets];
    }

    /**
     * Construct a {@code MutableDistribution}.
     *
     * @return an empty {@code MutableDistribution}.
     */
    static MutableDistribution create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new MutableDistribution(bucketBoundaries);
    }

    @Override
    void add(double value, Map<String, String> attachments, Timestamp timestamp) {
      sum += value;
      count++;

      /*
       * Update the sum of squared deviations from the mean with the given value. For values
       * x_i this is Sum[i=1..n]((x_i - mean)^2)
       *
       * Computed using Welfords method (see
       * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance, or Knuth, "The Art of
       * Computer Programming", Vol. 2, page 323, 3rd edition)
       */
      double deltaFromMean = value - mean;
      mean += deltaFromMean / count;
      double deltaFromMean2 = value - mean;
      sumOfSquaredDeviations += deltaFromMean * deltaFromMean2;

      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }

      int bucket = 0;
      for (; bucket < bucketBoundaries.getBoundaries().size(); bucket++) {
        if (value < bucketBoundaries.getBoundaries().get(bucket)) {
          break;
        }
      }
      bucketCounts[bucket]++;

      // No implicit recording for exemplars - if there are no attachments (contextual information),
      // don't record exemplars.
      if (!attachments.isEmpty() && exemplars.length > 0) {
        exemplars[bucket] = Exemplar.create(value, timestamp, attachments);
      }
    }

    // We don't compute fractional MutableDistribution, it's either whole or none.
    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableDistribution, "MutableDistribution expected.");
      if (Math.abs(1.0 - fraction) > TOLERANCE) {
        return;
      }

      MutableDistribution mutableDistribution = (MutableDistribution) other;
      checkArgument(
          this.bucketBoundaries.equals(mutableDistribution.bucketBoundaries),
          "Bucket boundaries should match.");

      // Algorithm for calculating the combination of sum of squared deviations:
      // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm.
      if (this.count + mutableDistribution.count > 0) {
        double delta = mutableDistribution.mean - this.mean;
        this.sumOfSquaredDeviations =
            this.sumOfSquaredDeviations
                + mutableDistribution.sumOfSquaredDeviations
                + Math.pow(delta, 2)
                    * this.count
                    * mutableDistribution.count
                    / (this.count + mutableDistribution.count);
      }

      this.count += mutableDistribution.count;
      this.sum += mutableDistribution.sum;
      this.mean = this.sum / this.count;

      if (mutableDistribution.min < this.min) {
        this.min = mutableDistribution.min;
      }
      if (mutableDistribution.max > this.max) {
        this.max = mutableDistribution.max;
      }

      long[] bucketCounts = mutableDistribution.getBucketCounts();
      for (int i = 0; i < bucketCounts.length; i++) {
        this.bucketCounts[i] += bucketCounts[i];
      }

      for (int i = 0; i < mutableDistribution.getExemplars().length; i++) {
        Exemplar exemplar = mutableDistribution.getExemplars()[i];
        if (exemplar != null) {
          this.exemplars[i] = exemplar;
        }
      }
    }

    double getMean() {
      return mean;
    }

    long getCount() {
      return count;
    }

    double getMin() {
      return min;
    }

    double getMax() {
      return max;
    }

    // Returns the aggregated sum of squared deviations.
    double getSumOfSquaredDeviations() {
      return sumOfSquaredDeviations;
    }

    long[] getBucketCounts() {
      return bucketCounts;
    }

    Exemplar[] getExemplars() {
      return exemplars;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3,
        Function<? super MutableLastValue, T> p4) {
      return p3.apply(this);
    }
  }

  /** Calculate last value on aggregated {@code MeasureValue}s. */
  static final class MutableLastValue extends MutableAggregation {

    // Initial value that will get reset as soon as first value is added.
    private double lastValue = Double.NaN;
    // TODO(songya): remove this once interval stats is completely removed.
    private boolean initialized = false;

    private MutableLastValue() {}

    /**
     * Construct a {@code MutableLastValue}.
     *
     * @return an empty {@code MutableLastValue}.
     */
    static MutableLastValue create() {
      return new MutableLastValue();
    }

    @Override
    void add(double value, Map<String, String> attachments, Timestamp timestamp) {
      lastValue = value;
      // TODO(songya): remove this once interval stats is completely removed.
      if (!initialized) {
        initialized = true;
      }
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableLastValue, "MutableLastValue expected.");
      MutableLastValue otherValue = (MutableLastValue) other;
      // Assume other is always newer than this, because we combined interval buckets in time order.
      // If there's a newer value, overwrite current value.
      this.lastValue = otherValue.initialized ? otherValue.getLastValue() : this.lastValue;
    }

    /**
     * Returns the last value.
     *
     * @return the last value.
     */
    double getLastValue() {
      return lastValue;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3,
        Function<? super MutableLastValue, T> p4) {
      return p4.apply(this);
    }
  }
}
