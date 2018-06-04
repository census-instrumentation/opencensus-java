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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;

/**
 * Helper class that allows users to register rpc views easily.
 *
 * @since 0.11
 */
@SuppressWarnings("deprecation")
public final class RpcViews {
  @VisibleForTesting
  static final ImmutableSet<View> RPC_CUMULATIVE_VIEWS_SET =
      ImmutableSet.of(
          RpcViewConstants.RPC_CLIENT_ERROR_COUNT_VIEW,
          RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW,
          RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_VIEW,
          RpcViewConstants.RPC_CLIENT_STARTED_COUNT_CUMULATIVE_VIEW,
          RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_CUMULATIVE_VIEW,
          RpcViewConstants.RPC_SERVER_ERROR_COUNT_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_LATENCY_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_ELAPSED_TIME_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_BYTES_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_COUNT_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW,
          RpcViewConstants.RPC_SERVER_STARTED_COUNT_CUMULATIVE_VIEW,
          RpcViewConstants.RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW);

  @VisibleForTesting
  static final ImmutableSet<View> GRPC_CUMULATIVE_VIEWS_SET =
      ImmutableSet.of(
          RpcViewConstants.GRPC_CLIENT_ROUNDTRIP_LATENCY_VIEW,
          RpcViewConstants.GRPC_CLIENT_SENT_BYTES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_CLIENT_RECEIVED_BYTES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_CLIENT_SENT_MESSAGES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_CLIENT_RECEIVED_MESSAGES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_CLIENT_SERVER_LATENCY_VIEW,
          RpcViewConstants.GRPC_CLIENT_COMPLETED_RPC_VIEW,
          RpcViewConstants.GRPC_CLIENT_STARTED_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_SERVER_LATENCY_VIEW,
          RpcViewConstants.GRPC_SERVER_SENT_BYTES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_RECEIVED_BYTES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_SENT_MESSAGES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_RECEIVED_MESSAGES_PER_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_COMPLETED_RPC_VIEW,
          RpcViewConstants.GRPC_SERVER_STARTED_RPC_VIEW);

  @VisibleForTesting
  static final ImmutableSet<View> RPC_INTERVAL_VIEWS_SET =
      ImmutableSet.of(
          RpcViewConstants.RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_ERROR_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_ELAPSED_TIME_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_STARTED_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW,
          RpcViewConstants.RPC_CLIENT_ERROR_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_SERVER_ELAPSED_TIME_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_STARTED_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_ERROR_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_LATENCY_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_SERVER_ELAPSED_TIME_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_REQUEST_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_STARTED_COUNT_HOUR_VIEW,
          RpcViewConstants.RPC_SERVER_FINISHED_COUNT_HOUR_VIEW);

  /**
   * Registers all standard gRPC views.
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing stats.
   *
   * @since 0.13
   */
  public static void registerAllGrpcViews() {
    registerAllGrpcViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllGrpcViews(ViewManager viewManager) {
    for (View view : GRPC_CUMULATIVE_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Registers all standard cumulative views.
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing stats.
   *
   * @since 0.11.0
   * @deprecated in favor of {@link #registerAllGrpcViews()}. It is likely that there won't be stats
   *     for the old views, but you may still want to register the old views before they are
   *     completely removed.
   */
  @Deprecated
  public static void registerAllCumulativeViews() {
    registerAllCumulativeViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllCumulativeViews(ViewManager viewManager) {
    for (View view : RPC_CUMULATIVE_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Registers all standard interval views.
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing stats.
   *
   * @since 0.11.0
   * @deprecated because interval window is deprecated. There won't be interval views in the future.
   */
  @Deprecated
  public static void registerAllIntervalViews() {
    registerAllIntervalViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllIntervalViews(ViewManager viewManager) {
    for (View view : RPC_INTERVAL_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Registers all views.
   *
   * <p>This is equivalent with calling {@link #registerAllCumulativeViews()} and {@link
   * #registerAllIntervalViews()}.
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing stats.
   *
   * @since 0.11.0
   * @deprecated in favor of {@link #registerAllGrpcViews()}.
   */
  @Deprecated
  public static void registerAllViews() {
    registerAllViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllViews(ViewManager viewManager) {
    registerAllCumulativeViews(viewManager);
    registerAllIntervalViews(viewManager);
  }

  private RpcViews() {}
}
