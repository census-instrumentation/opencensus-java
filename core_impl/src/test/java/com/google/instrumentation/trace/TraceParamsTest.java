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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceParams}. */
@RunWith(JUnit4.class)
public class TraceParamsTest {

  @Test
  public void defaultTraceParams() {
    assertThat(TraceParams.DEFAULT.getSampler()).isEqualTo(TraceParams.DEFAULT_SAMPLERS);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfAttributes())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfAnnotations())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ANNOTATIONS);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfNetworkEvents())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_NETWORK_EVENTS);
    assertThat(TraceParams.DEFAULT.getMaxNumberOfLinks())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_LINKS);
  }

  @Test
  public void updateTraceParams_OnlySampler() {
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setSampler(Samplers.alwaysSample()).build();
    assertThat(traceParams.getSampler()).isEqualTo(Samplers.alwaysSample());
    assertThat(traceParams.getMaxNumberOfAttributes())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(traceParams.getMaxNumberOfAnnotations())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ANNOTATIONS);
    assertThat(traceParams.getMaxNumberOfNetworkEvents())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_NETWORK_EVENTS);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_LINKS);
  }

  @Test
  public void updateTraceParams_OnlyMaxNumberOfAttributes() {
    TraceParams traceParams = TraceParams.DEFAULT.toBuilder().setMaxNumberOfAttributes(8).build();
    assertThat(traceParams.getSampler()).isEqualTo(TraceParams.DEFAULT_SAMPLERS);
    assertThat(traceParams.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(traceParams.getMaxNumberOfAnnotations())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ANNOTATIONS);
    assertThat(traceParams.getMaxNumberOfNetworkEvents())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_NETWORK_EVENTS);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_LINKS);
  }

  @Test
  public void updateTraceParams_OnlyMaxNumberOfAnnotations() {
    TraceParams traceParams = TraceParams.DEFAULT.toBuilder().setMaxNumberOfAnnotations(8).build();
    assertThat(traceParams.getSampler()).isEqualTo(TraceParams.DEFAULT_SAMPLERS);
    assertThat(traceParams.getMaxNumberOfAttributes())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(traceParams.getMaxNumberOfAnnotations()).isEqualTo(8);
    assertThat(traceParams.getMaxNumberOfNetworkEvents())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_NETWORK_EVENTS);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_LINKS);
  }

  @Test
  public void updateTraceParams_OnlyMaxNumberOfNetworkEvents() {
    TraceParams traceParams =
        TraceParams.DEFAULT.toBuilder().setMaxNumberOfNetworkEvents(8).build();
    assertThat(traceParams.getSampler()).isEqualTo(TraceParams.DEFAULT_SAMPLERS);
    assertThat(traceParams.getMaxNumberOfAttributes())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(traceParams.getMaxNumberOfAnnotations())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ANNOTATIONS);
    assertThat(traceParams.getMaxNumberOfNetworkEvents()).isEqualTo(8);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_LINKS);
  }

  @Test
  public void updateTraceParams_OnlyMaxNumberOfLinks() {
    TraceParams traceParams = TraceParams.DEFAULT.toBuilder().setMaxNumberOfLinks(8).build();
    assertThat(traceParams.getSampler()).isEqualTo(TraceParams.DEFAULT_SAMPLERS);
    assertThat(traceParams.getMaxNumberOfAttributes())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ATTRIBUTES);
    assertThat(traceParams.getMaxNumberOfAnnotations())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_ANNOTATIONS);
    assertThat(traceParams.getMaxNumberOfNetworkEvents())
        .isEqualTo(TraceParams.DEFAULT_SPAN_MAX_NUM_NETWORK_EVENTS);
    assertThat(traceParams.getMaxNumberOfLinks()).isEqualTo(8);
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
