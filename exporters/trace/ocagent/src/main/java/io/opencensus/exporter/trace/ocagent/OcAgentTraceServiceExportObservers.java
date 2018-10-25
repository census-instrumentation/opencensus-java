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

import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Stream observers of export service. */
final class OcAgentTraceServiceExportObservers {

  private OcAgentTraceServiceExportObservers() {}

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceServiceExportObservers.class.getName());

  // A reference to the callback exportRequestObserver returned from Agent.
  // Shared between multiple thread.
  // The reference is non-null only when the connection succeeded.
  static final AtomicReference</*@Nullable*/ StreamObserver<ExportTraceServiceRequest>>
      exportRequestObserverRef = new AtomicReference<>();

  // After resetting, the channel should also be closed.
  static void resetExportRequestObserverRef() {
    exportRequestObserverRef.set(null);
  }

  // Stream observer for ExportTraceServiceResponse.
  // This observer ignores the ExportTraceServiceResponse and won't block the thread.
  // TODO(songya): consider making this observer stateful and having a custom Runnable to onError.
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
          resetExportRequestObserverRef();
        }

        @Override
        public void onCompleted() {
          resetExportRequestObserverRef();
        }
      };

  // A reference to the exportResponseObserver defined in this class.
  // Could be shared between multiple threads.
  static final AtomicReference<StreamObserver<ExportTraceServiceResponse>>
      exportResponseObserverRef = new AtomicReference<>(exportResponseObserver);
}
