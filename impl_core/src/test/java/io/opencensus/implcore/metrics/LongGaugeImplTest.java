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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongGaugeImpl}. */
@RunWith(JUnit4.class)
public class LongGaugeImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("key", "key description");
  private static final LabelValue LABEL_VALUES = LabelValue.create("value");
  private static final LabelValue LABEL_VALUES_1 = LabelValue.create("value1");
  private final List<LabelKey> labelKeys = new ArrayList<LabelKey>();
  private final List<LabelValue> labelValues = new ArrayList<LabelValue>();
  private final List<LabelValue> labelValues1 = new ArrayList<LabelValue>();

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);

  private LongGaugeImpl longGaugeMetric;

  @Before
  public void setUp() {
    labelKeys.add(LABEL_KEY);
    labelValues.add(LABEL_VALUES);
    labelValues1.add(LABEL_VALUES_1);

    longGaugeMetric = new LongGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(longGaugeMetric.getMetric(testClock)).isEqualTo(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithLabels() {
    LongPoint point = longGaugeMetric.getOrCreateTimeSeries(labelValues);
    point.add(100);
    point.add(-60);
    point.set(500);

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.singletonList(LABEL_KEY)),
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        Collections.singletonList(LABEL_VALUES),
                        io.opencensus.metrics.export.Point.create(Value.longValue(500), TEST_TIME),
                        null))));
  }

  @Test
  public void getDefaultTimeSeries() {
    LongPoint point = longGaugeMetric.getDefaultTimeSeries();
    point.add(100);
    point.set(500);

    LongPoint point1 = longGaugeMetric.getDefaultTimeSeries();
    point1.add(-100);

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.singletonList(LABEL_KEY)),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.<LabelValue>emptyList(),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.longValue(400), TEST_TIME)),
                        null))));
  }

  @Test
  public void multipleMetrics_GetMetric() {
    LongPoint point = longGaugeMetric.getOrCreateTimeSeries(labelValues);
    point.add(1);
    point.add(2);

    LongPoint point1 = longGaugeMetric.getDefaultTimeSeries();
    point1.set(100);

    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
    timeSeriesList.add(
        TimeSeries.create(
            Collections.singletonList(LABEL_VALUES),
            Collections.singletonList(
                io.opencensus.metrics.export.Point.create(Value.longValue(3), TEST_TIME)),
            null));
    timeSeriesList.add(
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(
                io.opencensus.metrics.export.Point.create(Value.longValue(100), TEST_TIME)),
            null));

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric.getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                METRIC_NAME,
                METRIC_DESCRIPTION,
                METRIC_UNIT,
                Type.GAUGE_INT64,
                Collections.singletonList(LABEL_KEY)));

    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(timeSeriesList);
  }

  @Test
  public void getOrCreateTimeSeries_IncorrectLabels() {
    thrown.expect(IllegalArgumentException.class);
    longGaugeMetric.getOrCreateTimeSeries(new ArrayList<LabelValue>());
  }
}
