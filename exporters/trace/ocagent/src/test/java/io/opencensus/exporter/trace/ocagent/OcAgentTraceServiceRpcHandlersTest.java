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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link OcAgentTraceServiceConfigRpcHandler} and {@link
 * OcAgentTraceServiceExportRpcHandler}.
 */
@RunWith(JUnit4.class)
public class OcAgentTraceServiceRpcHandlersTest {

  @Mock private TraceConfig mockTraceConfig;

  private Server server;
  private FakeOcAgentTraceServiceGrpcImpl traceServiceGrpc;
  private String serverName;

  private static final Node NODE =
      Node.newBuilder()
          .setLibraryInfo(LibraryInfo.newBuilder().setLanguage(Language.JAVA).build())
          .build();
  private static final io.opencensus.proto.trace.v1.TraceConfig TRACE_CONFIG_DEFAULT_PROTO =
      TraceProtoUtils.toTraceConfigProto(TraceParams.DEFAULT);

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    Mockito.doReturn(TraceParams.DEFAULT).when(mockTraceConfig).getActiveTraceParams();
    Mockito.doNothing().when(mockTraceConfig).updateActiveTraceParams(any(TraceParams.class));

    traceServiceGrpc = new FakeOcAgentTraceServiceGrpcImpl();
    serverName = InProcessServerBuilder.generateName();
    server =
        InProcessServerBuilder.forName(serverName)
            .directExecutor() // directExecutor is fine for unit tests
            .addService(traceServiceGrpc)
            .build()
            .start();
  }

  @After
  public void tearDown() {
    if (server != null && !server.isTerminated()) {
      server.shutdown();
    }
  }

  @Test
  public void config_CreateAndSend() throws InterruptedException {
    // Config RPC handler needs to be running in another thread.
    Runnable configRunnable =
        new Runnable() {
          @Override
          public void run() {
            TraceServiceStub stub = getStub(serverName);
            OcAgentTraceServiceConfigRpcHandler configRpcHandler =
                OcAgentTraceServiceConfigRpcHandler.create(stub, mockTraceConfig);
            assertThat(configRpcHandler.isCompleted()).isFalse(); // connection should succeed
            configRpcHandler.sendInitialMessage(NODE); // this will block this thread
          }
        };

    Thread configThread = new Thread(configRunnable);
    configThread.setDaemon(true);
    configThread.setName("TestConfigRpcHandlerThread");
    configThread.start();

    CountDownLatch countDownLatch = new CountDownLatch(1);
    traceServiceGrpc.setCountDownLatch(countDownLatch);
    // Wait until fake agent received the first message.
    countDownLatch.await();
    traceServiceGrpc.closeConfigStream();

    // Verify fake Agent (server) received the expected CurrentLibraryConfig.
    CurrentLibraryConfig expectedCurrentConfig =
        CurrentLibraryConfig.newBuilder()
            .setNode(NODE)
            .setConfig(TRACE_CONFIG_DEFAULT_PROTO)
            .build();
    // Fake Agent may receive more that one CurrentLibraryConfigs since
    // ConfigRpcHandler is not rate-limited.
    assertThat(traceServiceGrpc.getCurrentLibraryConfigs()).contains(expectedCurrentConfig);

    // Verify ConfigRpcHandler (client) received the expected UpdatedLibraryConfig.
    TraceParams expectedParams =
        TraceProtoUtils.getUpdatedTraceParams(
            traceServiceGrpc.getUpdatedLibraryConfig(), mockTraceConfig);
    verify(mockTraceConfig, times(1)).updateActiveTraceParams(expectedParams);
  }

  @Test
  public void config_Create_ConnectionFailed() {
    String nonExistingServer = "unknown";
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(getStub(nonExistingServer), mockTraceConfig);
    verify(mockTraceConfig, times(0)).getActiveTraceParams();
    assertThat(configRpcHandler.isCompleted()).isTrue();
    assertThat(configRpcHandler.getTerminateStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
  }

  @Test
  public void config_Complete_Interrupted() {
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(getStub(serverName), mockTraceConfig);
    assertThat(configRpcHandler.isCompleted()).isFalse();
    configRpcHandler.onComplete(new InterruptedException());
    assertThat(configRpcHandler.isCompleted()).isTrue();
    assertThat(configRpcHandler.getTerminateStatus()).isEqualTo(Status.UNKNOWN);
  }

  @Test
  public void export_createAndExport() {
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(getStub(serverName));
    ExportTraceServiceRequest request = ExportTraceServiceRequest.newBuilder().build();
    exportRpcHandler.onExport(request);
    assertThat(traceServiceGrpc.getExportTraceServiceRequests()).containsExactly(request);
  }

  @Test
  public void export_Create_ConnectionFailed() {
    String nonExistingServer = "unknown";
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(getStub(nonExistingServer));
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getTerminateStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
  }

  @Test
  public void export_Complete_Interrupted() {
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(getStub(serverName));
    assertThat(exportRpcHandler.isCompleted()).isFalse();
    exportRpcHandler.onComplete(new InterruptedException());
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getTerminateStatus()).isEqualTo(Status.UNKNOWN);
  }

  private static TraceServiceStub getStub(String serverName) {
    ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    return TraceServiceGrpc.newStub(channel);
  }
}
