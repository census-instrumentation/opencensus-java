package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for RpcConstantsFromProto.
 */
@RunWith(JUnit4.class)
public class RpcConstantsFromProtoTest {
  @Test
  public void testConstants() {
    assertThat(RpcConstantsFromProto.RPC_CLIENT_ERROR_COUNT).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_CLIENT_ROUNDTRIP_LATENCY).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_CLIENT_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_CLIENT_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES).isNotNull();

    assertThat(RpcConstantsFromProto.RPC_SERVER_ERROR_COUNT).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_SERVER_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_SERVER_RESPONSE_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_SERVER_LATENCY).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES).isNotNull();
    assertThat(RpcConstantsFromProto.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES).isNotNull();
  }

  @Test(expected = AssertionError.class)
  public void testConstructor() {
    new RpcConstantsFromProto();
  }
}
