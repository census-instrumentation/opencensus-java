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
import io.opencensus.common.ToDoubleFunction;
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

/** Unit tests for {@link DerivedDoubleGaugeImpl}. */
@RunWith(JUnit4.class)
public class DerivedDoubleGaugeImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));
  private static final List<LabelValue> LABEL_VALUES_1 =
      Collections.singletonList(LabelValue.create("value1"));
  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private static final Map<LabelKey, LabelValue> EMPTY_CONSTANT_LABELS =
      Collections.<LabelKey, LabelValue>emptyMap();

  private final TestClock testClock = TestClock.create(TEST_TIME);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, LABEL_KEY);

  private final DerivedDoubleGaugeImpl derivedDoubleGauge =
      new DerivedDoubleGaugeImpl(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, LABEL_KEY, EMPTY_CONSTANT_LABELS);

  // helper class
  public static class QueueManager {
    public double size() {
      return 2.5;
    }
  }

  private static final ToDoubleFunction<Object> doubleFunction =
      new ToDoubleFunction<Object>() {
        @Override
        public double applyAsDouble(Object value) {
          return 5.5;
        }
      };
  private static final ToDoubleFunction<Object> negativeDoubleFunction =
      new ToDoubleFunction<Object>() {
        @Override
        public double applyAsDouble(Object value) {
          return -200.5;
        }
      };
  private static final ToDoubleFunction<QueueManager> queueManagerFunction =
      new ToDoubleFunction<QueueManager>() {
        @Override
        public double applyAsDouble(QueueManager queue) {
          return queue.size();
        }
      };

  @Test
  public void createTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedDoubleGauge.createTimeSeries(null, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);
    DerivedDoubleGaugeImpl derivedDoubleGauge =
        new DerivedDoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, EMPTY_CONSTANT_LABELS);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    derivedDoubleGauge.createTimeSeries(labelValues, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    derivedDoubleGauge.createTimeSeries(labelValues, null, doubleFunction);
  }

  @Test
  public void createTimeSeries_WithNullFunction() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("function");
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, null);
  }

  @Test
  public void createTimeSeries_WithObjFunction() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, new QueueManager(), queueManagerFunction);
    Metric metric = derivedDoubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(2.5), TEST_TIME), null)));
  }

  @Test
  public void createTimeSeries_WithSameLabel() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, new QueueManager(), queueManagerFunction);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("A different time series with the same labels already exists.");
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, queueManagerFunction);
  }

  @Test
  public void addTimeSeries_WithNullObj() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, negativeDoubleFunction);
    Metric metric = derivedDoubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(-200.5), TEST_TIME), null)));
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
    DerivedDoubleGaugeImpl derivedDoubleGauge2 =
        new DerivedDoubleGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels);

    derivedDoubleGauge2.createTimeSeries(labelValues, new QueueManager(), queueManagerFunction);

    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, allKeys);

    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    TimeSeries expectedTimeSeries =
        TimeSeries.createWithOnePoint(
            allValues, Point.create(Value.doubleValue(2.5), TEST_TIME), null);

    Metric metric = derivedDoubleGauge2.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList()).containsExactly(expectedTimeSeries);

    derivedDoubleGauge2.removeTimeSeries(labelValues);
    Metric metric2 = derivedDoubleGauge2.getMetric(testClock);
    assertThat(metric2).isNull();
  }

  @Test
  public void removeTimeSeries() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    Metric metric = derivedDoubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);
    derivedDoubleGauge.removeTimeSeries(LABEL_VALUES);
    assertThat(derivedDoubleGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedDoubleGauge.removeTimeSeries(null);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES_1, new QueueManager(), queueManagerFunction);
    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.doubleValue(5.5), TEST_TIME), null));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES_1, Point.create(Value.doubleValue(2.5), TEST_TIME), null));
    Metric metric = derivedDoubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeriesList);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(0).getLabelValues().get(0))
        .isEqualTo(LabelValue.create("value"));
    assertThat(metric.getTimeSeriesList().get(1).getLabelValues().size()).isEqualTo(1);
    assertThat(metric.getTimeSeriesList().get(1).getLabelValues().get(0))
        .isEqualTo(LabelValue.create("value1"));
  }

  @Test
  public void clear() {
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES_1, new QueueManager(), queueManagerFunction);
    Metric metric = derivedDoubleGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);
    derivedDoubleGauge.clear();
    assertThat(derivedDoubleGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(derivedDoubleGauge.getMetric(testClock)).isNull();
  }
}
