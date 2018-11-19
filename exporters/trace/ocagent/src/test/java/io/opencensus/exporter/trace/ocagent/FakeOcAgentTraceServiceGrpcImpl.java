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

import com.google.common.annotations.VisibleForTesting;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Fake implementation of {@link TraceServiceGrpc}. */
@ThreadSafe
final class FakeOcAgentTraceServiceGrpcImpl extends TraceServiceGrpc.TraceServiceImplBase {

  private static final Logger logger =
      Logger.getLogger(FakeOcAgentTraceServiceGrpcImpl.class.getName());

  // Default updatedLibraryConfig uses an always sampler.
  @GuardedBy("this")
  private UpdatedLibraryConfig updatedLibraryConfig =
      UpdatedLibraryConfig.newBuilder()
          .setConfig(
              TraceConfig.newBuilder()
                  .setConstantSampler(ConstantSampler.newBuilder().setDecision(true).build())
                  .build())
          .build();

  @GuardedBy("this")
  private final List<CurrentLibraryConfig> currentLibraryConfigs = new ArrayList<>();

  @GuardedBy("this")
  private final List<ExportTraceServiceRequest> exportTraceServiceRequests = new ArrayList<>();

  @GuardedBy("this")
  private final AtomicReference<StreamObserver<UpdatedLibraryConfig>> configRequestObserverRef =
      new AtomicReference<>();

  @GuardedBy("this")
  private final StreamObserver<CurrentLibraryConfig> configResponseObserver =
      new StreamObserver<CurrentLibraryConfig>() {
        @Override
        public void onNext(CurrentLibraryConfig value) {
          addCurrentLibraryConfig(value);
          try {
            // Do not send UpdatedLibraryConfigs too frequently.
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread interrupted.", e);
          }
          sendUpdatedLibraryConfig();
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for config stream: " + t);
          resetConfigRequestObserverRef();
        }

        @Override
        public void onCompleted() {
          resetConfigRequestObserverRef();
        }
      };

  @GuardedBy("this")
  private final StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
      new StreamObserver<ExportTraceServiceRequest>() {
        @Override
        public void onNext(ExportTraceServiceRequest value) {
          addExportRequest(value);
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for export stream: " + t);
        }

        @Override
        public void onCompleted() {}
      };

  @GuardedBy("this")
  private CountDownLatch countDownLatch;

  @Override
  public synchronized StreamObserver<CurrentLibraryConfig> config(
      StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigStreamObserver) {
    configRequestObserverRef.set(updatedLibraryConfigStreamObserver);
    return configResponseObserver;
  }

  @Override
  public synchronized StreamObserver<ExportTraceServiceRequest> export(
      StreamObserver<ExportTraceServiceResponse> exportTraceServiceResponseStreamObserver) {
    return exportRequestObserver;
  }

  private synchronized void addCurrentLibraryConfig(CurrentLibraryConfig currentLibraryConfig) {
    currentLibraryConfigs.add(currentLibraryConfig);
  }

  private synchronized void addExportRequest(ExportTraceServiceRequest request) {
    exportTraceServiceRequests.add(request);
  }

  // Returns the stored CurrentLibraryConfigs.
  synchronized List<CurrentLibraryConfig> getCurrentLibraryConfigs() {
    return Collections.unmodifiableList(currentLibraryConfigs);
  }

  // Returns the stored ExportTraceServiceRequests.
  synchronized List<ExportTraceServiceRequest> getExportTraceServiceRequests() {
    return Collections.unmodifiableList(exportTraceServiceRequests);
  }

  // Sets the UpdatedLibraryConfig that will be passed to client.
  synchronized void setUpdatedLibraryConfig(UpdatedLibraryConfig updatedLibraryConfig) {
    this.updatedLibraryConfig = updatedLibraryConfig;
  }

  // Gets the UpdatedLibraryConfig that will be passed to client.
  synchronized UpdatedLibraryConfig getUpdatedLibraryConfig() {
    return updatedLibraryConfig;
  }

  private synchronized void sendUpdatedLibraryConfig() {
    @Nullable
    StreamObserver<UpdatedLibraryConfig> configRequestObserver = configRequestObserverRef.get();
    if (configRequestObserver != null) {
      configRequestObserver.onNext(updatedLibraryConfig);
    }
    if (countDownLatch != null) {
      countDownLatch.countDown();
    }
  }

  // Closes config stream and resets the reference to configRequestObserver.
  synchronized void closeConfigStream() {
    configResponseObserver.onCompleted();
  }

  private synchronized void resetConfigRequestObserverRef() {
    configRequestObserverRef.set(null);
  }

  @VisibleForTesting
  synchronized void setCountDownLatch(CountDownLatch countDownLatch) {
    this.countDownLatch = countDownLatch;
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
