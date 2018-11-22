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

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * Collects DropWizard metrics from a list {@link io.dropwizard.metrics5.MetricRegistry}s.
 *
 * <p>A {@link io.opencensus.metrics.export.MetricProducer} that wraps a DropWizardMetrics.
 *
 * @since 0.17
 */
public class DropWizardMetrics extends MetricProducer {
  @DefaultVisibilityForTesting static final String DEFAULT_UNIT = "1";
  private final List<io.dropwizard.metrics5.MetricRegistry> metricRegistryList;
  private final Clock clock;
  private final Timestamp cumulativeStartTimestamp;

  /**
   * Hook the Dropwizard registry into the OpenCensus registry.
   *
   * @param metricRegistryList a list of {@link io.dropwizard.metrics5.MetricRegistry}s.
   * @since 0.17
   */
  public DropWizardMetrics(List<io.dropwizard.metrics5.MetricRegistry> metricRegistryList) {
    Utils.checkListElementNotNull(
        Utils.checkNotNull(metricRegistryList, "metricRegistryList"), "metricRegistry");
    this.metricRegistryList = metricRegistryList;
    clock = MillisClock.getInstance();
    cumulativeStartTimestamp = clock.now();
  }

  /**
   * Returns a {@code Metric} collected from {@link Gauge}.
   *
   * @param dropwizardMetric the metric name.
   * @param gauge the gauge object to collect.
   * @return a {@code Metric}.
   */
  @SuppressWarnings("rawtypes")
  @Nullable
  private Metric collectGauge(MetricName dropwizardMetric, Gauge gauge) {
    // TODO cache dropwizard MetricName -> OC MetricDescriptor, Labels conversion
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardMetric.getKey(), "gauge");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardMetric.getKey(), gauge);
    List<LabelValue> labelValues = new ArrayList<>();
    List<LabelKey> labelKeys = new ArrayList<>();
    for (Map.Entry<String, String> tag: dropwizardMetric.getTags().entrySet()) {
      labelKeys.add(LabelKey.create(tag.getKey(), tag.getKey()));
      labelValues.add(LabelValue.create(tag.getValue()));
    }

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
            metricName, metricDescription, DEFAULT_UNIT, type, labelKeys);
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            labelValues, Point.create(value, clock.now()), null);
    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link Counter}.
   *
   * @param dropwizardMetric the metric name.
   * @param counter the counter object to collect.
   * @return a {@code Metric}.
   */
  private Metric collectCounter(MetricName dropwizardMetric, Counter counter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardMetric.getKey(), "counter");
    List<LabelKey> labelKeys = new ArrayList<>();
    List<LabelValue> labelValues = new ArrayList<>();
    for (Map.Entry<String, String> tag: dropwizardMetric.getTags().entrySet()) {
      labelKeys.add(LabelKey.create(tag.getKey(), tag.getKey()));
      labelValues.add(LabelValue.create(tag.getValue()));
    }

    String metricDescription =
        DropWizardUtils.generateFullMetricDescription(dropwizardMetric.getKey(), counter);

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.GAUGE_INT64,
            labelKeys);
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            labelValues,
            Point.create(Value.longValue(counter.getCount()), clock.now()),
            null);
    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link io.dropwizard.metrics5.Meter}.
   *
   * @param dropwizardMetric the metric name.
   * @param meter the meter object to collect
   * @return a {@code Metric}.
   */
  private Metric collectMeter(MetricName dropwizardMetric, Meter meter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardMetric.getKey(), "meter");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardMetric.getKey(), meter);
    List<LabelValue> labelValues = new ArrayList<>();
    List<LabelKey> labelKeys = new ArrayList<>();
    for (Map.Entry<String, String> tag: dropwizardMetric.getTags().entrySet()) {
      labelKeys.add(LabelKey.create(tag.getKey(), tag.getKey()));
      labelValues.add(LabelValue.create(tag.getValue()));
    }

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.CUMULATIVE_INT64,
            labelKeys);
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            labelValues,
            Point.create(Value.longValue(meter.getCount()), clock.now()),
            null);

    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  /**
   * Returns a {@code Metric} collected from {@link Histogram}.
   *
   * @param dropwizardMetric the metric name.
   * @param histogram the histogram object to collect
   * @return a {@code Metric}.
   */
  private Metric collectHistogram(MetricName dropwizardMetric, Histogram histogram) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardMetric.getKey(), "histogram");
    String metricDescription =
        DropWizardUtils.generateFullMetricDescription(dropwizardMetric.getKey(), histogram);
    List<LabelValue> labelValues = new ArrayList<>();
    List<LabelKey> labelKeys = new ArrayList<>();
    for (Map.Entry<String, String> tag: dropwizardMetric.getTags().entrySet()) {
      labelKeys.add(LabelKey.create(tag.getKey(), tag.getKey()));
      labelValues.add(LabelValue.create(tag.getValue()));
    }
    return collectSnapshotAndCount(
        metricName, metricDescription, labelKeys, labelValues, histogram.getSnapshot(), histogram.getCount());
  }

  /**
   * Returns a {@code Metric} collected from {@link Timer}.
   *
   * @param dropwizardMetric the metric name.
   * @param timer the timer object to collect
   * @return a {@code Metric}.
   */
  private Metric collectTimer(MetricName dropwizardMetric, Timer timer) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardMetric.getKey(), "timer");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardMetric.getKey(), timer);
    List<LabelValue> labelValues = new ArrayList<>();
    List<LabelKey> labelKeys = new ArrayList<>();
    for (Map.Entry<String, String> tag: dropwizardMetric.getTags().entrySet()) {
      labelKeys.add(LabelKey.create(tag.getKey(), tag.getKey()));
      labelValues.add(LabelValue.create(tag.getValue()));
    }
    return collectSnapshotAndCount(
        metricName, metricDescription, labelKeys, labelValues, timer.getSnapshot(), timer.getCount());
  }

  /**
   * Returns a {@code Metric} collected from {@link Snapshot}.
   *
   * @param metricName the metric name.
   * @param metricDescription the metric description.
   * @param labelKeys metric label keys
   * @param labelValues metric label values
   * @param codahaleSnapshot the snapshot object to collect
   * @param count the value or count
   * @return a {@code Metric}.
   */
  private Metric collectSnapshotAndCount(
          String metricName,
          String metricDescription,
          List<LabelKey> labelKeys,
          List<LabelValue> labelValues,
          io.dropwizard.metrics5.Snapshot codahaleSnapshot,
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
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.SUMMARY,
            labelKeys);
    TimeSeries timeSeries =
        TimeSeries.createWithOnePoint(
            labelValues, point, cumulativeStartTimestamp);

    return Metric.createWithOneTimeSeries(metricDescriptor, timeSeries);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Collection<Metric> getMetrics() {
    ArrayList<Metric> metrics = new ArrayList<Metric>();

    for (io.dropwizard.metrics5.MetricRegistry metricRegistry : metricRegistryList) {
      for (Entry<MetricName, Counter> counterEntry : metricRegistry.getCounters().entrySet()) {
        metrics.add(collectCounter(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<MetricName, Gauge> gaugeEntry : metricRegistry.getGauges().entrySet()) {
        Metric metric = collectGauge(gaugeEntry.getKey(), gaugeEntry.getValue());
        if (metric != null) {
          metrics.add(metric);
        }
      }

      for (Entry<MetricName, Meter> counterEntry : metricRegistry.getMeters().entrySet()) {
        metrics.add(collectMeter(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<MetricName, Histogram> counterEntry : metricRegistry.getHistograms().entrySet()) {
        metrics.add(collectHistogram(counterEntry.getKey(), counterEntry.getValue()));
      }

      for (Entry<MetricName, Timer> counterEntry : metricRegistry.getTimers().entrySet()) {
        metrics.add(collectTimer(counterEntry.getKey(), counterEntry.getValue()));
      }
    }

    return metrics;
  }
}
