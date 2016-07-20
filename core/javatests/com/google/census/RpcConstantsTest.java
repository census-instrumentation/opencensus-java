/*
 * Copyright 2016, Google Inc.
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

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link RpcConstants}
 */
@RunWith(JUnit4.class)
public final class RpcConstantsTest {
  @Test
  public void testConstants() {
    assertThat(RpcConstants.RPC_STATUS).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_METHOD).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_METHOD).isNotNull();

    assertThat(RpcConstants.RPC_CLIENT_BYTES_RECEIVED).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_BYTES_SENT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_LATENCY).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_BYTES_RECEIVED).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_BYTES_SENT).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_LATENCY).isNotNull();
  }

  @Test
  public void testConstructor() {
    boolean threw = false;
    try {
      new RpcConstants();
    } catch (AssertionError expected) {
      threw = true;
    }
    assertThat(threw).isTrue();
  }
}
