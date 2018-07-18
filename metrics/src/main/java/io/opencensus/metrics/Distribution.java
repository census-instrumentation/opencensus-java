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

package io.opencensus.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Distribution} contains summary statistics for a population of values. It optionally
 * contains a histogram representing the distribution of those values across a set of buckets.
 *
 * @since 0.16
 */
@ExperimentalApi
@AutoValue
@Immutable
public abstract class Distribution {

  Distribution() {}

  /**
   * Creates a {@link Distribution}.
   *
   * @param mean mean of the population values.
   * @param count count of the population values.
   * @param sumOfSquaredDeviations sum of squared deviations of the population values.
   * @param range {@link Range} of the population values, or {@code null} if count is 0.
   * @param bucketBoundaries bucket boundaries of a histogram.
   * @param buckets {@link Bucket}s of a histogram.
   * @param exemplars the exemplars associated with histogram buckets.
   * @return a {@code Distribution}.
   * @since 0.16
   */
  public static Distribution create(
      double mean,
      long count,
      double sumOfSquaredDeviations,
      @Nullable Range range,
      List<Double> bucketBoundaries,
      List<Bucket> buckets,
      List<Exemplar> exemplars) {
    Utils.checkArgument(count >= 0, "count should be non-negative.");
    Utils.checkArgument(
        sumOfSquaredDeviations >= 0, "sum of squared deviations should be non-negative.");
    if (count == 0) {
      Utils.checkArgument(range == null, "range should not be present if count is 0.");
      Utils.checkArgument(mean == 0, "mean should be 0 if count is 0.");
      Utils.checkArgument(
          sumOfSquaredDeviations == 0, "sum of squared deviations should be 0 if count is 0.");
    } else {
      Utils.checkArgument(range != null, "range should be present if count is not 0.");
    }
    Utils.checkNotNull(exemplars, "exemplar list should not be null.");
    Utils.checkListElementNotNull(exemplars, "exemplar should not be null.");
    return new AutoValue_Distribution(
        mean,
        count,
        sumOfSquaredDeviations,
        range,
        copyBucketBounds(bucketBoundaries),
        copyBucketCount(buckets),
        Collections.<Exemplar>unmodifiableList(new ArrayList<Exemplar>(exemplars)));
  }

  private static List<Double> copyBucketBounds(List<Double> bucketBoundaries) {
    Utils.checkNotNull(bucketBoundaries, "bucketBoundaries list should not be null.");
    List<Double> bucketBoundariesCopy = new ArrayList<Double>(bucketBoundaries); // Deep copy.
    // Check if sorted.
    if (bucketBoundariesCopy.size() > 1) {
      double lower = bucketBoundariesCopy.get(0);
      for (int i = 1; i < bucketBoundariesCopy.size(); i++) {
        double next = bucketBoundariesCopy.get(i);
        Utils.checkArgument(lower < next, "bucket boundaries not sorted.");
        lower = next;
      }
    }
    return Collections.unmodifiableList(bucketBoundariesCopy);
  }

  private static List<Bucket> copyBucketCount(List<Bucket> buckets) {
    Utils.checkNotNull(buckets, "bucket list should not be null.");
    List<Bucket> bucketsCopy = new ArrayList<Bucket>(buckets);
    for (Bucket bucket : bucketsCopy) {
      Utils.checkNotNull(bucket, "bucket should not be null.");
    }
    return Collections.unmodifiableList(bucketsCopy);
  }

  /**
   * Returns the aggregated mean.
   *
   * @return the aggregated mean.
   * @since 0.16
   */
  public abstract double getMean();

  /**
   * Returns the aggregated count.
   *
   * @return the aggregated count.
   * @since 0.16
   */
  public abstract long getCount();

  /**
   * Returns the aggregated sum of squared deviations.
   *
   * <p>The sum of squared deviations from the mean of the values in the population. For values x_i
   * this is:
   *
   * <p>Sum[i=1..n]((x_i - mean)^2)
   *
   * <p>If count is zero then this field must be zero.
   *
   * @return the aggregated sum of squared deviations.
   * @since 0.16
   */
  public abstract double getSumOfSquaredDeviations();

  /**
   * Returns the {@link Range} of the population values, or returns {@code null} if there are no
   * population values.
   *
   * @return the {@code Range} of the population values.
   * @since 0.16
   */
  @Nullable
  public abstract Range getRange();

  /**
   * Returns the bucket boundaries of this distribution.
   *
   * <p>The bucket boundaries for that histogram are described by bucket_bounds. This defines
   * size(bucket_bounds) + 1 (= N) buckets. The boundaries for bucket index i are:
   *
   * <ul>
   *   <li>{@code (-infinity, bucket_bounds[i]) for i == 0}
   *   <li>{@code [bucket_bounds[i-1], bucket_bounds[i]) for 0 < i < N-2}
   *   <li>{@code [bucket_bounds[i-1], +infinity) for i == N-1}
   * </ul>
   *
   * <p>i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
   * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
   *
   * <p>If bucket_bounds has no elements (zero size), then there is no histogram associated with the
   * Distribution. If bucket_bounds has only one element, there are no finite buckets, and that
   * single element is the common boundary of the overflow and underflow buckets. The values must be
   * monotonically increasing.
   *
   * @return the bucket boundaries of this distribution.
   * @since 0.16
   */
  public abstract List<Double> getBucketBoundaries();

  /**
   * Returns the the aggregated histogram {@link Bucket}s.
   *
   * @return the the aggregated histogram buckets.
   * @since 0.16
   */
  public abstract List<Bucket> getBuckets();

  /**
   * Returns the {@link Exemplar}s associated with histogram buckets.
   *
   * @return the {@code Exemplar}s associated with histogram buckets.
   * @since 0.16
   */
  public abstract List<Exemplar> getExemplars();

  /**
   * The range of the population values.
   *
   * @since 0.16
   */
  @AutoValue
  @Immutable
  public abstract static class Range {

    Range() {}

    /**
     * Creates a {@link Range}.
     *
     * @param min the minimum of the population values.
     * @param max the maximum of the population values.
     * @return a {@code Range}.
     * @since 0.16
     */
    public static Range create(double min, double max) {
      Utils.checkArgument(min <= max, "max should be greater or equal to min.");
      return new AutoValue_Distribution_Range(min, max);
    }

    /**
     * Returns the minimum of the population values.
     *
     * @return the minimum of the population values.
     * @since 0.16
     */
    public abstract double getMin();

    /**
     * Returns the maximum of the population values.
     *
     * @return the maximum of the population values.
     * @since 0.16
     */
    public abstract double getMax();
  }

  /**
   * The histogram bucket of the population values.
   *
   * @since 0.16
   */
  @AutoValue
  @Immutable
  public abstract static class Bucket {

    Bucket() {}

    /**
     * Creates a {@link Bucket}.
     *
     * @param count the number of values in each bucket of the histogram.
     * @return a {@code Bucket}.
     * @since 0.16
     */
    public static Bucket create(long count) {
      Utils.checkArgument(count >= 0, "bucket count should be non-negative.");
      return new AutoValue_Distribution_Bucket(count);
    }

    /**
     * Returns the number of values in each bucket of the histogram.
     *
     * @return the number of values in each bucket of the histogram.
     * @since 0.16
     */
    public abstract long getCount();
  }

  /**
   * An example point that may be used to annotate aggregated distribution values, associated with a
   * histogram bucket.
   *
   * @since 0.16
   */
  @Immutable
  @AutoValue
  public abstract static class Exemplar {

    Exemplar() {}

    /**
     * Returns value of the {@link Exemplar} point.
     *
     * @return value of the {@code Exemplar} point.
     * @since 0.16
     */
    public abstract double getValue();

    /**
     * Returns the time that this {@link Exemplar}'s value was recorded.
     *
     * @return the time that this {@code Exemplar}'s value was recorded.
     * @since 0.16
     */
    public abstract Timestamp getTimestamp();

    /**
     * Returns the contextual information about the example value, represented as a string map.
     *
     * @return the contextual information about the example value.
     * @since 0.16
     */
    public abstract Map<String, String> getAttachments();

    /**
     * Creates an {@link Exemplar}.
     *
     * @param value value of the {@link Exemplar} point.
     * @param timestamp the time that this {@code Exemplar}'s value was recorded.
     * @param attachments the contextual information about the example value.
     * @return an {@code Exemplar}.
     * @since 0.16
     */
    public static Exemplar create(
        double value, Timestamp timestamp, Map<String, String> attachments) {
      Utils.checkNotNull(attachments, "attachments");
      Map<String, String> attachmentsCopy =
          Collections.unmodifiableMap(new HashMap<String, String>(attachments));
      for (Entry<String, String> entry : attachmentsCopy.entrySet()) {
        Utils.checkNotNull(entry.getKey(), "key of attachments");
        Utils.checkNotNull(entry.getValue(), "value of attachments");
      }
      return new AutoValue_Distribution_Exemplar(value, timestamp, attachmentsCopy);
    }
  }
}
