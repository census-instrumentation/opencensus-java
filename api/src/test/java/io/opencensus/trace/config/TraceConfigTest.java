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

package io.opencensus.trace.config;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.samplers.Samplers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfig}. */
@RunWith(JUnit4.class)
public class TraceConfigTest {
  TraceConfig traceConfig = TraceConfig.getNoopTraceConfig();

  @Test
  public void activeTraceParams_NoOpImplementation() {
    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(TraceParams.DEFAULT);
  }

  @Test
  public void updateActiveTraceParams_NoOpImplementation() {
    TraceParams traceParams =
        TraceParams.DEFAULT
            .toBuilder()
            .setSampler(Samplers.alwaysSample())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfAnnotations(9)
            .setMaxNumberOfNetworkEvents(10)
            .setMaxNumberOfMessageEvents(10)
            .setMaxNumberOfLinks(11)
            .build();
    traceConfig.updateActiveTraceParams(traceParams);
    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(TraceParams.DEFAULT);
  }
}
