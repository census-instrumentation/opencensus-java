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

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValue;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.MetricDescriptor.Type;
import io.opencensus.metrics.Point;
import io.opencensus.metrics.TimeSeriesGauge;
import io.opencensus.metrics.TimeSeriesList.TimeSeriesGaugeList;
import io.opencensus.metrics.Value;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MetricMap}. */
@RunWith(JUnit4.class)
public class MetricMapTest {

  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE_2");
  private static final TagValue VALUE_3 = TagValue.create("VALUE_3");
  private static final Timestamp TIMESTAMP_EMPTY = Timestamp.create(0, 0);

  private static final LabelKey LABEL_KEY = LabelKey.create("KEY", "");
  private static final LabelValue LABEL_VALUE = LabelValue.create("VALUE");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("VALUE_2");
  private static final LabelValue LABEL_VALUE_3 = LabelValue.create("VALUE_3");
  private static final MetricDescriptor DESCRIPTOR =
      MetricDescriptor.create(
          "name", "description", "1", Type.GAUGE_DOUBLE, Collections.singletonList(LABEL_KEY));

  @Test
  public void noDataForMetricDescriptor() {
    MetricMap metricMap = new MetricMap();
    metricMap.registerMetricDescriptor(DESCRIPTOR, TIMESTAMP_EMPTY);
    assertThat(metricMap.toMetrics()).isEmpty();
  }

  @Test
  public void recordAndExport() {
    MetricMap metricMap = new MetricMap();
    Timestamp timestamp1 = Timestamp.fromMillis(1000);
    Timestamp timestamp2 = Timestamp.fromMillis(2000);
    Timestamp timestamp3 = Timestamp.fromMillis(3000);
    Timestamp timestamp4 = Timestamp.fromMillis(4000);
    metricMap.registerMetricDescriptor(DESCRIPTOR, timestamp1);

    MutableLastValue lastValue1 = MutableLastValue.create();
    lastValue1.add(10.0, Collections.<String, String>emptyMap(), timestamp2);
    metricMap.record(DESCRIPTOR, Collections.singletonList(VALUE), lastValue1, timestamp2);
    lastValue1.add(19.0, Collections.<String, String>emptyMap(), timestamp3);
    metricMap.record(DESCRIPTOR, Collections.singletonList(VALUE), lastValue1, timestamp3);

    MutableLastValue lastValue2 = MutableLastValue.create();
    lastValue2.add(-7.0, Collections.<String, String>emptyMap(), timestamp3);
    metricMap.record(DESCRIPTOR, Collections.singletonList(VALUE_2), lastValue2, timestamp3);

    TimeSeriesGauge expectedTimeSeries1 =
        TimeSeriesGauge.create(
            Collections.singletonList(LABEL_VALUE),
            Arrays.asList(
                Point.create(Value.doubleValue(10.0), timestamp2),
                Point.create(Value.doubleValue(19.0), timestamp3)));
    TimeSeriesGauge expectedTimeSeries2 =
        TimeSeriesGauge.create(
            Collections.singletonList(LABEL_VALUE_2),
            Collections.singletonList(Point.create(Value.doubleValue(-7.0), timestamp3)));
    assertThat(metricMap.toMetrics())
        .containsExactly(
            Metric.create(
                DESCRIPTOR,
                TimeSeriesGaugeList.create(
                    Arrays.asList(expectedTimeSeries1, expectedTimeSeries2))));

    assertThat(metricMap.toMetrics()).isEmpty(); // Flush stats after exporting.

    // Record another Measurement, should only produce one Point.
    lastValue1.add(6.0, Collections.<String, String>emptyMap(), timestamp4);
    metricMap.record(DESCRIPTOR, Collections.singletonList(VALUE_3), lastValue1, timestamp4);
    assertThat(metricMap.toMetrics())
        .containsExactly(
            Metric.create(
                DESCRIPTOR,
                TimeSeriesGaugeList.create(
                    Collections.singletonList(
                        TimeSeriesGauge.create(
                            Collections.singletonList(LABEL_VALUE_3),
                            Collections.singletonList(
                                Point.create(Value.doubleValue(6.0), timestamp4)))))));
  }
}
