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

import io.opencensus.common.Duration;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.Collection;
import javax.annotation.Nullable;

/** Exporting handler for OC-Agent Tracing. */
final class OcAgentTraceExporterHandler extends Handler {

  // private static final String DEFAULT_END_POINT = "localhost:55678";
  // private static final String DEFAULT_SERVICE_NAME = "OpenCensus";
  // private static final Duration DEFAULT_RETRY_INTERVAL = Duration.create(300, 0); // 5 minutes

  OcAgentTraceExporterHandler() {
    this(null, null, null, null, /* enableConfig= */ true);
  }

  OcAgentTraceExporterHandler(
      @Nullable String endPoint,
      @Nullable String serviceName,
      @Nullable Boolean useInsecure,
      @Nullable Duration retryInterval,
      boolean enableConfig) {
    // if (endPoint == null) {
    //   endPoint = DEFAULT_END_POINT;
    // }
    // if (serviceName == null) {
    //   serviceName = DEFAULT_SERVICE_NAME;
    // }
    // if (useInsecure == null) {
    //   useInsecure = false;
    // }
    // if (retryInterval == null) {
    //   retryInterval = DEFAULT_RETRY_INTERVAL;
    // }
    // OcAgentTraceServiceClients.startAttemptsToConnectToAgent(
    //     endPoint, useInsecure, serviceName, retryInterval.toMillis(), enableConfig);
  }

  /*
   * TODO(songya): move these paragraphs to worker thread class.
   *
   * We should use ManagedChannel.getState() to determine the state of current connection.
   *
   * 0. For export RPC we hold a reference "exportRequestObserverRef" to a StreamObserver for
   * request stream, and "exportResponseObserverRef" to a StreamObserver for response stream.
   * For config RPC we have "configResponseObserverRef" and "configRequestObserverRef".
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
   *   1-1. If the initial attempt succeeded, Exporter should receive messages from the Agent, and
   *   we should be able to get references of exportRequestObserver and configResponseObserver.
   *
   *   1-2. If the attempt failed, TraceServiceStub.export or TraceServiceStub.config will throw
   *   an exception. We should catch the exceptions and keep retrying. References of
   *   exportRequestObserver and configResponseObserverRef should be null in this case.
   *
   *   1-3. After each attempt, we should check if the connection succeeded or not with
   *   ManagedChannel.getState(). (This is done in the daemon thread.)
   *   If connection succeeded and Exporter has the references to exportRequestObserver and
   *   currentConfigObserver, we can move forward and start sending/receiving streams
   *   (move to STATE 2). Otherwise Exporter should retry.
   *
   * STATE 2: Already connected.
   *
   * 2. Once streams are open, they should be kept alive. Exporter sends spans to Agent by calling
   * exportRequestObserver.onNext. In addition, in another daemon thread Exporter watches config
   * updates from Agent.
   *
   *   2-1. If for some reason the connection is interrupted or ended, the errors (if any) will be
   *   caught by the onError method of any stream observer. In that case, we should reset references
   *   to all the stream observers.
   *   Then we will create a new channel and stub, try to connect to Agent, and try to get new
   *   exportRequestObserver and currentConfigObserver from Agent. (Back to STATE 1.)
   *
   *
   * Therefore we have an invariant throughout the entire process: if the references of
   * configResponseObserverRef and exportRequestObserverRef are non-null, the streams must still be
   * open and alive; otherwise streams must be disconnected and we should attempt to re-connect.
   *
   * FYI the method signatures on both sides:
   *
   * Agent has:
   *
   * public abstract static class TraceServiceImplBase {
   *
   *   public abstract StreamObserver<CurrentLibraryConfig> config(
   *     StreamObserver<UpdatedLibraryConfig> responseObserver);
   *
   *   public abstract StreamObserver<ExportTraceServiceRequest> export(
   *     StreamObserver<ExportTraceServiceResponse> responseObserver);
   * }
   *
   * Exporter has:
   *
   * public static final class TraceServiceStub {
   *
   *   public StreamObserver<CurrentLibraryConfig> config(
   *     StreamObserver<UpdatedLibraryConfig> responseObserver) {
   *     // implementation
   *   }
   *
   *   public StreamObserver<ExportTraceServiceRequest> export(
   *     StreamObserver<ExportTraceServiceResponse> responseObserver) {
   *     // implementation
   *   }
   *
   * }
   */

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // OcAgentTraceServiceClients.onExport(spanDataList);
  }
}
