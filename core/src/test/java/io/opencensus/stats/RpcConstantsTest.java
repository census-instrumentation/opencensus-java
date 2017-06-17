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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link RpcConstants}
 */
@Deprecated
@RunWith(JUnit4.class)
public final class RpcConstantsTest {
  @Test
  public void testConstants() {
    assertThat(RpcConstants.RPC_STATUS).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_METHOD).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_METHOD).isNotNull();

    // Test client measurement descriptors.
    assertThat(RpcConstants.RPC_CLIENT_ERROR_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_STARTED_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_FINISHED_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_SERVER_ELAPSED_TIME).isNotNull();

    // Test server measurement descriptors.
    assertThat(RpcConstants.RPC_SERVER_ERROR_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_SERVER_LATENCY).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_STARTED_COUNT).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_FINISHED_COUNT).isNotNull();

    // Test client distribution view descriptors.
    assertThat(RpcConstants.RPC_CLIENT_ERROR_COUNT_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_COUNT_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW).isNotNull();

    // Test server distribution view descriptors.
    assertThat(RpcConstants.RPC_SERVER_ERROR_COUNT_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_SERVER_LATENCY_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_COUNT_VIEW).isNotNull();

    // Test client interval view descriptors.
    assertThat(RpcConstants.RPC_CLIENT_ERROR_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_STARTED_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_FINISHED_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_REQUEST_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_CLIENT_RESPONSE_COUNT_INTERVAL_VIEW).isNotNull();

    // Test server interval view descriptors.
    assertThat(RpcConstants.RPC_SERVER_ERROR_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_SERVER_LATENCY_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_STARTED_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_FINISHED_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_REQUEST_COUNT_INTERVAL_VIEW).isNotNull();
    assertThat(RpcConstants.RPC_SERVER_RESPONSE_COUNT_INTERVAL_VIEW).isNotNull();
  }

  @Test(expected = AssertionError.class)
  public void testConstructor() {
    new RpcConstants();
  }
}
