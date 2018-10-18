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
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Implementation of {@link DerivedDoubleGauge}. */
public final class DerivedDoubleGaugeImpl extends DerivedDoubleGauge implements Meter {
  private final MetricDescriptor metricDescriptor;
  private final int labelKeysSize;

  @SuppressWarnings("rawtypes")
  private volatile Map<List<LabelValue>, PointWithFunction> registeredPoints =
      Collections.<List<LabelValue>, PointWithFunction>emptyMap();

  DerivedDoubleGaugeImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    labelKeysSize = labelKeys.size();
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public synchronized <T> void createTimeSeries(
      List<LabelValue> labelValues,
      /*@Nullable*/ T obj,
      ToDoubleFunction</*@Nullable*/ T> function) {
    Utils.checkListElementNotNull(
        checkNotNull(labelValues, "labelValues"), "labelValue element should not be null.");
    checkArgument(labelKeysSize == labelValues.size(), "Incorrect number of labels.");
    checkNotNull(function, "function");

    List<LabelValue> labelValuesCopy =
        Collections.<LabelValue>unmodifiableList(new ArrayList<LabelValue>(labelValues));

    PointWithFunction existingPoint = registeredPoints.get(labelValuesCopy);
    if (existingPoint != null) {
      throw new IllegalArgumentException(
          "A different time series with the same labels already exists.");
    }

    PointWithFunction newPoint = new PointWithFunction<T>(labelValuesCopy, obj, function);
    // Updating the map of time series happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, PointWithFunction> registeredPointsCopy =
        new LinkedHashMap<List<LabelValue>, PointWithFunction>(registeredPoints);
    registeredPointsCopy.put(labelValuesCopy, newPoint);
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public synchronized void removeTimeSeries(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues");

    Map<List<LabelValue>, PointWithFunction> registeredPointsCopy =
        new LinkedHashMap<List<LabelValue>, PointWithFunction>(registeredPoints);
    if (registeredPointsCopy.remove(labelValues) == null) {
      // The element not present, no need to update the current map of time series.
      return;
    }
    registeredPoints = Collections.unmodifiableMap(registeredPointsCopy);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public synchronized void clear() {
    registeredPoints = Collections.<List<LabelValue>, PointWithFunction>emptyMap();
  }

  /*@Nullable*/
  @Override
  @SuppressWarnings("rawtypes")
  public Metric getMetric(Clock clock) {
    Map<List<LabelValue>, PointWithFunction> currentRegisteredPoints = registeredPoints;
    if (currentRegisteredPoints.isEmpty()) {
      return null;
    }

    if (currentRegisteredPoints.size() == 1) {
      PointWithFunction point = currentRegisteredPoints.values().iterator().next();
      return Metric.createWithOneTimeSeries(metricDescriptor, point.getTimeSeries(clock));
    }

    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(currentRegisteredPoints.size());
    for (Map.Entry<List<LabelValue>, PointWithFunction> entry :
        currentRegisteredPoints.entrySet()) {
      timeSeriesList.add(entry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link PointWithFunction} with an object and a callback function. */
  public static final class PointWithFunction<T> {
    private final List<LabelValue> labelValues;
    @javax.annotation.Nullable private final WeakReference<T> ref;
    private final ToDoubleFunction</*@Nullable*/ T> function;

    PointWithFunction(
        List<LabelValue> labelValues,
        /*@Nullable*/ T obj,
        ToDoubleFunction</*@Nullable*/ T> function) {
      this.labelValues = labelValues;
      ref = obj != null ? new WeakReference<T>(obj) : null;
      this.function = function;
    }

    private TimeSeries getTimeSeries(Clock clock) {
      final T obj = ref != null ? ref.get() : null;
      double value = function.applyAsDouble(obj);

      // TODO(mayurkale): OPTIMIZATION: Avoid re-evaluate the labelValues all the time (issue#1490).
      return TimeSeries.createWithOnePoint(
          labelValues, Point.create(Value.doubleValue(value), clock.now()), null);
    }
  }
}
