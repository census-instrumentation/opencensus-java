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

package io.opencensus.implcore.metrics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.implcore.internal.Utils;
import io.opencensus.metrics.DerivedDoubleGauge;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Implementation of {@link DerivedDoubleGauge}. */
public final class DerivedDoubleGaugeImpl extends DerivedDoubleGauge implements Meter {
  private final MetricDescriptor metricDescriptor;
  private volatile Map<List<LabelValue>, TimeSeriesProducer> registeredPoints =
      Collections.unmodifiableMap(new LinkedHashMap<List<LabelValue>, TimeSeriesProducer>());
  private final int labelKeysSize;

  DerivedDoubleGaugeImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    labelKeysSize = labelKeys.size();
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
  }

  @Override
  public <T> void createTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
    checkNotNull(labelValues, "labelValues should not be null.");
    checkArgument(labelKeysSize == labelValues.size(), "Incorrect number of labels.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");

    registerTimeSeries(
        new ArrayList<LabelValue>(labelValues), obj, checkNotNull(function, "function"));
  }

  @Override
  public void removeTimeSeries(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");

    remove(labelValues);
  }

  @Override
  public synchronized void clear() {
    registeredPoints.clear();
  }

  private synchronized void remove(List<LabelValue> labelValues) {
    Map<List<LabelValue>, TimeSeriesProducer> registeredPointsCopy =
        new HashMap<List<LabelValue>, TimeSeriesProducer>(registeredPoints);
    if (registeredPointsCopy.remove(labelValues) == null) {
      // The element not present, no need to update the current map of time series.
      return;
    }
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  private synchronized <T> void registerTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
    TimeSeriesProducer existingTimeSeries = registeredPoints.get(labelValues);
    if (existingTimeSeries != null) {
      throw new IllegalArgumentException(
          "A different time series with the same labels already exists.");
    }

    TimeSeriesProducer newTimeSeries = new PointWithFunction<T>(labelValues, obj, function);
    // Updating the map of time series happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, TimeSeriesProducer> registeredPointsCopy =
        new HashMap<List<LabelValue>, TimeSeriesProducer>(registeredPoints);
    registeredPointsCopy.put(labelValues, newTimeSeries);
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  @Override
  public Metric getMetric(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(registeredPoints.size());
    for (Map.Entry<List<LabelValue>, TimeSeriesProducer> entry : registeredPoints.entrySet()) {
      timeSeriesList.add(entry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link PointWithFunction} with a obj and function. */
  public static final class PointWithFunction<T> implements TimeSeriesProducer {
    private final List<LabelValue> labelValues;
    @Nullable private final T obj;
    private final ToDoubleFunction<T> function;
    private static final double DEFAULT_VALUE = 0.0;

    PointWithFunction(List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
      this.labelValues = labelValues;
      this.obj = obj;
      this.function = function;
    }

    @Override
    public TimeSeries getTimeSeries(Clock clock) {
      return TimeSeries.create(
          labelValues,
          Collections.singletonList(
              io.opencensus.metrics.export.Point.create(
                  Value.doubleValue(obj != null ? function.applyAsDouble(obj) : DEFAULT_VALUE),
                  clock.now())),
          null);
    }
  }
}
