/*
 * Copyright 2017, Google Inc.
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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.internal.ZeroTimeClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceComponent}. */
@RunWith(JUnit4.class)
public class TraceComponentTest {
  @Test
  public void defaultTracer() {
    assertThat(TraceComponent.getNoopTraceComponent().getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void defaultBinaryPropagationHandler() {
    assertThat(TraceComponent.getNoopTraceComponent().getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandler.getNoopBinaryPropagationHandler());
  }

  @Test
  public void defaultClock() {
    assertThat(TraceComponent.getNoopTraceComponent().getClock()).isInstanceOf(ZeroTimeClock.class);
  }

  @Test
  public void defaultTraceExporter() {
    assertThat(TraceComponent.getNoopTraceComponent().getTraceExporter())
        .isSameAs(TraceExporter.getNoopTraceExporter());
  }
}
