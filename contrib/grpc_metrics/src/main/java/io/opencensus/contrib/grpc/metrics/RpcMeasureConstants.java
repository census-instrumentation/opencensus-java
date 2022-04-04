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
   * @deprecated in favor of {@link #GRPC_CLIENT_STATUS} and {@link #GRPC_SERVER_STATUS}.
   */
  @Deprecated public static final TagKey RPC_STATUS = TagKey.create("canonical_status");

  /**
   * Tag key that represents a gRPC method.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_METHOD} and {@link #GRPC_SERVER_METHOD}.
   */
  @Deprecated public static final TagKey RPC_METHOD = TagKey.create("method");

  /**
   * Tag key that represents a client gRPC canonical status. Refer to
   * https://github.com/grpc/grpc/blob/master/doc/statuscodes.md.
   *
   * <p>{@link #GRPC_CLIENT_STATUS} is set when an outgoing request finishes and is only available
   * around metrics recorded at the end of the outgoing request.
   *
   * @since 0.13
   */
  public static final TagKey GRPC_CLIENT_STATUS = TagKey.create("grpc_client_status");

  /**
   * Tag key that represents a server gRPC canonical status. Refer to
   * https://github.com/grpc/grpc/blob/master/doc/statuscodes.md.
   *
   * <p>{@link #GRPC_SERVER_STATUS} is set when an incoming request finishes and is only available
   * around metrics recorded at the end of the incoming request.
   *
   * @since 0.13
   */
  public static final TagKey GRPC_SERVER_STATUS = TagKey.create("grpc_server_status");

  /**
   * Tag key that represents a client gRPC method.
   *
   * <p>{@link #GRPC_CLIENT_METHOD} is set when an outgoing request starts and is available in all
   * the recorded metrics.
   *
   * @since 0.13
   */
  public static final TagKey GRPC_CLIENT_METHOD = TagKey.create("grpc_client_method");

  /**
   * Tag key that represents a server gRPC method.
   *
   * <p>{@link #GRPC_SERVER_METHOD} is set when an incoming request starts and is available in the
   * context for the entire RPC call handling.
   *
   * @since 0.13
   */
  public static final TagKey GRPC_SERVER_METHOD = TagKey.create("grpc_server_method");

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
   * {@link Measure} for total bytes sent across all request messages per RPC.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_CLIENT_SENT_BYTES_PER_RPC =
      Measure.MeasureDouble.create(
          "grpc.io/client/sent_bytes_per_rpc",
          "Total bytes sent across all request messages per RPC",
          BYTE);

  /**
   * {@link Measure} for total bytes received across all response messages per RPC.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_CLIENT_RECEIVED_BYTES_PER_RPC =
      Measure.MeasureDouble.create(
          "grpc.io/client/received_bytes_per_rpc",
          "Total bytes received across all response messages per RPC",
          BYTE);

  /**
   * {@link Measure} for total bytes sent per method, recorded real-time as bytes are sent.
   *
   * @since 0.18
   */
  public static final MeasureDouble GRPC_CLIENT_SENT_BYTES_PER_METHOD =
      Measure.MeasureDouble.create(
          "grpc.io/client/sent_bytes_per_method",
          "Total bytes sent per method, recorded real-time as bytes are sent.",
          BYTE);

  /**
   * {@link Measure} for total bytes received per method, recorded real-time as bytes are received.
   *
   * @since 0.18
   */
  public static final MeasureDouble GRPC_CLIENT_RECEIVED_BYTES_PER_METHOD =
      Measure.MeasureDouble.create(
          "grpc.io/client/received_bytes_per_method",
          "Total bytes received per method, recorded real-time as bytes are received.",
          BYTE);

  /**
   * {@link Measure} for total client sent messages.
   *
   * @since 0.18
   */
  public static final MeasureLong GRPC_CLIENT_SENT_MESSAGES_PER_METHOD =
      Measure.MeasureLong.create(
          "grpc.io/client/sent_messages_per_method", "Total messages sent per method.", COUNT);

  /**
   * {@link Measure} for total client received messages.
   *
   * @since 0.18
   */
  public static final MeasureLong GRPC_CLIENT_RECEIVED_MESSAGES_PER_METHOD =
      Measure.MeasureLong.create(
          "grpc.io/client/received_messages_per_method",
          "Total messages received per method.",
          COUNT);

  /**
   * {@link Measure} for gRPC client roundtrip latency in milliseconds.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_CLIENT_ROUNDTRIP_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/client/roundtrip_latency",
          "Time between first byte of request sent to last byte of response received, "
              + "or terminal error.",
          MILLISECOND);

  /**
   * {@link Measure} for number of messages sent in the RPC.
   *
   * @since 0.13
   */
  public static final MeasureLong GRPC_CLIENT_SENT_MESSAGES_PER_RPC =
      Measure.MeasureLong.create(
          "grpc.io/client/sent_messages_per_rpc", "Number of messages sent in the RPC", COUNT);

  /**
   * {@link Measure} for number of response messages received per RPC.
   *
   * @since 0.13
   */
  public static final MeasureLong GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC =
      Measure.MeasureLong.create(
          "grpc.io/client/received_messages_per_rpc",
          "Number of response messages received per RPC",
          COUNT);

  /**
   * {@link Measure} for gRPC server latency in milliseconds.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_CLIENT_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/client/server_latency", "Server latency in msecs", MILLISECOND);

  /**
   * {@link Measure} for total number of client RPCs ever opened, including those that have not
   * completed.
   *
   * @since 0.14
   */
  public static final MeasureLong GRPC_CLIENT_STARTED_RPCS =
      Measure.MeasureLong.create(
          "grpc.io/client/started_rpcs", "Number of started client RPCs.", COUNT);

  /**
   * {@link Measure} for total number of retry or hedging attempts excluding transparent retries
   * made during the client call.
   *
   * @since 0.31.0
   */
  public static final MeasureLong GRPC_CLIENT_RETRIES_PER_CALL =
      Measure.MeasureLong.create(
          "grpc.io/client/retries_per_call", "Number of retries per call", COUNT);

  /**
   * {@link Measure} for total number of transparent retries made during the client call.
   *
   * @since 0.28
   */
  public static final MeasureLong GRPC_CLIENT_TRANSPARENT_RETRIES_PER_CALL =
      Measure.MeasureLong.create(
          "grpc.io/client/transparent_retries_per_call", "Transparent retries per call", COUNT);

  /**
   * {@link Measure} for total time of delay while there is no active attempt during the client
   * call.
   *
   * @since 0.28
   */
  public static final MeasureDouble GRPC_CLIENT_RETRY_DELAY_PER_CALL =
      Measure.MeasureDouble.create(
          "grpc.io/client/retry_delay_per_call", "Retry delay per call", MILLISECOND);

  /**
   * {@link Measure} for gRPC client error counts.
   *
   * @since 0.8
   * @deprecated because error counts can be computed on your metrics backend by totalling the
   *     different per-status values.
   */
  @Deprecated
  public static final MeasureLong RPC_CLIENT_ERROR_COUNT =
      Measure.MeasureLong.create("grpc.io/client/error_count", "RPC Errors", COUNT);

  /**
   * {@link Measure} for gRPC client request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_REQUEST_BYTES = GRPC_CLIENT_SENT_BYTES_PER_RPC;

  /**
   * {@link Measure} for gRPC client response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_RESPONSE_BYTES = GRPC_CLIENT_RECEIVED_BYTES_PER_RPC;

  /**
   * {@link Measure} for gRPC client roundtrip latency in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_ROUNDTRIP_LATENCY}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_ROUNDTRIP_LATENCY = GRPC_CLIENT_ROUNDTRIP_LATENCY;

  /**
   * {@link Measure} for gRPC client server elapsed time in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SERVER_LATENCY}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_SERVER_ELAPSED_TIME = GRPC_CLIENT_SERVER_LATENCY;

  /**
   * {@link Measure} for gRPC client uncompressed request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_request_bytes", "Uncompressed Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC client uncompressed response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/client/uncompressed_response_bytes", "Uncompressed Response bytes", BYTE);

  /**
   * {@link Measure} for number of started client RPCs.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_STARTED_RPCS}.
   */
  @Deprecated public static final MeasureLong RPC_CLIENT_STARTED_COUNT = GRPC_CLIENT_STARTED_RPCS;

  /**
   * {@link Measure} for number of finished client RPCs.
   *
   * @since 0.8
   * @deprecated since finished count can be inferred with a {@code Count} aggregation on {@link
   *     #GRPC_CLIENT_SERVER_LATENCY}.
   */
  @Deprecated
  public static final MeasureLong RPC_CLIENT_FINISHED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/client/finished_count", "Number of client RPCs (streams) finished", COUNT);

  /**
   * {@link Measure} for client RPC request message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_MESSAGES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureLong RPC_CLIENT_REQUEST_COUNT = GRPC_CLIENT_SENT_MESSAGES_PER_RPC;

  /**
   * {@link Measure} for client RPC response message counts.
   *
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC}.
   * @since 0.8
   */
  @Deprecated
  public static final MeasureLong RPC_CLIENT_RESPONSE_COUNT = GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC;

  // RPC server Measures.

  /**
   * {@link Measure} for total bytes sent across all response messages per RPC.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_SERVER_SENT_BYTES_PER_RPC =
      Measure.MeasureDouble.create(
          "grpc.io/server/sent_bytes_per_rpc",
          "Total bytes sent across all response messages per RPC",
          BYTE);

  /**
   * {@link Measure} for total bytes received across all messages per RPC.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_SERVER_RECEIVED_BYTES_PER_RPC =
      Measure.MeasureDouble.create(
          "grpc.io/server/received_bytes_per_rpc",
          "Total bytes received across all messages per RPC",
          BYTE);

  /**
   * {@link Measure} for total bytes sent per method, recorded real-time as bytes are sent.
   *
   * @since 0.18
   */
  public static final MeasureDouble GRPC_SERVER_SENT_BYTES_PER_METHOD =
      Measure.MeasureDouble.create(
          "grpc.io/server/sent_bytes_per_method",
          "Total bytes sent per method, recorded real-time as bytes are sent.",
          BYTE);

  /**
   * {@link Measure} for total bytes received per method, recorded real-time as bytes are received.
   *
   * @since 0.18
   */
  public static final MeasureDouble GRPC_SERVER_RECEIVED_BYTES_PER_METHOD =
      Measure.MeasureDouble.create(
          "grpc.io/server/received_bytes_per_method",
          "Total bytes received per method, recorded real-time as bytes are received.",
          BYTE);

  /**
   * {@link Measure} for total server sent messages.
   *
   * @since 0.18
   */
  public static final MeasureLong GRPC_SERVER_SENT_MESSAGES_PER_METHOD =
      Measure.MeasureLong.create(
          "grpc.io/server/sent_messages_per_method", "Total messages sent per method.", COUNT);

  /**
   * {@link Measure} for total server received messages.
   *
   * @since 0.18
   */
  public static final MeasureLong GRPC_SERVER_RECEIVED_MESSAGES_PER_METHOD =
      Measure.MeasureLong.create(
          "grpc.io/server/received_messages_per_method",
          "Total messages received per method.",
          COUNT);

  /**
   * {@link Measure} for number of messages sent in each RPC.
   *
   * @since 0.13
   */
  public static final MeasureLong GRPC_SERVER_SENT_MESSAGES_PER_RPC =
      Measure.MeasureLong.create(
          "grpc.io/server/sent_messages_per_rpc", "Number of messages sent in each RPC", COUNT);

  /**
   * {@link Measure} for number of messages received in each RPC.
   *
   * @since 0.13
   */
  public static final MeasureLong GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC =
      Measure.MeasureLong.create(
          "grpc.io/server/received_messages_per_rpc",
          "Number of messages received in each RPC",
          COUNT);

  /**
   * {@link Measure} for gRPC server latency in milliseconds.
   *
   * @since 0.13
   */
  public static final MeasureDouble GRPC_SERVER_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_latency",
          "Time between first byte of request received to last byte of response sent, "
              + "or terminal error.",
          MILLISECOND);

  /**
   * {@link Measure} for total number of server RPCs ever opened, including those that have not
   * completed.
   *
   * @since 0.14
   */
  public static final MeasureLong GRPC_SERVER_STARTED_RPCS =
      Measure.MeasureLong.create(
          "grpc.io/server/started_rpcs", "Number of started server RPCs.", COUNT);

  /**
   * {@link Measure} for gRPC server error counts.
   *
   * @since 0.8
   * @deprecated because error counts can be computed on your metrics backend by totalling the
   *     different per-status values.
   */
  @Deprecated
  public static final MeasureLong RPC_SERVER_ERROR_COUNT =
      Measure.MeasureLong.create("grpc.io/server/error_count", "RPC Errors", COUNT);

  /**
   * {@link Measure} for gRPC server request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_REQUEST_BYTES = GRPC_SERVER_RECEIVED_BYTES_PER_RPC;

  /**
   * {@link Measure} for gRPC server response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_RESPONSE_BYTES = GRPC_SERVER_SENT_BYTES_PER_RPC;

  /**
   * {@link Measure} for gRPC server elapsed time in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SERVER_LATENCY}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_SERVER_ELAPSED_TIME =
      Measure.MeasureDouble.create(
          "grpc.io/server/server_elapsed_time", "Server elapsed time in msecs", MILLISECOND);

  /**
   * {@link Measure} for gRPC server latency in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SERVER_LATENCY}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_SERVER_LATENCY = GRPC_SERVER_SERVER_LATENCY;

  /**
   * {@link Measure} for gRPC server uncompressed request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_request_bytes", "Uncompressed Request bytes", BYTE);

  /**
   * {@link Measure} for gRPC server uncompressed response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_BYTES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureDouble RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      Measure.MeasureDouble.create(
          "grpc.io/server/uncompressed_response_bytes", "Uncompressed Response bytes", BYTE);

  /**
   * {@link Measure} for number of started server RPCs.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_STARTED_RPCS}.
   */
  @Deprecated public static final MeasureLong RPC_SERVER_STARTED_COUNT = GRPC_SERVER_STARTED_RPCS;

  /**
   * {@link Measure} for number of finished server RPCs.
   *
   * @since 0.8
   * @deprecated since finished count can be inferred with a {@code Count} aggregation on {@link
   *     #GRPC_SERVER_SERVER_LATENCY}.
   */
  @Deprecated
  public static final MeasureLong RPC_SERVER_FINISHED_COUNT =
      Measure.MeasureLong.create(
          "grpc.io/server/finished_count", "Number of server RPCs (streams) finished", COUNT);

  /**
   * {@link Measure} for server RPC request message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureLong RPC_SERVER_REQUEST_COUNT = GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC;

  /**
   * {@link Measure} for server RPC response message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_MESSAGES_PER_RPC}.
   */
  @Deprecated
  public static final MeasureLong RPC_SERVER_RESPONSE_COUNT = GRPC_SERVER_SENT_MESSAGES_PER_RPC;

  private RpcMeasureConstants() {}
}
