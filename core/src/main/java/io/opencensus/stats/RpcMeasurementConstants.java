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

import io.opencensus.stats.MeasurementDescriptor.BasicUnit;
import io.opencensus.stats.MeasurementDescriptor.MeasurementUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Constants for collecting rpc stats.
 */
public final class RpcMeasurementConstants {

  // Rpc tag keys.
  public static final TagKey RPC_STATUS = TagKey.create("canonical_status");
  public static final TagKey RPC_CLIENT_METHOD = TagKey.create("method");
  public static final TagKey RPC_SERVER_METHOD = TagKey.create("method");

  // Constants used to define the following MeasurementDescriptors.
  private static final List<BasicUnit> bytes = Arrays.asList(BasicUnit.BYTES);
  private static final List<BasicUnit> scalar = Arrays.asList(BasicUnit.SCALAR);
  private static final List<BasicUnit> seconds = Arrays.asList(BasicUnit.SECONDS);

  // RPC client {@link MeasurementDescriptor}s.
  public static final MeasurementDescriptor RPC_CLIENT_ERROR_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/client/error_count",
          "RPC Errors",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_REQUEST_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/client/request_bytes",
          "Request bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_CLIENT_RESPONSE_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/client/response_bytes",
          "Response bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY =
      MeasurementDescriptor.create(
          "grpc.io/client/roundtrip_latency",
          "RPC roundtrip latency msec",
          MeasurementUnit.create(-3, seconds));
  public static final MeasurementDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME =
      MeasurementDescriptor.create(
          "grpc.io/client/server_elapsed_time",
          "Server elapsed time in msecs",
          MeasurementUnit.create(-3, seconds));
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/client/uncompressed_request_bytes",
          "Uncompressed Request bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/client/uncompressed_response_bytes",
          "Uncompressed Response bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_CLIENT_STARTED_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/client/started_count",
          "Number of client RPCs (streams) started",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_FINISHED_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/client/finished_count",
          "Number of client RPCs (streams) finished",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_REQUEST_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/client/request_count",
          "Number of client RPC request messages",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_CLIENT_RESPONSE_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/client/response_count",
          "Number of client RPC response messages",
          MeasurementUnit.create(0, scalar));


  // RPC server {@link MeasurementDescriptor}s.
  public static final MeasurementDescriptor RPC_SERVER_ERROR_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/server/error_count",
          "RPC Errors",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_SERVER_REQUEST_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/server/request_bytes",
          "Request bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_SERVER_RESPONSE_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/server/response_bytes",
          "Response bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_SERVER_SERVER_ELAPSED_TIME =
      MeasurementDescriptor.create(
          "grpc.io/server/server_elapsed_time",
          "Server elapsed time in msecs",
          MeasurementUnit.create(-3, seconds));
  public static final MeasurementDescriptor RPC_SERVER_SERVER_LATENCY =
      MeasurementDescriptor.create(
          "grpc.io/server/server_latency",
          "Latency in msecs",
          MeasurementUnit.create(-3, seconds));
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/server/uncompressed_request_bytes",
          "Uncompressed Request bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      MeasurementDescriptor.create(
          "grpc.io/server/uncompressed_response_bytes",
          "Uncompressed Response bytes",
          MeasurementUnit.create(0, bytes));
  public static final MeasurementDescriptor RPC_SERVER_STARTED_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/server/started_count",
          "Number of server RPCs (streams) started",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_SERVER_FINISHED_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/server/finished_count",
          "Number of server RPCs (streams) finished",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_SERVER_REQUEST_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/server/request_count",
          "Number of server RPC request messages",
          MeasurementUnit.create(0, scalar));
  public static final MeasurementDescriptor RPC_SERVER_RESPONSE_COUNT =
      MeasurementDescriptor.create(
          "grpc.io/server/response_count",
          "Number of server RPC response messages",
          MeasurementUnit.create(0, scalar));

  // Visible for testing.
  RpcMeasurementConstants() {
    throw new AssertionError();
  }
}
