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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

/** Implementation of {@link DoubleGaugeMetric}. */
public final class DoubleGaugeMetricImpl extends DoubleGaugeMetric implements Meter {
  private final MetricDescriptor metricDescriptor;
  private final ConcurrentMap<List<LabelValue>, Point> pointConcurrentHashMap =
      new ConcurrentHashMap<List<LabelValue>, Point>();
  private final List<LabelKey> labelKeys;

  DoubleGaugeMetricImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
    this.labelKeys = labelKeys;
  }

  @Override
  public Point addPoint(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");
    checkArgument(labelKeys.size() == labelValues.size(), "Incorrect number of labels.");

    return addAndGetPoint(labelValues, null, null);
  }

  @Override
  public <T> void addPoint(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
    checkNotNull(labelValues, "labelValues should not be null.");
    checkArgument(labelKeys.size() == labelValues.size(), "Incorrect number of labels.");

    addAndGetPoint(labelValues, obj, checkNotNull(function, "function"));
  }

  @Override
  public Point getDefaultPoint() {
    List<LabelValue> labelValues = new ArrayList<LabelValue>(labelKeys.size());

    return addAndGetPoint(labelValues, null, null);
  }

  private <T> Point addAndGetPoint(
      List<LabelValue> labelValues, @Nullable T obj, @Nullable ToDoubleFunction<T> function) {
    Point currentPoint = pointConcurrentHashMap.get(labelValues);
    if (currentPoint != null) {
      return currentPoint;
    } else {
      Point newPoint =
          function != null
              ? new PointImpl<T>(labelValues, obj, function)
              : new PointImpl<T>(labelValues);

      pointConcurrentHashMap.put(labelValues, newPoint);
      return newPoint;
    }
  }

  private List<TimeSeries> getTimeSeries(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(pointConcurrentHashMap.size());

    for (Map.Entry<List<LabelValue>, Point> pointEntry : pointConcurrentHashMap.entrySet()) {
      timeSeriesList.add(
          TimeSeries.create(
              pointEntry.getKey(),
              Collections.singletonList(
                  io.opencensus.metrics.export.Point.create(
                      Value.doubleValue(((PointImpl) pointEntry.getValue()).get()), clock.now())),
              null));
    }
    return timeSeriesList;
  }

  @Override
  public Metric getMetric(Clock clock) {
    return Metric.create(metricDescriptor, getTimeSeries(clock));
  }

  /** Implementation of {@link io.opencensus.metrics.DoubleGaugeMetric.Point}. */
  public static final class PointImpl<T> extends Point {
    private final AtomicDouble value = new AtomicDouble(0);
    private final List<LabelValue> labelValues;
    @Nullable private T obj;
    @Nullable private ToDoubleFunction<T> function;

    PointImpl(List<LabelValue> labelValues) {
      this.labelValues = Collections.unmodifiableList(labelValues);
    }

    PointImpl(List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
      this.labelValues = Collections.unmodifiableList(labelValues);
      this.obj = obj;
      this.function = function;
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

    public double get() {
      return obj != null && function != null ? function.applyAsDouble(obj) : value.get();
    }

    @Override
    public void set(double val) {
      value.set(val);
    }

    public List<LabelValue> getLabelValues() {
      return labelValues;
    }
  }
}
