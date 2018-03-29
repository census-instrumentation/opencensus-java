/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.grpc.metrics;

import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagKey;

/**
 * Constants for collecting rpc stats.
 *
 * @since 0.8
 */
public final class RpcMeasureConstants {

  /**
   * Tag key that represents a gRPC canonical status. Refer to
   * https://github.com/grpc/grpc/blob/master/doc/statuscodes.md.
   *
   * @since 0.8
   */
  public static final TagKey RPC_STATUS = TagKey.create("canonical_status");

  /**
   * Tag key that represents a gRPC method.
   *
   * @since 0.8
   */
  public static final TagKey RPC_METHOD = TagKey.create("method");

  // Constants used to define the following Measures.

  /**
   * Unit string for byte.
   *
   * @since 0.8
   */
  private static final String BYTE = "By";

  /**
   * Unit string for count.
   *
   * @since 0.8
   */
  private static final String COUNT = "1";

  /**
   * Unit string for millisecond.
   *
   * @since 0.8
   */
  private static final String MILLISECOND = "ms";

  // RPC client Measures.

  /**
   * {@link Measure} for gRPC client error counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_CLIENT_ERROR_COUNT =
      Measure.MeasureLong.create("grpc.io/client/error_count", "RPC Errors", COUNT);

  /**
   * {@link Measure} for gRPC client request bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_REQUEST_BYTES =
      Measure.MeasureDouble.create("grpc.io/client/request_bytes", "Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC client response bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_RESPONSE_BYTES =
      Measure.MeasureDouble.create("grpc.io/client/response_bytes", "Response bytes", BYTE);

  /**
   * {@link Measure} for gRPC client roundtrip latency in milliseconds.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_ROUNDTRIP_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/client/roundtrip_latency", "RPC roundtrip latency msec", MILLISECOND);

  /**
   * {@link Measure} for gRPC client server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_SERVER_ELAPSED_TIME =
      Measure.MeasureDouble.create(
          "grpc.io/client/server_elapsed_time", "Server elapsed time in msecs", MILLISECOND);

  /**
   * {@link Measure} for gRPC client uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_request_bytes", "Uncompressed Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC client uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_response_bytes", "Uncompressed Response bytes", BYTE);

  /**
   * {@link Measure} for number of started client RPCs.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_CLIENT_STARTED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/client/started_count", "Number of client RPCs (streams) started", COUNT);

  /**
   * {@link Measure} for number of finished client RPCs.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_CLIENT_FINISHED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/client/finished_count", "Number of client RPCs (streams) finished", COUNT);

  /**
   * {@link Measure} for client RPC request message counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_CLIENT_REQUEST_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/client/request_count", "Number of client RPC request messages", COUNT);

  /**
   * {@link Measure} for client RPC response message counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_CLIENT_RESPONSE_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/client/response_count", "Number of client RPC response messages", COUNT);

  // RPC server Measures.

  /**
   * {@link Measure} for gRPC server error counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_SERVER_ERROR_COUNT =
      Measure.MeasureLong.create("grpc.io/server/error_count", "RPC Errors", COUNT);

  /**
   * {@link Measure} for gRPC server request bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_REQUEST_BYTES =
      Measure.MeasureDouble.create("grpc.io/server/request_bytes", "Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC server response bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_RESPONSE_BYTES =
      Measure.MeasureDouble.create("grpc.io/server/response_bytes", "Response bytes", BYTE);

  /**
   * {@link Measure} for gRPC server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_SERVER_ELAPSED_TIME =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_elapsed_time", "Server elapsed time in msecs", MILLISECOND);

  /**
   * {@link Measure} for gRPC server latency in milliseconds.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_latency", "Latency in msecs", MILLISECOND);

  /**
   * {@link Measure} for gRPC server uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_request_bytes", "Uncompressed Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC server uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final MeasureDouble RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_response_bytes", "Uncompressed Response bytes", BYTE);

  /**
   * {@link Measure} for number of started server RPCs.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_SERVER_STARTED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/server/started_count", "Number of server RPCs (streams) started", COUNT);

  /**
   * {@link Measure} for number of finished server RPCs.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_SERVER_FINISHED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/server/finished_count", "Number of server RPCs (streams) finished", COUNT);

  /**
   * {@link Measure} for server RPC request message counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_SERVER_REQUEST_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/server/request_count", "Number of server RPC request messages", COUNT);

  /**
   * {@link Measure} for server RPC response message counts.
   *
   * @since 0.8
   */
  public static final MeasureLong RPC_SERVER_RESPONSE_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/server/response_count", "Number of server RPC response messages", COUNT);

  private RpcMeasureConstants() {}
}
