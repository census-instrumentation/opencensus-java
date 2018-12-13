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

/**
 * Daemon worker thread that tries to connect to OC-Agent at a configured time interval
 * (retryIntervalMillis). Unless interrupted, this thread will be running forever.
 */
final class OcAgentTraceServiceConnectionWorker extends Thread {

  /*
   * We should use ManagedChannel.getState() to determine the state of current connection.
   *
   * 0. For export RPC we have ExportRpcHandler which holds references to request and response
   * observers. For config RPC we have ConfigRpcHandler.
   *
   * Think of the whole process as a Finite State Machine.
   *
   * Note: "Exporter" = Agent Exporter and "Agent" = OC-Agent in the sections below.
   *
   * STATE 1: Unconnected/Disconnected.
   *
   * 1. First, Exporter will try to establish the streams in a daemon thread.
   * This is done when ExportRpcHandler and ConfigRpcHandler are created,
   * by calling TraceServiceStub.export and TraceServiceStub.config.
   *
   *   1-1. If the initial attempt succeeded, Exporter should receive messages from the Agent, and
   *   ExportRpcHandler and ConfigRpcHandler should be created successfully.
   *
   *   1-2. If the attempt failed, TraceServiceStub.export or TraceServiceStub.config will throw
   *   an exception. We should catch the exceptions and keep retrying.
   *   ExportRpcHandler and ConfigRpcHandler will store the exiting RPC status and pass it to a
   *   Runnable defined by the daemon worker.
   *
   *   1-3. After each attempt, we should check if the connection is established with
   *   ManagedChannel.getState(). (This is done in the daemon thread.)
   *   If connection succeeded, we can move forward and start sending/receiving streams
   *   (move to STATE 2). Otherwise Exporter should retry.
   *
   * STATE 2: Already connected.
   *
   * 2. Once streams are open, they should be kept alive. The daemon worker should be blocked and
   * watching UpdatedLibraryConfig messages from Agent.
   *
   *   2-1. If for some reason the connection is interrupted or ended, the errors (if any) will be
   *   caught by ExportRpcHandler and ConfigRpcHandler. They will store the exiting RPC status and
   *   pass it to a Runnable defined by the daemon worker. At that time the ExportRpcHandler and
   *   ConfigRpcHandler will be considered completed.
   *
   *   Then we will create a new channel and stub, try to connect to Agent, and try to create new
   *   ExportRpcHandler and ConfigRpcHandler. (Back to STATE 1.)
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

  // private final String endPoint;
  // private final boolean useInsecure;
  // private final Node node;
  private final long retryIntervalMillis;
  // private final boolean enableConfig;

  @VisibleForTesting
  OcAgentTraceServiceConnectionWorker(
      String endPoint,
      boolean useInsecure,
      String serviceName,
      long retryIntervalMillis,
      boolean enableConfig) {
    // this.endPoint = endPoint;
    // this.useInsecure = useInsecure;
    // this.node = OcAgentNodeUtils.getNodeInfo(serviceName);
    this.retryIntervalMillis = retryIntervalMillis;
    // this.enableConfig = enableConfig;
    setDaemon(true);
    setName("OcAgentTraceServiceConnectionWorker");
  }

  static void startThread(
      String endPoint,
      boolean useInsecure,
      String serviceName,
      long retryIntervalMillis,
      boolean enableConfig) {
    new OcAgentTraceServiceConnectionWorker(
            endPoint, useInsecure, serviceName, retryIntervalMillis, enableConfig)
        .start();
  }

  @Override
  public void run() {
    try {
      // Infinite outer loop to keep this thread alive.
      // This thread should never exit unless interrupted.
      while (true) {

        // TODO(songya): implement this.

        // Retry connection after the configured time interval.
        Thread.sleep(retryIntervalMillis);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
