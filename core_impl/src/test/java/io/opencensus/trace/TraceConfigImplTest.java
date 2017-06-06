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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.TraceConfig.TraceParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfigImpl}. */
@RunWith(JUnit4.class)
public class TraceConfigImplTest {
  private final TraceConfigImpl traceConfig = new TraceConfigImpl();

  @Test
  public void defaultActiveTraceParams() {
    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(TraceParams.DEFAULT);
  }

  @Test
  public void updateTraceParams() {
    TraceParams traceParams =
        TraceParams.DEFAULT
            .toBuilder()
            .setSampler(Samplers.alwaysSample())
            .setMaxNumberOfAttributes(8)
            .setMaxNumberOfAnnotations(9)
            .setMaxNumberOfNetworkEvents(10)
            .setMaxNumberOfLinks(11)
            .build();
    traceConfig.updateActiveTraceParams(traceParams);
    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(traceParams);
    traceConfig.updateActiveTraceParams(TraceParams.DEFAULT);
    assertThat(traceConfig.getActiveTraceParams()).isEqualTo(TraceParams.DEFAULT);
  }
}
