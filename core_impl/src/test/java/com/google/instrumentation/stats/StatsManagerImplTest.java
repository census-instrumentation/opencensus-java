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

import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.View.DistributionView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StatsManagerImpl}.
 */
@RunWith(JUnit4.class)
public class StatsManagerImplTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private final StatsManager statsManager = new StatsManagerImpl();

  @Test
  public void testRegisterAndGetView() throws Exception {
    View expected = DistributionView.create(
        RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW,
        null,
        Timestamp.fromMillis(System.currentTimeMillis()),
        Timestamp.fromMillis(System.currentTimeMillis()));
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // TODO(songya): verify if the distributions of actual and expected view are equal.
  }

  @Test
  public void testRegisterUnsupportedViewDesciptor() throws Exception {
    thrown.expect(UnsupportedOperationException.class);
    statsManager.registerView(RpcConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }

  @Test
  public void testRegisterViewDesciptorTwice(){
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
  }

  @Test
  public void testGetNonexistentView() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    statsManager.getView(RpcConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }
}