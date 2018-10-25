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

/** Stream observers of config service. */
final class OcAgentTraceServiceConfigObservers {

  private OcAgentTraceServiceConfigObservers() {}

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceServiceConfigObservers.class.getName());

  // A reference to the callback currentConfigObserver returned from Agent.
  // Shared between multiple threads.
  // The reference is non-null only when the connection succeeded.
  static final AtomicReference</*@Nullable*/ StreamObserver<CurrentLibraryConfig>>
      currentConfigObserverRef = new AtomicReference<>();

  // After resetting, the channel should also be closed.
  static void resetCurrentConfigObserverRef() {
    currentConfigObserverRef.set(null);
  }

  // Stream observer for UpdatedLibraryConfig messages. Once there's a new UpdatedLibraryConfig,
  // apply it to the global TraceConfig.
  private static final StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigObserver =
      new UpdatedLibraryConfigObserver(Tracing.getTraceConfig());

  // A reference to the updatedLibraryConfigObserver defined in this class.
  // Could be shared between multiple threads.
  static final AtomicReference<StreamObserver<UpdatedLibraryConfig>> updateConfigObserverRef =
      new AtomicReference<>(updatedLibraryConfigObserver);

  // TODO(songya): consider making this observer stateful and having a custom Runnable to onError.
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
      resetCurrentConfigObserverRef();
    }

    @Override
    public void onCompleted() {
      resetCurrentConfigObserverRef();
    }
  }
}
