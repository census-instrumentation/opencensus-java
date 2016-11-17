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

package com.google.instrumentation.stats;

import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Census constants for collecting rpc stats.
 */
public final class RpcConstants {
  /**
   * Census defined tag keys.
   */
  public static final TagKey RPC_STATUS = new TagKey("/rpc/status");
  public static final TagKey RPC_CLIENT_METHOD = new TagKey("/rpc/client_method");
  public static final TagKey RPC_SERVER_METHOD = new TagKey("/rpc/server_method");


  private static final List<BasicUnit> bytes = Arrays.asList(new BasicUnit[] { BasicUnit.BYTES });
  private static final List<BasicUnit> scalar = Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR });
  private static final List<BasicUnit> secs = Arrays.asList(new BasicUnit[] { BasicUnit.SECS });

  /**
   * Census defined measurement descriptors.
   */
  public static final MeasurementDescriptor RPC_CLIENT_ERROR_COUNT =
      new MeasurementDescriptor(
          "/rpc/client/error_count",
          "RPC Errors",
          new MeasurementUnit(1, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_REQUEST_BYTES =
      new MeasurementDescriptor(
          "/rpc/client/request_bytes",
          "Request MB/s",
          new MeasurementUnit(6, bytes, secs));
  public static final MeasurementDescriptor RPC_CLIENT_RESPONSE_BYTES =
      new MeasurementDescriptor(
          "/rpc/client/response_bytes",
          "Response MB/s",
          new MeasurementUnit(6, bytes, secs));
  // TODO(dpo): verify this encoding
  public static final MeasurementDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY =
      new MeasurementDescriptor(
          "/rpc/client/roundtrip_latency",
          "RPC roundtrip latency us",
          new MeasurementUnit(1, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      new MeasurementDescriptor(
          "/rpc/client/uncompressed_request_bytes",
          "Uncompressed Request MB/s",
          new MeasurementUnit(6, bytes, secs));
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      new MeasurementDescriptor(
          "/rpc/client/uncompressed_response_bytes",
          "Uncompressed Request MB/s",
          new MeasurementUnit(6, bytes, secs));

  public static final MeasurementDescriptor RPC_SERVER_ERROR_COUNT =
      new MeasurementDescriptor(
          "/rpc/server/error_count",
          "RPC Errors",
          new MeasurementUnit(1, scalar));
  public static final MeasurementDescriptor RPC_SERVER_REQUEST_BYTES =
      new MeasurementDescriptor(
          "/rpc/server/request_bytes",
          "Request MB/s",
          new MeasurementUnit(6, bytes, secs));
  public static final MeasurementDescriptor RPC_SERVER_RESPONSE_BYTES =
      new MeasurementDescriptor(
          "/rpc/server/response_bytes",
          "Response MB/s",
          new MeasurementUnit(6, bytes, secs));
  public static final MeasurementDescriptor RPC_SERVER_SERVER_LATENCY =
      new MeasurementDescriptor(
          "/rpc/server/server_latency",
          "Latency in msecs",
          new MeasurementUnit(3, scalar));
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      new MeasurementDescriptor(
          "/rpc/server/uncompressed_request_bytes",
          "Uncompressed Request MB/s",
          new MeasurementUnit(6, bytes, secs));
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      new MeasurementDescriptor(
          "/rpc/server/uncompressed_response_bytes",
          "Uncompressed Request MB/s",
          new MeasurementUnit(6, bytes, secs));

  // Visible for testing
  RpcConstants() {
    throw new AssertionError();
  }
}
