/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.trace.export.ExportComponentImpl;
import io.opencensus.implcore.trace.internal.RandomHandler.SecureRandomHandler;
import io.opencensus.implcore.trace.propagation.PropagationComponentImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceComponentImplBase}. */
@RunWith(JUnit4.class)
public class TraceComponentImplBaseTest {
  private final TraceComponentImplBase traceComponentImplBase =
      new TraceComponentImplBase(
          MillisClock.getInstance(), new SecureRandomHandler(), new SimpleEventQueue());

  @Test
  public void implementationOfTracer() {
    assertThat(traceComponentImplBase.getTracer()).isInstanceOf(TracerImpl.class);
  }

  @Test
  public void implementationOfBinaryPropagationHandler() {
    assertThat(traceComponentImplBase.getPropagationComponent())
        .isInstanceOf(PropagationComponentImpl.class);
  }

  @Test
  public void implementationOfClock() {
    assertThat(traceComponentImplBase.getClock()).isInstanceOf(MillisClock.class);
  }

  @Test
  public void implementationOfTraceExporter() {
    assertThat(traceComponentImplBase.getExportComponent()).isInstanceOf(ExportComponentImpl.class);
  }
}
