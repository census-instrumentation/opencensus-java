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
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Handler of config service RPC. */
@ThreadSafe
final class OcAgentTraceServiceConfigRpcHandler {

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceServiceConfigRpcHandler.class.getName());

  // A reference to the currentConfigObserver returned from stub.
  @GuardedBy("this")
  @Nullable
  private StreamObserver<CurrentLibraryConfig> currentConfigObserver;

  // The RPC status when this stream finishes/disconnects. Null if the stream is still connected.
  @GuardedBy("this")
  @Nullable
  private Status terminateStatus;

  private final TraceConfig traceConfig; // Inject a mock TraceConfig in unit tests.

  private OcAgentTraceServiceConfigRpcHandler(TraceConfig traceConfig) {
    this.traceConfig = traceConfig;
  }

  private synchronized void setCurrentConfigObserver(
      StreamObserver<CurrentLibraryConfig> currentConfigObserver) {
    this.currentConfigObserver = currentConfigObserver;
  }

  // Creates an OcAgentTraceServiceConfigRpcHandler. Tries to initiate the config stream with the
  // given TraceServiceStub.
  static OcAgentTraceServiceConfigRpcHandler create(
      TraceServiceStub stub, TraceConfig traceConfig) {
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        new OcAgentTraceServiceConfigRpcHandler(traceConfig);
    UpdatedLibraryConfigObserver updatedLibraryConfigObserver =
        new UpdatedLibraryConfigObserver(traceConfig, configRpcHandler);
    try {
      StreamObserver<CurrentLibraryConfig> currentConfigObserver =
          stub.config(updatedLibraryConfigObserver);
      configRpcHandler.setCurrentConfigObserver(currentConfigObserver);
    } catch (StatusRuntimeException e) {
      configRpcHandler.onComplete(e);
    }
    return configRpcHandler;
  }

  // Sends the initial config message with Node to Agent.
  // Once the initial config message is sent, the current thread will be blocked watching for
  // subsequent updated library configs, unless the stream is interrupted.
  synchronized void sendInitialMessage(Node node) {
    io.opencensus.proto.trace.v1.TraceConfig currentTraceConfigProto =
        TraceProtoUtils.getCurrentTraceConfig(traceConfig);
    // First config must have Node set.
    CurrentLibraryConfig firstConfig =
        CurrentLibraryConfig.newBuilder().setNode(node).setConfig(currentTraceConfigProto).build();
    sendCurrentConfig(firstConfig);
  }

  // Follow up after applying the updated library config.
  private synchronized void sendCurrentConfig() {
    // Bouncing back CurrentLibraryConfig to Agent.
    io.opencensus.proto.trace.v1.TraceConfig currentTraceConfigProto =
        TraceProtoUtils.getCurrentTraceConfig(traceConfig);
    CurrentLibraryConfig currentLibraryConfig =
        CurrentLibraryConfig.newBuilder().setConfig(currentTraceConfigProto).build();
    sendCurrentConfig(currentLibraryConfig);
  }

  // Sends current config to Agent if the stream is still connected, otherwise do nothing.
  private synchronized void sendCurrentConfig(CurrentLibraryConfig currentLibraryConfig) {
    if (isCompleted() || currentConfigObserver == null) {
      return;
    }
    try {
      currentConfigObserver.onNext(currentLibraryConfig);
    } catch (Exception e) { // Catch client side exceptions.
      onComplete(e);
    }
  }

  // Marks this config stream as completed with an optional error.
  // Once onComplete is called, this OcAgentTraceServiceConfigRpcHandler instance can be discarded
  // and GC'ed in the worker thread.
  synchronized void onComplete(@javax.annotation.Nullable Throwable error) {
    if (isCompleted()) {
      return;
    }
    currentConfigObserver = null;
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
  static class UpdatedLibraryConfigObserver implements StreamObserver<UpdatedLibraryConfig> {

    private final TraceConfig traceConfig;
    private final OcAgentTraceServiceConfigRpcHandler configRpcHandler;

    @VisibleForTesting
    UpdatedLibraryConfigObserver(
        TraceConfig traceConfig, OcAgentTraceServiceConfigRpcHandler configRpcHandler) {
      this.traceConfig = traceConfig;
      this.configRpcHandler = configRpcHandler;
    }

    @Override
    public void onNext(UpdatedLibraryConfig value) {
      // First, apply the incoming updated config.
      TraceParams updatedTraceParams = TraceProtoUtils.getUpdatedTraceParams(value, traceConfig);
      traceConfig.updateActiveTraceParams(updatedTraceParams);

      // Then echo back current config.
      configRpcHandler.sendCurrentConfig();
    }

    @Override
    public void onError(Throwable t) {
      logger.log(Level.WARNING, "Config stream is disconnected.", t);
      configRpcHandler.onComplete(t);
    }

    @Override
    public void onCompleted() {
      configRpcHandler.onComplete(null);
    }
  }
}
