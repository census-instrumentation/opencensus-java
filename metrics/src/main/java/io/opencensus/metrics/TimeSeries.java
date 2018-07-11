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

import java.util.List;
import javax.annotation.concurrent.Immutable;

/** A collection of data points that describes the time-varying values of a {@code Metric}. */
@Immutable
abstract class TimeSeries {

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
}
