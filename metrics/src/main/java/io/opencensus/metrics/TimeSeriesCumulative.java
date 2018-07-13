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
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of data points that describes the time-varying values of a cumulative {@code
 * Metric}.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class TimeSeriesCumulative extends TimeSeries {

  TimeSeriesCumulative() {}

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
    Utils.checkListElementNotNull(labelValues, "labelValue");
    Utils.checkListElementNotNull(points, "point");
    return new AutoValue_TimeSeriesCumulative(
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
