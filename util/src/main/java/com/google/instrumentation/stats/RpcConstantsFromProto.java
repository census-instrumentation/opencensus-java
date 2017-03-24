package com.google.instrumentation.stats;

import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.Map;

/**
 * Constants for collecting rpc stats.
 */
public final class RpcConstantsFromProto {

  private static final ViewConstantsProtoBinaryParser parser = new ViewConstantsProtoBinaryParser(
      "../proto/stats/rpc_constants.pb");
  private static final Map<String, MeasurementDescriptor> measurementDescriptorMap = parser
      .getMeasurementDescriptorMap();
  private static final Map<String, ViewDescriptor> viewDescriptorMap = parser
      .getViewDescriptorMap();

  // RPC client {@link MeasurementDescriptor}s.
  public static final MeasurementDescriptor RPC_CLIENT_ERROR_COUNT =
      measurementDescriptorMap.get("grpc.io/client/error_count");
  public static final MeasurementDescriptor RPC_CLIENT_REQUEST_BYTES =
      measurementDescriptorMap.get("grpc.io/client/request_bytes");
  public static final MeasurementDescriptor RPC_CLIENT_RESPONSE_BYTES =
      measurementDescriptorMap.get("grpc.io/client/response_bytes");
  public static final MeasurementDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY =
      measurementDescriptorMap.get("grpc.io/client/roundtrip_latency");
  public static final MeasurementDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME =
      measurementDescriptorMap.get("grpc.io/client/server_elapsed_time");
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES =
      measurementDescriptorMap.get("grpc.io/client/uncompressed_request_bytes");
  public static final MeasurementDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES =
      measurementDescriptorMap.get("grpc.io/client/uncompressed_response_bytes");
  public static final MeasurementDescriptor RPC_CLIENT_STARTED_COUNT =
      measurementDescriptorMap.get("grpc.io/client/started_count");
  public static final MeasurementDescriptor RPC_CLIENT_FINISHED_COUNT =
      measurementDescriptorMap.get("grpc.io/client/finished_count");
  public static final MeasurementDescriptor RPC_CLIENT_REQUEST_COUNT =
      measurementDescriptorMap.get("grpc.io/client/request_count");
  public static final MeasurementDescriptor RPC_CLIENT_RESPONSE_COUNT =
      measurementDescriptorMap.get("grpc.io/client/response_count");


  // RPC server {@link MeasurementDescriptor}s.
  public static final MeasurementDescriptor RPC_SERVER_ERROR_COUNT =
      measurementDescriptorMap.get("grpc.io/server/error_count");
  public static final MeasurementDescriptor RPC_SERVER_REQUEST_BYTES =
      measurementDescriptorMap.get("grpc.io/server/request_bytes");
  public static final MeasurementDescriptor RPC_SERVER_RESPONSE_BYTES =
      measurementDescriptorMap.get("grpc.io/server/response_bytes");
  public static final MeasurementDescriptor RPC_SERVER_LATENCY =
      measurementDescriptorMap.get("grpc.io/server/server_latency");
  public static final MeasurementDescriptor RPC_SERVER_SERVER_ELAPSED_TIME =
      measurementDescriptorMap.get("grpc.io/server/server_elapsed_time");
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES =
      measurementDescriptorMap.get("grpc.io/server/uncompressed_request_bytes");
  public static final MeasurementDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES =
      measurementDescriptorMap.get("grpc.io/server/uncompressed_response_bytes");
  public static final MeasurementDescriptor RPC_SERVER_STARTED_COUNT =
      measurementDescriptorMap.get("grpc.io/server/started_count");
  public static final MeasurementDescriptor RPC_SERVER_FINISHED_COUNT =
      measurementDescriptorMap.get("grpc.io/server/finished_count");
  public static final MeasurementDescriptor RPC_SERVER_REQUEST_COUNT =
      measurementDescriptorMap.get("grpc.io/server/request_count");
  public static final MeasurementDescriptor RPC_SERVER_RESPONSE_COUNT =
      measurementDescriptorMap.get("grpc.io/server/response_count");

  // Rpc client {@link ViewDescriptor}s.
  public static final DistributionViewDescriptor RPC_CLIENT_ERROR_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/error_count/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/roundtrip_latency/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/server_elapsed_time/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_REQUEST_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/request_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_RESPONSE_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/response_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap
              .get("grpc.io/client/uncompressed_request_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap
              .get("grpc.io/client/uncompressed_response_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_REQUEST_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/request_count/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_CLIENT_RESPONSE_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/response_count/distribution_cumulative");


  // Rpc server {@link ViewDescriptor}s.
  public static final DistributionViewDescriptor RPC_SERVER_ERROR_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/error_count/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_LATENCY_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/server_latency/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_SERVER_ELAPSED_TIME_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/server_elapsed_time/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_REQUEST_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/request_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_RESPONSE_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/response_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap
              .get("grpc.io/server/uncompressed_request_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap
              .get("grpc.io/server/uncompressed_response_bytes/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_REQUEST_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/request_count/distribution_cumulative");
  public static final DistributionViewDescriptor RPC_SERVER_RESPONSE_COUNT_VIEW =
      (DistributionViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/response_count/distribution_cumulative");

  // RPC client {@link IntervalViewDescriptor}s.
  public static final IntervalViewDescriptor RPC_CLIENT_ROUNDTRIP_LATENCY_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/roundtrip_latency/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_REQUEST_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/request_bytes/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_RESPONSE_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/response_bytes/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_ERROR_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/error_count/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/uncompressed_request_bytes/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/uncompressed_response_bytes/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_SERVER_ELAPSED_TIME_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/server_elapsed_time/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_STARTED_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/started_count/interval");
  public static final IntervalViewDescriptor RPC_CLIENT_FINISHED_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/client/finished_count/interval");

  // RPC server {@link IntervalViewDescriptor}s.
  public static final IntervalViewDescriptor RPC_SERVER_SERVER_LATENCY_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/server_latency/interval");
  public static final IntervalViewDescriptor RPC_SERVER_REQUEST_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/request_bytes/interval");
  public static final IntervalViewDescriptor RPC_SERVER_RESPONSE_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/response_bytes/interval");
  public static final IntervalViewDescriptor RPC_SERVER_ERROR_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/error_count/interval");
  public static final IntervalViewDescriptor RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/uncompressed_request_bytes/interval");
  public static final IntervalViewDescriptor RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/uncompressed_response_bytes/interval");
  public static final IntervalViewDescriptor RPC_SERVER_SERVER_ELAPSED_TIME_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/server_elapsed_time/interval");
  public static final IntervalViewDescriptor RPC_SERVER_STARTED_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/started_count/interval");
  public static final IntervalViewDescriptor RPC_SERVER_FINISHED_COUNT_INTERVAL_VIEW =
      (IntervalViewDescriptor)
          viewDescriptorMap.get("grpc.io/server/finished_count/interval");

  // Visible for testing.
  RpcConstantsFromProto() {
    throw new AssertionError();
  }
}
