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

/**
 * Constants for collecting rpc stats.
 */
// TODO(songya): change *_COUNT constants to LongMeasure if it's supported in v0.1.
public final class RpcMeasurementConstants {

  // Rpc tag keys.
  public static final TagKey RPC_STATUS = TagKey.create("canonical_status");
  public static final TagKey RPC_CLIENT_METHOD = TagKey.create("method");
  public static final TagKey RPC_SERVER_METHOD = TagKey.create("method");

  // Constants used to define the following Measures.
  private static final String BYTE = "By";
  private static final String COUNT = "1";
  private static final String MILLISECOND = "ms";

  // RPC client Measures.
  public static final Measure RPC_CLIENT_ERROR_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/client/error_count",
          "RPC Errors",
          COUNT);
  public static final Measure RPC_CLIENT_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/request_bytes",
          "Request bytes",
          BYTE);
  public static final Measure RPC_CLIENT_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/response_bytes",
          "Response bytes",
          BYTE);
  public static final Measure RPC_CLIENT_ROUNDTRIP_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/client/roundtrip_latency",
          "RPC roundtrip latency msec",
          MILLISECOND);
  public static final Measure RPC_CLIENT_SERVER_ELAPSED_TIME =
      Measure.MeasureDouble.create(
          "grpc.io/client/server_elapsed_time",
          "Server elapsed time in msecs",
          MILLISECOND);
  public static final Measure RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_request_bytes",
          "Uncompressed Request bytes",
          BYTE);
  public static final Measure RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_response_bytes",
          "Uncompressed Response bytes",
          BYTE);
  public static final Measure RPC_CLIENT_STARTED_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/client/started_count",
          "Number of client RPCs (streams) started",
          COUNT);
  public static final Measure RPC_CLIENT_FINISHED_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/client/finished_count",
          "Number of client RPCs (streams) finished",
          COUNT);
  public static final Measure RPC_CLIENT_REQUEST_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/client/request_count",
          "Number of client RPC request messages",
          COUNT);
  public static final Measure RPC_CLIENT_RESPONSE_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/client/response_count",
          "Number of client RPC response messages",
          COUNT);


  // RPC server Measures.
  public static final Measure RPC_SERVER_ERROR_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/server/error_count",
          "RPC Errors",
          COUNT);
  public static final Measure RPC_SERVER_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/request_bytes",
          "Request bytes",
          BYTE);
  public static final Measure RPC_SERVER_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/response_bytes",
          "Response bytes",
          BYTE);
  public static final Measure RPC_SERVER_SERVER_ELAPSED_TIME =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_elapsed_time",
          "Server elapsed time in msecs",
          MILLISECOND);
  public static final Measure RPC_SERVER_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_latency",
          "Latency in msecs",
          MILLISECOND);
  public static final Measure RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_request_bytes",
          "Uncompressed Request bytes",
          BYTE);
  public static final Measure RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_response_bytes",
          "Uncompressed Response bytes",
          BYTE);
  public static final Measure RPC_SERVER_STARTED_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/server/started_count",
          "Number of server RPCs (streams) started",
          COUNT);
  public static final Measure RPC_SERVER_FINISHED_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/server/finished_count",
          "Number of server RPCs (streams) finished",
          COUNT);
  public static final Measure RPC_SERVER_REQUEST_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/server/request_count",
          "Number of server RPC request messages",
          COUNT);
  public static final Measure RPC_SERVER_RESPONSE_COUNT =
      Measure.MeasureDouble.create(
          "grpc.io/server/response_count",
          "Number of server RPC response messages",
          COUNT);

  // Visible for testing.
  RpcMeasurementConstants() {
    throw new AssertionError();
  }
}
