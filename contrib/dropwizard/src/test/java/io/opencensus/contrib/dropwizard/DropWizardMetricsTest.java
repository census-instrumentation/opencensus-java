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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.DEFAULT_UNIT;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.NS_UNIT;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DropWizardMetrics}. */
@RunWith(JUnit4.class)
public class DropWizardMetricsTest {

  private com.codahale.metrics.MetricRegistry metricRegistry;
  private DropWizardMetrics dropWizardMetrics;

  @Before
  public void setUp() {
    metricRegistry = new com.codahale.metrics.MetricRegistry();
    dropWizardMetrics = new DropWizardMetrics(Collections.singletonList(metricRegistry));
  }

  @Test
  public void collect_Counter() {
    // create dropwizard metrics
    Counter evictions = metricRegistry.counter("cache_evictions");
    evictions.inc();
    evictions.inc(3);
    evictions.dec();
    evictions.dec(2);

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(1);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_cache_evictions_counter",
                "Collected from codahale (metric=cache_evictions, "
                    + "type=com.codahale.metrics.Counter)",
                DEFAULT_UNIT,
                Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNull();
  }

  @Test
  public void collect_Gauge() {
    Gauge<Integer> integerGauge =
        new Gauge<Integer>() {
          @Override
          public Integer getValue() {
            return 1234;
          }
        };
    metricRegistry.register("integer_gauge", integerGauge);

    Gauge<Double> doubleGauge =
        new Gauge<Double>() {
          @Override
          public Double getValue() {
            return 1.234D;
          }
        };
    metricRegistry.register("double_gauge", doubleGauge);

    Gauge<Long> longGauge =
        new Gauge<Long>() {
          @Override
          public Long getValue() {
            return 1234L;
          }
        };
    metricRegistry.register("long_gauge", longGauge);

    Gauge<Float> floatGauge =
        new Gauge<Float>() {
          @Override
          public Float getValue() {
            return 0.1234F;
          }
        };
    metricRegistry.register("float_gauge", floatGauge);

    Gauge<Boolean> boolGauge =
        new Gauge<Boolean>() {
          @Override
          public Boolean getValue() {
            return Boolean.TRUE;
          }
        };
    metricRegistry.register("boolean_gauge", boolGauge);

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(5);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_boolean_gauge_gauge",
                "Collected from codahale (metric=boolean_gauge, "
                    + "type=io.opencensus.contrib.dropwizard.DropWizardMetricsTest$5)",
                DEFAULT_UNIT,
                Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(1).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_double_gauge_gauge",
                "Collected from codahale (metric=double_gauge, "
                    + "type=io.opencensus.contrib.dropwizard.DropWizardMetricsTest$2)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(1).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1.234));
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(2).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_float_gauge_gauge",
                "Collected from codahale (metric=float_gauge, "
                    + "type=io.opencensus.contrib.dropwizard.DropWizardMetricsTest$4)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(2).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(0.1234000027179718));
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(3).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_integer_gauge_gauge",
                "Collected from codahale (metric=integer_gauge, "
                    + "type=io.opencensus.contrib.dropwizard.DropWizardMetricsTest$1)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(3).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1234.0));
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getStartTimestamp()).isNull();

    assertThat(metrics.get(4).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_long_gauge_gauge",
                "Collected from codahale (metric=long_gauge, "
                    + "type=io.opencensus.contrib.dropwizard.DropWizardMetricsTest$3)",
                DEFAULT_UNIT,
                Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(4).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1234.0));
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getStartTimestamp()).isNull();
  }

  @Test
  public void collect_Meter() {
    Meter getRequests = metricRegistry.meter("get_requests");
    getRequests.mark();
    getRequests.mark();

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(1);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_get_requests_meter",
                "Collected from codahale (metric=get_requests, "
                    + "type=com.codahale.metrics.Meter)",
                DEFAULT_UNIT,
                Type.CUMULATIVE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(2));
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNull();
  }

  @Test
  public void collect_Histogram() {
    Histogram resultCounts = metricRegistry.histogram("result");
    resultCounts.update(200);

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(1);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_result_histogram",
                "Collected from codahale (metric=result, " + "type=com.codahale.metrics.Histogram)",
                DEFAULT_UNIT,
                Type.SUMMARY,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
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
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNotNull();
  }

  @Test
  public void collect_Timer() throws InterruptedException {
    Timer timer = metricRegistry.timer("requests");
    Timer.Context context = timer.time();
    Thread.sleep(1L);
    context.stop();

    ArrayList<Metric> metrics = new ArrayList<>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(1);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "codahale_requests_timer",
                "Collected from codahale (metric=requests, " + "type=com.codahale.metrics.Timer)",
                NS_UNIT,
                Type.SUMMARY,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
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
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isNotNull();
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(dropWizardMetrics.getMetrics()).isEmpty();
  }
}
