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
import io.opencensus.implcore.temporary.metrics.LabelKey;
import io.opencensus.implcore.temporary.metrics.LabelValue;
import io.opencensus.implcore.temporary.metrics.Metric;
import io.opencensus.implcore.temporary.metrics.MetricDescriptor;
import io.opencensus.implcore.temporary.metrics.MetricDescriptor.Type;
import io.opencensus.implcore.temporary.metrics.Point;
import io.opencensus.implcore.temporary.metrics.TimeSeries;
import io.opencensus.implcore.temporary.metrics.Value;
import io.opencensus.testing.common.TestClock;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistryImpl}. */
@RunWith(JUnit4.class)
public class MetricRegistryImplTest {
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("key", "key description");
  private static final LabelValue LABEL_VALUES = LabelValue.create("value");
  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);

  @Rule public ExpectedException thrown = ExpectedException.none();

  private final TestClock testClock = TestClock.create(TEST_TIME);
  private final MetricRegistryImpl metricRegistry = new MetricRegistryImpl(testClock);
  private final LinkedHashMap<LabelKey, LabelValue> labels =
      new LinkedHashMap<LabelKey, LabelValue>();

  @Before
  public void setUp() {
    labels.put(LABEL_KEY, LABEL_VALUES);
  }

  @Test
  public void addDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(
        null,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
  }

  @Test
  public void addDoubleGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(
        NAME,
        null,
        UNIT,
        labels,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
  }

  @Test
  public void addDoubleGauge_NullUnit() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(
        NAME,
        DESCRIPTION,
        null,
        labels,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
  }

  @Test
  public void addDoubleGauge_NullLabels() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        null,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
  }

  @Test
  public void addDoubleGauge_NullFunction() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(NAME, DESCRIPTION, UNIT, labels, null, null);
  }

  @Test
  public void addDoubleGauge_GetMetrics() {
    metricRegistry.addDoubleGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
    assertThat(metricRegistry.getMetrics())
        .containsExactly(
            Metric.create(
                MetricDescriptor.create(
                    NAME,
                    DESCRIPTION,
                    UNIT,
                    Type.GAUGE_DOUBLE,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(Point.create(Value.doubleValue(5.0), TEST_TIME)),
                        null))));
  }

  @Test
  public void addLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        null,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 7;
          }
        });
  }

  @Test
  public void addLongGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        NAME,
        null,
        UNIT,
        labels,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 5;
          }
        });
  }

  @Test
  public void addLongGauge_NullUnit() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        NAME,
        DESCRIPTION,
        null,
        labels,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 5;
          }
        });
  }

  @Test
  public void addLongGauge_NullLabels() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        null,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 5;
          }
        });
  }

  @Test
  public void addLongGauge_NullFunction() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge("name", DESCRIPTION, UNIT, labels, null, null);
  }

  @Test
  public void addLongGauge_GetMetrics() {
    metricRegistry.addLongGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 7;
          }
        });
    assertThat(metricRegistry.getMetrics())
        .containsExactly(
            Metric.create(
                MetricDescriptor.create(
                    NAME,
                    DESCRIPTION,
                    UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(Point.create(Value.longValue(7), TEST_TIME)),
                        null))));
  }

  @Test
  public void multipleMetrics_GetMetrics() {
    metricRegistry.addLongGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 7;
          }
        });
    metricRegistry.addDoubleGauge(
        NAME,
        DESCRIPTION,
        UNIT,
        labels,
        null,
        new ToDoubleFunction<Object>() {
          @Override
          public double applyAsDouble(Object value) {
            return 5.0;
          }
        });
    assertThat(metricRegistry.getMetrics())
        .containsExactly(
            Metric.create(
                MetricDescriptor.create(
                    NAME,
                    DESCRIPTION,
                    UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(Point.create(Value.longValue(7), TEST_TIME)),
                        null))),
            Metric.create(
                MetricDescriptor.create(
                    NAME,
                    DESCRIPTION,
                    UNIT,
                    Type.GAUGE_DOUBLE,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(Point.create(Value.doubleValue(5.0), TEST_TIME)),
                        null))));
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(metricRegistry.getMetrics()).isEmpty();
  }
}
