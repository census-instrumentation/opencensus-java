/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.exporter.trace.ocagent;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.ConstantSampler;
import io.opencensus.proto.trace.v1.TraceConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Fake implementation of {@link TraceServiceGrpc}. */
final class FakeOcAgentTraceServiceGrpcImpl extends TraceServiceGrpc.TraceServiceImplBase {

  private static final Logger logger =
      Logger.getLogger(FakeOcAgentTraceServiceGrpcImpl.class.getName());

  // Default updatedLibraryConfig uses an always sampler.
  private UpdatedLibraryConfig updatedLibraryConfig =
      UpdatedLibraryConfig.newBuilder()
          .setConfig(
              TraceConfig.newBuilder()
                  .setConstantSampler(ConstantSampler.newBuilder().setDecision(true).build())
                  .build())
          .build();

  private final List<CurrentLibraryConfig> currentLibraryConfigs = new ArrayList<>();
  private final List<ExportTraceServiceRequest> exportTraceServiceRequests = new ArrayList<>();

  private final AtomicReference<StreamObserver<UpdatedLibraryConfig>> updatedConfigObserverRef =
      new AtomicReference<>();

  private final StreamObserver<CurrentLibraryConfig> currentConfigObserver =
      new StreamObserver<CurrentLibraryConfig>() {
        @Override
        public void onNext(CurrentLibraryConfig value) {
          currentLibraryConfigs.add(value);
          @Nullable
          StreamObserver<UpdatedLibraryConfig> updatedConfigObserver =
              updatedConfigObserverRef.get();
          if (updatedConfigObserver != null) {
            updatedConfigObserver.onNext(updatedLibraryConfig);
          }
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for config stream: " + t);
        }

        @Override
        public void onCompleted() {}
      };

  private final StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
      new StreamObserver<ExportTraceServiceRequest>() {
        @Override
        public void onNext(ExportTraceServiceRequest value) {
          exportTraceServiceRequests.add(value);
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for export stream: " + t);
        }

        @Override
        public void onCompleted() {}
      };

  @Override
  public StreamObserver<CurrentLibraryConfig> config(
      StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigStreamObserver) {
    updatedConfigObserverRef.set(updatedLibraryConfigStreamObserver);
    return currentConfigObserver;
  }

  @Override
  public StreamObserver<ExportTraceServiceRequest> export(
      StreamObserver<ExportTraceServiceResponse> exportTraceServiceResponseStreamObserver) {
    return exportRequestObserver;
  }

  // Returns the stored CurrentLibraryConfigs.
  List<CurrentLibraryConfig> getCurrentLibraryConfigs() {
    return Collections.unmodifiableList(currentLibraryConfigs);
  }

  // Returns the stored ExportTraceServiceRequests.
  List<ExportTraceServiceRequest> getExportTraceServiceRequests() {
    return Collections.unmodifiableList(exportTraceServiceRequests);
  }

  // Sets the UpdatedLibraryConfig that will be passed to client.
  void setUpdatedLibraryConfig(UpdatedLibraryConfig updatedLibraryConfig) {
    this.updatedLibraryConfig = updatedLibraryConfig;
  }

  // Gets the UpdatedLibraryConfig that will be passed to client.
  UpdatedLibraryConfig getUpdatedLibraryConfig() {
    return updatedLibraryConfig;
  }

  static void startServer(String endPoint) throws IOException {
    ServerBuilder<?> builder = NettyServerBuilder.forAddress(parseEndpoint(endPoint));
    Executor executor = MoreExecutors.directExecutor();
    builder.executor(executor);
    final Server server = builder.addService(new FakeOcAgentTraceServiceGrpcImpl()).build();
    server.start();
    logger.info("Server started at " + endPoint);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                server.shutdown();
              }
            });

    try {
      server.awaitTermination();
    } catch (InterruptedException e) {
      logger.warning("Thread interrupted: " + e.getMessage());
      Thread.currentThread().interrupt();
    }
  }

  private static InetSocketAddress parseEndpoint(String endPoint) {
    try {
      int colonIndex = endPoint.indexOf(":");
      String host = endPoint.substring(0, colonIndex);
      int port = Integer.parseInt(endPoint.substring(colonIndex + 1));
      return new InetSocketAddress(host, port);
    } catch (RuntimeException e) {
      logger.warning("Unexpected format of end point: " + endPoint + ", use default end point.");
      return new InetSocketAddress("localhost", 55678);
    }
  }
}
