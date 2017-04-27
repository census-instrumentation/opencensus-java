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

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for collecting rpc stats.
 */
// Deprecated, please use RpcMeasurementConstants for MeasurementDescriptor constants, and
// RpcViewConstants for ViewDescriptor constants.
@Deprecated
public final class RpcConstants {
  // Rpc tag keys.
  public static final TagKey RPC_STATUS = TagKey.create("OpStatus");
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

  // Common histogram bucket boundaries for bytes received/sets DistributionViewDescriptors.
  static final List<Double> RPC_BYTES_BUCKET_BOUNDARIES = Collections.unmodifiableList(
      Arrays.asList(0.0, 1024.0, 2048.0, 4096.0, 16384.0, 65536.0, 262144.0, 1048576.0, 4194304.0,
          16777216.0, 67108864.0, 268435456.0, 1073741824.0, 4294967296.0));

  // Common histogram bucket boundaries for latency and elapsed-time DistributionViewDescriptors.
  static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES = Collections.unmodifiableList(
      Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0,
          40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0, 500.0, 650.0,
          800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0));

  // Rpc client {@link ViewDescriptor}s.
  public static final DistributionViewDescriptor RPC_CLIENT_ERROR_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/error_count/distribution_cumulative",
          "RPC Errors",
          RPC_CLIENT_ERROR_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_STATUS, RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/roundtrip_latency/distribution_cumulative",
          "Latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          DistributionAggregationDescriptor.create(RPC_MILLIS_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/server_elapsed_time/distribution_cumulative",
          "Server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          DistributionAggregationDescriptor.create(RPC_MILLIS_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_REQUEST_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/request_bytes/distribution_cumulative",
          "Request bytes",
          RPC_CLIENT_REQUEST_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_RESPONSE_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/response_bytes/distribution_cumulative",
          "Response bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/uncompressed_request_bytes/distribution_cumulative",
          "Uncompressed Request bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/uncompressed_response_bytes/distribution_cumulative",
          "Uncompressed Response bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_REQUEST_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/request_count/distribution_cumulative",
          "Count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_CLIENT_METHOD));
  public static final DistributionViewDescriptor RPC_CLIENT_RESPONSE_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/client/response_count/distribution_cumulative",
          "Count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_CLIENT_METHOD));


  // Rpc server {@link ViewDescriptor}s.
  public static final DistributionViewDescriptor RPC_SERVER_ERROR_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/error_count/distribution_cumulative",
          "RPC Errors",
          RPC_SERVER_ERROR_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_STATUS, RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_SERVER_LATENCY_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/server_latency/distribution_cumulative",
          "Latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          DistributionAggregationDescriptor.create(RPC_MILLIS_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_SERVER_ELAPSED_TIME_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/elapsed_time/distribution_cumulative",
          "Server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          DistributionAggregationDescriptor.create(RPC_MILLIS_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_REQUEST_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/request_bytes/distribution_cumulative",
          "Request bytes",
          RPC_SERVER_REQUEST_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_RESPONSE_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/response_bytes/distribution_cumulative",
          "Response bytes",
          RPC_SERVER_RESPONSE_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/uncompressed_request_bytes/distribution_cumulative",
          "Uncompressed Request bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/uncompressed_response_bytes/distribution_cumulative",
          "Uncompressed Response bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          DistributionAggregationDescriptor.create(RPC_BYTES_BUCKET_BOUNDARIES),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_REQUEST_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/request_count/distribution_cumulative",
          "Count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_SERVER_METHOD));
  public static final DistributionViewDescriptor RPC_SERVER_RESPONSE_COUNT_VIEW =
      DistributionViewDescriptor.create(
          "grpc.io/server/response_count/distribution_cumulative",
          "Count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(RPC_SERVER_METHOD));

  // Interval Stats
  static final Duration MINUTE = Duration.create(60, 0);
  static final Duration HOUR = Duration.create(60 * 60, 0);

  // RPC client {@link IntervalViewDescriptor}s.
  public static final IntervalViewDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/roundtrip_latency/interval",
          "Minute and Hour stats for latency in msecs",
          RPC_CLIENT_ROUNDTRIP_LATENCY,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_REQUEST_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/request_bytes/interval",
          "Minute and Hour stats for request size in bytes",
          RPC_CLIENT_REQUEST_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_RESPONSE_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/response_bytes/interval",
          "Minute and Hour stats for response size in bytes",
          RPC_CLIENT_RESPONSE_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_ERROR_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/error_count/interval",
          "Minute and Hour stats for rpc errors",
          RPC_CLIENT_ERROR_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/uncompressed_request_bytes/interval",
          "Minute and Hour stats for uncompressed request size in bytes",
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/uncompressed_response_bytes/interval",
          "Minute and Hour stats for uncompressed response size in bytes",
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/server_elapsed_time/interval",
          "Minute and Hour stats for server elapsed time in msecs",
          RPC_CLIENT_SERVER_ELAPSED_TIME,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_STARTED_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/started_count/interval",
          "Minute and Hour stats on the number of client RPCs started",
          RPC_CLIENT_STARTED_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_FINISHED_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/finished_count/interval",
          "Minute and Hour stats on the number of client RPCs finished",
          RPC_CLIENT_FINISHED_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_REQUEST_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/request_count/interval",
          "Minute and Hour stats on the count of request messages per client RPC",
          RPC_CLIENT_REQUEST_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  public static final IntervalViewDescriptor RPC_CLIENT_RESPONSE_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/client/response_count/interval",
          "Minute and Hour stats on the count of response messages per client RPC",
          RPC_CLIENT_RESPONSE_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_CLIENT_METHOD));

  // RPC server {@link IntervalViewDescriptor}s.
  public static final IntervalViewDescriptor RPC_SERVER_SERVER_LATENCY_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/server_latency/interval",
          "Minute and Hour stats for server latency in msecs",
          RPC_SERVER_SERVER_LATENCY,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_REQUEST_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/request_bytes/interval",
          "Minute and Hour stats for request size in bytes",
          RPC_SERVER_REQUEST_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_RESPONSE_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/response_bytes/interval",
          "Minute and Hour stats for response size in bytes",
          RPC_SERVER_RESPONSE_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_ERROR_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/error_count/interval",
          "Minute and Hour stats for rpc errors",
          RPC_SERVER_ERROR_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/uncompressed_request_bytes/interval",
          "Minute and Hour stats for uncompressed request size in bytes",
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/uncompressed_response_bytes/interval",
          "Minute and Hour stats for uncompressed response size in bytes",
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_SERVER_ELAPSED_TIME_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/server_elapsed_time/interval",
          "Minute and Hour stats for server elapsed time in msecs",
          RPC_SERVER_SERVER_ELAPSED_TIME,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_STARTED_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/started_count/interval",
          "Minute and Hour stats on the number of server RPCs started",
          RPC_SERVER_STARTED_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_FINISHED_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/finished_count/interval",
          "Minute and Hour stats on the number of server RPCs finished",
          RPC_SERVER_FINISHED_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_REQUEST_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/request_count/interval",
          "Minute and Hour stats on the count of request messages per server RPC",
          RPC_SERVER_REQUEST_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  public static final IntervalViewDescriptor RPC_SERVER_RESPONSE_COUNT_INTERVAL_VIEW =
      IntervalViewDescriptor.create(
          "grpc.io/server/response_count/interval",
          "Minute and Hour stats on the count of response messages per server RPC",
          RPC_SERVER_RESPONSE_COUNT,
          IntervalAggregationDescriptor.create(Arrays.asList(MINUTE, HOUR)),
          Arrays.asList(RPC_SERVER_METHOD));

  // Visible for testing
  RpcConstants() {
    throw new AssertionError();
  }
}
