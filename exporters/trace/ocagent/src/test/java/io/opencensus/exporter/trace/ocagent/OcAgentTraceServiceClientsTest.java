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
import static org.mockito.Matchers.eq;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import io.opencensus.common.Timestamp;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceServiceClients.ExportServiceClient;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceServiceClients.ServiceName;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.CurrentLibraryConfig;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceResponse;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc.TraceServiceStub;
import io.opencensus.proto.agent.trace.v1.UpdatedLibraryConfig;
import io.opencensus.proto.trace.v1.Span;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.Attributes;
import io.opencensus.trace.export.SpanData.Links;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.util.Collections;
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

  private static final Node NODE_EMPTY = Node.getDefaultInstance();

  // No-op implementation of ClientCall.
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
  private static final ClientCall<?, ?> fakeErrorClientCall =
      new ClientCall<Object, Object>() {
        @Override
        public void start(Listener<Object> responseListener, Metadata headers) {
          throw new RuntimeException();
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

  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
  private static final String SPAN_ID = "24aa0b2d371f48c9";
  private static final String SPAN_NAME = "MySpanName";
  private static final SpanId spanId = SpanId.fromLowerBase16(SPAN_ID);
  private static final TraceId traceId = TraceId.fromLowerBase16(TRACE_ID);
  private static final TraceOptions traceOptions = TraceOptions.DEFAULT;
  private static final Tracestate tracestate = Tracestate.builder().build();
  private static final SpanContext spanContext =
      SpanContext.create(traceId, spanId, traceOptions, tracestate);
  private static final Timestamp startTimestamp = Timestamp.create(123, 456);
  private static final Timestamp endTimestamp = Timestamp.create(123, 460);
  private static final Attributes attributes =
      Attributes.create(Collections.<String, AttributeValue>emptyMap(), 0);
  private static final Links links = Links.create(Collections.<Link>emptyList(), 0);
  private static final TimedEvents<Annotation> annotations =
      TimedEvents.create(Collections.<TimedEvent<Annotation>>emptyList(), 0);
  private static final TimedEvents<io.opencensus.trace.MessageEvent> messageEvents =
      TimedEvents.create(Collections.<TimedEvent<MessageEvent>>emptyList(), 0);
  private static final SpanData SPAN_DATA =
      SpanData.create(
          spanContext,
          null,
          /* hasRemoteParent= */ false,
          SPAN_NAME,
          Kind.CLIENT,
          startTimestamp,
          attributes,
          annotations,
          messageEvents,
          links,
          null,
          null,
          endTimestamp);

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
  public void initiateExportConnection_Succeeded() {
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    ExportTraceServiceRequest request =
        ExportTraceServiceRequest.newBuilder().setNode(NODE_EMPTY).build();
    StreamObserver<ExportTraceServiceRequest> observer =
        OcAgentTraceServiceClients.initiateConnectionWithRetries(
            fakeStub, request, mockExportResponseObserver, ServiceName.EXPORT);
    assertThat(observer).isNotNull();
  }

  @Test
  public void initiateExportConnection_Failed_NoRetries() {
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    ExportTraceServiceRequest request =
        ExportTraceServiceRequest.newBuilder().setNode(NODE_EMPTY).build();
    StreamObserver<ExportTraceServiceRequest> observer =
        OcAgentTraceServiceClients.initiateConnectionWithRetries(
            fakeStub, request, mockExportResponseObserver, ServiceName.EXPORT);
    assertThat(observer).isNull();
  }

  @Test
  public void initiateConfigConnection_Succeeded() {
    Mockito.doReturn(fakeClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    CurrentLibraryConfig request = TraceProtoUtils.getCurrentLibraryConfig(NODE_EMPTY);
    StreamObserver<CurrentLibraryConfig> observer =
        OcAgentTraceServiceClients.initiateConnectionWithRetries(
            fakeStub, request, mockUpdatedConfigObserver, ServiceName.CONFIG);
    assertThat(observer).isNotNull();
  }

  @Test
  public void initiateConfigConnection_Failed_NoRetries() {
    Mockito.doReturn(fakeErrorClientCall)
        .when(mockChannel)
        .newCall((MethodDescriptor<?, ?>) any(), any(CallOptions.class));
    CurrentLibraryConfig request = TraceProtoUtils.getCurrentLibraryConfig(NODE_EMPTY);
    StreamObserver<CurrentLibraryConfig> observer =
        OcAgentTraceServiceClients.initiateConnectionWithRetries(
            fakeStub, request, mockUpdatedConfigObserver, ServiceName.CONFIG);
    assertThat(observer).isNull();
  }

  @Test
  public void onExport() {
    ExportServiceClient client = new ExportServiceClient(mockExportRequestObserver);
    client.onExport(Collections.singletonList(SPAN_DATA));
    Span expected = TraceProtoUtils.toSpanProto(SPAN_DATA);
    Mockito.verify(mockExportRequestObserver, Mockito.times(1))
        .onNext(eq(ExportTraceServiceRequest.newBuilder().addSpans(expected).build()));
  }
}
