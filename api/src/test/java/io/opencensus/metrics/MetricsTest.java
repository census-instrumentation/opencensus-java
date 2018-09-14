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

import io.opencensus.metrics.export.ExportComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Metrics}. */
@RunWith(JUnit4.class)
public class MetricsTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void loadMetricsComponent_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    Metrics.loadMetricsComponent(
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) {
            throw toThrow;
          }
        });
  }

  @Test
  public void loadMetricsComponent_IgnoresMissingClasses() {
    ClassLoader classLoader =
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
          }
        };
    assertThat(Metrics.loadMetricsComponent(classLoader).getClass().getName())
        .isEqualTo("io.opencensus.metrics.MetricsComponent$NoopMetricsComponent");
  }

  @Test
  public void defaultExportComponent() {
    assertThat(Metrics.getExportComponent())
        .isInstanceOf(ExportComponent.newNoopExportComponent().getClass());
  }

  @Test
  public void defaultMetricRegistry() {
    assertThat(Metrics.getMetricRegistry())
        .isInstanceOf(MetricRegistry.newNoopMetricRegistry().getClass());
  }
}
