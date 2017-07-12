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

package io.opencensus.stats;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Aggregation} is the process of combining a certain set of {@code MeasureValue}s
 * for a given {@code Measure} into an {@code Aggregate}.
 *
 * <p>{@link Aggregation} currently supports 6 types of basic aggregation:
 * <ul>
 *   <li>SUM
 *   <li>COUNT
 *   <li>HISTOGRAM
 *   <li>RANGE
 *   <li>MEAN
 *   <li>STDDEV (standard deviation)
 * </ul>
 *
 * <p>When creating a {@link View}, one or more {@link Aggregation} needs to be specified as how to
 * aggregate {@code MeasureValue}s.
 */
@Immutable
public abstract class Aggregation {

  private Aggregation() {
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super AggregationSum, T> p0,
      Function<? super AggregationCount, T> p1,
      Function<? super AggregationHistogram, T> p2,
      Function<? super AggregationRange, T> p3,
      Function<? super AggregationMean, T> p4,
      Function<? super AggregationStdDev, T> p5);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationSum extends Aggregation {

    AggregationSum() {
    }

    /**
     * Construct a {@code AggregationSum}.
     *
     * @return a new {@code AggregationSum}.
     */
    public static AggregationSum create() {
      return new AutoValue_Aggregation_AggregationSum();
    }

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationCount extends Aggregation {

    AggregationCount() {
    }

    /**
     * Construct a {@code AggregationCount}.
     *
     * @return a new {@code AggregationCount}.
     */
    public static AggregationCount create() {
      return new AutoValue_Aggregation_AggregationCount();
    }

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p1.apply(this);
    }
  }

  /** Calculate histogram on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationHistogram extends Aggregation {

    AggregationHistogram() {
    }

    /**
     * Construct a {@code AggregationHistogram}.
     *
     * @return a new {@code AggregationHistogram}.
     */
    public static AggregationHistogram create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new AutoValue_Aggregation_AggregationHistogram(bucketBoundaries);
    }

    abstract BucketBoundaries getBucketBoundaries();

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p2.apply(this);
    }
  }

  /** Calculate range on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationRange extends Aggregation {

    AggregationRange() {
    }

    /**
     * Construct a {@code AggregationRange}.
     *
     * @return a new {@code AggregationRange}.
     */
    // TODO(songya): not v0.1, expose this method later.
    static AggregationRange create() {
      return new AutoValue_Aggregation_AggregationRange();
    }

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p3.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationMean extends Aggregation {

    AggregationMean() {
    }

    /**
     * Construct a {@code AggregationMean}.
     *
     * @return a new {@code AggregationMean}.
     */
    // TODO(songya): not v0.1, expose this method later.
    static AggregationMean create() {
      return new AutoValue_Aggregation_AggregationMean();
    }

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p4.apply(this);
    }
  }

  /** Calculate standard deviation on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregationStdDev extends Aggregation {

    AggregationStdDev() {
    }

    /**
     * Construct a {@code AggregationStdDev}.
     *
     * @return a new {@code AggregationStdDev}.
     */
    // TODO(songya): not v0.1, expose this method later.
    static AggregationStdDev create() {
      return new AutoValue_Aggregation_AggregationStdDev();
    }

    @Override
    public final <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p5.apply(this);
    }
  }
}
