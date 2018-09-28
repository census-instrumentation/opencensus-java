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
import io.grpc.StatusRuntimeException;
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
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Implementation of OC-Agent Trace Service clients.
 *
 * <p>Since there are two RPC services config() and export(), there should be two clients
 * ExportServiceClient and ConfigServiceClient. ExportServiceClient is passed as a callback to
 * OcAgentTraceExporterHandler. ConfigServiceClient will be running in a worker thread and observing
 * incoming updated config.
 */
final class OcAgentTraceServiceClients {

  private OcAgentTraceServiceClients() {}

  private static final int MAX_RETRY_TIMES = 10;
  private static final int NANOS_PER_MICRO = 1000;
  private static final int BACKOFF_BASE_UNIT_NANOS = 200 * NANOS_PER_MICRO;
  private static final Logger logger = Logger.getLogger(OcAgentTraceServiceClients.class.getName());
  private static final DaemonThreadFactory factory = new DaemonThreadFactory();

  // A reference to currentConfigObserver, shared between this thread and the daemon thread that
  // handles Config stream.
  private static final AtomicReference<StreamObserver<CurrentLibraryConfig>>
      currentConfigObserverRef = new AtomicReference<>();

  // Stream observer for ExportTraceServiceResponse.
  // This observer is a no-op and won't block the thread.
  private static final StreamObserver<ExportTraceServiceResponse> exportResponseObserver =
      new StreamObserver<ExportTraceServiceResponse>() {
        @Override
        public void onNext(ExportTraceServiceResponse value) {
          // Do nothing since ExportTraceServiceResponse is an empty message.
        }

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onCompleted() {}
      };

  // Tries to establish connections to Agent server with the given endPoint. If succeeded, create
  // ExportServiceClient and ConfigServiceClientWorker, start ConfigServiceClientWorker in a worker
  // thread, and return ExportServiceClient. Otherwise return null.
  @Nullable
  static ExportServiceClient openStreams(String endPoint, boolean useInsecure, String serviceName) {
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(endPoint);
    if (useInsecure) {
      channelBuilder.usePlaintext();
    }
    ManagedChannel channel = channelBuilder.build();
    TraceServiceStub stub = TraceServiceGrpc.newStub(channel);
    Node node = OcAgentNodeUtils.getNodeInfo(serviceName);

    // First message must have Node set.
    ExportTraceServiceRequest firstExportReq =
        ExportTraceServiceRequest.newBuilder().setNode(node).build();

    // Export will be handled in this thread.
    @Nullable
    StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
        initiateConnectionWithRetries(
            stub, firstExportReq, exportResponseObserver, ServiceName.EXPORT);

    // In a daemon thread, handle trace configurations that are beamed down by the agent, also
    // reply to it with the applied configuration.
    factory.newThread(new ConfigServiceClientWorker(stub, node)).start();

    if (exportRequestObserver == null || currentConfigObserverRef.get() == null) {
      return null;
    }
    return new ExportServiceClient(exportRequestObserver);
  }

  // Tries to initiate connection with Agent server with exponential backoff.
  @Nullable
  @VisibleForTesting
  static <ReqT, RespT> StreamObserver<ReqT> initiateConnectionWithRetries(
      TraceServiceStub stub,
      ReqT request,
      StreamObserver<RespT> responseObserver,
      ServiceName serviceName) {
    for (int i = 0; i < MAX_RETRY_TIMES; i++) {
      try {
        return initiateConnection(stub, request, responseObserver, serviceName);
      } catch (StatusRuntimeException e) { // Only retry on gRPC StatusRuntimeException.
        logger.warning(
            String.format(
                "Failed to connect to OC-Agent because of %s. Retried %d time(s).", e, i));
        // Exponential backoff.
        try {
          Thread.sleep(0, getBackoffTimeInNanos(i));
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
        }
      } catch (Exception e) {
        logger.warning(
            String.format(
                "Failed to connect to OC-Agent because of %s. Not a retriable exception.", e));
        return null;
      }
    }
    return null; // Running out of retries.
  }

  // Tries to initiate connection with Agent server. If succeeded returns a request stream observer,
  // otherwise returns null.
  @Nullable
  @SuppressWarnings("unchecked")
  private static <ReqT, RespT> StreamObserver<ReqT> initiateConnection(
      TraceServiceStub stub,
      ReqT request,
      StreamObserver<RespT> responseObserver,
      ServiceName serviceName) {
    @Nullable StreamObserver<ReqT> requestObserver = null;
    switch (serviceName) {
      case EXPORT:
        requestObserver =
            (StreamObserver<ReqT>)
                stub.export((StreamObserver<ExportTraceServiceResponse>) responseObserver);
        break;
      case CONFIG:
        requestObserver =
            (StreamObserver<ReqT>)
                stub.config((StreamObserver<UpdatedLibraryConfig>) responseObserver);
        break;
        // default: null.
    }
    if (requestObserver != null) {
      requestObserver.onNext(request);
    }
    return requestObserver;
  }

  @VisibleForTesting
  enum ServiceName {
    CONFIG,
    EXPORT
  }

  private static int getBackoffTimeInNanos(int retries) {
    return BACKOFF_BASE_UNIT_NANOS * ((int) Math.round(Math.pow(2, retries)))
        + (new Random().nextInt(NANOS_PER_MICRO) + NANOS_PER_MICRO);
  }

  // Client for export RPC service.
  static final class ExportServiceClient {

    // A reference of the stream observer for ExportTraceServiceRequest messages from Agent server.
    // Once it's connected, it should be kept alive and we should reuse this stream observer.
    private final StreamObserver<ExportTraceServiceRequest> exportRequestObserver;

    @VisibleForTesting
    ExportServiceClient(StreamObserver<ExportTraceServiceRequest> exportRequestObserver) {
      this.exportRequestObserver = exportRequestObserver;
    }

    void onExport(Collection<SpanData> spanDataList) {
      List<Span> spanProtos = new ArrayList<Span>();
      for (SpanData spanData : spanDataList) {
        spanProtos.add(TraceProtoUtils.toSpanProto(spanData));
      }
      // No need to set Node for subsequent ExportTraceServiceRequests, since we're using the same
      // stream.
      ExportTraceServiceRequest request =
          ExportTraceServiceRequest.newBuilder().addAllSpans(spanProtos).build();
      exportRequestObserver.onNext(request);
    }
  }

  // Worker that observes the Config stream, apply incoming configs and echo back config of current
  // library.
  private static final class ConfigServiceClientWorker implements Runnable {

    // Stream observer for UpdatedLibraryConfig messages. Once there's a new UpdatedLibraryConfig,
    // apply it to the global TraceConfig.
    private static final StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver =
        new StreamObserver<UpdatedLibraryConfig>() {
          @Override
          public void onNext(UpdatedLibraryConfig value) {
            TraceProtoUtils.applyUpdatedConfig(value);
            currentConfigObserverRef.get().onNext(TraceProtoUtils.getCurrentLibraryConfig(null));
          }

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onCompleted() {}
        };

    private final TraceServiceStub stub;
    private final Node node;

    ConfigServiceClientWorker(TraceServiceStub stub, Node node) {
      this.stub = stub;
      this.node = node;
    }

    @Override
    public void run() {
      CountDownLatch finishedLatch = new CountDownLatch(1);
      CurrentLibraryConfig firstConfig = TraceProtoUtils.getCurrentLibraryConfig(node);
      @Nullable
      StreamObserver<CurrentLibraryConfig> currentConfigObserver =
          initiateConnectionWithRetries(
              stub, firstConfig, updatedLibraryConfigObserver, ServiceName.CONFIG);
      if (currentConfigObserver == null) {
        return;
      }
      currentConfigObserverRef.set(currentConfigObserver);
      currentConfigObserver.onNext(firstConfig);
      try {
        finishedLatch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
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
