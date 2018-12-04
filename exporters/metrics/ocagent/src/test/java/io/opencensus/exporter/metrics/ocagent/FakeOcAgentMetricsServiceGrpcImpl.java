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

package io.opencensus.exporter.metrics.ocagent;

import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.metrics.v1.ExportMetricsServiceRequest;
import io.opencensus.proto.agent.metrics.v1.ExportMetricsServiceResponse;
import io.opencensus.proto.agent.metrics.v1.MetricsServiceGrpc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Fake implementation of {@link MetricsServiceGrpc}. Used for unit tests only. */
@ThreadSafe
final class FakeOcAgentMetricsServiceGrpcImpl extends MetricsServiceGrpc.MetricsServiceImplBase {

  private static final Logger logger =
      Logger.getLogger(FakeOcAgentMetricsServiceGrpcImpl.class.getName());

  @GuardedBy("this")
  private final List<ExportMetricsServiceRequest> exportMetricsServiceRequests = new ArrayList<>();

  @GuardedBy("this")
  private final StreamObserver<ExportMetricsServiceRequest> exportRequestObserver =
      new StreamObserver<ExportMetricsServiceRequest>() {
        @Override
        public void onNext(ExportMetricsServiceRequest value) {
          addExportRequest(value);
        }

        @Override
        public void onError(Throwable t) {
          logger.warning("Exception thrown for export stream: " + t);
        }

        @Override
        public void onCompleted() {}
      };

  @Override
  public synchronized StreamObserver<ExportMetricsServiceRequest> export(
      StreamObserver<ExportMetricsServiceResponse> responseObserver) {
    return exportRequestObserver;
  }

  private synchronized void addExportRequest(ExportMetricsServiceRequest request) {
    exportMetricsServiceRequests.add(request);
  }

  synchronized List<ExportMetricsServiceRequest> getExportMetricsServiceRequests() {
    return Collections.unmodifiableList(exportMetricsServiceRequests);
  }
}
