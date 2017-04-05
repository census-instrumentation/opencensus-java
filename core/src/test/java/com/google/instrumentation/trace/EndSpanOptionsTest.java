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

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link EndSpanOptions}. */
@RunWith(JUnit4.class)
public class EndSpanOptionsTest {
  @Test(expected = NullPointerException.class)
  public void setNullStatus() {
    EndSpanOptions.builder().setStatus(null);
  }

  @Test
  public void endSpanOptions_DefaultOptions() {
    assertThat(EndSpanOptions.DEFAULT.getStatus()).isEqualTo(Status.OK);
  }

  @Test
  public void setStatus() {
    EndSpanOptions endSpanOptions =
        EndSpanOptions.builder()
            .setStatus(Status.CANCELLED.withDescription("ThisIsAnError"))
            .build();
    assertThat(endSpanOptions.getStatus())
        .isEqualTo(Status.CANCELLED.withDescription("ThisIsAnError"));
  }

  @Test
  public void endSpanOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        EndSpanOptions.builder()
            .setStatus(Status.CANCELLED.withDescription("ThisIsAnError"))
            .build(),
        EndSpanOptions.builder()
            .setStatus(Status.CANCELLED.withDescription("ThisIsAnError"))
            .build());
    tester.addEqualityGroup(EndSpanOptions.builder().build(), EndSpanOptions.DEFAULT);
    tester.testEquals();
  }

  @Test
  public void endSpanOptions_ToString() {
    EndSpanOptions endSpanOptions =
        EndSpanOptions.builder()
            .setStatus(Status.CANCELLED.withDescription("ThisIsAnError"))
            .build();
    assertThat(endSpanOptions.toString()).contains("ThisIsAnError");
  }
}
