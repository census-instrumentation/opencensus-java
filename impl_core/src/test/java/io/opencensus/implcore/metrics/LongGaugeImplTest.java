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
import static io.opencensus.implcore.metrics.LongGaugeImpl.UNSET_VALUE;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));
  private static final List<LabelValue> LABEL_VALUES1 =
      Collections.singletonList(LabelValue.create("value1"));
  private static final List<LabelValue> DEFAULT_LABEL_VALUES =
      Collections.singletonList(UNSET_VALUE);

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_INT64, LABEL_KEY);
  private final LongGaugeImpl longGaugeMetric =
      new LongGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, LABEL_KEY);

  @Test
  public void getOrCreateTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues should not be null.");
    longGaugeMetric.getOrCreateTimeSeries(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    LongGaugeImpl longGauge =
        new LongGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues element should not be null.");
    longGauge.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    longGaugeMetric.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries() {
    LongPoint point = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(100);
    LongPoint point1 = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point1.set(500);

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric)
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        LABEL_VALUES, Point.create(Value.longValue(500), TEST_TIME), null))));
    new EqualsTester().addEqualityGroup(point, point1).testEquals();
  }

  @Test
  public void getOrCreateTimeSeries_WithNegativePointValues() {
    LongPoint point = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(-100);
    point.add(-33);

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(-133));
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getTimestamp())
        .isEqualTo(TEST_TIME);
    assertThat(metric.getTimeSeriesList().get(0).getStartTimestamp()).isEqualTo(null);
  }

  @Test
  public void getDefaultTimeSeries() {
    LongPoint point = longGaugeMetric.getDefaultTimeSeries();
    point.add(100);
    point.set(500);

    LongPoint point1 = longGaugeMetric.getDefaultTimeSeries();
    point1.add(-100);

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric)
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        DEFAULT_LABEL_VALUES,
                        Point.create(Value.longValue(400), TEST_TIME),
                        null))));
    new EqualsTester().addEqualityGroup(point, point1).testEquals();
  }

  @Test
  public void removeTimeSeries() {
    longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        LABEL_VALUES, Point.create(Value.longValue(0), TEST_TIME), null))));

    longGaugeMetric.removeTimeSeries(LABEL_VALUES);
    assertThat(longGaugeMetric.getMetric(testClock)).isEqualTo(null);
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues should not be null.");
    longGaugeMetric.removeTimeSeries(null);
  }

  @Test
  public void removeTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    LongGaugeImpl longGauge1 = new LongGaugeImpl("name", "description", "1", labelKeys);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues element should not be null.");
    longGauge1.removeTimeSeries(labelValues);
  }

  @Test
  public void removeTimeSeries_WithInvalidLabelsSize() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    longGaugeMetric.removeTimeSeries(new ArrayList<LabelValue>());
  }

  @Test
  public void clear() {
    LongPoint longPoint = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    longPoint.add(-11);
    LongPoint defaultPoint = longGaugeMetric.getDefaultTimeSeries();
    defaultPoint.set(100);

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);

    longGaugeMetric.clear();
    assertThat(longGaugeMetric.getMetric(testClock)).isEqualTo(null);
  }

  @Test
  public void setDefaultLabelValues() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    LongGaugeImpl longGauge =
        new LongGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);
    LongPoint defaultPoint = longGauge.getDefaultTimeSeries();
    defaultPoint.set(-230);

    Metric metric = longGauge.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(0)).isEqualTo(UNSET_VALUE);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(1)).isEqualTo(UNSET_VALUE);
  }

  @Test
  public void pointImpl_InstanceOf() {
    LongPoint longPoint = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(longPoint).isInstanceOf(LongGaugeImpl.PointImpl.class);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    LongPoint longPoint = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    longPoint.add(1);
    longPoint.add(2);

    LongPoint defaultPoint = longGaugeMetric.getDefaultTimeSeries();
    defaultPoint.set(100);

    LongPoint longPoint1 = longGaugeMetric.getOrCreateTimeSeries(LABEL_VALUES1);
    longPoint1.add(-100);
    longPoint1.add(-20);

    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
    timeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.longValue(3), TEST_TIME), null));
    timeSeriesList.add(
        TimeSeries.createWithOnePoint(
            DEFAULT_LABEL_VALUES, Point.create(Value.longValue(100), TEST_TIME), null));
    timeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES1, Point.create(Value.longValue(-120), TEST_TIME), null));

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric).isNotEqualTo(null);
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(3);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(timeSeriesList);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(longGaugeMetric.getMetric(testClock)).isEqualTo(null);
  }

  @Test
  public void testEquals() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    LongGaugeImpl longGauge =
        new LongGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);

    LongPoint defaultPoint1 = longGauge.getDefaultTimeSeries();
    LongPoint defaultPoint2 = longGauge.getDefaultTimeSeries();
    LongPoint longPoint1 = longGauge.getOrCreateTimeSeries(labelValues);
    LongPoint longPoint2 = longGauge.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(longPoint1, longPoint2)
        .testEquals();

    longGauge.clear();

    LongPoint newDefaultPointAfterClear = longGauge.getDefaultTimeSeries();
    LongPoint newLongPointAfterClear = longGauge.getOrCreateTimeSeries(labelValues);

    longGauge.removeTimeSeries(labelValues);
    LongPoint newLongPointAfterRemove = longGauge.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(longPoint1, longPoint2)
        .addEqualityGroup(newDefaultPointAfterClear)
        .addEqualityGroup(newLongPointAfterClear)
        .addEqualityGroup(newLongPointAfterRemove)
        .testEquals();
  }
}
