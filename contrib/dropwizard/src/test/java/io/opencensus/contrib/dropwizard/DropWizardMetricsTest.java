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
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_50_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_75_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_95_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_98_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_999_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_99_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.QUANTILE_LABEL_KEY;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.RATE_FIFTEEN_MINUTE_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.RATE_FIVE_MINUTE_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.RATE_LABEL_KEY;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.RATE_MEAN_LABEL_VALUE;
import static io.opencensus.contrib.dropwizard.DropWizardMetrics.RATE_ONE_MINUTE_LABEL_VALUE;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.Value;
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
  DropWizardMetrics dropWizardMetrics;

  @Before
  public void setUp() throws Exception {
    metricRegistry = new MetricRegistry();
    dropWizardMetrics = new DropWizardMetrics(metricRegistry);
  }

  @Test
  public void collect() throws InterruptedException {

    // create dropwizard metrics
    Counter evictions = metricRegistry.counter("cache_evictions");
    evictions.inc();
    evictions.inc(3);
    evictions.dec();
    evictions.dec(2);

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

    Meter getRequests = metricRegistry.meter("get_requests");
    getRequests.mark();
    getRequests.mark();

    Histogram resultCounts = metricRegistry.histogram("result");
    resultCounts.update(200);

    Timer t = metricRegistry.timer("requests");
    Timer.Context context = t.time();
    Thread.sleep(1L);
    context.stop();

    ArrayList<Metric> metrics = new ArrayList<Metric>(dropWizardMetrics.getMetrics());
    assertThat(metrics.size()).isEqualTo(11);

    assertThat(metrics.get(0).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "cache_evictions_count",
                "DropWizard Metric=Counter Data=count",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(0).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(1).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "double_gauge_value",
                "DropWizard Metric=Gauge Data=value",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(1).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(1.234));
    assertThat(metrics.get(1).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(2).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "float_gauge_value",
                "DropWizard Metric=Gauge Data=value",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_DOUBLE,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(2).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(0.1234000027179718));
    assertThat(metrics.get(2).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(3).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "integer_gauge_value",
                "DropWizard Metric=Gauge Data=value",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(3).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1234));
    assertThat(metrics.get(3).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(4).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "long_gauge_value",
                "DropWizard Metric=Gauge Data=value",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(4).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1234));
    assertThat(metrics.get(4).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(5).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "get_requests_count",
                "DropWizard Metric=Meter Data=count",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(5).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(2));
    assertThat(metrics.get(5).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(6).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "get_requests_rate",
                "DropWizard Metric=Meter Data=rate",
                "events/second",
                MetricDescriptor.Type.GAUGE_DOUBLE,
                new ArrayList<LabelKey>(Arrays.asList(RATE_LABEL_KEY))));
    assertThat(metrics.get(6).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(4);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getLabelValues())
        .isEqualTo(
            Arrays.asList(
                RATE_MEAN_LABEL_VALUE,
                RATE_ONE_MINUTE_LABEL_VALUE,
                RATE_FIVE_MINUTE_LABEL_VALUE,
                RATE_FIFTEEN_MINUTE_LABEL_VALUE));
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(4);
    assertThat(metrics.get(6).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(7).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "result_count",
                "DropWizard Metric=Snapshot Data=count",
                DEFAULT_UNIT,
                MetricDescriptor.Type.CUMULATIVE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(7).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(7).getTimeSeriesList().get(0).getStartTimestamp())
        .isInstanceOf(Timestamp.class);

    assertThat(metrics.get(8).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "result_quantile",
                "DropWizard Metric=Snapshot Data=quantile",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_DOUBLE,
                new ArrayList<LabelKey>(Arrays.asList(QUANTILE_LABEL_KEY))));
    assertThat(metrics.get(8).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(6);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getLabelValues())
        .isEqualTo(
            Arrays.asList(
                QUANTILE_50_LABEL_VALUE,
                QUANTILE_75_LABEL_VALUE,
                QUANTILE_95_LABEL_VALUE,
                QUANTILE_98_LABEL_VALUE,
                QUANTILE_99_LABEL_VALUE,
                QUANTILE_999_LABEL_VALUE));
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(6);
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(200));
    assertThat(metrics.get(8).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);

    assertThat(metrics.get(9).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "requests_count",
                "DropWizard Metric=Snapshot Data=count",
                DEFAULT_UNIT,
                MetricDescriptor.Type.CUMULATIVE_INT64,
                Collections.<LabelKey>emptyList()));
    assertThat(metrics.get(9).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(0);
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(1));
    assertThat(metrics.get(9).getTimeSeriesList().get(0).getStartTimestamp())
        .isInstanceOf(Timestamp.class);

    assertThat(metrics.get(10).getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                "requests_quantile",
                "DropWizard Metric=Snapshot Data=quantile",
                DEFAULT_UNIT,
                MetricDescriptor.Type.GAUGE_DOUBLE,
                new ArrayList<LabelKey>(Arrays.asList(QUANTILE_LABEL_KEY))));
    assertThat(metrics.get(10).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(10).getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(6);
    assertThat(metrics.get(10).getTimeSeriesList().get(0).getLabelValues())
        .isEqualTo(
            Arrays.asList(
                QUANTILE_50_LABEL_VALUE,
                QUANTILE_75_LABEL_VALUE,
                QUANTILE_95_LABEL_VALUE,
                QUANTILE_98_LABEL_VALUE,
                QUANTILE_99_LABEL_VALUE,
                QUANTILE_999_LABEL_VALUE));
    assertThat(metrics.get(10).getTimeSeriesList().get(0).getPoints().size()).isEqualTo(6);
    assertThat(metrics.get(10).getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(dropWizardMetrics.getMetrics()).isEmpty();
  }
}
