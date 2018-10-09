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
import io.opencensus.implcore.internal.Utils;
import io.opencensus.metrics.DoubleGauge;
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

/** Implementation of {@link DoubleGauge}. */
public final class DoubleGaugeImpl extends DoubleGauge implements Meter {
  private final MetricDescriptor metricDescriptor;
  private volatile Map<List<LabelValue>, TimeSeriesProducer> registeredTimeSeries =
      Maps.newHashMap();
  private final int labelKeysSize;
  private final List<LabelValue> defaultLabelValues;

  DoubleGaugeImpl(String name, String description, String unit, List<LabelKey> labelKeys) {
    labelKeysSize = labelKeys.size();
    defaultLabelValues = new ArrayList<LabelValue>(labelKeysSize);
    this.metricDescriptor =
        MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys);
  }

  @Override
  public Point getOrCreateTimeSeries(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");
    checkArgument(labelKeysSize == labelValues.size(), "Incorrect number of labels.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");

    return registerTimeSeries(new ArrayList<LabelValue>(labelValues));
  }

  @Override
  public Point getDefaultTimeSeries() {
    return registerTimeSeries(defaultLabelValues);
  }

  @Override
  public void removeTimeSeries(List<LabelValue> labelValues) {
    checkNotNull(labelValues, "labelValues should not be null.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");

    remove(labelValues);
  }

  @Override
  public synchronized void clear() {
    registeredTimeSeries.clear();
  }

  private synchronized void remove(List<LabelValue> labelValues) {
    Map<List<LabelValue>, TimeSeriesProducer> registeredTimeSeriesCopy =
        new HashMap<List<LabelValue>, TimeSeriesProducer>(registeredTimeSeries);
    if (registeredTimeSeriesCopy.remove(labelValues) == null) {
      // The element not present, no need to update the current map of time series.
      return;
    }
    registeredTimeSeries = Collections.unmodifiableMap(registeredTimeSeriesCopy);
  }

  private synchronized <T> Point registerTimeSeries(List<LabelValue> labelValues) {
    TimeSeriesProducer existingTimeSeries = registeredTimeSeries.get(labelValues);
    if (existingTimeSeries != null) {
      // Return a TimeSeries that are already registered.
      return (PointImpl) existingTimeSeries;
    }

    TimeSeriesProducer newTimeSeries = new PointImpl(labelValues);
    // Updating the map of time series happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Map<List<LabelValue>, TimeSeriesProducer> registeredTimeSeriesCopy =
        new HashMap<List<LabelValue>, TimeSeriesProducer>(registeredTimeSeries);
    registeredTimeSeriesCopy.put(labelValues, newTimeSeries);
    registeredTimeSeries = Collections.unmodifiableMap(registeredTimeSeriesCopy);

    return (PointImpl) newTimeSeries;
  }

  @Override
  public Metric getMetric(Clock clock) {
    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>(registeredTimeSeries.size());
    for (Map.Entry<List<LabelValue>, TimeSeriesProducer> entry : registeredTimeSeries.entrySet()) {
      timeSeriesList.add(entry.getValue().getTimeSeries(clock));
    }
    return Metric.create(metricDescriptor, timeSeriesList);
  }

  /** Implementation of {@link DoubleGauge.Point}. */
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
}
