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
 * for a given {@code Measure} into an {@link AggregationData}.
 *
 * <p>{@link Aggregation} currently supports 6 types of basic aggregation:
 * <ul>
 *   <li>Sum
 *   <li>Count
 *   <li>Histogram
 *   <li>Range
 *   <li>Mean
 *   <li>StdDev (standard deviation)
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
      Function<? super Sum, T> p0,
      Function<? super Count, T> p1,
      Function<? super Histogram, T> p2,
      Function<? super Range, T> p3,
      Function<? super Mean, T> p4,
      Function<? super StdDev, T> p5,
      Function<? super Aggregation, T> defaultFunction);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Sum extends Aggregation {

    Sum() {
    }

    /**
     * Construct a {@code Sum}.
     *
     * @return a new {@code Sum}.
     */
    public static Sum create() {
      return new AutoValue_Aggregation_Sum();
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Count extends Aggregation {

    Count() {
    }

    /**
     * Construct a {@code Count}.
     *
     * @return a new {@code Count}.
     */
    public static Count create() {
      return new AutoValue_Aggregation_Count();
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /** Calculate histogram on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Histogram extends Aggregation {

    Histogram() {
    }

    /**
     * Construct a {@code Histogram}.
     *
     * @return a new {@code Histogram}.
     */
    public static Histogram create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new AutoValue_Aggregation_Histogram(bucketBoundaries);
    }

    public abstract BucketBoundaries getBucketBoundaries();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /** Calculate range on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Range extends Aggregation {

    Range() {
    }

    /**
     * Construct a {@code Range}.
     *
     * @return a new {@code Range}.
     */
    // TODO(songya): not v0.1, expose factory method later.
    static Range create() {
      return new AutoValue_Aggregation_Range();
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p3.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Mean extends Aggregation {

    Mean() {
    }

    /**
     * Construct a {@code Mean}.
     *
     * @return a new {@code Mean}.
     */
    // TODO(songya): not v0.1, expose factory method later.
    static Mean create() {
      return new AutoValue_Aggregation_Mean();
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p4.apply(this);
    }
  }

  /** Calculate standard deviation on aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  // TODO(songya): StackDriver uses sumOfSquaredDeviation instead of standard deviation, do we want
  // to change this aggregation to sumOfSquaredDeviation instead?
  public abstract static class StdDev extends Aggregation {

    StdDev() {
    }

    /**
     * Construct a {@code StdDev}.
     *
     * @return a new {@code StdDev}.
     */
    // TODO(songya): not v0.1, expose factory method later.
    static StdDev create() {
      return new AutoValue_Aggregation_StdDev();
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregation, T> defaultFunction) {
      return p5.apply(this);
    }
  }
}
