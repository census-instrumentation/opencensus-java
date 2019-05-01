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

package io.opencensus.trace.propagation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link PropagationComponent}. */
@RunWith(JUnit4.class)
public class PropagationComponentTest {
  private final PropagationComponent propagationComponent =
      PropagationComponent.getNoopPropagationComponent();

  @Test
  public void implementationOfBinaryFormat() {
    assertThat(propagationComponent.getBinaryFormat())
        .isSameInstanceAs(BinaryFormat.getNoopBinaryFormat());
  }

  @Test
  public void implementationOfB3Format() {
    assertThat(propagationComponent.getB3Format()).isSameInstanceAs(TextFormat.getNoopTextFormat());
  }

  @Test
  public void implementationOfTraceContextFormat() {
    assertThat(propagationComponent.getTraceContextFormat())
        .isSameInstanceAs(TextFormat.getNoopTextFormat());
  }
}
