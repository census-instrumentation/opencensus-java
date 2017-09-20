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

package io.opencensus.stats;

import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_ERROR_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_FINISHED_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_METHOD;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_REQUEST_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_REQUEST_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_RESPONSE_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_RESPONSE_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_SERVER_ELAPSED_TIME;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_STARTED_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_ERROR_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_FINISHED_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_METHOD;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_REQUEST_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_REQUEST_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_RESPONSE_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_RESPONSE_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_SERVER_ELAPSED_TIME;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_SERVER_LATENCY;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_STARTED_COUNT;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES;
import static io.opencensus.stats.RpcMeasureConstants.RPC_STATUS;

import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for exporting views on rpc stats.
 */
public final class RpcViewConstants {

  // Common histogram bucket boundaries for bytes received/sets Views.
  static final List<Double> RPC_BYTES_BUCKET_BOUNDARIES = Collections.unmodifiableList(
      Arrays.asList(0.0, 1024.0, 2048.0, 4096.0, 16384.0, 65536.0, 262144.0, 1048576.0, 4194304.0,
          16777216.0, 67108864.0, 268435456.0, 1073741824.0, 4294967296.0));

  // Common histogram bucket boundaries for latency and elapsed-time Views.
  static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES = Collections.unmodifiableList(
      Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0,
          40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0, 500.0, 650.0,
          800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0));

  // Common histogram bucket boundaries for request/response count Views.
  static final List<Double> RPC_COUNT_BUCKET_BOUNDARIES = Collections.unmodifiableList(
      Arrays.asList(0.0, 1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0, 1024.0, 2048.0,
          4096.0, 8192.0, 16384.0, 32768.0, 65536.0));

  // Use Aggregation.Mean to record sum and count stats at the same time.
  static final Aggregation MEAN = Mean.create();

  static final Aggregation AGGREGATION_WITH_BYTES_HISTOGRAM = 
      Distribution.create(BucketBoundaries.create(RPC_BYTES_BUCKET_BOUNDARIES));

  static final Aggregation AGGREGATION_WITH_MILLIS_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_MILLIS_BUCKET_BOUNDARIES));

  static final Aggregation AGGREGATION_WITH_COUNT_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_COUNT_BUCKET_BOUNDARIES));

  static final Duration MINUTE = Duration.create(60, 0);
  static final Duration HOUR = Duration.create(60 * 60, 0);
  static final AggregationWindow CUMULATIVE = Cumulative.create();
  static final AggregationWindow INTERVAL_MINUTE = Interval.create(MINUTE);
  static final AggregationWindow INTERVAL_HOUR = Interval.create(HOUR);

  // Rpc client cumulative {@link View}s.
  public static final View RPC_CLIENT_ERROR_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/distribution_cumulative"),
          "RPC Errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_STATUS, RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/distribution_cumulative"),
          "Latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/distribution_cumulative"),
          "Server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/distribution_cumulative"),
          "Request bytes",
          RPC_CLIENT_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/distribution_cumulative"),
          "Response bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/distribution_cumulative"),
          "Uncompressed Request bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/distribution_cumulative"),
          "Uncompressed Response bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/distribution_cumulative"),
          "Count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);
  public static final View RPC_CLIENT_RESPONSE_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/distribution_cumulative"),
          "Count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_CLIENT_METHOD),
          CUMULATIVE);


  // Rpc server cumulative {@link View}s.
  public static final View RPC_SERVER_ERROR_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/distribution_cumulative"),
          "RPC Errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_STATUS, RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/distribution_cumulative"),
          "Latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_VIEW =
      View.create(
          View.Name.create("grpc.io/server/elapsed_time/distribution_cumulative"),
          "Server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/distribution_cumulative"),
          "Request bytes",
          RPC_SERVER_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/distribution_cumulative"),
          "Response bytes",
          RPC_SERVER_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/distribution_cumulative"),
          "Uncompressed Request bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/distribution_cumulative"),
          "Uncompressed Response bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          AGGREGATION_WITH_BYTES_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/distribution_cumulative"),
          "Count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);
  public static final View RPC_SERVER_RESPONSE_COUNT_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/distribution_cumulative"),
          "Count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          AGGREGATION_WITH_COUNT_HISTOGRAM,
          Arrays.asList(RPC_SERVER_METHOD),
          CUMULATIVE);

  // Interval Stats

  // RPC client interval {@link View}s.
  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/interval"),
          "Minute stats for latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/interval"),
          "Minute stats for request size in bytes",
          RPC_CLIENT_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/interval"),
          "Minute stats for response size in bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/interval"),
          "Minute stats for rpc errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/interval"),
          "Minute stats for uncompressed request size in bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/interval"),
          "Minute stats for uncompressed response size in bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/interval"),
          "Minute stats for server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_count/interval"),
          "Minute stats on the number of client RPCs started",
          RPC_CLIENT_STARTED_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/finished_count/interval"),
          "Minute stats on the number of client RPCs finished",
          RPC_CLIENT_FINISHED_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/interval"),
          "Minute stats on the count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/interval"),
          "Minute stats on the count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/roundtrip_latency/interval"),
          "Hour stats for latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_bytes/interval"),
          "Hour stats for request size in bytes",
          RPC_CLIENT_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_bytes/interval"),
          "Hour stats for response size in bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_ERROR_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/error_count/interval"),
          "Hour stats for rpc errors",
          RPC_CLIENT_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_request_bytes/interval"),
          "Hour stats for uncompressed request size in bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/uncompressed_response_bytes/interval"),
          "Hour stats for uncompressed response size in bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_SERVER_ELAPSED_TIME_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/server_elapsed_time/interval"),
          "Hour stats for server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_STARTED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/started_count/interval"),
          "Hour stats on the number of client RPCs started",
          RPC_CLIENT_STARTED_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/finished_count/interval"),
          "Hour stats on the number of client RPCs finished",
          RPC_CLIENT_FINISHED_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/request_count/interval"),
          "Hour stats on the count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/client/response_count/interval"),
          "Hour stats on the count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_CLIENT_METHOD),
          INTERVAL_HOUR);

  // RPC server interval {@link View}s.
  public static final View RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/interval"),
          "Minute stats for server latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/interval"),
          "Minute stats for request size in bytes",
          RPC_SERVER_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/interval"),
          "Minute stats for response size in bytes",
          RPC_SERVER_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_ERROR_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/interval"),
          "Minute stats for rpc errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/interval"),
          "Minute stats for uncompressed request size in bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/interval"),
          "Minute stats for uncompressed response size in bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_elapsed_time/interval"),
          "Minute stats for server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_STARTED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_count/interval"),
          "Minute stats on the number of server RPCs started",
          RPC_SERVER_STARTED_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/finished_count/interval"),
          "Minute stats on the number of server RPCs finished",
          RPC_SERVER_FINISHED_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/interval"),
          "Minute stats on the count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/interval"),
          "Minute stats on the count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_MINUTE);

  public static final View RPC_SERVER_SERVER_LATENCY_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_latency/interval"),
          "Hour stats for server latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_bytes/interval"),
          "Hour stats for request size in bytes",
          RPC_SERVER_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_bytes/interval"),
          "Hour stats for response size in bytes",
          RPC_SERVER_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_ERROR_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/error_count/interval"),
          "Hour stats for rpc errors",
          RPC_SERVER_ERROR_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_request_bytes/interval"),
          "Hour stats for uncompressed request size in bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/uncompressed_response_bytes/interval"),
          "Hour stats for uncompressed response size in bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_SERVER_ELAPSED_TIME_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/server_elapsed_time/interval"),
          "Hour stats for server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_STARTED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/started_count/interval"),
          "Hour stats on the number of server RPCs started",
          RPC_SERVER_STARTED_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_FINISHED_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/finished_count/interval"),
          "Hour stats on the number of server RPCs finished",
          RPC_SERVER_FINISHED_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_REQUEST_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/request_count/interval"),
          "Hour stats on the count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  public static final View RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW =
      View.create(
          View.Name.create("grpc.io/server/response_count/interval"),
          "Hour stats on the count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          MEAN,
          Arrays.asList(RPC_SERVER_METHOD),
          INTERVAL_HOUR);

  // Visible for testing.
  RpcViewConstants() {
    throw new AssertionError();
  }
}
