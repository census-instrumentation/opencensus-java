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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.opencensus.common.Duration;
import io.opencensus.exporter.trace.util.TimeLimitedHandler;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.trace.v1.ExportTraceServiceRequest;
import io.opencensus.proto.agent.trace.v1.TraceServiceGrpc;
import io.opencensus.trace.export.SpanData;
import java.util.Collection;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Exporting handler for OC-Agent Tracing. */
final class OcAgentTraceExporterHandler extends TimeLimitedHandler {

  private static final Logger logger =
      Logger.getLogger(OcAgentTraceExporterHandler.class.getName());
  private static final String EXPORT_SPAN_NAME = "ExportOpenCensusProtoSpans";

  private final String endPoint;
  private final Node node;
  private final Boolean useInsecure;
  @Nullable private final SslContext sslContext;

  @javax.annotation.Nullable
  private OcAgentTraceServiceExportRpcHandler exportRpcHandler; // Thread-safe

  OcAgentTraceExporterHandler(
      String endPoint,
      String serviceName,
      boolean useInsecure,
      @Nullable SslContext sslContext,
      Duration retryInterval,
      boolean enableConfig,
      Duration deadline) {
    super(deadline, EXPORT_SPAN_NAME);
    this.endPoint = endPoint;
    this.node = OcAgentNodeUtils.getNodeInfo(serviceName);
    this.useInsecure = useInsecure;
    this.sslContext = sslContext;
  }

  @Override
  public void timeLimitedExport(Collection<SpanData> spanDataList) {
    if (exportRpcHandler == null || exportRpcHandler.isCompleted()) {
      // If not connected, try to initiate a new connection when a new batch of spans arrive.
      // Export RPC doesn't respect the retry interval.
      TraceServiceGrpc.TraceServiceStub stub =
          getTraceServiceStub(endPoint, useInsecure, sslContext);
      exportRpcHandler = createExportRpcHandlerAndConnect(stub, node);
    }

    if (exportRpcHandler == null || exportRpcHandler.isCompleted()) { // Failed to connect to Agent.
      logger.info("Export RPC disconnected, dropping " + spanDataList.size() + " spans.");
      exportRpcHandler = null;
    } else { // Connection succeeded, send export request.
      ExportTraceServiceRequest.Builder requestBuilder = ExportTraceServiceRequest.newBuilder();
      for (SpanData spanData : spanDataList) {
        requestBuilder.addSpans(TraceProtoUtils.toSpanProto(spanData));
      }
      exportRpcHandler.onExport(requestBuilder.build());
    }
  }

  @Nullable
  private static OcAgentTraceServiceExportRpcHandler createExportRpcHandlerAndConnect(
      TraceServiceGrpc.TraceServiceStub stub, Node node) {
    @Nullable OcAgentTraceServiceExportRpcHandler exportRpcHandler = null;
    try {
      exportRpcHandler = OcAgentTraceServiceExportRpcHandler.create(stub);
      // First message must have Node set.
      ExportTraceServiceRequest firstExportReq =
          ExportTraceServiceRequest.newBuilder().setNode(node).build();
      exportRpcHandler.onExport(firstExportReq);
    } catch (RuntimeException e) {
      if (exportRpcHandler != null) {
        exportRpcHandler.onComplete(e);
      }
    }
    return exportRpcHandler;
  }

  // Creates a TraceServiceStub with the given parameters.
  // One stub can be used for both Export RPC and Config RPC.
  private static TraceServiceGrpc.TraceServiceStub getTraceServiceStub(
      String endPoint, Boolean useInsecure, SslContext sslContext) {
    ManagedChannelBuilder<?> channelBuilder;
    if (useInsecure) {
      channelBuilder = ManagedChannelBuilder.forTarget(endPoint).usePlaintext();
    } else {
      channelBuilder =
          NettyChannelBuilder.forTarget(endPoint)
              .negotiationType(NegotiationType.TLS)
              .sslContext(sslContext);
    }
    ManagedChannel channel = channelBuilder.build();
    return TraceServiceGrpc.newStub(channel);
  }
}
