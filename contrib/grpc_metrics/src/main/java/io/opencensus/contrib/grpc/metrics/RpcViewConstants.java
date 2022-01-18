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

import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RECEIVED_BYTES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RECEIVED_BYTES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RECEIVED_MESSAGES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RETRIES_PER_CALL;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_RETRY_DELAY_PER_CALL;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_SENT_BYTES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_SENT_BYTES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_SENT_MESSAGES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_SENT_MESSAGES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_SERVER_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_STARTED_RPCS;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_STATUS;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_CLIENT_TRANSPARENT_RETRIES_PER_CALL;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_RECEIVED_BYTES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_RECEIVED_BYTES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_RECEIVED_MESSAGES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_SENT_BYTES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_SENT_BYTES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_SENT_MESSAGES_PER_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_SENT_MESSAGES_PER_RPC;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_SERVER_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_STARTED_RPCS;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.GRPC_SERVER_STATUS;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_ERROR_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_FINISHED_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_REQUEST_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_REQUEST_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_RESPONSE_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_RESPONSE_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_SERVER_ELAPSED_TIME;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_STARTED_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_METHOD;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_ERROR_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_FINISHED_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_REQUEST_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_REQUEST_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_RESPONSE_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_RESPONSE_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_SERVER_ELAPSED_TIME;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_SERVER_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_STARTED_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_STATUS;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.View;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for exporting views on rpc stats.
 *
 * @since 0.8
 */
@SuppressWarnings("deprecation")
public final class RpcViewConstants {

  // Common histogram bucket boundaries for bytes received/sets Views.
  @VisibleForTesting
  static final List<Double> RPC_BYTES_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              0.0,
              1024.0,
              2048.0,
              4096.0,
              16384.0,
              65536.0,
              262144.0,
              1048576.0,
              4194304.0,
              16777216.0,
              67108864.0,
              268435456.0,
              1073741824.0,
              4294967296.0));

  // Common histogram bucket boundaries for latency and elapsed-time Views.
  @VisibleForTesting
  static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              0.0, 0.01, 0.05, 0.1, 0.3, 0.6, 0.8, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0,
              16.0, 20.0, 25.0, 30.0, 40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0,
              300.0, 400.0, 500.0, 650.0, 800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0,
              100000.0));

  static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES_DEPRECATED =
      Collections.unmodifiableList(
          Arrays.asList(
              0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0, 40.0,
              50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0, 500.0, 650.0,
              800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0));

  // Common histogram bucket boundaries for request/response count Views.
  @VisibleForTesting
  static final List<Double> RPC_COUNT_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              0.0, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0, 1024.0, 2048.0,
              4096.0, 8192.0, 16384.0, 32768.0, 65536.0));

  @VisibleForTesting
  static final List<Double> RETRY_COUNT_PER_CALL_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

  // Use Aggregation.Mean to record sum and count stats at the same time.
  @VisibleForTesting static final Aggregation MEAN = Aggregation.Mean.create();
  @VisibleForTesting static final Aggregation COUNT = Count.create();
  @VisibleForTesting static final Aggregation SUM = Sum.create();

  @VisibleForTesting
  static final Aggregation AGGREGATION_WITH_BYTES_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_BYTES_BUCKET_BOUNDARIES));

  @VisibleForTesting
  static final Aggregation AGGREGATION_WITH_MILLIS_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_MILLIS_BUCKET_BOUNDARIES));

  static final Aggregation AGGREGATION_WITH_MILLIS_HISTOGRAM_DEPRECATED =
      Distribution.create(BucketBoundaries.create(RPC_MILLIS_BUCKET_BOUNDARIES_DEPRECATED));

  @VisibleForTesting
  static final Aggregation AGGREGATION_WITH_COUNT_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_COUNT_BUCKET_BOUNDARIES));

  @VisibleForTesting
  static final Aggregation AGGREGATION_WITH_COUNT_RETRY_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RETRY_COUNT_PER_CALL_BUCKET_BOUNDARIES));

  @VisibleForTesting static final Duration MINUTE = Duration.create(60, 0);
  @VisibleForTesting static final Duration HOUR = Duration.create(60 * 60, 0);

  @VisibleForTesting
  static final View.AggregationWindow CUMULATIVE = View.AggregationWindow.Cumulative.create();

  @VisibleForTesting
  static final View.AggregationWindow INTERVAL_MINUTE =
      View.AggregationWindow.Interval.create(MINUTE);

  @VisibleForTesting
  static final View.AggregationWindow INTERVAL_HOUR = View.AggregationWindow.Interval.create(HOUR);

  // Rpc client cumulative views.

  /**
   * Cumulative {@link View} for client RPC errors.
   *
   * @since 0.8
   * @deprecated since error count measure is deprecated.
   */
  @Deprecated
  public static final View RPC_CLIENT_ERROR_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/cumulative"),
          "RPC Errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_STATUS, RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client roundtrip latency in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_ROUNDTRIP_LATENCY_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/cumulative"),
          "Latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM_DEPRECATED,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client server elapsed time in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SERVER_LATENCY_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/cumulative"),
          "Server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          AGGREGATION_WITH_MILLIS_HISTOGRAM_DEPRECATED,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/cumulative"),
          "Request bytes",
          RPC_CLIENT_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/cumulative"),
          "Response bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client uncompressed request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/cumulative"),
          "Uncompressed Request bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client uncompressed response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/cumulative"),
          "Uncompressed Response bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client request message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_SENT_MESSAGES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/cumulative"),
          "Count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for client response message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_RESPONSE_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/cumulative"),
          "Count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for started client RPCs.
   *
   * @since 0.12
   * @deprecated in favor of {@link #GRPC_CLIENT_STARTED_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_STARTED_COUNT_CUMULATIVE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_count/cumulative"),
          "Number of started client RPCs",
          RPC_CLIENT_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for finished client RPCs.
   *
   * @since 0.12
   * @deprecated in favor of {@link #GRPC_CLIENT_COMPLETED_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_CLIENT_FINISHED_COUNT_CUMULATIVE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/finished_count/cumulative"),
          "Number of finished client RPCs",
          RPC_CLIENT_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * {@link View} for client roundtrip latency in milliseconds.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency"),
          "Latency in msecs",
          GRPC_CLIENT_ROUNDTRIP_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client server latency in milliseconds.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_latency"),
          "Server latency in msecs",
          GRPC_CLIENT_SERVER_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client sent bytes per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_SENT_BYTES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/sent_bytes_per_rpc"),
          "Sent bytes per RPC",
          GRPC_CLIENT_SENT_BYTES_PER_RPC,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client received bytes per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_RECEIVED_BYTES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/received_bytes_per_rpc"),
          "Received bytes per RPC",
          GRPC_CLIENT_RECEIVED_BYTES_PER_RPC,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client sent messages per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_SENT_MESSAGES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/sent_messages_per_rpc"),
          "Number of messages sent in the RPC",
          GRPC_CLIENT_SENT_MESSAGES_PER_RPC,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client received messages per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/received_messages_per_rpc"),
          "Number of response messages received per RPC",
          GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client sent bytes per method.
   *
   * @since 0.18
   */
  public static final View GRPC_CLIENT_SENT_BYTES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/client/sent_bytes_per_method"),
          "Sent bytes per method",
          GRPC_CLIENT_SENT_BYTES_PER_METHOD,
          SUM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client received bytes per method.
   *
   * @since 0.18
   */
  public static final View GRPC_CLIENT_RECEIVED_BYTES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/client/received_bytes_per_method"),
          "Received bytes per method",
          GRPC_CLIENT_RECEIVED_BYTES_PER_METHOD,
          SUM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client sent messages.
   *
   * @since 0.18
   */
  public static final View GRPC_CLIENT_SENT_MESSAGES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/client/sent_messages_per_method"),
          "Number of messages sent",
          GRPC_CLIENT_SENT_MESSAGES_PER_METHOD,
          COUNT,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client received messages.
   *
   * @since 0.18
   */
  public static final View GRPC_CLIENT_RECEIVED_MESSAGES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/client/received_messages_per_method"),
          "Number of messages received",
          GRPC_CLIENT_RECEIVED_MESSAGES_PER_METHOD,
          COUNT,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for completed client RPCs.
   *
   * <p>This {@code View} uses measure {@code GRPC_CLIENT_ROUNDTRIP_LATENCY}, since completed RPCs
   * can be inferred over any measure recorded once per RPC (since it's just a count aggregation
   * over the measure). It would be unnecessary to use a separate "count" measure.
   *
   * @since 0.13
   */
  public static final View GRPC_CLIENT_COMPLETED_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/completed_rpcs"),
          "Number of completed client RPCs",
          GRPC_CLIENT_ROUNDTRIP_LATENCY,
          COUNT,
          Arrays.asList(GRPC_CLIENT_METHOD, GRPC_CLIENT_STATUS));

  /**
   * {@link View} for started client RPCs.
   *
   * @since 0.14
   */
  public static final View GRPC_CLIENT_STARTED_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_rpcs"),
          "Number of started client RPCs",
          GRPC_CLIENT_STARTED_RPCS,
          COUNT,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for client retries per call.
   *
   * @since 0.28
   */
  public static final View GRPC_CLIENT_RETRIES_PER_CALL_VIEW =
      View.create(
          View.Name.create("grpc.io/client/retries_per_call"),
          "Number of client retries per call",
          GRPC_CLIENT_RETRIES_PER_CALL,
          AGGREGATION_WITH_COUNT_RETRY_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for total transparent client retries across calls.
   *
   * @since 0.28
   */
  public static final View GRPC_CLIENT_TRANSPARENT_RETRIES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/transparent_retries"),
          "Total number of transparent client retries across calls",
          GRPC_CLIENT_TRANSPARENT_RETRIES_PER_CALL,
          SUM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for total time of delay while there is no active attempt during the client call.
   *
   * @since 0.28
   */
  public static final View GRPC_CLIENT_RETRY_DELAY_PER_CALL_VIEW =
      View.create(
          View.Name.create("grpc.io/client/retry_delay_per_call"),
          "Total time of delay while there is no active attempt during the client call",
          GRPC_CLIENT_RETRY_DELAY_PER_CALL,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for total retries across all calls, excluding transparent retries.
   *
   * @since 0.28
   */
  public static final View GRPC_CLIENT_RETRIES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/retries"),
          "Total number of client retries across all calls",
          GRPC_CLIENT_RETRIES_PER_CALL,
          SUM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  /**
   * {@link View} for transparent retries per call.
   *
   * @since 0.28
   */
  public static final View GRPC_CLIENT_TRANSPARENT_RETRIES_PER_CALL_VIEW =
      View.create(
          View.Name.create("grpc.io/client/transparent_retries_per_call"),
          "Number of transparent client retries per call",
          GRPC_CLIENT_TRANSPARENT_RETRIES_PER_CALL,
          AGGREGATION_WITH_COUNT_RETRY_HISTOGRAM,
          Arrays.asList(GRPC_CLIENT_METHOD));

  // Rpc server cumulative views.

  /**
   * Cumulative {@link View} for server RPC errors.
   *
   * @since 0.8
   * @deprecated since error count measure is deprecated.
   */
  @Deprecated
  public static final View RPC_SERVER_ERROR_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/cumulative"),
          "RPC Errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_STATUS, RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server latency in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SERVER_LATENCY_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/cumulative"),
          "Latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM_DEPRECATED,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server elapsed time in milliseconds.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SERVER_LATENCY_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_VIEW =
      View.create(
          View.Name.create("grpc.io/server/elapsed_time/cumulative"),
          "Server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          AGGREGATION_WITH_MILLIS_HISTOGRAM_DEPRECATED,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/cumulative"),
          "Request bytes",
          RPC_SERVER_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/cumulative"),
          "Response bytes",
          RPC_SERVER_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server uncompressed request bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/cumulative"),
          "Uncompressed Request bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server uncompressed response bytes.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_BYTES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/cumulative"),
          "Uncompressed Response bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server request message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/cumulative"),
          "Count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for server response message counts.
   *
   * @since 0.8
   * @deprecated in favor of {@link #GRPC_SERVER_SENT_MESSAGES_PER_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_RESPONSE_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/cumulative"),
          "Count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for started server RPCs.
   *
   * @since 0.12
   * @deprecated in favor of {@link #GRPC_SERVER_STARTED_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_STARTED_COUNT_CUMULATIVE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_count/cumulative"),
          "Number of started server RPCs",
          RPC_SERVER_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * Cumulative {@link View} for finished server RPCs.
   *
   * @since 0.12
   * @deprecated in favor of {@link #GRPC_SERVER_COMPLETED_RPC_VIEW}.
   */
  @Deprecated
  public static final View RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/finished_count/cumulative"),
          "Number of finished server RPCs",
          RPC_SERVER_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          CUMULATIVE);

  /**
   * {@link View} for server server latency in milliseconds.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency"),
          "Server latency in msecs",
          GRPC_SERVER_SERVER_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server sent bytes per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_SENT_BYTES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/sent_bytes_per_rpc"),
          "Sent bytes per RPC",
          GRPC_SERVER_SENT_BYTES_PER_RPC,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server received bytes per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_RECEIVED_BYTES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/received_bytes_per_rpc"),
          "Received bytes per RPC",
          GRPC_SERVER_RECEIVED_BYTES_PER_RPC,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server sent messages per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_SENT_MESSAGES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/sent_messages_per_rpc"),
          "Number of messages sent in each RPC",
          GRPC_SERVER_SENT_MESSAGES_PER_RPC,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server received messages per RPC.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/received_messages_per_rpc"),
          "Number of response messages received in each RPC",
          GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for total server sent bytes per method.
   *
   * @since 0.18
   */
  public static final View GRPC_SERVER_SENT_BYTES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/server/sent_bytes_per_method"),
          "Sent bytes per method",
          GRPC_SERVER_SENT_BYTES_PER_METHOD,
          SUM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for total server received bytes per method.
   *
   * @since 0.18
   */
  public static final View GRPC_SERVER_RECEIVED_BYTES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/server/received_bytes_per_method"),
          "Received bytes per method",
          GRPC_SERVER_RECEIVED_BYTES_PER_METHOD,
          SUM,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server sent messages.
   *
   * @since 0.18
   */
  public static final View GRPC_SERVER_SENT_MESSAGES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/server/sent_messages_per_method"),
          "Number of messages sent",
          GRPC_SERVER_SENT_MESSAGES_PER_METHOD,
          COUNT,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for server received messages.
   *
   * @since 0.18
   */
  public static final View GRPC_SERVER_RECEIVED_MESSAGES_PER_METHOD_VIEW =
      View.create(
          View.Name.create("grpc.io/server/received_messages_per_method"),
          "Number of messages received",
          GRPC_SERVER_RECEIVED_MESSAGES_PER_METHOD,
          COUNT,
          Arrays.asList(GRPC_SERVER_METHOD));

  /**
   * {@link View} for completed server RPCs.
   *
   * <p>This {@code View} uses measure {@code GRPC_SERVER_SERVER_LATENCY}, since completed RPCs can
   * be inferred over any measure recorded once per RPC (since it's just a count aggregation over
   * the measure). It would be unnecessary to use a separate "count" measure.
   *
   * @since 0.13
   */
  public static final View GRPC_SERVER_COMPLETED_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/completed_rpcs"),
          "Number of completed server RPCs",
          GRPC_SERVER_SERVER_LATENCY,
          COUNT,
          Arrays.asList(GRPC_SERVER_METHOD, GRPC_SERVER_STATUS));

  /**
   * {@link View} for started server RPCs.
   *
   * @since 0.14
   */
  public static final View GRPC_SERVER_STARTED_RPC_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_rpcs"),
          "Number of started server RPCs",
          GRPC_SERVER_STARTED_RPCS,
          COUNT,
          Arrays.asList(GRPC_SERVER_METHOD));

  // Interval Stats

  // RPC client interval views.

  /**
   * Minute {@link View} for client roundtrip latency in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/minute"),
          "Minute stats for latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/minute"),
          "Minute stats for request size in bytes",
          RPC_CLIENT_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/minute"),
          "Minute stats for response size in bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client RPC errors.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/minute"),
          "Minute stats for rpc errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/minute"),
          "Minute stats for uncompressed request size in bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/minute"),
          "Minute stats for uncompressed response size in bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/minute"),
          "Minute stats for server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for started client RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_count/minute"),
          "Minute stats on the number of client RPCs started",
          RPC_CLIENT_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for finished client RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/finished_count/minute"),
          "Minute stats on the number of client RPCs finished",
          RPC_CLIENT_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client request messages.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/minute"),
          "Minute stats on the count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for client response messages.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/minute"),
          "Minute stats on the count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Hour {@link View} for client roundtrip latency in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/hour"),
          "Hour stats for latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/hour"),
          "Hour stats for request size in bytes",
          RPC_CLIENT_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/hour"),
          "Hour stats for response size in bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client RPC errors.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_ERROR_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/hour"),
          "Hour stats for rpc errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/hour"),
          "Hour stats for uncompressed request size in bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/hour"),
          "Hour stats for uncompressed response size in bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/hour"),
          "Hour stats for server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for started client RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_STARTED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_count/hour"),
          "Hour stats on the number of client RPCs started",
          RPC_CLIENT_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for finished client RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/finished_count/hour"),
          "Hour stats on the number of client RPCs finished",
          RPC_CLIENT_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client request messages.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/hour"),
          "Hour stats on the count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for client response messages.
   *
   * @since 0.8
   */
  public static final View RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/hour"),
          "Hour stats on the count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  // RPC server interval views.

  /**
   * Minute {@link View} for server latency in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/minute"),
          "Minute stats for server latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/minute"),
          "Minute stats for request size in bytes",
          RPC_SERVER_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/minute"),
          "Minute stats for response size in bytes",
          RPC_SERVER_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server RPC errors.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_ERROR_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/minute"),
          "Minute stats for rpc errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/minute"),
          "Minute stats for uncompressed request size in bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/minute"),
          "Minute stats for uncompressed response size in bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_elapsed_time/minute"),
          "Minute stats for server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for started server RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_STARTED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_count/minute"),
          "Minute stats on the number of server RPCs started",
          RPC_SERVER_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for finished server RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/finished_count/minute"),
          "Minute stats on the number of server RPCs finished",
          RPC_SERVER_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server request messages.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/minute"),
          "Minute stats on the count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Minute {@link View} for server response messages.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/minute"),
          "Minute stats on the count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_MINUTE);

  /**
   * Hour {@link View} for server latency in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_SERVER_LATENCY_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/hour"),
          "Hour stats for server latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/hour"),
          "Hour stats for request size in bytes",
          RPC_SERVER_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/hour"),
          "Hour stats for response size in bytes",
          RPC_SERVER_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server RPC errors.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_ERROR_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/hour"),
          "Hour stats for rpc errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server uncompressed request bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/hour"),
          "Hour stats for uncompressed request size in bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server uncompressed response bytes.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/hour"),
          "Hour stats for uncompressed response size in bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server elapsed time in milliseconds.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_elapsed_time/hour"),
          "Hour stats for server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for started server RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_STARTED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_count/hour"),
          "Hour stats on the number of server RPCs started",
          RPC_SERVER_STARTED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for finished server RPCs.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_FINISHED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/finished_count/hour"),
          "Hour stats on the number of server RPCs finished",
          RPC_SERVER_FINISHED_COUNT,
          COUNT,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server request messages.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_REQUEST_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/hour"),
          "Hour stats on the count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  /**
   * Hour {@link View} for server response messages.
   *
   * @since 0.8
   */
  public static final View RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/hour"),
          "Hour stats on the count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_METHOD),
          INTERVAL_HOUR);

  private RpcViewConstants() {}
}
