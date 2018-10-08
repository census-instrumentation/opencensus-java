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
import io.opencensus.common.Clock;
import io.opencensus.common.ToLongFunction;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGaugeMetric;
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
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/** Implementation of {@link LongGaugeMetric}. */
public final class LongGaugeMetricImpl extends LongGaugeMetric implements Meter {
  private final MetricDescriptor metricDescriptor;
  private volatile Map<List<LabelValue>, TimeSeriesCollector> registeredPoints = Maps.newHashMap();
  private final int labelKeysSize;
  private final List<LabelValue> defaultLabelValues;

  LongGaugeMetricImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    labelKeysSize = labelKeys.size();
    defaultLabelValues = new ArrayList<LabelValue>(labelKeysSize);
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_INT64, labelKeys);
  }

  @Override
  public Point addPoint(List<LabelValue> labelValues) {
    checkValidLabelValues(labelValues);

    return registerPoint(new ArrayList<LabelValue>(labelValues));
  }

  @Override
  public <T> void addPoint(
      List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
    checkValidLabelValues(labelValues);

    registerPoint(new ArrayList<LabelValue>(labelValues), obj, checkNotNull(function, "function"));
  }

  @Override
  public synchronized Point getDefaultPoint() {
    // lock free default retrieve point
    TimeSeriesCollector existingPoint = registeredPoints.get(defaultLabelValues);
    if (existingPoint != null) {
      return (PointImpl) existingPoint;
    }
    return registerPoint(defaultLabelValues);
  }

  private Point registerPoint(List<LabelValue> labelValues) {
    return (PointImpl) registerPoint(labelValues, null, null);
  }

  private synchronized <T> TimeSeriesCollector registerPoint(
      List<LabelValue> labelValues, @Nullable T obj, @Nullable ToLongFunction<T> function) {
    TimeSeriesCollector existingPoint = registeredPoints.get(labelValues);
    if (existingPoint != null) {
      if ((function != null && existingPoint instanceof PointImpl)
          || (function == null && existingPoint instanceof PointWithFunctionImpl)) {
        throw new IllegalArgumentException("A different point with the same labels already exists");
      }
      return existingPoint;
    }

    TimeSeriesCollector newPoint =
        function == null
            ? new PointImpl(labelValues)
            : new PointWithFunctionImpl<T>(labelValues, obj, function);

    // Updating the map of Points happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, TimeSeriesCollector> registeredPointsCopy =
        new HashMap<List<LabelValue>, TimeSeriesCollector>(registeredPoints);
    registeredPointsCopy.put(labelValues, newPoint);
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);

    return newPoint;
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
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(registeredPoints.size());
    for (Map.Entry<List<LabelValue>, TimeSeriesCollector> pointEntry : registeredPoints.entrySet()) {
      timeSeriesList.add(pointEntry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link io.opencensus.metrics.LongGaugeMetric.Point}. */
  public static final class PointImpl extends Point implements TimeSeriesCollector {

    // TODO(mayurkale): Consider to use LongAdder here, once we upgrade to Java8.
    private final AtomicLong value = new AtomicLong(0);
    private final List<LabelValue> labelValues;

    PointImpl(List<LabelValue> labelValues) {
      this.labelValues = labelValues;
    }

    @Override
    public void inc() {
      inc(1);
    }

    @Override
    public void inc(long amt) {
      value.addAndGet(amt);
    }

    @Override
    public void dec() {
      dec(1);
    }

    @Override
    public void dec(long amt) {
      value.addAndGet(-amt);
    }

    @Override
    public void set(long val) {
      value.set(val);
    }

    @Override
    public TimeSeries getTimeSeries(Clock clock) {
      return TimeSeries.create(
          labelValues,
          Collections.singletonList(
              io.opencensus.metrics.export.Point.create(Value.longValue(value.get()), clock.now())),
          null);
    }
  }

  /**
   * Implementation of {@link io.opencensus.metrics.LongGaugeMetric.Point} with a obj and function.
   */
  public static final class PointWithFunctionImpl<T> implements TimeSeriesCollector {
    private final List<LabelValue> labelValues;
    @Nullable private final T obj;
    private final ToLongFunction<T> function;

    PointWithFunctionImpl(
        List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
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
                  Value.longValue(function.applyAsLong(obj)), clock.now())),
          null);
    }
  }
}
