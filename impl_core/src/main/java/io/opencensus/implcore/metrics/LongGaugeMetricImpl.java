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
  private volatile Map<List<LabelValue>, Point> points = Maps.newHashMap();
  private final int labelKeysSize;

  LongGaugeMetricImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    this.labelKeysSize = labelKeys.size();
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_INT64, labelKeys);
  }

  @Override
  public Point addPoint(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");

    List<LabelValue> labelValuesCopy = new ArrayList<LabelValue>(labelValues); // Deep copy.
    checkArgument(labelKeysSize == labelValuesCopy.size(), "Incorrect number of labels.");

    return addAndGetPoint(labelValuesCopy, null, null);
  }

  @Override
  public <T> void addPoint(
      List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
    checkNotNull(labelValues, "labelValues should not be null.");

    List<LabelValue> labelValuesCopy = new ArrayList<LabelValue>(labelValues); // Deep copy.
    checkArgument(labelKeysSize == labelValuesCopy.size(), "Incorrect number of labels.");

    addAndGetPoint(labelValuesCopy, obj, checkNotNull(function, "function"));
  }

  @Override
  public Point getDefaultPoint() {
    List<LabelValue> labelValues = new ArrayList<LabelValue>(labelKeysSize);
    Point existingPoint = points.get(labelValues);
    if (existingPoint != null) {
      return existingPoint;
    }

    return addAndGetPoint(labelValues, null, null);
  }

  private synchronized <T> Point addAndGetPoint(
      List<LabelValue> labelValues, @Nullable T obj, @Nullable ToLongFunction<T> function) {

    Point existingPoint = points.get(labelValues);
    if (existingPoint != null) {
      throw new IllegalArgumentException("A different point with the same labels already exists");
    }
    // Updating the map of Points happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, Point> pointsCopy = new HashMap<List<LabelValue>, Point>(points);
    Point newPoint =
        function != null
            ? new PointImpl<T>(labelValues, obj, function)
            : new PointImpl<T>(labelValues);

    pointsCopy.put(labelValues, newPoint);
    points = Collections.unmodifiableMap(pointsCopy);
    return newPoint;
  }

  private List<TimeSeries> getTimeSeries(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(points.size());

    for (Map.Entry<List<LabelValue>, Point> pointEntry : points.entrySet()) {

      timeSeriesList.add(
          TimeSeries.create(
              pointEntry.getKey(),
              Collections.singletonList(
                  io.opencensus.metrics.export.Point.create(
                      Value.longValue(((PointImpl) pointEntry.getValue()).get()), clock.now())),
              null));
    }
    return timeSeriesList;
  }

  @Override
  public Metric getMetric(Clock clock) {
    return Metric.create(metricDescriptor, getTimeSeries(clock));
  }

  /** Implementation of {@link io.opencensus.metrics.LongGaugeMetric.Point}. */
  public static final class PointImpl<T> extends Point {
    private final AtomicLong value = new AtomicLong(0);
    private final List<LabelValue> labelValues;
    @Nullable private T obj;
    @Nullable private ToLongFunction<T> function;

    PointImpl(List<LabelValue> labelValues) {
      this.labelValues = labelValues;
    }

    PointImpl(List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
      this.labelValues = labelValues;
      this.obj = obj;
      this.function = function;
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

    public long get() {
      return obj != null && function != null ? function.applyAsLong(obj) : value.get();
    }

    @Override
    public void set(long val) {
      value.set(val);
    }

    public List<LabelValue> getLabelValues() {
      return labelValues;
    }
  }
}
