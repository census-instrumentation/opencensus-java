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
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
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
  private final List<LabelKey> labelKeys = new ArrayList<LabelKey>();
  private final List<LabelValue> labelValues = new ArrayList<LabelValue>();

  @Before
  public void setUp() {
    labelKeys.add(LABEL_KEY);
    labelValues.add(LABEL_VALUES);
  }

  // helper class
  public static class TotalMemory {
    public double getUsed() {
      return 2.13;
    }

    public long getAvailable() {
      return 7;
    }
  }

  @Test
  public void addLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(null, DESCRIPTION, UNIT, labelKeys);
  }

  @Test
  public void addLongGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(NAME, null, UNIT, labelKeys);
  }

  @Test
  public void addLongGauge_NullUnit() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(NAME, DESCRIPTION, null, labelKeys);
  }

  @Test
  public void addLongGauge_NullLabels() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, null);
  }

  @Test
  public void addLongGauge_SameMetricName() {
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, labelKeys);
    thrown.expect(IllegalArgumentException.class);
    metricRegistry.addLongGauge(NAME, "desc", UNIT, labelKeys);
  }

  @Test
  public void multipleMetrics_GetMetrics() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, labelKeys);
    LongPoint longPoint = longGauge.getOrCreateTimeSeries(labelValues);
    longPoint.add(7);

    assertThat(metricRegistry.getMetricProducer().getMetrics())
        .containsExactly(
            Metric.create(
                MetricDescriptor.create(
                    NAME,
                    DESCRIPTION,
                    UNIT,
                    Type.GAUGE_INT64,
                    Collections.singletonList(LABEL_KEY)),
                Collections.singletonList(
                    TimeSeries.createWithOnePoint(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Point.create(Value.longValue(7), TEST_TIME),
                        null))));
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(metricRegistry.getMetricProducer().getMetrics()).isEmpty();
  }
}
