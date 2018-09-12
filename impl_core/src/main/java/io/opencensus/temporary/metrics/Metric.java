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

package io.opencensus.temporary.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.internal.Utils;
import io.opencensus.temporary.metrics.Value.ValueDistribution;
import io.opencensus.temporary.metrics.Value.ValueDouble;
import io.opencensus.temporary.metrics.Value.ValueLong;
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
   * @param timeSeriesList the {@link TimeSeries} list for this metric.
   * @return a {@code Metric}.
   * @since 0.16
   */
  public static Metric create(MetricDescriptor metricDescriptor, List<TimeSeries> timeSeriesList) {
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
   * Returns the {@link TimeSeries} list for this metric.
   *
   * <p>The type of the {@link TimeSeries#getPoints()} must match {@link MetricDescriptor.Type}.
   *
   * @return the {@code TimeSeriesList} for this metric.
   * @since 0.16
   */
  public abstract List<TimeSeries> getTimeSeriesList();

  private static void checkTypeMatch(MetricDescriptor.Type type, List<TimeSeries> timeSeriesList) {
    for (TimeSeries timeSeries : timeSeriesList) {
      for (Point point : timeSeries.getPoints()) {
        Value value = point.getValue();
        String valueClassName = "";
        if (value.getClass().getSuperclass() != null) { // work around nullness check
          // AutoValue classes should always have a super class.
          valueClassName = value.getClass().getSuperclass().getSimpleName();
        }
        switch (type) {
          case GAUGE_INT64:
          case CUMULATIVE_INT64:
            Utils.checkArgument(
                value instanceof ValueLong, "Type mismatch: %s, %s.", type, valueClassName);
            break;
          case CUMULATIVE_DOUBLE:
          case GAUGE_DOUBLE:
            Utils.checkArgument(
                value instanceof ValueDouble, "Type mismatch: %s, %s.", type, valueClassName);
            break;
          case CUMULATIVE_DISTRIBUTION:
            Utils.checkArgument(
                value instanceof ValueDistribution, "Type mismatch: %s, %s.", type, valueClassName);
        }
      }
    }
  }
}
