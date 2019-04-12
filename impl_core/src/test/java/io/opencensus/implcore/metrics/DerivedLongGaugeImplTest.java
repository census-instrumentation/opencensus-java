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
import io.opencensus.common.ToLongFunction;
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

/** Unit tests for {@link DerivedLongGaugeImpl}. */
@RunWith(JUnit4.class)
public class DerivedLongGaugeImplTest {
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
  private static final Map<LabelKey, LabelValue> EMPTY_CONSTANT_LABELS =
      Collections.<LabelKey, LabelValue>emptyMap();

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);

  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_INT64, LABEL_KEY);

  private final DerivedLongGaugeImpl derivedLongGauge =
      new DerivedLongGaugeImpl(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, LABEL_KEY, EMPTY_CONSTANT_LABELS);

  // helper class
  public static class QueueManager {
    public long size() {
      return 2;
    }
  }

  private static final ToLongFunction<Object> longFunction =
      new ToLongFunction<Object>() {
        @Override
        public long applyAsLong(Object value) {
          return 5;
        }
      };
  private static final ToLongFunction<Object> negativeLongFunction =
      new ToLongFunction<Object>() {
        @Override
        public long applyAsLong(Object value) {
          return -200;
        }
      };
  private static final ToLongFunction<QueueManager> queueManagerFunction =
      new ToLongFunction<QueueManager>() {
        @Override
        public long applyAsLong(QueueManager queue) {
          return queue.size();
        }
      };

  @Test
  public void createTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedLongGauge.createTimeSeries(null, null, longFunction);
  }

  @Test
  public void createTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    DerivedLongGaugeImpl derivedLongGauge =
        new DerivedLongGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, EMPTY_CONSTANT_LABELS);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    derivedLongGauge.createTimeSeries(labelValues, null, longFunction);
  }

  @Test
  public void createTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    derivedLongGauge.createTimeSeries(labelValues, null, longFunction);
  }

  @Test
  public void createTimeSeries_WithNullFunction() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("function");
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, null);
  }

  @Test
  public void createTimeSeries_WithObjFunction() {
    derivedLongGauge.createTimeSeries(LABEL_VALUES, new QueueManager(), queueManagerFunction);

    Metric metric = derivedLongGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(2), TEST_TIME), null)));
  }

  @Test
  public void addTimeSeries_WithNullObj() {
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, negativeLongFunction);

    Metric metric = derivedLongGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric)
        .isEqualTo(
            Metric.createWithOneTimeSeries(
                METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(-200), TEST_TIME), null)));
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
    DerivedLongGaugeImpl derivedLongGauge2 =
        new DerivedLongGaugeImpl(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys, constantLabels);

    derivedLongGauge2.createTimeSeries(labelValues, new QueueManager(), queueManagerFunction);

    List<LabelKey> allKeys = new ArrayList<>(labelKeys);
    allKeys.add(constantKey);
    MetricDescriptor expectedDescriptor =
        MetricDescriptor.create(
            METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_INT64, allKeys);

    List<LabelValue> allValues = new ArrayList<>(labelValues);
    allValues.add(constantValue);
    TimeSeries expectedTimeSeries =
        TimeSeries.createWithOnePoint(allValues, Point.create(Value.longValue(2), TEST_TIME), null);

    Metric metric = derivedLongGauge2.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(expectedDescriptor);
    assertThat(metric.getTimeSeriesList()).containsExactly(expectedTimeSeries);

    derivedLongGauge2.removeTimeSeries(labelValues);
    Metric metric2 = derivedLongGauge2.getMetric(testClock);
    assertThat(metric2).isNull();
  }

  @Test
  public void removeTimeSeries() {
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, longFunction);
    Metric metric = derivedLongGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(1);

    derivedLongGauge.removeTimeSeries(LABEL_VALUES);
    assertThat(derivedLongGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    derivedLongGauge.removeTimeSeries(null);
  }

  @Test
  public void multipleMetrics_GetMetric() {
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, longFunction);
    derivedLongGauge.createTimeSeries(LABEL_VALUES_1, new QueueManager(), queueManagerFunction);

    List<TimeSeries> expectedTimeSeriesList = new ArrayList<TimeSeries>();
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES, Point.create(Value.longValue(5), TEST_TIME), null));
    expectedTimeSeriesList.add(
        TimeSeries.createWithOnePoint(
            LABEL_VALUES_1, Point.create(Value.longValue(2), TEST_TIME), null));

    Metric metric = derivedLongGauge.getMetric(testClock);
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
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, longFunction);
    derivedLongGauge.createTimeSeries(LABEL_VALUES_1, new QueueManager(), queueManagerFunction);

    Metric metric = derivedLongGauge.getMetric(testClock);
    assertThat(metric).isNotNull();
    assertThat(metric.getMetricDescriptor()).isEqualTo(METRIC_DESCRIPTOR);
    assertThat(metric.getTimeSeriesList().size()).isEqualTo(2);

    derivedLongGauge.clear();
    assertThat(derivedLongGauge.getMetric(testClock)).isNull();
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(derivedLongGauge.getMetric(testClock)).isNull();
  }
}
