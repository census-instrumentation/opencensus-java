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

package io.opencensus.implcore.temporary.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.temporary.metrics.MetricDescriptor.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Metric}. */
@RunWith(JUnit4.class)
public class MetricTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME_1 = "metric1";
  private static final String METRIC_NAME_2 = "metric2";
  private static final String DESCRIPTION = "Metric description.";
  private static final String UNIT = "kb/s";
  private static final LabelKey KEY_1 = LabelKey.create("key1", "some key");
  private static final LabelKey KEY_2 = LabelKey.create("key1", "some other key");
  private static final MetricDescriptor METRIC_DESCRIPTOR_1 =
      MetricDescriptor.create(
          METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, Arrays.asList(KEY_1, KEY_2));
  private static final MetricDescriptor METRIC_DESCRIPTOR_2 =
      MetricDescriptor.create(
          METRIC_NAME_2, DESCRIPTION, UNIT, Type.CUMULATIVE_INT64, Arrays.asList(KEY_1));
  private static final LabelValue LABEL_VALUE_1 = LabelValue.create("value1");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("value1");
  private static final LabelValue LABEL_VALUE_EMPTY = LabelValue.create("");
  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Value VALUE_DOUBLE_1 = Value.doubleValue(-345.77);
  private static final Value VALUE_DOUBLE_2 = Value.doubleValue(133.79);
  private static final Timestamp TIMESTAMP_1 = Timestamp.fromMillis(1000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(2000);
  private static final Timestamp TIMESTAMP_3 = Timestamp.fromMillis(3000);
  private static final Point POINT_1 = Point.create(VALUE_DOUBLE_1, TIMESTAMP_2);
  private static final Point POINT_2 = Point.create(VALUE_DOUBLE_2, TIMESTAMP_3);
  private static final Point POINT_3 = Point.create(VALUE_LONG, TIMESTAMP_3);
  private static final TimeSeries GAUGE_TIME_SERIES_1 =
      TimeSeries.create(Arrays.asList(LABEL_VALUE_1, LABEL_VALUE_2), Arrays.asList(POINT_1), null);
  private static final TimeSeries GAUGE_TIME_SERIES_2 =
      TimeSeries.create(Arrays.asList(LABEL_VALUE_1, LABEL_VALUE_2), Arrays.asList(POINT_2), null);
  private static final TimeSeries CUMULATIVE_TIME_SERIES =
      TimeSeries.create(Arrays.asList(LABEL_VALUE_EMPTY), Arrays.asList(POINT_3), TIMESTAMP_1);

  @Test
  public void testGet() {
    Metric metric =
        Metric.create(METRIC_DESCRIPTOR_1, Arrays.asList(GAUGE_TIME_SERIES_1, GAUGE_TIME_SERIES_2));
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR_1);
    assertThat(metric.getTimeSeriesList())
        .containsExactly(GAUGE_TIME_SERIES_1, GAUGE_TIME_SERIES_2)
        .inOrder();
  }

  @Test
  public void typeMismatch_GaugeDouble_Long() {
    typeMismatch(
        METRIC_DESCRIPTOR_1,
        Arrays.asList(CUMULATIVE_TIME_SERIES),
        String.format("Type mismatch: %s, %s.", Type.GAUGE_DOUBLE, "ValueLong"));
  }

  @Test
  public void typeMismatch_CumulativeInt64_Double() {
    typeMismatch(
        METRIC_DESCRIPTOR_2,
        Arrays.asList(GAUGE_TIME_SERIES_1),
        String.format("Type mismatch: %s, %s.", Type.CUMULATIVE_INT64, "ValueDouble"));
  }

  private void typeMismatch(
      MetricDescriptor metricDescriptor, List<TimeSeries> timeSeriesList, String errorMessage) {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(errorMessage);
    Metric.create(metricDescriptor, timeSeriesList);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Metric.create(
                METRIC_DESCRIPTOR_1, Arrays.asList(GAUGE_TIME_SERIES_1, GAUGE_TIME_SERIES_2)),
            Metric.create(
                METRIC_DESCRIPTOR_1, Arrays.asList(GAUGE_TIME_SERIES_1, GAUGE_TIME_SERIES_2)))
        .addEqualityGroup(Metric.create(METRIC_DESCRIPTOR_1, Collections.<TimeSeries>emptyList()))
        .addEqualityGroup(Metric.create(METRIC_DESCRIPTOR_2, Arrays.asList(CUMULATIVE_TIME_SERIES)))
        .addEqualityGroup(Metric.create(METRIC_DESCRIPTOR_2, Collections.<TimeSeries>emptyList()))
        .testEquals();
  }
}
