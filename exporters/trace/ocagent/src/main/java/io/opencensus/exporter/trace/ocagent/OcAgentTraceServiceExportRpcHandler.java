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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Handler of export service RPC. */
@ThreadSafe
final class OcAgentTraceServiceExportRpcHandler {

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceServiceExportRpcHandler.class.getName());

  // A reference to the exportRequestObserver returned from stub.
  @GuardedBy("this")
  @Nullable
  private StreamObserver<ExportTraceServiceRequest> exportRequestObserver;

  // The RPC status when this stream finishes/disconnects. Null if the stream is still connected.
  @GuardedBy("this")
  @Nullable
  private Status terminateStatus;

  private OcAgentTraceServiceExportRpcHandler() {}

  private synchronized void setExportRequestObserver(
      StreamObserver<ExportTraceServiceRequest> exportRequestObserver) {
    this.exportRequestObserver = exportRequestObserver;
  }

  // Creates an OcAgentTraceServiceExportRpcHandler. Tries to initiate the export stream with the
  // given TraceServiceStub.
  static OcAgentTraceServiceExportRpcHandler create(TraceServiceStub stub) {
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        new OcAgentTraceServiceExportRpcHandler();
    ExportResponseObserver exportResponseObserver = new ExportResponseObserver(exportRpcHandler);
    try {
      StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
          stub.export(exportResponseObserver);
      exportRpcHandler.setExportRequestObserver(exportRequestObserver);
    } catch (Exception e) {
      exportRpcHandler.onComplete(e);
    }
    return exportRpcHandler;
  }

  // Sends the export request to Agent if the stream is still connected, otherwise do nothing.
  synchronized void onExport(ExportTraceServiceRequest request) {
    if (isCompleted() || exportRequestObserver == null) {
      return;
    }
    try {
      exportRequestObserver.onNext(request);
    } catch (Exception e) { // Catch client side exceptions.
      onComplete(e);
    }
  }

  // Marks this export stream as completed with an optional error.
  // Once onComplete is called, this OcAgentTraceServiceExportRpcHandler instance can be discarded
  // and GC'ed in the worker thread.
  synchronized void onComplete(@javax.annotation.Nullable Throwable error) {
    if (isCompleted()) {
      return;
    }
    // TODO(songya): add Runnable
    Status status;
    if (error == null) {
      status = Status.OK;
    } else if (error instanceof StatusRuntimeException) {
      status = ((StatusRuntimeException) error).getStatus();
    } else {
      status = Status.UNKNOWN;
    }
    terminateStatus = status;
  }

  synchronized boolean isCompleted() {
    return terminateStatus != null;
  }

  @VisibleForTesting
  @Nullable
  synchronized Status getTerminateStatus() {
    return terminateStatus;
  }

  @VisibleForTesting
  static class ExportResponseObserver implements StreamObserver<ExportTraceServiceResponse> {

    private final OcAgentTraceServiceExportRpcHandler exportRpcHandler;

    ExportResponseObserver(OcAgentTraceServiceExportRpcHandler exportRpcHandler) {
      this.exportRpcHandler = exportRpcHandler;
    }

    @Override
    public void onNext(ExportTraceServiceResponse value) {
      // Do nothing since ExportTraceServiceResponse is an empty message.
    }

    @Override
    public void onError(Throwable t) {
      logger.log(Level.WARNING, "Export stream is disconnected.", t);
      exportRpcHandler.onComplete(t);
    }

    @Override
    public void onCompleted() {
      exportRpcHandler.onComplete(null);
    }
  }
}
