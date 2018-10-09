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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.trace.export.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Implementation of OC-Agent Trace Service clients. */
final class OcAgentTraceServiceClients {

  private OcAgentTraceServiceClients() {}

  private static final Logger logger = Logger.getLogger(OcAgentTraceServiceClients.class.getName());
  private static final DaemonThreadFactory factory = new DaemonThreadFactory();

  // A reference to the callback currentConfigObserver returned from Agent server.
  // Shared between main thread and AgentConnectionWorker thread.
  // The reference is non-null only when the connection succeeded.
  @VisibleForTesting
  static final AtomicReference<StreamObserver<CurrentLibraryConfig>> currentConfigObserverRef =
      new AtomicReference<>();

  // A reference to the callback exportRequestObserver returned from Agent server.
  // Shared between main thread and AgentConnectionWorker thread.
  // The reference is non-null only when the connection succeeded.
  @VisibleForTesting
  static final AtomicReference<StreamObserver<ExportTraceServiceRequest>> exportRequestObserverRef =
      new AtomicReference<>();

  private static boolean isExportConnected() {
    return exportRequestObserverRef.get() != null;
  }

  private static boolean isConfigConnected() {
    return currentConfigObserverRef.get() != null;
  }

  private static void resetExportClient() {
    exportRequestObserverRef.set(null);
  }

  private static void resetConfigClient() {
    currentConfigObserverRef.set(null);
  }

  // Creates and starts a daemon thread for AgentConnectionWorker. That thread will be kept alive
  // during the lifetime of application, and will keep trying to connect to Agent at a configured
  // frequency (retryIntervalMillis).
  static void startAttemptsToConnectToAgent(
      String endPoint,
      boolean useInsecure,
      String serviceName,
      long retryIntervalMillis,
      boolean enableConfig) {
    factory
        .newThread(
            new AgentConnectionWorker(
                endPoint, useInsecure, serviceName, retryIntervalMillis, enableConfig))
        .start();
  }

  // Handles the given SpanData list from SpanExporter.
  // If there's a non-null exportRequestObserver (which indicates the connection to Agent
  // succeeded), transforms SpanDatas and export them to Agent. Otherwise do nothing.
  static void onExport(Collection<SpanData> spanDataList) {
    @Nullable
    StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
        exportRequestObserverRef.get();
    if (exportRequestObserver == null) {
      return;
    }
    List<Span> spanProtos = new ArrayList<Span>();
    for (SpanData spanData : spanDataList) {
      spanProtos.add(TraceProtoUtils.toSpanProto(spanData));
    }
    // No need to set Node for subsequent ExportTraceServiceRequests, since we're using a stream
    // that is already established.
    ExportTraceServiceRequest request =
        ExportTraceServiceRequest.newBuilder().addAllSpans(spanProtos).build();
    exportRequestObserver.onNext(request);
  }

  // Worker thread that keeps trying to connect to Agent at a configured time interval
  // (retryIntervalMillis). Unless interrupted, this thread will be running forever.
  @VisibleForTesting
  static final class AgentConnectionWorker implements Runnable {

    private final String endPoint;
    private final boolean useInsecure;
    private final Node node;
    private final long retryIntervalMillis;
    private final boolean enableConfig;

    AgentConnectionWorker(
        String endPoint,
        boolean useInsecure,
        String serviceName,
        long retryIntervalMillis,
        boolean enableConfig) {
      this.endPoint = endPoint;
      this.useInsecure = useInsecure;
      this.node = OcAgentNodeUtils.getNodeInfo(serviceName);
      this.retryIntervalMillis = retryIntervalMillis;
      this.enableConfig = enableConfig;
    }

    @Override
    public void run() {
      try {
        // Infinite outer loop to keep this thread alive.
        // This thread should never exit unless interrupted.
        while (true) {
          // There's another inner loop in checkStreamsHeartbeat that attempts to connect
          // to remote Agent.
          // Once connection is established, checkStreamsHeartbeat will be blocked watching
          // UpdatedLibraryConfig from Agent if enableConfig is true.
          checkStreamsHeartbeat(endPoint, useInsecure, node, retryIntervalMillis, enableConfig);

          // Check the heartbeat again after the configured time interval.
          Thread.sleep(retryIntervalMillis);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    // Heartbeat check on stream RPC connection. If not connected, tries to establish connections to
    // Agent server at the given endPoint.
    // 1. If connection failed, this method will retry connection at the given retry time interval.
    // 2. If connection succeeded, the callback request stream observer will be stored locally.
    //    2-1. If enableConfig is true, this thread will be blocked waiting for the next
    //         UpdatedLibraryConfig. If later the connection for config service is dropped,
    //         this method will exit and the AgentConnectionWorker will try to re-establish the
    //         connection again.
    //    2-2. If enableConfig is false, this method will exit. Worker thread will invoke this
    //         method again to check the connection of export() after 5 minutes (or other configured
    //         time).
    private static void checkStreamsHeartbeat(
        String endPoint,
        boolean useInsecure,
        Node node,
        long retryIntervalMillis,
        boolean enableConfig)
        throws InterruptedException {
      ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(endPoint);
      if (useInsecure) {
        channelBuilder.usePlaintext();
      }
      ManagedChannel channel = channelBuilder.build();
      TraceServiceStub stub = TraceServiceGrpc.newStub(channel);

      // First message must have Node set.
      ExportTraceServiceRequest firstExportReq =
          ExportTraceServiceRequest.newBuilder().setNode(node).build();

      while (!isExportConnected()) {
        // If export stream is still alive, this will be skipped.
        initiateExportStream(stub, retryIntervalMillis, exportResponseObserver, firstExportReq);
      }

      if (enableConfig) {
        // First message must have Node set.
        CurrentLibraryConfig firstConfig = TraceProtoUtils.getCurrentLibraryConfig(node);
        while (!isConfigConnected()) {
          // If config stream is still alive, this will be skipped.
          initiateConfigStream(
              stub, retryIntervalMillis, updatedLibraryConfigObserver, firstConfig);
        }

        while (isConfigConnected()) {
          // Block current thread so as to wait for the next UpdatedLibraryConfig message from
          // Agent. If either of the two clients observes an error (e.g network failure, server
          // shutdown), we'll escape from this loop and return to the outer loop. Then the outer
          // loop will try to re-establish connections for the two services.
        }
      }
    }

    // Stream observer for ExportTraceServiceResponse.
    // This observer ignores the ExportTraceServiceResponse and won't block the thread.
    private static final StreamObserver<ExportTraceServiceResponse> exportResponseObserver =
        new StreamObserver<ExportTraceServiceResponse>() {
          @Override
          public void onNext(ExportTraceServiceResponse value) {
            // Do nothing since ExportTraceServiceResponse is an empty message.
          }

          @Override
          public void onError(Throwable t) {
            // If there's an error, reset the reference to exportRequestObserver to force
            // re-establish a connection.
            logger.warning("Export stream is disconnected because of: " + t.getMessage());
            resetExportClient();
          }

          @Override
          public void onCompleted() {
            resetExportClient();
          }
        };

    // Stream observer for UpdatedLibraryConfig messages. Once there's a new UpdatedLibraryConfig,
    // apply it to the global TraceConfig.
    private static final StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver =
        new StreamObserver<UpdatedLibraryConfig>() {
          @Override
          public void onNext(UpdatedLibraryConfig value) {
            TraceProtoUtils.applyUpdatedConfig(value);

            // Add a check in case that connection dropped while config is being updated.
            if (isConfigConnected()) {
              // Bouncing back CurrentLibraryConfig to Agent.
              currentConfigObserverRef.get().onNext(TraceProtoUtils.getCurrentLibraryConfig(null));
            }
          }

          @Override
          public void onError(Throwable t) {
            // If there's an error, reset the reference to currentConfigObserver to force
            // re-establish a connection.
            logger.warning("Config stream is disconnected because of: " + t.getMessage());
            resetConfigClient();
          }

          @Override
          public void onCompleted() {
            resetConfigClient();
          }
        };

    @VisibleForTesting
    static void initiateExportStream(
        TraceServiceStub stub,
        long retryIntervalMillis,
        StreamObserver<ExportTraceServiceResponse> exportResponseObserver,
        ExportTraceServiceRequest firstExportReq)
        throws InterruptedException {
      try {
        StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
            stub.export(exportResponseObserver);
        exportRequestObserverRef.set(exportRequestObserver);
        exportRequestObserver.onNext(firstExportReq);
      } catch (Exception e) {
        // If there are other exceptions not caught by exportResponseObserver.onError,
        // reset the reference to exportRequestObserver.
        resetExportClient();
        Thread.sleep(retryIntervalMillis);
      }
    }

    @VisibleForTesting
    static void initiateConfigStream(
        TraceServiceStub stub,
        long retryIntervalMillis,
        StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver,
        CurrentLibraryConfig firstConfig)
        throws InterruptedException {
      try {
        StreamObserver<CurrentLibraryConfig> currentConfigObserver =
            stub.config(updatedLibraryConfigObserver);
        currentConfigObserverRef.set(currentConfigObserver);
        currentConfigObserver.onNext(firstConfig);
      } catch (Exception e) {
        // If there are other exceptions not caught by updatedLibraryConfigObserver.onError,
        // reset the reference to currentConfigObserver.
        resetConfigClient();
        Thread.sleep(retryIntervalMillis);
      }
    }
  }

  /** A lightweight {@link ThreadFactory} to spawn threads in a GAE-Java7-compatible way. */
  private static final class DaemonThreadFactory implements ThreadFactory {
    // AppEngine runtimes have constraints on threading and socket handling
    // that need to be accommodated.
    private static final boolean IS_RESTRICTED_APPENGINE =
        System.getProperty("com.google.appengine.runtime.environment") != null
            && "1.7".equals(System.getProperty("java.specification.version"));
    private static final ThreadFactory threadFactory = MoreExecutors.platformThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = threadFactory.newThread(r);
      if (!IS_RESTRICTED_APPENGINE) {
        thread.setName("ConfigServiceClientWorkerThread");
        thread.setDaemon(true);
      }
      return thread;
    }
  }
}
