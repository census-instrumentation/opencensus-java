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
import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of data points that describes the time-varying values of a {@code Metric}.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
public abstract class TimeSeries {

  TimeSeries() {}

  /**
   * Returns the set of {@link LabelValue}s that uniquely identify this {@link TimeSeries}.
   *
   * <p>Apply to all {@link Point}s.
   *
   * <p>The order of {@link LabelValue}s must match that of {@link LabelKey}s in the {@code
   * MetricDescriptor}.
   *
   * @return the {@code LabelValue}s.
   * @since 0.16
   */
  public abstract List<LabelValue> getLabelValues();

  /**
   * Returns the data {@link Point}s of this {@link TimeSeries}.
   *
   * @return the data {@code Point}s.
   * @since 0.16
   */
  public abstract List<Point> getPoints();

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.16
   */
  public abstract <T> T match(
      Function<? super TimeSeriesGauge, T> p0,
      Function<? super TimeSeriesCumulative, T> p1,
      Function<? super TimeSeries, T> defaultFunction);

  /**
   * A collection of data points that describes the time-varying values of a gauge {@code Metric}.
   *
   * @since 0.16
   */
  @ExperimentalApi
  @Immutable
  @AutoValue
  public abstract static class TimeSeriesGauge extends TimeSeries {

    TimeSeriesGauge() {}

    @Override
    public final <T> T match(
        Function<? super TimeSeriesGauge, T> p0,
        Function<? super TimeSeriesCumulative, T> p1,
        Function<? super TimeSeries, T> defaultFunction) {
      return p0.apply(this);
    }

    /**
     * Creates a {@link TimeSeriesGauge}.
     *
     * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
     * @param points the data {@code Point}s of this {@code TimeSeries}.
     * @return a {@code TimeSeriesGauge}.
     * @since 0.16
     */
    public static TimeSeriesGauge create(List<LabelValue> labelValues, List<Point> points) {
      // Fail fast on null lists to prevent NullPointerException when copying the lists.
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkNotNull(points, "points");
      return new AutoValue_TimeSeries_TimeSeriesGauge(
          Collections.unmodifiableList(new ArrayList<LabelValue>(labelValues)),
          Collections.unmodifiableList(new ArrayList<Point>(points)));
    }
  }

  /**
   * A collection of data points that describes the time-varying values of a cumulative {@code
   * Metric}.
   *
   * @since 0.16
   */
  @ExperimentalApi
  @Immutable
  @AutoValue
  public abstract static class TimeSeriesCumulative extends TimeSeries {

    TimeSeriesCumulative() {}

    @Override
    public final <T> T match(
        Function<? super TimeSeriesGauge, T> p0,
        Function<? super TimeSeriesCumulative, T> p1,
        Function<? super TimeSeries, T> defaultFunction) {
      return p1.apply(this);
    }

    /**
     * Creates a {@link TimeSeriesCumulative}.
     *
     * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
     * @param points the data {@code Point}s of this {@code TimeSeries}.
     * @param startTimestamp the start {@code Timestamp} of this {@code TimeSeriesCumulative}.
     * @return a {@code TimeSeriesCumulative}.
     * @since 0.16
     */
    public static TimeSeriesCumulative create(
        List<LabelValue> labelValues, List<Point> points, Timestamp startTimestamp) {
      // Fail fast on null lists to prevent NullPointerException when copying the lists.
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkNotNull(points, "points");
      return new AutoValue_TimeSeries_TimeSeriesCumulative(
          Collections.unmodifiableList(new ArrayList<LabelValue>(labelValues)),
          Collections.unmodifiableList(new ArrayList<Point>(points)),
          startTimestamp);
    }

    /**
     * Returns the start {@link Timestamp} of this {@link TimeSeriesCumulative}.
     *
     * @return the start {@code Timestamp}.
     * @since 0.16
     */
    public abstract Timestamp getStartTimestamp();
  }
}
