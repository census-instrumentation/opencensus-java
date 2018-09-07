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
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.internal.DefaultVisibilityForTesting;
import io.opencensus.internal.Utils;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.MetricDescriptor.Type;
import io.opencensus.metrics.MetricProducer;
import io.opencensus.metrics.Point;
import io.opencensus.metrics.TimeSeries;
import io.opencensus.metrics.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Collect DropWizard metrics from a MetricRegistry.
 *
 * <p>A {@link MetricProducer} that wraps a DropWizardMetrics {@link
 * com.codahale.metrics.MetricRegistry}.
 *
 * @since 0.16
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

  @DefaultVisibilityForTesting
  static final LabelKey QUANTILE_LABEL_KEY =
      LabelKey.create(
          "quantile", "measures statistical distribution of values over a stream of data");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_50_LABEL_VALUE = LabelValue.create("PCT_50");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_75_LABEL_VALUE = LabelValue.create("PCT_75");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_95_LABEL_VALUE = LabelValue.create("PCT_95");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_98_LABEL_VALUE = LabelValue.create("PCT_98");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_99_LABEL_VALUE = LabelValue.create("PCT_99");

  @DefaultVisibilityForTesting
  static final LabelValue QUANTILE_999_LABEL_VALUE = LabelValue.create("PCT_999");

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
   * Collect and transform a gauge metric as a opencensus metric.
   *
   * @param name metric name
   * @param gauge Gauge object to collect
   * @return a list of metrics.
   */
  @SuppressWarnings("rawtypes")
  private List<Metric> collectGauge(String name, Gauge gauge) {
    String metricName = DropWizardUtils.generateFullMetricName(name, "value");
    String metricDescription = DropWizardUtils.generateFullMetricDescription("Gauge", "value");

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
    return Arrays.asList(Metric.create(metricDescriptor, Collections.singletonList(timeSeries)));
  }

  /**
   * Collect and transform a counter metric as a opencensus metric.
   *
   * @param name metric name
   * @param counter Counter object to collect
   * @return a list of metrics.
   */
  private List<Metric> collectCounter(String name, Counter counter) {
    String metricName = DropWizardUtils.generateFullMetricName(name, "count");
    String metricDescription = DropWizardUtils.generateFullMetricDescription("Counter", "count");

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
    return Arrays.asList(Metric.create(metricDescriptor, Collections.singletonList(timeSeries)));
  }

  /**
   * Collect and transform a meter metric as a opencensus metric.
   *
   * @param name metric name
   * @param meter Meter object to collect
   * @return a list of metrics.
   */
  private List<Metric> collectMeter(String name, Meter meter) {
    String metricName = DropWizardUtils.generateFullMetricName(name, "count");
    String metricDescription = DropWizardUtils.generateFullMetricDescription("Meter", "count");

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
    metricName = DropWizardUtils.generateFullMetricName(name, "rate");
    metricDescription = DropWizardUtils.generateFullMetricDescription("Meter", "rate");

    MetricDescriptor rateMetricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            "events/second",
            Type.GAUGE_DOUBLE,
            Collections.unmodifiableList(Collections.singletonList(RATE_LABEL_KEY)));

    List<LabelValue> rateLabelValues =
        Arrays.asList(
            RATE_MEAN_LABEL_VALUE,
            RATE_ONE_MINUTE_LABEL_VALUE,
            RATE_FIVE_MINUTE_LABEL_VALUE,
            RATE_FIFTEEN_MINUTE_LABEL_VALUE);

    List<Point> ratePoints =
        Arrays.asList(
            Point.create(Value.doubleValue(meter.getMeanRate()), clock.now()),
            Point.create(Value.doubleValue(meter.getOneMinuteRate()), clock.now()),
            Point.create(Value.doubleValue(meter.getFiveMinuteRate()), clock.now()),
            Point.create(Value.doubleValue(meter.getFifteenMinuteRate()), clock.now()));

    TimeSeries rateTimeSeries = TimeSeries.create(rateLabelValues, ratePoints, null);

    return Arrays.asList(
        Metric.create(countMetricDescriptor, Collections.singletonList(countTimeSeries)),
        Metric.create(rateMetricDescriptor, Collections.singletonList(rateTimeSeries)));
  }

  /**
   * Collect and transform a histogram metric as a opencensus metric.
   *
   * @param name metric name
   * @param histogram Histogram object to collect
   * @return a list of metrics.
   */
  private List<Metric> collectHistogram(String name, Histogram histogram) {
    return collectSnapshotAndCount(name, histogram.getSnapshot(), histogram.getCount());
  }

  /**
   * Collect and transform a timer metric as a opencensus metric.
   *
   * @param name metric name
   * @param timer Timer object to collect
   * @return a list of metrics.
   */
  private List<Metric> collectTimer(String name, Timer timer) {
    return collectSnapshotAndCount(name, timer.getSnapshot(), timer.getCount());
  }

  /**
   * Collect and transform a timer and histogram snapshot as a opencensus metric.
   *
   * @param name metric name
   * @param snapshot Snapshot object to collect
   * @param count the value or count
   * @return a list of metrics.
   */
  private List<Metric> collectSnapshotAndCount(String name, Snapshot snapshot, long count) {
    String metricName = DropWizardUtils.generateFullMetricName(name, "count");
    String metricDescription = DropWizardUtils.generateFullMetricDescription("Snapshot", "count");

    MetricDescriptor countMetricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.CUMULATIVE_INT64,
            Collections.<LabelKey>emptyList());
    TimeSeries countTimeSeries =
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(Point.create(Value.longValue(count), clock.now())),
            cumulativeStartTimestamp);

    // Collect quantile related metric
    metricName = DropWizardUtils.generateFullMetricName(name, "quantile");
    metricDescription = DropWizardUtils.generateFullMetricDescription("Snapshot", "quantile");

    MetricDescriptor quantileMetricDescriptor =
        MetricDescriptor.create(
            metricName,
            metricDescription,
            DEFAULT_UNIT,
            Type.GAUGE_DOUBLE,
            Collections.unmodifiableList(Collections.singletonList(QUANTILE_LABEL_KEY)));

    List<LabelValue> rateLabelValues =
        Arrays.asList(
            QUANTILE_50_LABEL_VALUE,
            QUANTILE_75_LABEL_VALUE,
            QUANTILE_95_LABEL_VALUE,
            QUANTILE_98_LABEL_VALUE,
            QUANTILE_99_LABEL_VALUE,
            QUANTILE_999_LABEL_VALUE);
    List<Point> ratePoints =
        Arrays.asList(
            Point.create(Value.doubleValue(snapshot.getMedian()), clock.now()),
            Point.create(Value.doubleValue(snapshot.get75thPercentile()), clock.now()),
            Point.create(Value.doubleValue(snapshot.get95thPercentile()), clock.now()),
            Point.create(Value.doubleValue(snapshot.get98thPercentile()), clock.now()),
            Point.create(Value.doubleValue(snapshot.get99thPercentile()), clock.now()),
            Point.create(Value.doubleValue(snapshot.get999thPercentile()), clock.now()));
    TimeSeries quantileTimeSeries = TimeSeries.create(rateLabelValues, ratePoints, null);
    return Arrays.asList(
        Metric.create(countMetricDescriptor, Collections.singletonList(countTimeSeries)),
        Metric.create(quantileMetricDescriptor, Collections.singletonList(quantileTimeSeries)));
  }

  /**
   * Collect and transform a dropwizard metric as a opencensus metric.
   *
   * @since 0.16
   */
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
