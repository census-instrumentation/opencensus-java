/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

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
  public void loadTraceService_UsesProvidedClassLoader() {
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
  public void loadSpanFactory_IgnoresMissingClasses() {
    assertThat(
            Tracing.loadTraceComponent(
                    new ClassLoader() {
                      @Override
                      public Class<?> loadClass(String name) throws ClassNotFoundException {
                        throw new ClassNotFoundException();
                      }
                    })
                .getClass()
                .getName())
        .isEqualTo("io.opencensus.trace.TraceComponent$NoopTraceComponent");
  }

  @Test
  public void defaultTracer() {
    assertThat(Tracing.getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void defaultBinaryPropagationHandler() {
    assertThat(Tracing.getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandler.getNoopBinaryPropagationHandler());
  }

  @Test
  public void defaultTraceExporter() {
    assertThat(Tracing.getTraceExporter()).isSameAs(TraceExporter.getNoopTraceExporter());
  }

  @Test
  public void defaultTraceConfig() {
    assertThat(Tracing.getTraceConfig()).isSameAs(TraceConfig.getNoopTraceConfig());
  }
}
