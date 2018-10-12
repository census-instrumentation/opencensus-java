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

package io.opencensus.metrics;

import com.google.common.testing.EqualsTester;
import io.opencensus.metrics.LongGauge.LongPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongGauge}. */
@RunWith(JUnit4.class)
public class LongGaugeTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private final MetricRegistry metricRegistry =
      MetricsComponent.newNoopMetricsComponent().getMetricRegistry();
  private static final List<LabelKey> EMPTY_LABEL_KEYS = new ArrayList<LabelKey>();
  private final LongGauge longGauge =
      metricRegistry.addLongGauge("name", "description", "1", EMPTY_LABEL_KEYS);

  @Test
  public void getOrCreateTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues should not be null.");
    longGauge.getOrCreateTimeSeries(null);
  }

  @Test
  public void getOrCreateTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    LongGauge longGauge1 = metricRegistry.addLongGauge("name", "description", "1", labelKeys);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues element should not be null.");
    longGauge1.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void getOrCreateTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    longGauge.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void removeTimeSeries_WithNullLabelValues() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues should not be null.");
    longGauge.removeTimeSeries(null);
  }

  @Test
  public void removeTimeSeries_WithNullElement() {
    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues = Arrays.asList(LabelValue.create("value1"), null);

    LongGauge longGauge1 = metricRegistry.addLongGauge("name", "description", "1", labelKeys);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues element should not be null.");
    longGauge1.removeTimeSeries(labelValues);
  }

  @Test
  public void removeTimeSeries_WithInvalidLabelSize() {
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Incorrect number of labels.");
    longGauge.removeTimeSeries(labelValues);
  }

  @Test
  public void getDefaultTimeSeries() {

    List<LabelKey> labelKeys =
        Arrays.asList(LabelKey.create("key1", "desc"), LabelKey.create("key2", "desc"));
    List<LabelValue> labelValues =
        Arrays.asList(LabelValue.create("value1"), LabelValue.create("value2"));

    LongGauge longGauge1 = metricRegistry.addLongGauge("name", "description", "1", labelKeys);

    LongPoint defaultPoint1 = longGauge1.getDefaultTimeSeries();
    LongPoint defaultPoint2 = longGauge1.getDefaultTimeSeries();

    LongPoint longPoint1 = longGauge1.getOrCreateTimeSeries(labelValues);
    LongPoint longPoint2 = longGauge1.getOrCreateTimeSeries(labelValues);

    new EqualsTester()
        .addEqualityGroup(defaultPoint1, defaultPoint2, longPoint1, longPoint2)
        .testEquals();
  }
}
