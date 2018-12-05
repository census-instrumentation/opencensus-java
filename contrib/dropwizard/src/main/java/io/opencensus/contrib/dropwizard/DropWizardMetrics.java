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

package io.opencensus.contrib.dropwizard;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Timer;
import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.internal.DefaultVisibilityForTesting;
import io.opencensus.internal.Utils;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * Collects DropWizard metrics from a list {@link com.codahale.metrics.MetricRegistry}s.
 *
 * <p>A {@link io.opencensus.metrics.export.MetricProducer} that wraps a DropWizardMetrics.
 *
 * @since 0.17
 */
public class DropWizardMetrics extends MetricProducer {

  @DefaultVisibilityForTesting static final String DEFAULT_UNIT = "1";
  @DefaultVisibilityForTesting static final String NS_UNIT = "ns";
  private final List<com.codahale.metrics.MetricRegistry> metricRegistryList;
  private final MetricFilter metricFilter;
  private final Clock clock;
  private final Timestamp cumulativeStartTimestamp;

  /**
   * Hook the Dropwizard registry into the OpenCensus registry.
   *
   * @param metricRegistryList a list of {@link com.codahale.metrics.MetricRegistry}s.
   * @since 0.17
   */
  public DropWizardMetrics(List<com.codahale.metrics.MetricRegistry> metricRegistryList) {
    this(metricRegistryList, MetricFilter.ALL);
  }

  /**
   * Hook the Dropwizard registry into the OpenCensus registry.
   *
   * @param metricRegistryList a list of {@link com.codahale.metrics.MetricRegistry}s.
   * @param metricFilter a filter to choose which metric to export
   * @since 0.19
   */
  public DropWizardMetrics(
      List<com.codahale.metrics.MetricRegistry> metricRegistryList, MetricFilter metricFilter) {
    Utils.checkListElementNotNull(
        Utils.checkNotNull(metricRegistryList, "metricRegistryList"), "metricRegistry");
    this.metricRegistryList = metricRegistryList;
    this.metricFilter = Utils.checkNotNull(metricFilter, "metricFilter");
    clock = MillisClock.getInstance();

    // TODO(mayurkale): consider to add cache map<string, CacheEntry> where CacheEntry is
    // {MetricDescriptor, startTime}
    cumulativeStartTimestamp = clock.now();
  }

  /**
   * Returns a {@code Metric} collected from {@link Gauge}.
   *
   * @param dropwizardName the metric name.
   * @param gauge the gauge object to collect.
   * @return a {@code Metric}.
   */
  @SuppressWarnings("rawtypes")
  @Nullable
  private Metric collectGauge(String dropwizardName, Gauge gauge) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "gauge");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, gauge);

    // Figure out which gauge instance and call the right method to get value
    Type type;
    Value value;

    Object obj = gauge.getValue();
    if (obj instanceof Number) {
      type = Type.GAUGE_DOUBLE;
      value = Value.doubleValue(((Number) obj).doubleValue());
    } else if (obj instanceof Boolean) {
      type = Type.GAUGE_INT64;
      value = Value.longValue(((Boolean) obj) ? 1 : 0);
    } else {
      // Ignoring Gauge (gauge.getKey()) with unhandled type.
      return null;
    }

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName, metricDescription, DEFAULT_UNIT, type, Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            Collections.<LabelValue>emptyList(), Point.create(value, clock.now()), null);
    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link Counter}.
   *
   * @param dropwizardName the metric name.
   * @param counter the counter object to collect.
   * @return a {@code Metric}.
   */
  private Metric collectCounter(String dropwizardName, Counter counter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "counter");
    String metricDescription =
        DropWizardUtils.generateFullMetricDescription(dropwizardName, counter);

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.GAUGE_INT64,
            Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            Collections.<LabelValue>emptyList(),
            Point.create(Value.longValue(counter.getCount()), clock.now()),
            null);
    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link Meter}.
   *
   * @param dropwizardName the metric name.
   * @param meter the meter object to collect
   * @return a {@code Metric}.
   */
  private Metric collectMeter(String dropwizardName, Meter meter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "meter");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, meter);

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.CUMULATIVE_INT64,
            Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            Collections.<LabelValue>emptyList(),
            Point.create(Value.longValue(meter.getCount()), clock.now()),
            cumulativeStartTimestamp);

    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link Histogram}.
   *
   * @param dropwizardName the metric name.
   * @param histogram the histogram object to collect
   * @return a {@code Metric}.
   */
  private Metric collectHistogram(String dropwizardName, Histogram histogram) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "histogram");
    String metricDescription =
        DropWizardUtils.generateFullMetricDescription(dropwizardName, histogram);
    return collectSnapshotAndCount(
        metricName, metricDescription, DEFAULT_UNIT, histogram.getSnapshot(), histogram.getCount());
  }

  /**
   * Returns a {@code Metric} collected from {@link Timer}.
   *
   * @param dropwizardName the metric name.
   * @param timer the timer object to collect
   * @return a {@code Metric}.
   */
  private Metric collectTimer(String dropwizardName, Timer timer) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "timer");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, timer);
    return collectSnapshotAndCount(
        metricName, metricDescription, NS_UNIT, timer.getSnapshot(), timer.getCount());
  }

  /**
   * Returns a {@code Metric} collected from {@link Snapshot}.
   *
   * @param metricName the metric name.
   * @param metricDescription the metric description.
   * @param unit the metric descriptor unit.
   * @param codahaleSnapshot the snapshot object to collect
   * @param count the value or count
   * @return a {@code Metric}.
   */
  private Metric collectSnapshotAndCount(
      String metricName,
      String metricDescription,
      String unit,
      com.codahale.metrics.Snapshot codahaleSnapshot,
      long count) {
    List<ValueAtPercentile> valueAtPercentiles =
        Arrays.asList(
            ValueAtPercentile.create(50.0, codahaleSnapshot.getMedian()),
            ValueAtPercentile.create(75.0, codahaleSnapshot.get75thPercentile()),
            ValueAtPercentile.create(98.0, codahaleSnapshot.get98thPercentile()),
            ValueAtPercentile.create(99.0, codahaleSnapshot.get99thPercentile()),
            ValueAtPercentile.create(99.9, codahaleSnapshot.get999thPercentile()));

    Snapshot snapshot = Snapshot.create((long) codahaleSnapshot.size(), 0.0, valueAtPercentiles);
    Point point =
        Point.create(Value.summaryValue(Summary.create(count, 0.0, snapshot)), clock.now());

    // TODO(mayurkale): OPTIMIZATION: Cache the MetricDescriptor objects.
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName, metricDescription, unit, Type.SUMMARY, Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            Collections.<LabelValue>emptyList(), point, cumulativeStartTimestamp);

    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Collection<Metric> getMetrics() {
    ArrayList<Metric> metrics = new ArrayList<Metric>();

    for (com.codahale.metrics.MetricRegistry metricRegistry : metricRegistryList) {
      for (Entry<String, Counter> counterEntry :
          metricRegistry.getCounters(metricFilter).entrySet()) {
        metrics.add(collectCounter(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<String, Gauge> gaugeEntry : metricRegistry.getGauges(metricFilter).entrySet()) {
        Metric metric = collectGauge(gaugeEntry.getKey(), gaugeEntry.getValue());
        if (metric != null) {
          metrics.add(metric);
        }
      }

      for (Entry<String, Meter> counterEntry : metricRegistry.getMeters(metricFilter).entrySet()) {
        metrics.add(collectMeter(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<String, Histogram> counterEntry :
          metricRegistry.getHistograms(metricFilter).entrySet()) {
        metrics.add(collectHistogram(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<String, Timer> counterEntry : metricRegistry.getTimers(metricFilter).entrySet()) {
        metrics.add(collectTimer(counterEntry.getKey(), counterEntry.getValue()));
      }
    }

    return metrics;
  }
}
