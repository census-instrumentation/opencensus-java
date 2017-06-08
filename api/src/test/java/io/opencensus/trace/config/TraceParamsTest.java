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

package io.opencensus.trace.config;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.samplers.Samplers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceParams}. */
@RunWith(JUnit4.class)
public class TraceParamsTest {
  @Test
  public void defaultTraceParams() {
    assertThat(TraceParams.DEFAULT.getSampler()).isEqualTo(Samplers.probabilitySampler(1e-4));
    assertThat(TraceParams.DEFAULT.getMaxNumberOfAttributes()).isEqualTo(32);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfAnnotations()).isEqualTo(32);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfNetworkEvents()).isEqualTo(128);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfLinks()).isEqualTo(128);
  }

  @Test(expected = NullPointerException.class)
  public void updateTraceParams_NullSampler() {
    TraceParams.DEFAULT.toBuilder().setSampler(null).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateTraceParams_NonPositiveMaxNumberOfAttributes() {
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(0).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateTraceParams_NonPositiveMaxNumberOfAnnotations() {
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfAnnotations(0).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateTraceParams_NonPositiveMaxNumberOfNetworkEvents() {
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfNetworkEvents(0).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateTraceParams_NonPositiveMaxNumberOfLinks() {
    TraceParams.DEFAULT.toBuilder().setMaxNumberOfLinks(0).build();
  }

  @Test
  public void updateTraceParams_All() {
    TraceParams traceParams =
        TraceParams.DEFAULT
            .toBuilder()
            .setSampler(Samplers.alwaysSample())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfAnnotations(9)
            .setMaxNumberOfNetworkEvents(10)
            .setMaxNumberOfLinks(11)
            .build();
    assertThat(traceParams.getSampler()).isEqualTo(Samplers.alwaysSample());
    assertThat(traceParams.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceParams.getMaxNumberOfAnnotations()).isEqualTo(9);
    assertThat(traceParams.getMaxNumberOfNetworkEvents()).isEqualTo(10);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(11);
  }
}
