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
import static io.opencensus.implcore.metrics.DoubleCumulativeImpl.UNSET_VALUE;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.DoubleCumulative.DoublePoint;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
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

/** Unit tests for {@link DoubleCumulativeImpl}. */
@RunWith(JUnit4.class)
public class DoubleCumulativeImplTest {
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
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DOUBLE, LABEL_KEY);
  private final DoubleCumulativeImpl doubleCumulativeMetric =
      new DoubleCumulativeImpl(
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
    doubleCumulativeMetric.getOrCreateTimeSeries(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    DoubleCumulativeImpl doubleCumulative =
        new DoubleCumulativeImpl(
            METRIC_NAME,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            labelKeys,
            EMPTY_CONSTANT_LABELS,
            START_TIME);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    doubleCumulative.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    doubleCumulativeMetric.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries() {
    DoublePoint point = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(100);
    DoublePoint point1 = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point1.add(500);
    assertThat(point).isSameAs(point1);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = doubleCumulativeMetric.getMetric(testClock);
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(600), endTime), START_TIME)));
  }

  @Test
  public void getOrCreateTimeSeries_IgnoreNegativePointValues() {
    DoublePoint point = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(-100);
    point.add(25);
    point.add(-33);

    testClock.advanceTime(ONE_MINUTE);
    Metric metric = doubleCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(25));
  }

  @Test
  public void getDefaultTimeSeries() {
    DoublePoint point = doubleCumulativeMetric.getDefaultTimeSeries();
    point.add(100);

    DoublePoint point1 = doubleCumulativeMetric.getDefaultTimeSeries();
    point1.add(-100);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    Metric metric = doubleCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    DEFAULT_LABEL_VALUES,
                    Point.create(Value.doubleValue(100), endTime),
                    START_TIME)));
    assertThat(point).isSameAs(point1);
  }

  @Test
  public void removeTimeSeries() {
    doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(doubleCumulativeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(0), START_TIME), START_TIME)));

    doubleCumulativeMetric.removeTimeSeries(LABEL_VALUES);
    assertThat(doubleCumulativeMetric.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    doubleCumulativeMetric.removeTimeSeries(null);
  }

  @Test
  public void clear() {
    DoublePoint doublePoint = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    doublePoint.add(100);

    Metric metric = doubleCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);

    doubleCumulativeMetric.clear();
    assertThat(doubleCumulativeMetric.getMetric(testClock)).isNull();
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
    DoubleCumulativeImpl doubleCumulative =
        new DoubleCumulativeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels, START_TIME);

    DoublePoint doublePoint = doubleCumulative.getOrCreateTimeSeries(labelValues);
    doublePoint.add(1);
    doublePoint.add(2);

    DoublePoint defaultPoint = doubleCumulative.getDefaultTimeSeries();
    defaultPoint.add(100);

    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DOUBLE, allKeys);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();
    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    TimeSeries defaultTimeSeries =
        TimeSeries.createWithOnePoint(
            Arrays.asList(UNSET_VALUE, UNSET_VALUE, constantValue),
            Point.create(Value.doubleValue(100), endTime),
            START_TIME);
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            allValues, Point.create(Value.doubleValue(3), endTime), START_TIME));
    expectedTimeSeriesList.add(defaultTimeSeries);

    Metric metric = doubleCumulative.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);

    doubleCumulative.removeTimeSeries(labelValues);
    Metric metric2 = doubleCumulative.getMetric(testClock);
    assertThat(metric2).isNotNull();
    assertThat(metric2.getTimeSeriesList()).containsExactly(defaultTimeSeries);
  }

  @Test
  public void pointImpl_InstanceOf() {
    DoublePoint doublePoint = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(doublePoint).isInstanceOf(DoubleCumulativeImpl.PointImpl.class);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    DoublePoint doublePoint = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES);
    doublePoint.add(1);
    doublePoint.add(2);

    DoublePoint defaultPoint = doubleCumulativeMetric.getDefaultTimeSeries();
    defaultPoint.add(100);

    DoublePoint doublePoint1 = doubleCumulativeMetric.getOrCreateTimeSeries(LABEL_VALUES1);
    doublePoint1.add(-100);
    doublePoint1.add(-20);

    testClock.advanceTime(ONE_MINUTE);
    Timestamp endTime = testClock.now();

    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.doubleValue(3), endTime), START_TIME));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            DEFAULT_LABEL_VALUES, Point.create(Value.doubleValue(100), endTime), START_TIME));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES1, Point.create(Value.doubleValue(0), endTime), START_TIME));

    Metric metric = doubleCumulativeMetric.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(3);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(doubleCumulativeMetric.getMetric(testClock)).isNull();
  }

  @Test
  public void testEquals() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    DoubleCumulativeImpl doubleCumulative =
        new DoubleCumulativeImpl(
            METRIC_NAME,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            labelKeys,
            EMPTY_CONSTANT_LABELS,
            START_TIME);

    DoublePoint defaultPoint1 = doubleCumulative.getDefaultTimeSeries();
    DoublePoint defaultPoint2 = doubleCumulative.getDefaultTimeSeries();
    DoublePoint doublePoint1 = doubleCumulative.getOrCreateTimeSeries(labelValues);
    DoublePoint doublePoint2 = doubleCumulative.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(doublePoint1, doublePoint2)
        .testEquals();

    doubleCumulative.clear();

    DoublePoint newDefaultPointAfterClear = doubleCumulative.getDefaultTimeSeries();
    DoublePoint newDoublePointAfterClear = doubleCumulative.getOrCreateTimeSeries(labelValues);

    doubleCumulative.removeTimeSeries(labelValues);
    DoublePoint newDoublePointAfterRemove = doubleCumulative.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(doublePoint1, doublePoint2)
        .addEqualityGroup(newDefaultPointAfterClear)
        .addEqualityGroup(newDoublePointAfterClear)
        .addEqualityGroup(newDoublePointAfterRemove)
        .testEquals();
  }
}
