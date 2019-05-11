/*
 * Copyright 2019, OpenCensus Authors
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

import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.opencensus.common.Scope;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** End-to-end integration test for {@link OcAgentTraceExporter}. */
@RunWith(JUnit4.class)
public class OcAgentTraceExporterIntegrationTest {

  private Server agent;
  private FakeOcAgentTraceServiceGrpcImpl fakeOcAgentTraceServiceGrpc;
  private final Tracer tracer = Tracing.getTracer();

  private static final String SERVICE_NAME = "integration-test";

  @Before
  public void setUp() throws IOException {
    fakeOcAgentTraceServiceGrpc = new FakeOcAgentTraceServiceGrpcImpl();
    agent =
        getServer(OcAgentTraceExporterConfiguration.DEFAULT_END_POINT, fakeOcAgentTraceServiceGrpc);
  }

  @After
  public void tearDown() {
    OcAgentTraceExporter.unregister();
    agent.shutdown();
    Tracing.getTraceConfig().updateActiveTraceParams(TraceParams.DEFAULT);
  }

  @Test
  public void testExportSpans() throws InterruptedException, IOException {
    // Mock a real-life scenario in production, where Agent is not enabled at first, then enabled
    // after an outage. Users should be able to see traces shortly after Agent is up.

    // Configure to be always-sampled.
    TraceConfig traceConfig = Tracing.getTraceConfig();
    TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
    traceConfig.updateActiveTraceParams(
        activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

    // Register the OcAgent Exporter first.
    // Agent is not yet up and running so Exporter will just retry connection.
    OcAgentTraceExporter.createAndRegister(
        OcAgentTraceExporterConfiguration.builder()
            .setServiceName(SERVICE_NAME)
            .setUseInsecure(true)
            .setEnableConfig(false)
            .build());

    // Create one root span and 5 children.
    try (Scope scope = tracer.spanBuilder("root").startScopedSpan()) {
      for (int i = 0; i < 5; i++) {
        // Fake work
        doWork("first-iteration-child-" + i, i);
      }
    }

    // Wait 5s so that SpanExporter exports exports all spans.
    Thread.sleep(5000);

    // No interaction with Agent so far.
    assertThat(fakeOcAgentTraceServiceGrpc.getExportTraceServiceRequests()).isEmpty();

    // Image an outage happened, now start Agent. Exporter should be able to connect to Agent
    // when the next batch of SpanData arrives.
    agent.start();

    // Create one root span and 8 children.
    try (Scope scope = tracer.spanBuilder("root2").startScopedSpan()) {
      for (int i = 0; i < 8; i++) {
        // Fake work
        doWork("second-iteration-child-" + i, i);
      }
    }

    // Wait 5s so that SpanExporter exports exports all spans.
    Thread.sleep(5000);

    List<ExportTraceServiceRequest> exportRequests =
        fakeOcAgentTraceServiceGrpc.getExportTraceServiceRequests();
    assertThat(exportRequests.size()).isAtLeast(2);

    ExportTraceServiceRequest firstRequest = exportRequests.get(0);
    Node expectedNode = OcAgentNodeUtils.getNodeInfo(SERVICE_NAME);
    Node actualNode = firstRequest.getNode();
    assertThat(actualNode.getIdentifier().getHostName())
        .isEqualTo(expectedNode.getIdentifier().getHostName());
    assertThat(actualNode.getIdentifier().getPid())
        .isEqualTo(expectedNode.getIdentifier().getPid());
    assertThat(actualNode.getLibraryInfo()).isEqualTo(expectedNode.getLibraryInfo());
    assertThat(actualNode.getServiceInfo()).isEqualTo(expectedNode.getServiceInfo());

    List<io.opencensus.proto.trace.v1.Span> spanProtos = new ArrayList<>();
    for (int i = 1; i < exportRequests.size(); i++) {
      spanProtos.addAll(exportRequests.get(i).getSpansList());
    }

    // On some platforms (e.g Windows) SpanData will never be dropped, so spans from the first batch
    // may also be exported after Agent is up.
    assertThat(spanProtos.size()).isAtLeast(9);

    Set<String> exportedSpanNames = new HashSet<>();
    for (io.opencensus.proto.trace.v1.Span spanProto : spanProtos) {
      if ("root2".equals(spanProto.getName().getValue())) {
        assertThat(spanProto.getChildSpanCount().getValue()).isEqualTo(8);
        assertThat(spanProto.getParentSpanId()).isEqualTo(ByteString.EMPTY);
      } else if ("root".equals(spanProto.getName().getValue())) {
        // This won't happen on Linux but does happen on Windows.
        assertThat(spanProto.getChildSpanCount().getValue()).isEqualTo(5);
        assertThat(spanProto.getParentSpanId()).isEqualTo(ByteString.EMPTY);
      }
      exportedSpanNames.add(spanProto.getName().getValue());
    }

    // The second batch of spans should be exported no matter what.
    assertThat(exportedSpanNames).contains("root2");
    for (int i = 0; i < 8; i++) {
      assertThat(exportedSpanNames).contains("second-iteration-child-" + i);
    }
  }

  @Test
  public void testConfig() {
    //    OcAgentTraceExporter.createAndRegister(
    //        OcAgentTraceExporterConfiguration.builder()
    //            .setServiceName(SERVICE_NAME)
    //            .setUseInsecure(true)
    //            .setEnableConfig(false)
    //            .build());

    // TODO(songya): complete this test once Config is fully implemented.
  }

  private void doWork(String spanName, int i) {
    try (Scope scope = tracer.spanBuilder(spanName).startScopedSpan()) {
      // Simulate some work.
      Span span = tracer.getCurrentSpan();

      try {
        Thread.sleep(10L);
      } catch (InterruptedException e) {
        span.setStatus(Status.INTERNAL.withDescription(e.toString()));
      }

      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("inner work iteration number", AttributeValue.longAttributeValue(i));
      span.addAnnotation("Invoking doWork", attributes);
    }
  }

  private static Server getServer(String endPoint, BindableService service) throws IOException {
    ServerBuilder<?> builder = NettyServerBuilder.forAddress(parseEndpoint(endPoint));
    Executor executor = MoreExecutors.directExecutor();
    builder.executor(executor);
    return builder.addService(service).build();
  }

  private static InetSocketAddress parseEndpoint(String endPoint) {
    try {
      int colonIndex = endPoint.indexOf(":");
      String host = endPoint.substring(0, colonIndex);
      int port = Integer.parseInt(endPoint.substring(colonIndex + 1));
      return new InetSocketAddress(host, port);
    } catch (RuntimeException e) {
      return new InetSocketAddress("localhost", 55678);
    }
  }
}
