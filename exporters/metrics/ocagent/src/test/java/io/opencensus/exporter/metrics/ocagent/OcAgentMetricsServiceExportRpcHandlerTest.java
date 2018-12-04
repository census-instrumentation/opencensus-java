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

import static com.google.common.truth.Truth.assertThat;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.metrics.v1.ExportMetricsServiceRequest;
import io.opencensus.proto.agent.metrics.v1.MetricsServiceGrpc;
import io.opencensus.proto.agent.metrics.v1.MetricsServiceGrpc.MetricsServiceStub;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcAgentMetricsServiceExportRpcHandler}. */
@RunWith(JUnit4.class)
public class OcAgentMetricsServiceExportRpcHandlerTest {

  private final FakeOcAgentMetricsServiceGrpcImpl traceServiceGrpc =
      new FakeOcAgentMetricsServiceGrpcImpl();
  private final String serverName = InProcessServerBuilder.generateName();
  private final Server server =
      InProcessServerBuilder.forName(serverName)
          .directExecutor() // directExecutor is fine for unit tests
          .addService(traceServiceGrpc)
          .build();

  private static final Node NODE =
      Node.newBuilder()
          .setLibraryInfo(LibraryInfo.newBuilder().setLanguage(Language.JAVA).build())
          .build();

  @Before
  public void setUp() throws IOException {
    server.start();
  }

  @After
  public void tearDown() {
    if (!server.isTerminated()) {
      server.shutdown();
    }
  }

  @Test
  public void export_createAndExport() {
    OcAgentMetricsServiceExportRpcHandler exportRpcHandler =
        OcAgentMetricsServiceExportRpcHandler.create(getStub(serverName));
    ExportMetricsServiceRequest request =
        ExportMetricsServiceRequest.newBuilder().setNode(NODE).build();
    exportRpcHandler.onExport(request);
    assertThat(traceServiceGrpc.getExportMetricsServiceRequests()).containsExactly(request);
  }

  @Test
  public void export_Create_ConnectionFailed() {
    String nonExistingServer = "unknown";
    OcAgentMetricsServiceExportRpcHandler exportRpcHandler =
        OcAgentMetricsServiceExportRpcHandler.create(getStub(nonExistingServer));
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getTerminateStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
  }

  @Test
  public void export_Complete_Interrupted() {
    OcAgentMetricsServiceExportRpcHandler exportRpcHandler =
        OcAgentMetricsServiceExportRpcHandler.create(getStub(serverName));
    assertThat(exportRpcHandler.isCompleted()).isFalse();
    exportRpcHandler.onComplete(new InterruptedException());
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getTerminateStatus()).isEqualTo(Status.UNKNOWN);
  }

  private static MetricsServiceStub getStub(String serverName) {
    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    return MetricsServiceGrpc.newStub(channel);
  }
}
