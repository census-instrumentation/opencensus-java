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

import com.google.instrumentation.internal.InstantClock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceComponentImpl}. */
@RunWith(JUnit4.class)
public class TraceComponentImplTest {
  @Test
  public void implementationOfTracer() {
    // TODO(bdrutu): Change this when TracerImpl is available.
    assertThat(Tracing.getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void implementationOfBinaryPropagationHandler() {
    assertThat(Tracing.getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandlerImpl.INSTANCE);
  }

  @Test
  public void implementationOfClock() {
    assertThat(Tracing.getClock()).isInstanceOf(InstantClock.class);
  }
}
