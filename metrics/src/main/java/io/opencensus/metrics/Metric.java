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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link Metric} with one or more {@link TimeSeries}.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class Metric {

  Metric() {}

  /**
   * Creates a {@link Metric}.
   *
   * @param metricDescriptor the {@link MetricDescriptor}.
   * @param timeSeries the {@link TimeSeries} for this metric.
   * @return a {@code Metric}.
   * @since 0.16
   */
  public static Metric create(MetricDescriptor metricDescriptor, List<TimeSeries> timeSeries) {
    Utils.checkNotNull(timeSeries, "timeSeriesList");
    Utils.checkListElementNotNull(timeSeries, "timeSeries");
    return new AutoValue_Metric(
        metricDescriptor, Collections.unmodifiableList(new ArrayList<TimeSeries>(timeSeries)));
  }

  /**
   * Returns the {@link MetricDescriptor} of this metric.
   *
   * @return the {@code MetricDescriptor} of this metric.
   * @since 0.16
   */
  public abstract MetricDescriptor getMetricDescriptor();

  /**
   * Returns the {@link TimeSeries}s for this metric.
   *
   * <p>Each {@link TimeSeries} has one or more {@link Point}.
   *
   * <p>The type of the {@link TimeSeries}s must match {@link MetricDescriptor.Type}, so the list
   * should only contains either {@link TimeSeries.TimeSeriesCumulative} or {@link
   * TimeSeries.TimeSeriesGauge}.
   *
   * @return the {@code TimeSeries}s for this metric.
   * @since 0.16
   */
  public abstract List<TimeSeries> getTimeSeries();
}
