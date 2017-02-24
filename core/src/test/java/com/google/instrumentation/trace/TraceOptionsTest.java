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

/** Unit tests for {@link TraceOptions}. */
@RunWith(JUnit4.class)
public class TraceOptionsTest {
  private static final TraceOptions noneEnabled = TraceOptions.getDefault();
  private static final TraceOptions sampledOptions = new TraceOptions(TraceOptions.IS_SAMPLED);

  @Test
  public void getOptions() {
    assertThat(noneEnabled.getOptions()).isEqualTo(0);
    assertThat(sampledOptions.getOptions()).isEqualTo(TraceOptions.IS_SAMPLED);
  }

  @Test
  public void isSampled() {
    assertThat(noneEnabled.isSampled()).isFalse();
    assertThat(sampledOptions.isSampled()).isTrue();
  }

  @Test
  public void traceOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(noneEnabled, TraceOptions.getDefault());
    tester.addEqualityGroup(sampledOptions, new TraceOptions(TraceOptions.IS_SAMPLED));
    tester.testEquals();
  }

  @Test
  public void traceOptions_ToString() {
    assertThat(noneEnabled.toString()).contains("sampled=false");
    assertThat(sampledOptions.toString()).contains("sampled=true");
  }
}
