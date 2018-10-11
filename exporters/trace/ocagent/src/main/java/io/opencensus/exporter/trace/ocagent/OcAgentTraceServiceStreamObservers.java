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
import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Stream observers of OC-Agent Trace Service. */
final class OcAgentTraceServiceStreamObservers {

  private OcAgentTraceServiceStreamObservers() {}

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceServiceStreamObservers.class.getName());

  /*
   * gRPC-Java has good abstractions over the transport layer, and there's no way we can know the
   * connection status directly (nothing like *grpc.ClientConn in Go). Instead, we should verify the
   * connection when initiating and sending streams:
   *
   * 0. On client side we defined exportResponseObserver and updatedLibraryConfigObserver. On server
   * side we defined exportRequestObserver and currentConfigObserver.
   * Note: client == Agent Exporter, server == OC-Agent.
   *
   * Think of the whole process as a Finite State Machine.
   *
   * STATE 1: Unconnected/Disconnected.
   *
   * 1. First, client will try to establish the streams by calling TraceServiceStub.export and
   * TraceServiceStub.config in a daemon thread.
   *
   *   1-1. If the initial attempt succeeded, the server side observers should be returned to client
   *   as callbacks, and we keep a reference of them in the exportRequestObserverRef and
   *   currentConfigObserverRef. Same thing happen for client side observers.
   *
   *   1-2. If the attempt failed, TraceServiceStub.export or TraceServiceStub.config will throw
   *   an exception. We should catch the exceptions and keep retrying. Server side observers will
   *   not be returned to client and the references are null.
   *
   *   1-3. Therefore, after each attempt, we will be able to know if the connection succeeded or
   *   not by checking the references of exportRequestObserverRef and currentConfigObserverRef.
   *   If they're non-null, that means connection succeeded and we can move forward and start
   *   sending/receiving streams (move to STATE 2). Otherwise client should retry.
   *
   * STATE 2: Already connected.
   *
   * 2. Once streams are open, they should be kept alive. Client sends spans to server by using
   * exportRequestObserver.onNext (the callback provided by server). In addition, in another daemon
   * thread client watches config updates from server, which is done by server calling back
   * updatedConfigObserver.onNext.
   *
   *   2-1. If for some reason the connection is interrupted, the errors will be caught by
   *   the onError method of exportResponseObserver and updatedLibraryConfigObserver. In that case,
   *   we consider the connections as disconnected, and reset references of server stream observers
   *   to null. Then we will create a new channel and stub, try to connect to server, and try to
   *   get another exportRequestObserver and currentConfigObserver from server. (Back to STATE 1.)
   *
   *   2-2. If for some reason server decided to end the stream by calling onComplete, do the same
   *   thing as onError.
   *
   *
   * Therefore we have an invariant throughout the entire process: if the references of
   * currentConfigObserverRef and exportRequestObserverRef are non-null, the streams must still be
   * open and alive; otherwise streams must be disconnected and we should attempt to re-connect.
   */

  // A reference to the callback exportRequestObserver returned from Agent server.
  // Shared between multiple thread.
  // The reference is non-null only when the connection succeeded.
  static final AtomicReference</*@Nullable*/ StreamObserver<ExportTraceServiceRequest>>
      exportRequestObserverRef = new AtomicReference<>();

  // A reference to the callback currentConfigObserver returned from Agent server.
  // Shared between multiple threads.
  // The reference is non-null only when the connection succeeded.
  static final AtomicReference</*@Nullable*/ StreamObserver<CurrentLibraryConfig>>
      currentConfigObserverRef = new AtomicReference<>();

  // Check if the export() connection is open and alive.
  static boolean isExportConnected() {
    return exportRequestObserverRef.get() != null;
  }

  // Check if the config() connection is open and alive.
  static boolean isConfigConnected() {
    return currentConfigObserverRef.get() != null;
  }

  static void resetExportClient() {
    exportRequestObserverRef.set(null);
  }

  static void resetConfigClient() {
    currentConfigObserverRef.set(null);
  }

  // Stream observer for ExportTraceServiceResponse.
  // This observer ignores the ExportTraceServiceResponse and won't block the thread.
  static final StreamObserver<ExportTraceServiceResponse> exportResponseObserver =
      new StreamObserver<ExportTraceServiceResponse>() {
        @Override
        public void onNext(ExportTraceServiceResponse value) {
          // Do nothing since ExportTraceServiceResponse is an empty message.
        }

        @Override
        public void onError(Throwable t) {
          // If there's an error, reset the reference to exportRequestObserver to force
          // re-establish a connection.
          // TODO(songya): add retries on this connection before starting a new connection.
          logger.log(Level.WARNING, "Export stream is disconnected because of: {0}", t);
          /*@Nullable*/ StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
              exportRequestObserverRef.get();
          if (exportRequestObserver != null) {
            exportRequestObserver.onError(t); // Tries to notify OC-Agent about the error.
            resetExportClient();
          }
        }

        @Override
        public void onCompleted() {
          resetExportClient();
        }
      };

  // Stream observer for UpdatedLibraryConfig messages. Once there's a new UpdatedLibraryConfig,
  // apply it to the global TraceConfig.
  static final StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver =
      new UpdatedLibraryConfigObserver(Tracing.getTraceConfig());

  @VisibleForTesting
  static class UpdatedLibraryConfigObserver implements StreamObserver<UpdatedLibraryConfig> {

    // Inject a mock TraceConfig in unit tests.
    private final TraceConfig traceConfig;

    UpdatedLibraryConfigObserver(TraceConfig traceConfig) {
      this.traceConfig = traceConfig;
    }

    @Override
    public void onNext(UpdatedLibraryConfig value) {
      TraceParams updatedTraceParams = TraceProtoUtils.getUpdatedTraceParams(value, traceConfig);
      traceConfig.updateActiveTraceParams(updatedTraceParams);

      /*@Nullable*/ StreamObserver<CurrentLibraryConfig> currentConfigObserver =
          currentConfigObserverRef.get();
      // Add a check in case that connection dropped while config is being updated.
      if (currentConfigObserver != null) {
        // Bouncing back CurrentLibraryConfig to Agent.
        io.opencensus.proto.trace.v1.TraceConfig currentTraceConfigProto =
            TraceProtoUtils.getCurrentTraceConfig(traceConfig);
        CurrentLibraryConfig currentLibraryConfig =
            CurrentLibraryConfig.newBuilder().setConfig(currentTraceConfigProto).build();
        currentConfigObserver.onNext(currentLibraryConfig);
      }
    }

    @Override
    public void onError(Throwable t) {
      // If there's an error, reset the reference to currentConfigObserver to force
      // re-establish a connection.
      // TODO(songya): add retries on this connection before starting a new connection.
      logger.log(Level.WARNING, "Config stream is disconnected because of: {0}", t);
      /*@Nullable*/ StreamObserver<CurrentLibraryConfig> currentConfigObserver =
          currentConfigObserverRef.get();
      if (currentConfigObserver != null) {
        currentConfigObserver.onError(t); // Tries to notify OC-Agent about the error.
        resetConfigClient();
      }
    }

    @Override
    public void onCompleted() {
      resetConfigClient();
    }
  }
}
