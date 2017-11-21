/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Tracing}. */
@RunWith(JUnit4.class)
public class TracingTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void loadTraceComponent_UsesProvidedClassLoader() {
    final RuntimeException toThrow = new RuntimeException("UseClassLoader");
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("UseClassLoader");
    Tracing.loadTraceComponent(
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) {
            throw toThrow;
          }
        });
  }

  @Test
  public void loadTraceComponent_IgnoresMissingClasses() {
    ClassLoader classLoader =
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
          }
        };
    assertThat(Tracing.loadTraceComponent(classLoader).getClass().getName())
        .isEqualTo("io.opencensus.trace.TraceComponent$NoopTraceComponent");
  }

  @Test
  public void defaultTracer() {
    assertThat(Tracing.getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void defaultBinaryPropagationHandler() {
    assertThat(Tracing.getPropagationComponent())
        .isSameAs(PropagationComponent.getNoopPropagationComponent());
  }

  @Test
  public void defaultTraceExporter() {
    assertThat(Tracing.getExportComponent())
        .isInstanceOf(ExportComponent.newNoopExportComponent().getClass());
  }

  @Test
  public void defaultTraceConfig() {
    assertThat(Tracing.getTraceConfig()).isSameAs(TraceConfig.getNoopTraceConfig());
  }
}
