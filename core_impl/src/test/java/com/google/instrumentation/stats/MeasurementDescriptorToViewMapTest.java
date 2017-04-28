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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.internal.TestClock;
import com.google.instrumentation.stats.MutableView.MutableDistributionView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasurementDescriptorToViewMap}. */
@RunWith(JUnit4.class)
public class MeasurementDescriptorToViewMapTest {
  private final MeasurementDescriptorToViewMap measurementDescriptorToViewMap =
      new MeasurementDescriptorToViewMap();

  @Test
  public void testPutAndGetView() {
    Clock clock = TestClock.create();
    MutableView expected =
        MutableDistributionView.create(
            RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW, clock.now());
    measurementDescriptorToViewMap.putView(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY.getMeasurementDescriptorName(),
        expected);
    View actual =
        measurementDescriptorToViewMap.getView(
            RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW, clock);
    assertThat(actual.getViewDescriptor()).isEqualTo(expected.getViewDescriptor());
  }
}
