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
import io.opencensus.common.ToLongFunction;
import io.opencensus.implcore.internal.Utils;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Implementation of {@link DerivedLongGauge}. */
public final class DerivedLongGaugeImpl extends DerivedLongGauge implements Meter {
  private final MetricDescriptor metricDescriptor;
  private final int labelKeysSize;
  private final List<LabelValue> constantLabelValues;

  private volatile Map<List<LabelValue>, PointWithFunction<?>> registeredPoints =
      Collections.<List<LabelValue>, PointWithFunction<?>>emptyMap();

  DerivedLongGaugeImpl(
      String name,
      String description,
      String unit,
      List<LabelKey> labelKeys,
      Map<LabelKey, LabelValue> constantLabels) {
    List<LabelValue> constantLabelValues = new ArrayList<LabelValue>();
    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    for (Entry<LabelKey, LabelValue> label : constantLabels.entrySet()) {
      // Ensure constant label keys and values are in the same order.
      allKeys.add(label.getKey());
      constantLabelValues.add(label.getValue());
    }
    labelKeysSize = allKeys.size();
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_INT64, allKeys);
    this.constantLabelValues = Collections.unmodifiableList(constantLabelValues);
  }

  @Override
  public synchronized <T> void createTimeSeries(
      List<LabelValue> labelValues,
      @javax.annotation.Nullable T obj,
      ToLongFunction</*@Nullable*/ T> function) {
    checkNotNull(function, "function");
    Utils.checkListElementNotNull(checkNotNull(labelValues, "labelValues"), "labelValue");
    List<LabelValue> labelValuesCopy = new ArrayList<LabelValue>(labelValues);
    labelValuesCopy.addAll(constantLabelValues);

    checkArgument(
        labelKeysSize == labelValuesCopy.size(),
        "Label Keys and Label Values don't have same size.");

    PointWithFunction<?> existingPoint =
        registeredPoints.get(Collections.unmodifiableList(labelValuesCopy));
    if (existingPoint != null) {
      throw new IllegalArgumentException(
          "A different time series with the same labels already exists.");
    }

    PointWithFunction<T> newPoint = new PointWithFunction<T>(labelValuesCopy, obj, function);
    // Updating the map of time series happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, PointWithFunction<?>> registeredPointsCopy =
        new LinkedHashMap<List<LabelValue>, PointWithFunction<?>>(registeredPoints);
    registeredPointsCopy.put(labelValuesCopy, newPoint);
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  @Override
  public synchronized void removeTimeSeries(List<LabelValue> labelValues) {
    List<LabelValue> labelValuesCopy =
        new ArrayList<LabelValue>(checkNotNull(labelValues, "labelValues"));
    labelValuesCopy.addAll(constantLabelValues);

    Map<List<LabelValue>, PointWithFunction<?>> registeredPointsCopy =
        new LinkedHashMap<List<LabelValue>, PointWithFunction<?>>(registeredPoints);
    if (registeredPointsCopy.remove(labelValuesCopy) == null) {
      // The element not present, no need to update the current map of time series.
      return;
    }
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  @Override
  public synchronized void clear() {
    registeredPoints = Collections.<List<LabelValue>, PointWithFunction<?>>emptyMap();
  }

  @javax.annotation.Nullable
  @Override
  public Metric getMetric(Clock clock) {
    Map<List<LabelValue>, PointWithFunction<?>> currentRegisteredPoints = registeredPoints;
    if (currentRegisteredPoints.isEmpty()) {
      return null;
    }

    if (currentRegisteredPoints.size() == 1) {
      PointWithFunction<?> point = currentRegisteredPoints.values().iterator().next();
      return Metric.createWithOneTimeSeries(metricDescriptor, point.getTimeSeries(clock));
    }

    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(currentRegisteredPoints.size());
    for (Map.Entry<List<LabelValue>, PointWithFunction<?>> entry :
        currentRegisteredPoints.entrySet()) {
      timeSeriesList.add(entry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link PointWithFunction} with an object and a callback function. */
  public static final class PointWithFunction<T> {
    private final TimeSeries defaultTimeSeries;
    @javax.annotation.Nullable private final WeakReference<T> ref;
    private final ToLongFunction</*@Nullable*/ T> function;

    PointWithFunction(
        List<LabelValue> labelValues,
        @javax.annotation.Nullable T obj,
        ToLongFunction</*@Nullable*/ T> function) {
      defaultTimeSeries = TimeSeries.create(labelValues);
      ref = obj != null ? new WeakReference<T>(obj) : null;
      this.function = function;
    }

    private TimeSeries getTimeSeries(Clock clock) {
      final T obj = ref != null ? ref.get() : null;
      long value = function.applyAsLong(obj);
      return defaultTimeSeries.setPoint(Point.create(Value.longValue(value), clock.now()));
    }
  }
}
