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

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceServiceClients.AgentConnectionWorker;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link OcAgentTraceServiceClients}. */
@RunWith(JUnit4.class)
public class OcAgentTraceServiceClientsTest {

  @Mock private ManagedChannel mockChannel;
  @Mock private StreamObserver<ExportTraceServiceRequest> mockExportRequestObserver;
  @Mock private StreamObserver<ExportTraceServiceResponse> mockExportResponseObserver;
  @Mock private StreamObserver<CurrentLibraryConfig> mockCurrentConfigObserver;
  @Mock private StreamObserver<UpdatedLibraryConfig> mockUpdatedConfigObserver;

  // TraceServiceStub is a final class and cannot be mocked.
  private TraceServiceStub fakeStub;

  private static final long RETRY_INTERVAL_MILLIS = 10;

  // No-op implementation of ClientCall.
  // ClientCall has an annotation:
  // @DoNotMock("Use InProcessServerBuilder and make a test server instead")
  private static final ClientCall<?, ?> fakeClientCall =
      new ClientCall<Object, Object>() {
        @Override
        public void start(Listener<Object> responseListener, Metadata headers) {}

        @Override
        public void request(int numMessages) {}

        @Override
        public void cancel(@Nullable String message, @Nullable Throwable cause) {}

        @Override
        public void halfClose() {}

        @Override
        public void sendMessage(Object message) {}
      };

  // Implementation of ClientCall that throws an exception when starting.
  // ClientCall has an annotation:
  // @DoNotMock("Use InProcessServerBuilder and make a test server instead")
  private static final ClientCall<?, ?> fakeErrorClientCall =
      new ClientCall<Object, Object>() {
        @Override
        public void start(Listener<Object> responseListener, Metadata headers) {
          throw new StatusRuntimeException(io.grpc.Status.DEADLINE_EXCEEDED);
        }

        @Override
        public void request(int numMessages) {}

        @Override
        public void cancel(@Nullable String message, @Nullable Throwable cause) {}

        @Override
        public void halfClose() {}

        @Override
        public void sendMessage(Object message) {}
      };

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    fakeStub = TraceServiceGrpc.newStub(mockChannel);
    Mockito.doNothing()
        .when(mockExportResponseObserver)
        .onNext(any(ExportTraceServiceResponse.class));
    Mockito.doNothing().when(mockUpdatedConfigObserver).onNext(any(UpdatedLibraryConfig.class));
    Mockito.doNothing()
        .when(mockExportRequestObserver)
        .onNext(any(ExportTraceServiceRequest.class));
    Mockito.doNothing().when(mockCurrentConfigObserver).onNext(any(CurrentLibraryConfig.class));
  }

  @Test
  public void initiateExportConnection_Succeeded() throws InterruptedException {
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    AgentConnectionWorker.initiateExportStream(
        fakeStub, RETRY_INTERVAL_MILLIS, mockExportResponseObserver);
    assertThat(OcAgentTraceServiceClients.exportRequestObserverRef.get()).isNotNull();
  }

  @Test
  public void initiateExportConnection_Failed() throws InterruptedException {
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    AgentConnectionWorker.initiateExportStream(
        fakeStub, RETRY_INTERVAL_MILLIS, mockExportResponseObserver);
    assertThat(OcAgentTraceServiceClients.exportRequestObserverRef.get()).isNull();
  }

  @Test
  public void initiateConfigConnection_Succeeded() throws InterruptedException {
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    AgentConnectionWorker.initiateConfigStream(
        fakeStub, RETRY_INTERVAL_MILLIS, mockUpdatedConfigObserver);
    assertThat(OcAgentTraceServiceClients.currentConfigObserverRef.get()).isNotNull();
  }

  @Test
  public void initiateConfigConnection_Failed() throws InterruptedException {
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    AgentConnectionWorker.initiateConfigStream(
        fakeStub, RETRY_INTERVAL_MILLIS, mockUpdatedConfigObserver);
    assertThat(OcAgentTraceServiceClients.currentConfigObserverRef.get()).isNull();
  }

  // TODO(songya): Add a fake implementation of Trace Service Server and add E2E tests.
}
