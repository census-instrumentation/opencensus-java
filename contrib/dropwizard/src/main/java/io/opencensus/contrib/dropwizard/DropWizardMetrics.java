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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Collects DropWizard metrics from a MetricRegistry.
 *
 * <p>A {@link io.opencensus.metrics.export.MetricProducer} that wraps a DropWizardMetrics {@link
 * com.codahale.metrics.MetricRegistry}.
 *
 * @since 0.17
 */
public class DropWizardMetrics extends MetricProducer {

  @DefaultVisibilityForTesting static final String DEFAULT_UNIT = "1";

  @DefaultVisibilityForTesting
  static final LabelKey RATE_LABEL_KEY =
      LabelKey.create("rate", "measures the rate of events over time");

  @DefaultVisibilityForTesting
  static final LabelValue RATE_MEAN_LABEL_VALUE = LabelValue.create("mean_rate");

  @DefaultVisibilityForTesting
  static final LabelValue RATE_ONE_MINUTE_LABEL_VALUE = LabelValue.create("m1_rate");

  @DefaultVisibilityForTesting
  static final LabelValue RATE_FIVE_MINUTE_LABEL_VALUE = LabelValue.create("m5_rate");

  @DefaultVisibilityForTesting
  static final LabelValue RATE_FIFTEEN_MINUTE_LABEL_VALUE = LabelValue.create("m15_rate");

  private final com.codahale.metrics.MetricRegistry metricRegistry;
  private final Clock clock;
  private final Timestamp cumulativeStartTimestamp;

  DropWizardMetrics(com.codahale.metrics.MetricRegistry metricRegistry) {
    Utils.checkNotNull(metricRegistry, "metricRegistry");
    this.metricRegistry = metricRegistry;
    clock = MillisClock.getInstance();
    cumulativeStartTimestamp = clock.now();
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Gauge}.
   *
   * @param dropwizardName the metric name.
   * @param gauge the gauge object to collect.
   * @return a list of {@link Metric}s.
   */
  @SuppressWarnings("rawtypes")
  private List<Metric> collectGauge(String dropwizardName, Gauge gauge) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "value");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, gauge);

    // Figure out which gauge instance and call the right method to get value
    Type type;
    Value value;
    if (gauge.getValue() instanceof Long
        || gauge.getValue() instanceof Integer
        || gauge.getValue() instanceof Short
        || gauge.getValue() instanceof Byte) {
      type = Type.GAUGE_INT64;
      value = Value.longValue(((Number) gauge.getValue()).longValue());
    } else if (gauge.getValue() instanceof Double || gauge.getValue() instanceof Float) {
      type = Type.GAUGE_DOUBLE;
      value = Value.doubleValue(((Number) gauge.getValue()).doubleValue());
    } else {
      // Ignoring Gauge (gauge.getKey()) with unhandled type.
      return new ArrayList<Metric>();
    }

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName, metricDescription, DEFAULT_UNIT, type, Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(Point.create(value, clock.now())),
            null);
    return Arrays.asList(
        Metric.create(
            metricDescriptor, Collections.unmodifiableList(Collections.singletonList(timeSeries))));
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Counter}.
   *
   * @param dropwizardName the metric name.
   * @param counter the counter object to collect.
   * @return a list of {@link Metric}s.
   */
  private List<Metric> collectCounter(String dropwizardName, Counter counter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "count");
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
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(
                Point.create(Value.longValue(counter.getCount()), clock.now())),
            null);
    return Arrays.asList(
        Metric.create(
            metricDescriptor, Collections.unmodifiableList(Collections.singletonList(timeSeries))));
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Meter}.
   *
   * @param dropwizardName the metric name.
   * @param meter the meter object to collect
   * @return a list of {@link Metric}s.
   */
  private List<Metric> collectMeter(String dropwizardName, Meter meter) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "count");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, meter);

    MetricDescriptor countMetricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.GAUGE_INT64,
            Collections.<LabelKey>emptyList());
    TimeSeries countTimeSeries =
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(Point.create(Value.longValue(meter.getCount()), clock.now())),
            null);

    // Collect rate related metric
    metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "rate");
    metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, meter);

    MetricDescriptor rateMetricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            "events/second",
            Type.GAUGE_DOUBLE,
            Collections.unmodifiableList(Collections.singletonList(RATE_LABEL_KEY)));

    List<TimeSeries> timeSeriesList =
        Arrays.asList(
            TimeSeries.create(
                Collections.singletonList(RATE_MEAN_LABEL_VALUE),
                Collections.singletonList(
                    Point.create(Value.doubleValue(meter.getMeanRate()), clock.now())),
                null),
            TimeSeries.create(
                Collections.singletonList(RATE_ONE_MINUTE_LABEL_VALUE),
                Collections.singletonList(
                    Point.create(Value.doubleValue(meter.getOneMinuteRate()), clock.now())),
                null),
            TimeSeries.create(
                Collections.singletonList(RATE_FIVE_MINUTE_LABEL_VALUE),
                Collections.singletonList(
                    Point.create(Value.doubleValue(meter.getFiveMinuteRate()), clock.now())),
                null),
            TimeSeries.create(
                Collections.singletonList(RATE_FIFTEEN_MINUTE_LABEL_VALUE),
                Collections.singletonList(
                    Point.create(Value.doubleValue(meter.getFifteenMinuteRate()), clock.now())),
                null));

    return Arrays.asList(
        Metric.create(
            countMetricDescriptor,
            Collections.unmodifiableList(Collections.singletonList(countTimeSeries))),
        Metric.create(rateMetricDescriptor, Collections.unmodifiableList(timeSeriesList)));
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Histogram}.
   *
   * @param dropwizardName the metric name.
   * @param histogram the histogram object to collect
   * @return a list of {@link Metric}s.
   */
  private List<Metric> collectHistogram(String dropwizardName, Histogram histogram) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "percentile");
    String metricDescription =
        DropWizardUtils.generateFullMetricDescription(dropwizardName, histogram);
    return collectSnapshotAndCount(
        metricName, metricDescription, histogram.getSnapshot(), histogram.getCount());
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Timer}.
   *
   * @param dropwizardName the metric name.
   * @param timer the timer object to collect
   * @return a list of {@link Metric}s.
   */
  private List<Metric> collectTimer(String dropwizardName, Timer timer) {
    String metricName = DropWizardUtils.generateFullMetricName(dropwizardName, "percentile");
    String metricDescription = DropWizardUtils.generateFullMetricDescription(dropwizardName, timer);
    return collectSnapshotAndCount(
        metricName, metricDescription, timer.getSnapshot(), timer.getCount());
  }

  /**
   * Returns a list of {@link Metric}s collected from {@link Snapshot}.
   *
   * @param metricName the metric name.
   * @param metricDescription the metric description.
   * @param codahaleSnapshot the snapshot object to collect
   * @param count the value or count
   * @return a list of {@link Metric}s.
   */
  private List<Metric> collectSnapshotAndCount(
      String metricName,
      String metricDescription,
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

    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.SUMMARY,
            Collections.<LabelKey>emptyList());
    TimeSeries timeSeries =
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(point),
            cumulativeStartTimestamp);

    return Arrays.asList(
        Metric.create(
            metricDescriptor, Collections.unmodifiableList(Collections.singletonList(timeSeries))));
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Collection<Metric> getMetrics() {
    ArrayList<Metric> metrics = new ArrayList<Metric>();

    Iterator<Entry<String, Counter>> counterIterator =
        metricRegistry.getCounters().entrySet().iterator();
    while (counterIterator.hasNext()) {
      Entry<String, Counter> counter = counterIterator.next();
      metrics.addAll(collectCounter(counter.getKey(), counter.getValue()));
    }

    Iterator<Entry<String, Gauge>> gaugeIterator = metricRegistry.getGauges().entrySet().iterator();
    while (gaugeIterator.hasNext()) {
      Entry<String, Gauge> gauge = gaugeIterator.next();
      metrics.addAll(collectGauge(gauge.getKey(), gauge.getValue()));
    }

    Iterator<Entry<String, Meter>> meterIterator = metricRegistry.getMeters().entrySet().iterator();
    while (meterIterator.hasNext()) {
      Entry<String, Meter> meter = meterIterator.next();
      metrics.addAll(collectMeter(meter.getKey(), meter.getValue()));
    }

    Iterator<Entry<String, Histogram>> histogramIterator =
        metricRegistry.getHistograms().entrySet().iterator();
    while (histogramIterator.hasNext()) {
      Entry<String, Histogram> histogram = histogramIterator.next();
      metrics.addAll(collectHistogram(histogram.getKey(), histogram.getValue()));
    }

    Iterator<Entry<String, Timer>> timerIterator = metricRegistry.getTimers().entrySet().iterator();
    while (timerIterator.hasNext()) {
      Entry<String, Timer> timer = timerIterator.next();
      metrics.addAll(collectTimer(timer.getKey(), timer.getValue()));
    }

    return metrics;
  }
}
