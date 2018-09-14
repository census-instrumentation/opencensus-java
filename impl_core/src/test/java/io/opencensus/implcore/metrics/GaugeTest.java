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
import io.opencensus.implcore.metrics.Gauge.DoubleGauge;
import io.opencensus.implcore.metrics.Gauge.LongGauge;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.MetricDescriptor.Type;
import io.opencensus.metrics.Point;
import io.opencensus.metrics.TimeSeries;
import io.opencensus.metrics.Value;
import io.opencensus.testing.common.TestClock;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Gauge}. */
@RunWith(JUnit4.class)
public class GaugeTest {
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<LabelKey> LABEL_KEYS =
      Collections.unmodifiableList(
          Collections.singletonList(LabelKey.create("key", "key description")));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.unmodifiableList(Collections.singletonList(LabelValue.create("value")));
  private static final Object OBJ = new Object();
  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);

  private final Gauge longGauge =
      new LongGauge<Object>(
          NAME,
          DESCRIPTION,
          UNIT,
          LABEL_KEYS,
          LABEL_VALUES,
          OBJ,
          new ToLongFunction<Object>() {
            @Override
            public long applyAsLong(Object value) {
              return value.hashCode();
            }
          });
  private final Gauge doubleGauge =
      new DoubleGauge<Object>(
          NAME,
          DESCRIPTION,
          UNIT,
          LABEL_KEYS,
          LABEL_VALUES,
          OBJ,
          new ToDoubleFunction<Object>() {
            @Override
            public double applyAsDouble(Object value) {
              return value.hashCode();
            }
          });
  private final TestClock testClock = TestClock.create(TEST_TIME);

  @Test
  public void longGauge_GetMetric() {
    assertThat(longGauge.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(NAME, DESCRIPTION, UNIT, Type.GAUGE_INT64, LABEL_KEYS),
                Collections.singletonList(
                    TimeSeries.create(
                        LABEL_VALUES,
                        Collections.singletonList(
                            Point.create(Value.longValue(OBJ.hashCode()), TEST_TIME)),
                        null))));
  }

  @Test
  public void doubleGauge_GetMetric() {
    assertThat(doubleGauge.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(NAME, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, LABEL_KEYS),
                Collections.singletonList(
                    TimeSeries.create(
                        LABEL_VALUES,
                        Collections.singletonList(
                            Point.create(Value.doubleValue(OBJ.hashCode()), TEST_TIME)),
                        null))));
  }
}
