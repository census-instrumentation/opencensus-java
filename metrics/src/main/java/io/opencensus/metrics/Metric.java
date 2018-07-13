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
import io.opencensus.metrics.TimeSeriesList.TimeSeriesCumulativeList;
import io.opencensus.metrics.TimeSeriesList.TimeSeriesGaugeList;
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
   * @param timeSeriesList the {@link TimeSeriesList} for this metric.
   * @return a {@code Metric}.
   * @since 0.16
   */
  public static Metric create(MetricDescriptor metricDescriptor, TimeSeriesList timeSeriesList) {
    checkTypeMatch(metricDescriptor.getType(), timeSeriesList);
    return new AutoValue_Metric(metricDescriptor, timeSeriesList);
  }

  /**
   * Returns the {@link MetricDescriptor} of this metric.
   *
   * @return the {@code MetricDescriptor} of this metric.
   * @since 0.16
   */
  public abstract MetricDescriptor getMetricDescriptor();

  /**
   * Returns the {@link TimeSeriesList} for this metric.
   *
   * <p>The type of the {@link TimeSeriesList} must match {@link MetricDescriptor.Type}.
   *
   * @return the {@code TimeSeriesList} for this metric.
   * @since 0.16
   */
  public abstract TimeSeriesList getTimeSeriesList();

  private static void checkTypeMatch(MetricDescriptor.Type type, TimeSeriesList timeSeriesList) {
    switch (type) {
      case GAUGE_INT64:
      case GAUGE_DOUBLE:
        Utils.checkArgument(
            timeSeriesList instanceof TimeSeriesGaugeList,
            String.format(
                "Type mismatch: %s, %s.", type, timeSeriesList.getClass().getSimpleName()));
        break;
      case CUMULATIVE_DISTRIBUTION:
      case CUMULATIVE_DOUBLE:
      case CUMULATIVE_INT64:
        Utils.checkArgument(
            timeSeriesList instanceof TimeSeriesCumulativeList,
            String.format(
                "Type mismatch: %s, %s.", type, timeSeriesList.getClass().getSimpleName()));
        break;
    }
  }
}
