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

import static com.google.common.truth.Truth.assertThat;

import io.grpc.stub.StreamObserver;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.ConstantSampler;
import io.opencensus.proto.trace.v1.TraceConfig;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FakeOcAgentTraceServiceGrpcImpl}. */
@RunWith(JUnit4.class)
public class FakeOcAgentTraceServiceGrpcImplTest {

  private final List<UpdatedLibraryConfig> updatedLibraryConfigs = new ArrayList<>();

  private final StreamObserver<UpdatedLibraryConfig> updatedConfigObserver =
      new StreamObserver<UpdatedLibraryConfig>() {

        @Override
        public void onNext(UpdatedLibraryConfig value) {
          updatedLibraryConfigs.add(value);
        }

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onCompleted() {}
      };

  private final StreamObserver<ExportTraceServiceResponse> exportResponseObserver =
      new StreamObserver<ExportTraceServiceResponse>() {
        @Override
        public void onNext(ExportTraceServiceResponse value) {}

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onCompleted() {}
      };

  private static final UpdatedLibraryConfig neverSampledLibraryConfig =
      UpdatedLibraryConfig.newBuilder()
          .setConfig(
              TraceConfig.newBuilder()
                  .setConstantSampler(ConstantSampler.newBuilder().setDecision(false).build())
                  .build())
          .build();

  @Test
  public void export() {
    FakeOcAgentTraceServiceGrpcImpl traceServiceGrpc = new FakeOcAgentTraceServiceGrpcImpl();
    StreamObserver<ExportTraceServiceRequest> exportRequestObserver =
        traceServiceGrpc.export(exportResponseObserver);
    ExportTraceServiceRequest request = ExportTraceServiceRequest.getDefaultInstance();
    exportRequestObserver.onNext(request);
    assertThat(traceServiceGrpc.getExportTraceServiceRequests()).containsExactly(request);
  }

  @Test
  public void config() {
    FakeOcAgentTraceServiceGrpcImpl traceServiceGrpc = new FakeOcAgentTraceServiceGrpcImpl();
    StreamObserver<CurrentLibraryConfig> currentConfigObsever =
        traceServiceGrpc.config(updatedConfigObserver);
    CurrentLibraryConfig currentLibraryConfig = CurrentLibraryConfig.getDefaultInstance();
    currentConfigObsever.onNext(currentLibraryConfig);
    assertThat(traceServiceGrpc.getCurrentLibraryConfigs()).containsExactly(currentLibraryConfig);
    assertThat(updatedLibraryConfigs).containsExactly(traceServiceGrpc.getUpdatedLibraryConfig());
    updatedLibraryConfigs.clear();
  }

  @Test
  public void config_WithNeverSampler() {
    FakeOcAgentTraceServiceGrpcImpl traceServiceGrpc = new FakeOcAgentTraceServiceGrpcImpl();
    traceServiceGrpc.setUpdatedLibraryConfig(neverSampledLibraryConfig);
    StreamObserver<CurrentLibraryConfig> currentConfigObsever =
        traceServiceGrpc.config(updatedConfigObserver);
    CurrentLibraryConfig currentLibraryConfig = CurrentLibraryConfig.getDefaultInstance();
    currentConfigObsever.onNext(currentLibraryConfig);
    assertThat(traceServiceGrpc.getCurrentLibraryConfigs()).containsExactly(currentLibraryConfig);
    assertThat(updatedLibraryConfigs).containsExactly(neverSampledLibraryConfig);
    updatedLibraryConfigs.clear();
  }
}
