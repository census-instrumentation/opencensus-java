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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link RpcViewConstants}.
 */
@RunWith(JUnit4.class)
public final class RpcViewConstantsTest {

  @Test
  public void testConstants() {
    // Test client distribution view descriptors.
    assertThat(RpcViewConstants.RPC_CLIENT_ERROR_COUNT_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW).isNotNull();

    // Test server distribution view descriptors.
    assertThat(RpcViewConstants.RPC_SERVER_ERROR_COUNT_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_SERVER_LATENCY_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_VIEW).isNotNull();

    // Test client interval view descriptors.
    assertThat(RpcViewConstants.RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW).isNotNull();

    assertThat(RpcViewConstants.RPC_CLIENT_ERROR_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_STARTED_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW).isNotNull();

    // Test server interval view descriptors.
    assertThat(RpcViewConstants.RPC_SERVER_ERROR_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_STARTED_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW).isNotNull();

    assertThat(RpcViewConstants.RPC_SERVER_ERROR_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_SERVER_LATENCY_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_STARTED_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_FINISHED_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_REQUEST_COUNT_HOUR_VIEW).isNotNull();
    assertThat(RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW).isNotNull();
  }

  @Test(expected = AssertionError.class)
  public void testConstructor() {
    new RpcViewConstants();
  }
}
