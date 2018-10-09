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

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import io.opencensus.common.Clock;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.metrics.DoubleGaugeMetric;
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
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Implementation of {@link DoubleGaugeMetric}. */
public final class DoubleGaugeMetricImpl extends DoubleGaugeMetric implements Meter {
  private final MetricDescriptor metricDescriptor;
  private volatile Map<List<LabelValue>, TimeSeriesProducer> registeredTimeSeries =
      Maps.newHashMap();
  private final int labelKeysSize;
  private final List<LabelValue> defaultLabelValues;

  DoubleGaugeMetricImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    labelKeysSize = labelKeys.size();
    defaultLabelValues = new ArrayList<LabelValue>(labelKeysSize);
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
  }

  @Override
  public Point addTimeSeries(List<LabelValue> labelValues) {
    checkValidLabelValues(labelValues);

    return registerTimeSeries(new ArrayList<LabelValue>(labelValues));
  }

  @Override
  public <T> void addTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
    checkValidLabelValues(labelValues);

    registerTimeSeries(
        new ArrayList<LabelValue>(labelValues), obj, checkNotNull(function, "function"));
  }

  @Override
  public Point getDefaultTimeSeries() {
    // lock free default point retrieval
    TimeSeriesProducer existingTimeSeries = registeredTimeSeries.get(defaultLabelValues);
    if (existingTimeSeries != null) {
      return (PointImpl) existingTimeSeries;
    }
    return registerTimeSeries(defaultLabelValues);
  }

  private Point registerTimeSeries(List<LabelValue> labelValues) {
    return (PointImpl) registerTimeSeries(labelValues, null, null);
  }

  private synchronized <T> TimeSeriesProducer registerTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, @Nullable ToDoubleFunction<T> function) {
    TimeSeriesProducer existingTimeSeries = registeredTimeSeries.get(labelValues);

    if (existingTimeSeries != null) {
      boolean isRegisteredWithPoint = function != null && existingTimeSeries instanceof PointImpl;
      boolean isRegisteredWithPointFunction =
          function == null && existingTimeSeries instanceof PointWithFunctionImpl;

      if (isRegisteredWithPoint || isRegisteredWithPointFunction) {
        throw new IllegalArgumentException(
            "A different time series with the same labels already exists");
      }

      // Return a TimeSeries that are already registered.
      return existingTimeSeries;
    }

    TimeSeriesProducer newTimeSeries =
        function == null
            ? new PointImpl(labelValues)
            : new PointWithFunctionImpl<T>(labelValues, obj, function);

    // Updating the map of time series happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, TimeSeriesProducer> registeredTimeSeriesCopy =
        new HashMap<List<LabelValue>, TimeSeriesProducer>(registeredTimeSeries);
    registeredTimeSeriesCopy.put(labelValues, newTimeSeries);
    registeredTimeSeries = Collections.unmodifiableMap(registeredTimeSeriesCopy);

    return newTimeSeries;
  }

  private void checkValidLabelValues(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");
    checkArgument(labelKeysSize == labelValues.size(), "Incorrect number of labels.");

    for (LabelValue labelValue : labelValues) {
      checkNotNull(labelValue, "labelValues element should not be null.");
    }
  }

  @Override
  public Metric getMetric(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(registeredTimeSeries.size());
    for (Map.Entry<List<LabelValue>, TimeSeriesProducer> entry : registeredTimeSeries.entrySet()) {
      timeSeriesList.add(entry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link io.opencensus.metrics.DoubleGaugeMetric.Point}. */
  public static final class PointImpl extends Point implements TimeSeriesProducer {

    // TODO(mayurkale): Consider to use DoubleAdder here, once we upgrade to Java8.
    private final AtomicDouble value = new AtomicDouble(0);
    private final List<LabelValue> labelValues;

    PointImpl(List<LabelValue> labelValues) {
      this.labelValues = labelValues;
    }

    @Override
    public void inc() {
      inc(1);
    }

    @Override
    public void inc(double amt) {
      value.addAndGet(amt);
    }

    @Override
    public void dec() {
      dec(1);
    }

    @Override
    public void dec(double amt) {
      value.addAndGet(-amt);
    }

    @Override
    public void set(double val) {
      value.set(val);
    }

    @Override
    public TimeSeries getTimeSeries(Clock clock) {
      return TimeSeries.create(
          labelValues,
          Collections.singletonList(
              io.opencensus.metrics.export.Point.create(
                  Value.doubleValue(value.get()), clock.now())),
          null);
    }
  }

  /**
   * Implementation of {@link io.opencensus.metrics.DoubleGaugeMetric.Point} with a obj and
   * function.
   */
  public static final class PointWithFunctionImpl<T> implements TimeSeriesProducer {
    private final List<LabelValue> labelValues;
    @Nullable private final T obj;
    private final ToDoubleFunction<T> function;
    private static final double DEFAULT_VALUE = 0.0;

    PointWithFunctionImpl(
        List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
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
