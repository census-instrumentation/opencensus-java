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

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.DistributionData.Exemplar;
import io.opencensus.stats.AttachmentValue;
import io.opencensus.stats.BucketBoundaries;
import java.util.ArrayList;
import java.util.List;
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
  abstract void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp);

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

  abstract AggregationData toAggregationData();

  abstract Point toPoint(Timestamp timestamp);

  /** Calculate sum of doubles on aggregated {@code MeasureValue}s. */
  static class MutableSumDouble extends MutableAggregation {

    private double sum = 0.0;

    private MutableSumDouble() {}

    /**
     * Construct a {@code MutableSumDouble}.
     *
     * @return an empty {@code MutableSumDouble}.
     */
    static MutableSumDouble create() {
      return new MutableSumDouble();
    }

    @Override
    void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp) {
      sum += value;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableSumDouble, "MutableSumDouble expected.");
      this.sum += fraction * ((MutableSumDouble) other).sum;
    }

    @Override
    AggregationData toAggregationData() {
      return AggregationData.SumDataDouble.create(sum);
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.doubleValue(sum), timestamp);
    }

    @VisibleForTesting
    double getSum() {
      return sum;
    }
  }

  /** Calculate sum of longs on aggregated {@code MeasureValue}s. */
  static final class MutableSumLong extends MutableSumDouble {
    private MutableSumLong() {
      super();
    }

    /**
     * Construct a {@code MutableSumLong}.
     *
     * @return an empty {@code MutableSumLong}.
     */
    static MutableSumLong create() {
      return new MutableSumLong();
    }

    @Override
    AggregationData toAggregationData() {
      return AggregationData.SumDataLong.create(Math.round(getSum()));
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.longValue(Math.round(getSum())), timestamp);
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
    void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp) {
      count++;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableCount, "MutableCount expected.");
      this.count += Math.round(fraction * ((MutableCount) other).getCount());
    }

    @Override
    AggregationData toAggregationData() {
      return AggregationData.CountData.create(count);
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.longValue(count), timestamp);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
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
    void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp) {
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

    @SuppressWarnings("deprecation")
    @Override
    AggregationData toAggregationData() {
      return AggregationData.MeanData.create(getMean(), count);
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.doubleValue(getMean()), timestamp);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    double getMean() {
      return count == 0 ? 0 : sum / count;
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
    }

    @VisibleForTesting
    double getSum() {
      return sum;
    }
  }

  /** Calculate distribution stats on aggregated {@code MeasureValue}s. */
  static final class MutableDistribution extends MutableAggregation {

    private double sum = 0.0;
    private double mean = 0.0;
    private long count = 0;
    private double sumOfSquaredDeviations = 0.0;

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;

    // If there's a histogram (i.e bucket boundaries are not empty) in this MutableDistribution,
    // exemplars will have the same size to bucketCounts; otherwise exemplars are null.
    // Only the newest exemplar will be kept at each index.
    @javax.annotation.Nullable private final Exemplar[] exemplars;

    private MutableDistribution(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      int buckets = bucketBoundaries.getBoundaries().size() + 1;
      this.bucketCounts = new long[buckets];
      // In the implementation, each histogram bucket can have up to one exemplar, and the exemplar
      // array is guaranteed to be in ascending order.
      // If there's no histogram, don't record exemplars.
      this.exemplars = bucketBoundaries.getBoundaries().isEmpty() ? null : new Exemplar[buckets];
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
    void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp) {
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

      int bucket = 0;
      for (; bucket < bucketBoundaries.getBoundaries().size(); bucket++) {
        if (value < bucketBoundaries.getBoundaries().get(bucket)) {
          break;
        }
      }
      bucketCounts[bucket]++;

      // No implicit recording for exemplars - if there are no attachments (contextual information),
      // don't record exemplars.
      if (!attachments.isEmpty() && exemplars != null) {
        exemplars[bucket] = Exemplar.createNew(value, timestamp, attachments);
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

      long[] bucketCounts = mutableDistribution.getBucketCounts();
      for (int i = 0; i < bucketCounts.length; i++) {
        this.bucketCounts[i] += bucketCounts[i];
      }

      Exemplar[] otherExemplars = mutableDistribution.getExemplars();
      if (exemplars != null && otherExemplars != null) {
        for (int i = 0; i < otherExemplars.length; i++) {
          Exemplar exemplar = otherExemplars[i];
          // Assume other is always newer than this, because we combined interval buckets in time
          // order.
          // If there's a newer exemplar, overwrite current value.
          if (exemplar != null) {
            this.exemplars[i] = exemplar;
          }
        }
      }
    }

    @Override
    AggregationData toAggregationData() {
      List<Long> boxedBucketCounts = new ArrayList<Long>();
      for (long bucketCount : bucketCounts) {
        boxedBucketCounts.add(bucketCount);
      }
      List<Exemplar> exemplarList = new ArrayList<Exemplar>();
      if (exemplars != null) {
        for (Exemplar exemplar : exemplars) {
          if (exemplar != null) {
            exemplarList.add(exemplar);
          }
        }
      }
      return DistributionData.create(
          mean, count, sumOfSquaredDeviations, boxedBucketCounts, exemplarList);
    }

    @SuppressWarnings("deprecation")
    @Override
    Point toPoint(Timestamp timestamp) {
      List<Distribution.Bucket> buckets = new ArrayList<Distribution.Bucket>();
      for (int bucket = 0; bucket < bucketCounts.length; bucket++) {
        long bucketCount = bucketCounts[bucket];
        @javax.annotation.Nullable AggregationData.DistributionData.Exemplar exemplar = null;
        if (exemplars != null) {
          exemplar = exemplars[bucket];
        }

        Distribution.Bucket metricBucket;
        if (exemplar != null) {
          // Bucket with an Exemplar.
          metricBucket =
              Distribution.Bucket.create(
                  bucketCount,
                  Distribution.Exemplar.create(
                      exemplar.getValue(), exemplar.getTimestamp(), exemplar.getAttachments()));
        } else {
          // Bucket with no Exemplar.
          metricBucket = Distribution.Bucket.create(bucketCount);
        }
        buckets.add(metricBucket);
      }

      BucketOptions bucketOptions = BucketOptions.explicitOptions(bucketBoundaries.getBoundaries());

      return Point.create(
          Value.distributionValue(
              Distribution.create(
                  count, mean * count, sumOfSquaredDeviations, bucketOptions, buckets)),
          timestamp);
    }

    double getMean() {
      return mean;
    }

    long getCount() {
      return count;
    }

    // Returns the aggregated sum of squared deviations.
    double getSumOfSquaredDeviations() {
      return sumOfSquaredDeviations;
    }

    long[] getBucketCounts() {
      return bucketCounts;
    }

    BucketBoundaries getBucketBoundaries() {
      return bucketBoundaries;
    }

    @javax.annotation.Nullable
    Exemplar[] getExemplars() {
      return exemplars;
    }
  }

  /** Calculate double last value on aggregated {@code MeasureValue}s. */
  static class MutableLastValueDouble extends MutableAggregation {

    // Initial value that will get reset as soon as first value is added.
    private double lastValue = Double.NaN;
    // TODO(songya): remove this once interval stats is completely removed.
    private boolean initialized = false;

    private MutableLastValueDouble() {}

    /**
     * Construct a {@code MutableLastValueDouble}.
     *
     * @return an empty {@code MutableLastValueDouble}.
     */
    static MutableLastValueDouble create() {
      return new MutableLastValueDouble();
    }

    @Override
    void add(double value, Map<String, AttachmentValue> attachments, Timestamp timestamp) {
      lastValue = value;
      // TODO(songya): remove this once interval stats is completely removed.
      if (!initialized) {
        initialized = true;
      }
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableLastValueDouble, "MutableLastValueDouble expected.");
      MutableLastValueDouble otherValue = (MutableLastValueDouble) other;
      // Assume other is always newer than this, because we combined interval buckets in time order.
      // If there's a newer value, overwrite current value.
      this.lastValue = otherValue.initialized ? otherValue.getLastValue() : this.lastValue;
    }

    @Override
    AggregationData toAggregationData() {
      return AggregationData.LastValueDataDouble.create(lastValue);
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.doubleValue(lastValue), timestamp);
    }

    @VisibleForTesting
    double getLastValue() {
      return lastValue;
    }
  }

  /** Calculate last long value on aggregated {@code MeasureValue}s. */
  static final class MutableLastValueLong extends MutableLastValueDouble {
    private MutableLastValueLong() {
      super();
    }

    /**
     * Construct a {@code MutableLastValueLong}.
     *
     * @return an empty {@code MutableLastValueLong}.
     */
    static MutableLastValueLong create() {
      return new MutableLastValueLong();
    }

    @Override
    AggregationData toAggregationData() {
      return AggregationData.LastValueDataLong.create(Math.round(getLastValue()));
    }

    @Override
    Point toPoint(Timestamp timestamp) {
      return Point.create(Value.longValue(Math.round(getLastValue())), timestamp);
    }
  }
}
