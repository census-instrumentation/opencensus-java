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

import io.opencensus.common.Clock;
import io.opencensus.common.ToLongFunction;
import io.opencensus.internal.Utils;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGaugeMetric;
import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.MetricDescriptor.Type;
import io.opencensus.metrics.Point;
import io.opencensus.metrics.TimeSeries;
import io.opencensus.metrics.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class LongGaugeMetricImp<T> extends LongGaugeMetric<T> {

  private final MetricDescriptor metricDescriptor;
  private final ConcurrentMap<List<LabelValue>, DataPoint<T>> dataPointConcurrentMap =
      new ConcurrentHashMap<List<LabelValue>, DataPoint<T>>();
  private final List<LabelKey> labelKeys;

  LongGaugeMetricImp(String name, String description, String unit, List<LabelKey> labelKeys) {

    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
    this.labelKeys = labelKeys;
  }

  @Override
  public DataPoint<T> addDataPoint(List<LabelValue> labelValues) {
    Utils.checkNotNull(labelValues, "labelValues");
    Utils.checkArgument(labelKeys.size() == labelValues.size(), "Incorrect number of labels.");

    return addAndGetDefaultDataPoint(labelValues, null, null);
  }

  @Override
  public DataPoint<T> addDataPoint(
      List<LabelValue> labelValues, T obj, ToLongFunction<T> function) {
    Utils.checkNotNull(labelValues, "labelValues");
    Utils.checkArgument(labelKeys.size() == labelValues.size(), "Incorrect number of labels.");

    return addAndGetDefaultDataPoint(
        labelValues, Utils.checkNotNull(obj, "obj"), Utils.checkNotNull(function, "function"));
  }

  @Override
  public DataPoint<T> getDefaultDataPoint() {
    List<LabelValue> labelValues = new ArrayList<LabelValue>(labelKeys.size());

    return addAndGetDefaultDataPoint(labelValues, null, null);
  }

  private LongGaugeMetric.DataPoint<T> addAndGetDefaultDataPoint(
      List<LabelValue> labelValues, T obj, ToLongFunction<T> function) {
    DataPoint<T> currentDataPoint = dataPointConcurrentMap.get(labelValues);
    if (currentDataPoint != null) {
      return currentDataPoint;
    } else {
      DataPoint<T> newDataPoint = new DataPointImp<T>(labelValues, obj, function);
      dataPointConcurrentMap.put(labelValues, newDataPoint);
      return newDataPoint;
    }
  }

  List<TimeSeries> getTimeSeries(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(dataPointConcurrentMap.size());

    for (Map.Entry<List<LabelValue>, DataPoint<T>> dataPoint : dataPointConcurrentMap.entrySet()) {

      timeSeriesList.add(
          TimeSeries.create(
              dataPoint.getKey(),
              Collections.singletonList(
                  Point.create(Value.longValue(dataPoint.getValue().get()), clock.now())),
              null));
    }
    return timeSeriesList;
  }

  @Override
  public Metric getMetric(Clock clock) {
    return Metric.create(metricDescriptor, getTimeSeries(clock));
  }

  public static final class DataPointImp<T> extends DataPoint<T> {

    private final AtomicLong value = new AtomicLong(0);
    private final List<LabelValue> labelValues;
    private final T obj;
    private final ToLongFunction<T> function;

    DataPointImp(List<LabelValue> labelValues) {
      this(Collections.unmodifiableList(labelValues), null, null);
    }

    DataPointImp(List<LabelValue> labelValues, T obj, ToLongFunction<T> function) {
      this.labelValues = Collections.unmodifiableList(labelValues);
      this.obj = obj;
      this.function = function;
    }

    // Increment the gauge by 1.
    @Override
    public void inc() {
      inc(1);
    }

    // Increment the gauge by the given amount.
    @Override
    public void inc(long amt) {
      value.addAndGet(amt);
    }

    // Decrement the gauge by 1.
    @Override
    public void dec() {
      dec(1);
    }

    // Decrement the gauge by the given amount.
    @Override
    public void dec(long amt) {
      value.addAndGet(-amt);
    }

    // Get the value of the gauge.
    @Override
    public long get() {
      return obj != null ? function.applyAsLong(obj) : value.get();
    }

    // Set the gauge to the given value.
    @Override
    public void set(long val) {
      value.set(val);
    }
  }
}
