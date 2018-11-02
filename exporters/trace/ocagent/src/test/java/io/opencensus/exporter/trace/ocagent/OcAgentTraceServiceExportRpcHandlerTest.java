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
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OcAgentTraceServiceExportRpcHandler}. */
@RunWith(JUnit4.class)
public class OcAgentTraceServiceExportRpcHandlerTest {

  @Mock private Channel mockChannel;

  private TraceServiceStub stub;

  private static final MethodDescriptor<ExportTraceServiceRequest, ExportTraceServiceResponse>
      METHOD_DESCRIPTOR = TraceServiceGrpc.getExportMethod();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    stub = TraceServiceGrpc.newStub(mockChannel);
  }

  @Test
  public void createAndExport() {
    FakeClientCall fakeClientCall = new FakeClientCall();
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(stub);
    ExportTraceServiceRequest request = ExportTraceServiceRequest.newBuilder().build();
    exportRpcHandler.onExport(request);
    verify(mockChannel, times(1)).newCall(METHOD_DESCRIPTOR, stub.getCallOptions());
    assertThat(fakeClientCall.getMessages()).containsExactly(request);
  }

  @Test
  public void create_ConnectionFailed() {
    FakeErrorStartClientCall fakeErrorClientCall =
        new FakeErrorStartClientCall(new StatusRuntimeException(Status.PERMISSION_DENIED));
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(stub);
    verify(mockChannel, times(1)).newCall(METHOD_DESCRIPTOR, stub.getCallOptions());
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getEndStatus()).isEqualTo(Status.PERMISSION_DENIED);
  }

  @Test
  public void export_ConnectionFailed() {
    FakeErrorSendClientCall fakeErrorClientCall =
        new FakeErrorSendClientCall(new StatusRuntimeException(Status.UNAVAILABLE));
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(stub);
    assertThat(exportRpcHandler.isCompleted()).isFalse();
    ExportTraceServiceRequest request = ExportTraceServiceRequest.newBuilder().build();
    exportRpcHandler.onExport(request);
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getEndStatus()).isEqualTo(Status.UNAVAILABLE);
  }

  @Test
  public void complete() {
    FakeClientCall fakeClientCall = new FakeClientCall();
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    OcAgentTraceServiceExportRpcHandler exportRpcHandler =
        OcAgentTraceServiceExportRpcHandler.create(stub);
    assertThat(exportRpcHandler.isCompleted()).isFalse();
    exportRpcHandler.onComplete(new InterruptedException());
    assertThat(exportRpcHandler.isCompleted()).isTrue();
    assertThat(exportRpcHandler.getEndStatus()).isEqualTo(Status.UNKNOWN);
  }
}
