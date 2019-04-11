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
import static io.opencensus.implcore.metrics.DoubleGaugeImpl.UNSET_VALUE;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.DoubleGauge.DoublePoint;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleGaugeImpl}. */
@RunWith(JUnit4.class)
public class DoubleGaugeImplTest {
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

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, LABEL_KEY);
  private final DoubleGaugeImpl doubleGauge =
      new DoubleGaugeImpl(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, LABEL_KEY, EMPTY_CONSTANT_LABELS);

  @Test
  public void getOrCreateTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    doubleGauge.getOrCreateTimeSeries(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    DoubleGaugeImpl doubleGauge =
        new DoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, EMPTY_CONSTANT_LABELS);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    doubleGauge.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    doubleGauge.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries() {
    DoublePoint point = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(100);
    DoublePoint point1 = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    point1.set(500);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        LABEL_VALUES, Point.create(Value.doubleValue(500), TEST_TIME), null))));
    assertThat(point).isSameAs(point1);
  }

  @Test
  public void getOrCreateTimeSeries_WithNegativePointValues() {
    DoublePoint point = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    point.add(-100);
    point.add(-33);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getValue())
        .isEqualTo(Value.doubleValue(-133));
    assertThat(metric.getTimeSeriesList().get(0).getPoints().get(0).getTimestamp())
        .isEqualTo(TEST_TIME);
    assertThat(metric.getTimeSeriesList().get(0).getStartTimestamp()).isNull();
  }

  @Test
  public void getDefaultTimeSeries() {
    DoublePoint point = doubleGauge.getDefaultTimeSeries();
    point.add(100);
    point.set(500);

    DoublePoint point1 = doubleGauge.getDefaultTimeSeries();
    point1.add(-100);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        DEFAULT_LABEL_VALUES,
                        Point.create(Value.doubleValue(400), TEST_TIME),
                        null))));
    assertThat(point).isSameAs(point1);
  }

  @Test
  public void removeTimeSeries() {
    doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(doubleGauge.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                METRIC_DESCRIPTOR,
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        LABEL_VALUES, Point.create(Value.doubleValue(0), TEST_TIME), null))));

    doubleGauge.removeTimeSeries(LABEL_VALUES);
    assertThat(doubleGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    doubleGauge.removeTimeSeries(null);
  }

  @Test
  public void clear() {
    DoublePoint doublePoint = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    doublePoint.add(-11);
    DoublePoint defaultPoint = doubleGauge.getDefaultTimeSeries();
    defaultPoint.set(100);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);

    doubleGauge.clear();
    assertThat(doubleGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void setDefaultLabelValues() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    DoubleGaugeImpl doubleGauge =
        new DoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, EMPTY_CONSTANT_LABELS);
    DoublePoint defaultPoint = doubleGauge.getDefaultTimeSeries();
    defaultPoint.set(-230);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(0)).isEqualTo(UNSET_VALUE);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(1)).isEqualTo(UNSET_VALUE);
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
    DoubleGaugeImpl doubleGauge =
        new DoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels);

    DoublePoint doublePoint = doubleGauge.getOrCreateTimeSeries(labelValues);
    doublePoint.add(1);
    doublePoint.add(2);

    DoublePoint defaultPoint = doubleGauge.getDefaultTimeSeries();
    defaultPoint.set(100);

    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, allKeys);

    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    TimeSeries defaultTimeSeries =
        TimeSeries.createWithOnePoint(
            Arrays.asList(UNSET_VALUE, UNSET_VALUE, constantValue),
            Point.create(Value.doubleValue(100), TEST_TIME),
            null);
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            allValues, Point.create(Value.doubleValue(3), TEST_TIME), null));
    expectedTimeSeriesList.add(defaultTimeSeries);

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);

    doubleGauge.removeTimeSeries(labelValues);
    Metric metric2 = doubleGauge.getMetric(testClock);
    assertThat(metric2).isNotNull();
    assertThat(metric2.getTimeSeriesList()).containsExactly(defaultTimeSeries);
  }

  @Test
  public void pointImpl_InstanceOf() {
    DoublePoint doublePoint = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    assertThat(doublePoint).isInstanceOf(DoubleGaugeImpl.PointImpl.class);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    DoublePoint doublePoint = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    doublePoint.add(1);
    doublePoint.add(2);

    DoublePoint defaultPoint = doubleGauge.getDefaultTimeSeries();
    defaultPoint.set(100);

    DoublePoint doublePoint1 = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES1);
    doublePoint1.add(-100);
    doublePoint1.add(-20);

    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.doubleValue(3), TEST_TIME), null));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            DEFAULT_LABEL_VALUES, Point.create(Value.doubleValue(100), TEST_TIME), null));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES1, Point.create(Value.doubleValue(-120), TEST_TIME), null));

    Metric metric = doubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(3);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(doubleGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void testEquals() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    DoubleGaugeImpl doubleGauge =
        new DoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, EMPTY_CONSTANT_LABELS);

    DoublePoint defaultPoint1 = doubleGauge.getDefaultTimeSeries();
    DoublePoint defaultPoint2 = doubleGauge.getDefaultTimeSeries();
    DoublePoint doublePoint1 = doubleGauge.getOrCreateTimeSeries(labelValues);
    DoublePoint doublePoint2 = doubleGauge.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(doublePoint1, doublePoint2)
        .testEquals();

    doubleGauge.clear();

    DoublePoint newDefaultPointAfterClear = doubleGauge.getDefaultTimeSeries();
    DoublePoint newDoublePointAfterClear = doubleGauge.getOrCreateTimeSeries(labelValues);

    doubleGauge.removeTimeSeries(labelValues);
    DoublePoint newDoublePointAfterRemove = doubleGauge.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2)
        .addEqualityGroup(doublePoint1, doublePoint2)
        .addEqualityGroup(newDefaultPointAfterClear)
        .addEqualityGroup(newDoublePointAfterClear)
        .addEqualityGroup(newDoublePointAfterRemove)
        .testEquals();
  }
}
