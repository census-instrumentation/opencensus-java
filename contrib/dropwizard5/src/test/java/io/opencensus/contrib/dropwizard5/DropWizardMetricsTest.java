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

package io.opencensus.contrib.dropwizard5;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.contrib.dropwizard5.DropWizardMetrics.DEFAULT_UNIT;
import static io.opencensus.contrib.dropwizard5.DropWizardMetrics.NS_UNIT;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.Timer;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DropWizardMetrics}. */
@RunWith(JUnit4.class)
public class DropWizardMetricsTest {

  private io.dropwizard.metrics5.MetricRegistry metricRegistry;
  private DropWizardMetrics dropWizardMetrics;

  @Before
  public void setUp() {
    metricRegistry = new io.dropwizard.metrics5.MetricRegistry();
    dropWizardMetrics = new DropWizardMetrics(Collections.singletonList(metricRegistry));
  }

  @Test
  public void collect() throws InterruptedException {

    // create dropwizard metrics
    Map<String, String> tags = new HashMap<>();
    tags.put("tag1", "value1");
    tags.put("tag2", "value2");
    List<LabelKey> labelKeys = new ArrayList<>();
    List<LabelValue> labelValues = new ArrayList<>();
    for (Map.Entry<String, String> e : tags.entrySet()) {
      labelKeys.add(LabelKey.create(e.getKey(), e.getKey()));
      labelValues.add(LabelValue.create(e.getValue()));
    }
    Counter evictions = metricRegistry.counter(new MetricName("cache_evictions", tags));
    evictions.inc();
    evictions.inc(3);
    evictions.dec();
    evictions.dec(2);
    metricRegistry.gauge(new MetricName("boolean_gauge", tags), BooleanGauge::new);
    metricRegistry.gauge(new MetricName("double_gauge", tags), DoubleGauge::new);
    metricRegistry.gauge(new MetricName("float_gauge", tags), FloatGauge::new);
    metricRegistry.gauge(new MetricName("integer_gauge", tags), IntegerGauge::new);
    metricRegistry.gauge(new MetricName("long_gauge", tags), LongGauge::new);
    metricRegistry.gauge(
        new MetricName("notags_boolean_gauge", Collections.emptyMap()), BooleanGauge::new);

    Meter getRequests = metricRegistry.meter(new MetricName("get_requests", tags));
    getRequests.mark();
    getRequests.mark();

    Histogram resultCounts = metricRegistry.histogram(new MetricName("result", tags));
    resultCounts.update(200);

    Timer timer = metricRegistry.timer(new MetricName("requests", tags));
    Timer.Context context = timer.time();
    Thread.sleep(1L);
    context.stop();

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(10);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_cache_evictions_counter",
                "Collected from dropwizard5 (metric=cache_evictions, "
                    + "type=io.dropwizard.metrics5.Counter)",
                DEFAULT_UNIT,
                Type.GAUGE_INT64,
                labelKeys));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    // boolean gauge with tags
    assertThat(metrics.get(1).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_boolean_gauge_gauge",
                "Collected from dropwizard5 (metric=boolean_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$BooleanGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_INT64,
                labelKeys));
    assertThat(metrics.get(1).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(2).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_double_gauge_gauge",
                "Collected from dropwizard5 (metric=double_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$DoubleGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                labelKeys));
    assertThat(metrics.get(2).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1.234));
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(3).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_float_gauge_gauge",
                "Collected from dropwizard5 (metric=float_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$FloatGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                labelKeys));
    assertThat(metrics.get(3).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(0.1234000027179718));
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(4).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_integer_gauge_gauge",
                "Collected from dropwizard5 (metric=integer_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$IntegerGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                labelKeys));
    assertThat(metrics.get(4).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1234.0));
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(5).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_long_gauge_gauge",
                "Collected from dropwizard5 (metric=long_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$LongGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                labelKeys));
    assertThat(metrics.get(5).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1234.0));
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    // boolean gauge with tags
    assertThat(metrics.get(6).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_notags_boolean_gauge_gauge",
                "Collected from dropwizard5 (metric=notags_boolean_gauge, "
                    + "type=io.opencensus.contrib.dropwizard5.DropWizardMetricsTest$BooleanGauge)",
                DEFAULT_UNIT,
                Type.GAUGE_INT64,
                Collections.emptyList()));
    assertThat(metrics.get(6).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(7).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_get_requests_meter",
                "Collected from dropwizard5 (metric=get_requests, "
                    + "type=io.dropwizard.metrics5.Meter)",
                DEFAULT_UNIT,
                Type.CUMULATIVE_INT64,
                labelKeys));
    assertThat(metrics.get(7).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(2));
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getStartTimestamp()).isNotNull();

    assertThat(metrics.get(8).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_result_histogram",
                "Collected from dropwizard5 (metric=result, "
                    + "type=io.dropwizard.metrics5.Histogram)",
                DEFAULT_UNIT,
                Type.SUMMARY,
                labelKeys));
    assertThat(metrics.get(8).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getLabelValues()).isEqualTo(labelValues);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(
            Value.summaryValue(
                Summary.create(
                    1L,
                    0.0,
                    Snapshot.create(
                        1L,
                        0.0,
                        Arrays.asList(
                            ValueAtPercentile.create(50.0, 200.0),
                            ValueAtPercentile.create(75.0, 200.0),
                            ValueAtPercentile.create(98.0, 200.0),
                            ValueAtPercentile.create(99.0, 200.0),
                            ValueAtPercentile.create(99.9, 200.0))))));
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getStartTimestamp())
        .isInstanceOf(Timestamp.class);

    assertThat(metrics.get(9).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "dropwizard5_requests_timer",
                "Collected from dropwizard5 (metric=requests, "
                    + "type=io.dropwizard.metrics5.Timer)",
                NS_UNIT,
                Type.SUMMARY,
                labelKeys));
    assertThat(metrics.get(9).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getLabelValues().size())
        .isEqualTo(tags.size());
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(
            Value.summaryValue(
                Summary.create(
                    1L,
                    0.0,
                    Snapshot.create(
                        1L,
                        0.0,
                        Arrays.asList(
                            ValueAtPercentile.create(50.0, timer.getSnapshot().getMedian()),
                            ValueAtPercentile.create(75.0, timer.getSnapshot().get75thPercentile()),
                            ValueAtPercentile.create(98.0, timer.getSnapshot().get98thPercentile()),
                            ValueAtPercentile.create(99.0, timer.getSnapshot().get99thPercentile()),
                            ValueAtPercentile.create(
                                99.9, timer.getSnapshot().get999thPercentile()))))));

    assertThat(metrics.get(9).getTimeSeriesList().get(0).getStartTimestamp())
        .isInstanceOf(Timestamp.class);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(dropWizardMetrics.getMetrics()).isEmpty();
  }

  @Test
  public void filter_GetMetrics() {
    MetricFilter filter =
        new MetricFilter() {
          @Override
          public boolean matches(MetricName name, io.dropwizard.metrics5.Metric metric) {
            return name.getKey().startsWith("test");
          }
        };
    dropWizardMetrics = new DropWizardMetrics(Collections.singletonList(metricRegistry), filter);
    metricRegistry.timer("test_requests");
    metricRegistry.timer("requests");

    Collection<Metric> metrics = dropWizardMetrics.getMetrics();
    assertThat(metrics).hasSize(1);
    Metric value = metrics.iterator().next();
    assertThat(value.getMetricDescriptor().getName()).isEqualTo("dropwizard5_test_requests_timer");
  }

  static class IntegerGauge implements Gauge<Integer> {
    @Override
    public Integer getValue() {
      return 1234;
    }
  }

  static class DoubleGauge implements Gauge<Double> {
    @Override
    public Double getValue() {
      return 1.234D;
    }
  }

  static class LongGauge implements Gauge<Long> {
    @Override
    public Long getValue() {
      return 1234L;
    }
  }

  static class FloatGauge implements Gauge<Float> {
    @Override
    public Float getValue() {
      return 0.1234F;
    }
  }

  static class BooleanGauge implements Gauge<Boolean> {
    @Override
    public Boolean getValue() {
      return Boolean.TRUE;
    }
  }
}
