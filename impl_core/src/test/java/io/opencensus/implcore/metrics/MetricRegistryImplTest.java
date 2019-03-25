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
import io.opencensus.common.ToLongFunction;
import io.opencensus.metrics.DerivedDoubleGauge;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.DoubleGauge;
import io.opencensus.metrics.DoubleGauge.DoublePoint;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.MetricOptions;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistryImpl}. */
@RunWith(JUnit4.class)
public class MetricRegistryImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "test_name";
  private static final String NAME_2 = "test_name2";
  private static final String NAME_3 = "test_name3";
  private static final String NAME_4 = "test_name4";
  private static final String DESCRIPTION = "test_description";
  private static final String UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("test_key", "test key description");
  private static final List<LabelKey> LABEL_KEYS = Collections.singletonList(LABEL_KEY);
  private static final LabelValue LABEL_VALUE = LabelValue.create("test_value");
  private static final List<LabelValue> LABEL_VALUES = Collections.singletonList(LABEL_VALUE);
  private static final MetricOptions METRIC_OPTIONS =
      MetricOptions.builder()
          .setDescription(DESCRIPTION)
          .setUnit(UNIT)
          .setLabelKeys(LABEL_KEYS)
          .build();

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);
  private final MetricRegistryImpl metricRegistry = new MetricRegistryImpl(testClock);

  private static final MetricDescriptor LONG_METRIC_DESCRIPTOR =
      MetricDescriptor.create(NAME, DESCRIPTION, UNIT, Type.GAUGE_INT64, LABEL_KEYS);
  private static final MetricDescriptor DOUBLE_METRIC_DESCRIPTOR =
      MetricDescriptor.create(NAME_2, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, LABEL_KEYS);
  private static final MetricDescriptor DERIVED_LONG_METRIC_DESCRIPTOR =
      MetricDescriptor.create(NAME_3, DESCRIPTION, UNIT, Type.GAUGE_INT64, LABEL_KEYS);
  private static final MetricDescriptor DERIVED_DOUBLE_METRIC_DESCRIPTOR =
      MetricDescriptor.create(NAME_4, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, LABEL_KEYS);

  private static final ToLongFunction<Object> longFunction =
      new ToLongFunction<Object>() {
        @Override
        public long applyAsLong(Object value) {
          return 5;
        }
      };
  private static final ToDoubleFunction<Object> doubleFunction =
      new ToDoubleFunction<Object>() {
        @Override
        public double applyAsDouble(Object value) {
          return 5.0;
        }
      };

  @Test
  public void addLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void addDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void addDerivedLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedLongGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void addDerivedDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addDerivedDoubleGauge(null, METRIC_OPTIONS);
  }

  @Test
  public void addLongGauge_GetMetrics() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, METRIC_OPTIONS);
    longGauge.getOrCreateTimeSeries(LABEL_VALUES);

    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(1);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                LONG_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(0), TEST_TIME), null)));
  }

  @Test
  public void addDoubleGauge_GetMetrics() {
    DoubleGauge doubleGauge = metricRegistry.addDoubleGauge(NAME_2, METRIC_OPTIONS);
    doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(1);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                DOUBLE_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(0.0), TEST_TIME), null)));
  }

  @Test
  public void addDerivedLongGauge_GetMetrics() {
    DerivedLongGauge derivedLongGauge = metricRegistry.addDerivedLongGauge(NAME_3, METRIC_OPTIONS);
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, longFunction);
    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(1);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                DERIVED_LONG_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(5), TEST_TIME), null)));
  }

  @Test
  public void addDerivedDoubleGauge_GetMetrics() {
    DerivedDoubleGauge derivedDoubleGauge =
        metricRegistry.addDerivedDoubleGauge(NAME_4, METRIC_OPTIONS);
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, doubleFunction);
    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(1);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                DERIVED_DOUBLE_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(5.0), TEST_TIME), null)));
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(metricRegistry.getMetricProducer().getMetrics()).isEmpty();
  }

  @Test
  public void checkInstanceOf() {
    assertThat(metricRegistry.addLongGauge(NAME, METRIC_OPTIONS)).isInstanceOf(LongGaugeImpl.class);
    assertThat(metricRegistry.addDoubleGauge(NAME_2, METRIC_OPTIONS))
        .isInstanceOf(DoubleGaugeImpl.class);
    assertThat(metricRegistry.addDerivedLongGauge(NAME_3, METRIC_OPTIONS))
        .isInstanceOf(DerivedLongGaugeImpl.class);
    assertThat(metricRegistry.addDerivedDoubleGauge(NAME_4, METRIC_OPTIONS))
        .isInstanceOf(DerivedDoubleGaugeImpl.class);
  }

  @Test
  public void getMetrics() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, METRIC_OPTIONS);
    LongPoint longPoint = longGauge.getOrCreateTimeSeries(LABEL_VALUES);
    longPoint.set(200);
    DoubleGauge doubleGauge = metricRegistry.addDoubleGauge(NAME_2, METRIC_OPTIONS);
    DoublePoint doublePoint = doubleGauge.getOrCreateTimeSeries(LABEL_VALUES);
    doublePoint.set(-300.13);
    DerivedLongGauge derivedLongGauge = metricRegistry.addDerivedLongGauge(NAME_3, METRIC_OPTIONS);
    derivedLongGauge.createTimeSeries(LABEL_VALUES, null, longFunction);
    DerivedDoubleGauge derivedDoubleGauge =
        metricRegistry.addDerivedDoubleGauge(NAME_4, METRIC_OPTIONS);
    derivedDoubleGauge.createTimeSeries(LABEL_VALUES, null, doubleFunction);

    Collection<Metric> metricCollections = metricRegistry.getMetricProducer().getMetrics();
    assertThat(metricCollections.size()).isEqualTo(4);
    assertThat(metricCollections)
        .containsExactly(
            Metric.createWithOneTimeSeries(
                LONG_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(200), TEST_TIME), null)),
            Metric.createWithOneTimeSeries(
                DOUBLE_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(-300.13), TEST_TIME), null)),
            Metric.createWithOneTimeSeries(
                DERIVED_LONG_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.longValue(5), TEST_TIME), null)),
            Metric.createWithOneTimeSeries(
                DERIVED_DOUBLE_METRIC_DESCRIPTOR,
                TimeSeries.createWithOnePoint(
                    LABEL_VALUES, Point.create(Value.doubleValue(5.0), TEST_TIME), null)));
  }

  @Test
  public void registerDifferentMetricSameName() {
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEYS);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("A different metric with the same name already registered.");
    metricRegistry.addDoubleGauge(NAME, DESCRIPTION, UNIT, LABEL_KEYS);
  }
}
