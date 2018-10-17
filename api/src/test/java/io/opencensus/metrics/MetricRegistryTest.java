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

import static com.google.common.truth.Truth.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistry}. */
@RunWith(JUnit4.class)
public class MetricRegistryTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));
  private final MetricRegistry metricRegistry =
      MetricsComponent.newNoopMetricsComponent().getMetricRegistry();

  @Test
  public void noopAddLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    metricRegistry.addLongGauge(null, DESCRIPTION, UNIT, LABEL_KEY);
  }

  @Test
  public void noopAddLongGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    metricRegistry.addLongGauge(NAME, null, UNIT, LABEL_KEY);
  }

  @Test
  public void noopAddLongGauge_NullUnit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, null, LABEL_KEY);
  }

  @Test
  public void noopAddLongGauge_NullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, null);
  }

  @Test
  public void noopAddLongGauge_WithNullElement() {
    List<LabelKey> labelKeys = Collections.singletonList(null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKey element should not be null.");
    metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, labelKeys);
  }

  @Test
  public void noopSameAs() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    assertThat(longGauge.getDefaultTimeSeries()).isSameAs(longGauge.getDefaultTimeSeries());
    assertThat(longGauge.getDefaultTimeSeries())
        .isSameAs(longGauge.getOrCreateTimeSeries(LABEL_VALUES));
  }

  @Test
  public void noopInstanceOf() {
    LongGauge longGauge = metricRegistry.addLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    assertThat(longGauge)
        .isInstanceOf(LongGauge.newNoopLongGauge(NAME, DESCRIPTION, UNIT, LABEL_KEY).getClass());
  }
}
