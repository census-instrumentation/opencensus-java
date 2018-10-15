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
   * We can use ManagedChannel.getState() to determine the state of current connection.
   *
   * 0. For export RPC we hold a reference "exportRequestObserverRef" to a StreamObserver for
   * request stream, and "exportRequestObserverRef" to a StreamObserver for response stream.
   * For config RPC we have "currentConfigObserverRef" and "updatedConfigObserverRef". Note that
   * exportRequestObserver and currentConfigObserver are defined by and returned from Agent, while
   * exportRequestObserver and updatedConfigObserver are defined in this class and will be passed to
   * Agent.
   *
   * Think of the whole process as a Finite State Machine.
   *
   * Note: "Exporter" = Agent Exporter and "Agent" = OC-Agent in the sections below.
   *
   * STATE 1: Unconnected/Disconnected.
   *
   * 1. First, Exporter will try to establish the streams by calling TraceServiceStub.export and
   * TraceServiceStub.config in a daemon thread.
   *
   *   1-1. If the initial attempt succeeded, exportRequestObserver and currentConfigObserver
   *   should be returned to Exporter as callbacks, and we keep a reference of them in the
   *   exportRequestObserverRef and currentConfigObserverRef. At the same time,
   *   exportRequestObserver and updatedConfigObserver will be passed to Agent.
   *
   *   1-2. If the attempt failed, TraceServiceStub.export or TraceServiceStub.config will throw
   *   an exception. We should catch the exceptions and keep retrying. exportRequestObserver and
   *   currentConfigObserver will not be returned to Exporter and the references are null.
   *
   *   1-3. After each attempt, we should check if the connection succeeded or not.
   *   If connection succeeded and Exporter has the reference to exportRequestObserver and
   *   currentConfigObserver, we can move forward and start sending/receiving streams
   *   (move to STATE 2). Otherwise Exporter should retry.
   *
   * STATE 2: Already connected.
   *
   * 2. Once streams are open, they should be kept alive. Exporter sends spans to Agent by calling
   * exportRequestObserver.onNext (the callback provided by Agent). In addition, in another daemon
   * thread Exporter watches config updates from Agent, which is done by Agent calling back
   * updatedConfigObserver.onNext.
   *
   *   2-1. If for some reason the connection is interrupted, the errors will be caught by
   *   the onError method of exportResponseObserver and updatedLibraryConfigObserver. In that case,
   *   we should reset references of exportRequestObserver and currentConfigObserver to null.
   *   Then we will create a new channel and stub, try to connect to Agent, and try to get new
   *   exportRequestObserver and currentConfigObserver from Agent. (Back to STATE 1.)
   *
   *   2-2. If for some reason Agent decided to end the stream by calling onComplete, do the same
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

  // After resetting, the channel should also be closed.
  static void resetExportRequestObserverRef() {
    exportRequestObserverRef.set(null);
  }

  // After resetting, the channel should also be closed.
  static void resetCurrentConfigObserverRef() {
    currentConfigObserverRef.set(null);
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
          // TODO(songya): add retries on this connection before starting a new connection.
          logger.log(Level.WARNING, "Export stream is disconnected.", t);
          /*@Nullable*/ StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
              exportRequestObserverRef.get();
          if (exportRequestObserver != null) {
            exportRequestObserver.onError(t); // Tries to notify OC-Agent about the error.
            resetExportRequestObserverRef();
          }
        }

        @Override
        public void onCompleted() {
          resetExportRequestObserverRef();
        }
      };

  // Stream observer for UpdatedLibraryConfig messages. Once there's a new UpdatedLibraryConfig,
  // apply it to the global TraceConfig.
  private static final StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver =
      new UpdatedLibraryConfigObserver(Tracing.getTraceConfig());

  // A reference to the exportResponseObserver defined in this class.
  // Could be shared between multiple threads.
  static final AtomicReference<StreamObserver<ExportTraceServiceResponse>>
      exportResponseObserverRef = new AtomicReference<>(exportResponseObserver);

  // A reference to the updatedLibraryConfigObserver defined in this class.
  // Could be shared between multiple threads.
  static final AtomicReference<StreamObserver<UpdatedLibraryConfig>> updateConfigObserverRef =
      new AtomicReference<>(updatedLibraryConfigObserver);

  @VisibleForTesting
  static class UpdatedLibraryConfigObserver implements StreamObserver<UpdatedLibraryConfig> {

    // Inject a mock TraceConfig in unit tests.
    private final TraceConfig traceConfig;

    @VisibleForTesting
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
      logger.log(Level.WARNING, "Config stream is disconnected.", t);
      /*@Nullable*/ StreamObserver<CurrentLibraryConfig> currentConfigObserver =
          currentConfigObserverRef.get();
      if (currentConfigObserver != null) {
        currentConfigObserver.onError(t); // Tries to notify OC-Agent about the error.
        resetCurrentConfigObserverRef();
      }
    }

    @Override
    public void onCompleted() {
      resetCurrentConfigObserverRef();
    }
  }
}
