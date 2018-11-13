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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.opencensus.exporter.trace.ocagent.FakeClientCalls.FakeClientCall;
import io.opencensus.exporter.trace.ocagent.FakeClientCalls.FakeErrorSendClientCall;
import io.opencensus.exporter.trace.ocagent.FakeClientCalls.FakeErrorStartClientCall;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OcAgentTraceServiceConfigRpcHandler}. */
@RunWith(JUnit4.class)
public class OcAgentTraceServiceConfigRpcHandlerTest {

  @Mock private Channel mockChannel;
  @Mock private TraceConfig mockTraceConfig;

  private TraceServiceStub stub;

  private static final MethodDescriptor<CurrentLibraryConfig, UpdatedLibraryConfig>
      METHOD_DESCRIPTOR = TraceServiceGrpc.getConfigMethod();
  private static final Node NODE =
      Node.newBuilder()
          .setLibraryInfo(LibraryInfo.newBuilder().setLanguage(Language.JAVA).build())
          .build();
  private static final io.opencensus.proto.trace.v1.TraceConfig TRACE_CONFIG_DEFAULT_PROTO =
      TraceProtoUtils.toTraceConfigProto(TraceParams.DEFAULT);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    stub = TraceServiceGrpc.newStub(mockChannel);
    Mockito.doReturn(TraceParams.DEFAULT).when(mockTraceConfig).getActiveTraceParams();
  }

  @Test
  public void createAndSend() {
    FakeClientCall fakeClientCall = new FakeClientCall();
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(stub, mockTraceConfig);
    configRpcHandler.sendInitialMessage(NODE);
    verify(mockChannel, times(1)).newCall(METHOD_DESCRIPTOR, stub.getCallOptions());
    verify(mockTraceConfig, times(1)).getActiveTraceParams();
    CurrentLibraryConfig expected =
        CurrentLibraryConfig.newBuilder()
            .setNode(NODE)
            .setConfig(TRACE_CONFIG_DEFAULT_PROTO)
            .build();
    assertThat(fakeClientCall.getMessages()).containsExactly(expected);
  }

  @Test
  public void create_ConnectionFailed() {
    FakeErrorStartClientCall fakeErrorClientCall =
        new FakeErrorStartClientCall(new StatusRuntimeException(Status.PERMISSION_DENIED));
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(stub, mockTraceConfig);
    verify(mockChannel, times(1)).newCall(METHOD_DESCRIPTOR, stub.getCallOptions());
    verify(mockTraceConfig, times(0)).getActiveTraceParams();
    assertThat(configRpcHandler.isCompleted()).isTrue();
    assertThat(configRpcHandler.getTerminateStatus()).isEqualTo(Status.PERMISSION_DENIED);
  }

  @Test
  public void send_ConnectionFailed() {
    FakeErrorSendClientCall fakeErrorClientCall =
        new FakeErrorSendClientCall(new StatusRuntimeException(Status.UNAVAILABLE));
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(stub, mockTraceConfig);
    assertThat(configRpcHandler.isCompleted()).isFalse();
    Node node =
        Node.newBuilder()
            .setLibraryInfo(LibraryInfo.newBuilder().setLanguage(Language.JAVA).build())
            .build();
    configRpcHandler.sendInitialMessage(node);
    assertThat(configRpcHandler.isCompleted()).isTrue();
    assertThat(configRpcHandler.getTerminateStatus()).isEqualTo(Status.UNAVAILABLE);
  }

  @Test
  public void complete() {
    FakeClientCall fakeClientCall = new FakeClientCall();
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceConfigRpcHandler configRpcHandler =
        OcAgentTraceServiceConfigRpcHandler.create(stub, mockTraceConfig);
    assertThat(configRpcHandler.isCompleted()).isFalse();
    configRpcHandler.onComplete(new InterruptedException());
    assertThat(configRpcHandler.isCompleted()).isTrue();
    assertThat(configRpcHandler.getTerminateStatus()).isEqualTo(Status.UNKNOWN);
  }
}
