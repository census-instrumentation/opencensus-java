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

package io.opencensus.implcore.temporary.metrics;

import io.opencensus.common.ToDoubleFunction;
import io.opencensus.common.ToLongFunction;
import java.util.LinkedHashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricRegistry}. */
@RunWith(JUnit4.class)
public class MetricRegistryTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private final MetricRegistry metricRegistry =
      MetricsComponent.newNoopMetricsComponent().getMetricRegistry();

  @Test
  public void addDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addDoubleGauge(
        null,
        "description",
        "1",
        new LinkedHashMap<LabelKey, LabelValue>(),
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
        "name",
        null,
        "1",
        new LinkedHashMap<LabelKey, LabelValue>(),
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
        "name",
        "description",
        null,
        new LinkedHashMap<LabelKey, LabelValue>(),
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
        "name",
        "description",
        "1",
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
    metricRegistry.addDoubleGauge(
        "name", "description", "1", new LinkedHashMap<LabelKey, LabelValue>(), null, null);
  }

  @Test
  public void addLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        null,
        "description",
        "1",
        new LinkedHashMap<LabelKey, LabelValue>(),
        null,
        new ToLongFunction<Object>() {
          @Override
          public long applyAsLong(Object value) {
            return 5;
          }
        });
  }

  @Test
  public void addLongGauge_NullDescription() {
    thrown.expect(NullPointerException.class);
    metricRegistry.addLongGauge(
        "name",
        null,
        "1",
        new LinkedHashMap<LabelKey, LabelValue>(),
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
        "name",
        "description",
        null,
        new LinkedHashMap<LabelKey, LabelValue>(),
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
        "name",
        "description",
        "1",
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
    metricRegistry.addLongGauge(
        "name", "description", "1", new LinkedHashMap<LabelKey, LabelValue>(), null, null);
  }
}
