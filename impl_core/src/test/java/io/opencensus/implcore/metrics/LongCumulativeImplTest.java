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
import static io.opencensus.implcore.metrics.LongCumulativeImpl.UNSET_VALUE;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongCumulative.LongPoint;
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
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongCumulativeImpl}. */
@RunWith(JUnit4.class)
public class LongCumulativeImplTest {
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
  private static final Map<LabelKey, LabelValue> EMPTY_CONSTANT_LABELS =
      Collections.<LabelKey, LabelValue>emptyMap();

  private static final Timestamp START_TIME = Timestamp.create(60, 0);
  private static final Duration ONE_MINUTE = Duration.create(60, 0);
  private final TestClock testClock = TestClock.create();
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);
  private final LongCumulativeImpl longCumulativeMetric =
      new LongCumulativeImpl(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          LABEL_KEY,
          EMPTY_CONSTANT_LABELS,
          START_TIME);

  @Before
  public void setUp() {
    testClock.setTime(START_TIME);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    longCumulativeMetric.getOrCreateTimeSeries(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    LongCumulativeImpl longCumulative =
        new LongCumulativeImpl(
            METRIC_NAME,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            labelKeys,
            EMPTY_CONSTANT_LABELS,
            START_TIME);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    longCumulative.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    longCumulativeMetric.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries() {
    LongPoint point = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(100);
    LongPoint point1 = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point1.add(500);
    assertThat(point).isSameAs(point1);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = longCumulativeMetric.getMetric(testClock);
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(600), endTime), START_TIME)));
  }

  @Test
  public void getOrCreateTimeSeries_IgnoreNegativePointValues() {
    LongPoint point = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(-100);
    point.add(25);
    point.add(-33);

    testClock.advanceTime(ONE_MINUTE);
    Metric metric = longCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.longValue(25));
  }

  @Test
  public void getDefaultTimeSeries() {
    LongPoint point = longCumulativeMetric.getDefaultTimeSeries();
    point.add(100);

    LongPoint point1 = longCumulativeMetric.getDefaultTimeSeries();
    point1.add(-100);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = longCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    DEFAULT_LABEL_VALUES,
                    Point.create(Value.longValue(100), endTime),
                    START_TIME)));
    assertThat(point).isSameAs(point1);
  }

  @Test
  public void removeTimeSeries() {
    longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(longCumulativeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(0), START_TIME), START_TIME)));

    longCumulativeMetric.removeTimeSeries(LABEL_VALUES);
    assertThat(longCumulativeMetric.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    longCumulativeMetric.removeTimeSeries(null);
  }

  @Test
  public void clear() {
    LongPoint longPoint = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    longPoint.add(100);

    Metric metric = longCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);

    longCumulativeMetric.clear();
    assertThat(longCumulativeMetric.getMetric(testClock)).isNull();
  }

  @Test
  public void withConstantLabels() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));
    LabelKey constantKey = LabelKey.create("constant_key", "desc");
    LabelValue constantValue = LabelValue.create("constant_value");
    Map<LabelKey, LabelValue> constantLabels =
        Collections.<LabelKey, LabelValue>singletonMap(constantKey, constantValue);
    LongCumulativeImpl longCumulative =
        new LongCumulativeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels, START_TIME);

    LongPoint longPoint = longCumulative.getOrCreateTimeSeries(labelValues);
    longPoint.add(1);
    longPoint.add(2);

    LongPoint defaultPoint = longCumulative.getDefaultTimeSeries();
    defaultPoint.add(100);

    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, allKeys);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    TimeSeries defaultTimeSeries =
        TimeSeries.createWithOnePoint(
            Arrays.asList(UNSET_VALUE, UNSET_VALUE, constantValue),
            Point.create(Value.longValue(100), endTime),
            START_TIME);
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            allValues, Point.create(Value.longValue(3), endTime), START_TIME));
    expectedTimeSeriesList.add(defaultTimeSeries);

    Metric metric = longCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);

    longCumulative.removeTimeSeries(labelValues);
    Metric metric2 = longCumulative.getMetric(testClock);
    assertThat(metric2).isNotNull();
    assertThat(metric2.getTimeSeriesList()).containsExactly(defaultTimeSeries);
  }

  @Test
  public void pointImpl_InstanceOf() {
    LongPoint longPoint = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(longPoint).isInstanceOf(LongCumulativeImpl.PointImpl.class);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    LongPoint longPoint = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    longPoint.add(1);
    longPoint.add(2);

    LongPoint defaultPoint = longCumulativeMetric.getDefaultTimeSeries();
    defaultPoint.add(100);

    LongPoint longPoint1 = longCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES1);
    longPoint1.add(-100);
    longPoint1.add(-20);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();

    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.longValue(3), endTime), START_TIME));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            DEFAULT_LABEL_VALUES, Point.create(Value.longValue(100), endTime), START_TIME));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES1, Point.create(Value.longValue(0), endTime), START_TIME));

    Metric metric = longCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(3);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(longCumulativeMetric.getMetric(testClock)).isNull();
  }

  @Test
  public void testEquals() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    LongCumulativeImpl longCumulative =
        new LongCumulativeImpl(
            METRIC_NAME,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            labelKeys,
            EMPTY_CONSTANT_LABELS,
            START_TIME);

    LongPoint defaultPoint1 = longCumulative.getDefaultTimeSeries();
    LongPoint defaultPoint2 = longCumulative.getDefaultTimeSeries();
    LongPoint longPoint1 = longCumulative.getOrCreateTimeSeries(labelValues);
    LongPoint longPoint2 = longCumulative.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(longPoint1, longPoint2)
        .testEquals();

    longCumulative.clear();

    LongPoint newDefaultPointAfterClear = longCumulative.getDefaultTimeSeries();
    LongPoint newLongPointAfterClear = longCumulative.getOrCreateTimeSeries(labelValues);

    longCumulative.removeTimeSeries(labelValues);
    LongPoint newLongPointAfterRemove = longCumulative.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(longPoint1, longPoint2)
        .addEqualityGroup(newDefaultPointAfterClear)
        .addEqualityGroup(newLongPointAfterClear)
        .addEqualityGroup(newLongPointAfterRemove)
        .testEquals();
  }
}
