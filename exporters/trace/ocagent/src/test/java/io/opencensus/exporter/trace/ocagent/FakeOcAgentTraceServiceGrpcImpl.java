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
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.ConstantSampler;
import io.opencensus.proto.trace.v1.ConstantSampler.ConstantDecision;
import io.opencensus.proto.trace.v1.TraceConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Fake implementation of {@link TraceServiceGrpc}. */
@ThreadSafe
final class FakeOcAgentTraceServiceGrpcImpl extends TraceServiceGrpc.TraceServiceImplBase {

  private static final Logger logger =
      Logger.getLogger(FakeOcAgentTraceServiceGrpcImpl.class.getName());

  // Default updatedLibraryConfig uses an always sampler.
  @GuardedBy("this")
  private UpdatedLibraryConfig updatedLibraryConfig =
      UpdatedLibraryConfig.newBuilder()
          .setConfig(
              TraceConfig.newBuilder()
                  .setConstantSampler(
                      ConstantSampler.newBuilder().setDecision(ConstantDecision.ALWAYS_ON).build())
                  .build())
          .build();

  @GuardedBy("this")
  private final List<CurrentLibraryConfig> currentLibraryConfigs = new ArrayList<>();

  @GuardedBy("this")
  private final List<ExportTraceServiceRequest> exportTraceServiceRequests = new ArrayList<>();

  @GuardedBy("this")
  private final AtomicReference<StreamObserver<UpdatedLibraryConfig>> configRequestObserverRef =
      new AtomicReference<>();

  @GuardedBy("this")
  private final StreamObserver<CurrentLibraryConfig> configResponseObserver =
      new StreamObserver<CurrentLibraryConfig>() {
        @Override
        public void onNext(CurrentLibraryConfig value) {
          addCurrentLibraryConfig(value);
          try {
            // Do not send UpdatedLibraryConfigs too frequently.
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Thread interrupted.", e);
          }
          sendUpdatedLibraryConfig();
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for config stream: " + t);
          resetConfigRequestObserverRef();
        }

        @Override
        public void onCompleted() {
          resetConfigRequestObserverRef();
        }
      };

  @GuardedBy("this")
  private final StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
      new StreamObserver<ExportTraceServiceRequest>() {
        @Override
        public void onNext(ExportTraceServiceRequest value) {
          addExportRequest(value);
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for export stream: " + t);
        }

        @Override
        public void onCompleted() {}
      };

  @GuardedBy("this")
  private CountDownLatch countDownLatch;

  @Override
  public synchronized StreamObserver<CurrentLibraryConfig> config(
      StreamObserver<UpdatedLibraryConfig> updatedLibraryConfigStreamObserver) {
    configRequestObserverRef.set(updatedLibraryConfigStreamObserver);
    return configResponseObserver;
  }

  @Override
  public synchronized StreamObserver<ExportTraceServiceRequest> export(
      StreamObserver<ExportTraceServiceResponse> exportTraceServiceResponseStreamObserver) {
    return exportRequestObserver;
  }

  private synchronized void addCurrentLibraryConfig(CurrentLibraryConfig currentLibraryConfig) {
    if (countDownLatch != null && countDownLatch.getCount() == 0) {
      return;
    }
    currentLibraryConfigs.add(currentLibraryConfig);
  }

  private synchronized void addExportRequest(ExportTraceServiceRequest request) {
    exportTraceServiceRequests.add(request);
  }

  // Returns the stored CurrentLibraryConfigs.
  synchronized List<CurrentLibraryConfig> getCurrentLibraryConfigs() {
    return Collections.unmodifiableList(currentLibraryConfigs);
  }

  // Returns the stored ExportTraceServiceRequests.
  synchronized List<ExportTraceServiceRequest> getExportTraceServiceRequests() {
    return Collections.unmodifiableList(exportTraceServiceRequests);
  }

  // Sets the UpdatedLibraryConfig that will be passed to client.
  synchronized void setUpdatedLibraryConfig(UpdatedLibraryConfig updatedLibraryConfig) {
    this.updatedLibraryConfig = updatedLibraryConfig;
  }

  // Gets the UpdatedLibraryConfig that will be passed to client.
  synchronized UpdatedLibraryConfig getUpdatedLibraryConfig() {
    return updatedLibraryConfig;
  }

  private synchronized void sendUpdatedLibraryConfig() {
    @Nullable
    StreamObserver<UpdatedLibraryConfig> configRequestObserver = configRequestObserverRef.get();
    if (configRequestObserver != null) {
      configRequestObserver.onNext(updatedLibraryConfig);
    }
    if (countDownLatch != null) {
      countDownLatch.countDown();
    }
  }

  // Closes config stream and resets the reference to configRequestObserver.
  synchronized void closeConfigStream() {
    configResponseObserver.onCompleted();
  }

  private synchronized void resetConfigRequestObserverRef() {
    configRequestObserverRef.set(null);
  }

  @VisibleForTesting
  synchronized void setCountDownLatch(CountDownLatch countDownLatch) {
    this.countDownLatch = countDownLatch;
  }
}
