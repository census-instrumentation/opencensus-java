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

import com.google.common.collect.Lists;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opencensus.common.Duration;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.proto.agent.metrics.v1.ExportMetricsServiceRequest;
import io.opencensus.proto.agent.metrics.v1.MetricsServiceGrpc;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Worker {@code Runnable} that polls Metric from Metrics library and batch export to Agent.
 *
 * <p>{@code OcAgentMetricsExporterWorker} will be started in a daemon {@code Thread}.
 *
 * <p>The state of this class should only be accessed from the thread which {@link
 * OcAgentMetricsExporterWorker} resides in.
 */
@NotThreadSafe
final class OcAgentMetricsExporterWorker implements Runnable {

  private static final Logger logger =
      Logger.getLogger(OcAgentMetricsExporterWorker.class.getName());

  private final String endPoint;
  private final boolean useInsecure;
  private final long exportIntervalMillis;
  private final long retryIntervalMillis;
  private final String serviceName;
  private final MetricProducerManager metricProducerManager;
  private OcAgentMetricsServiceExportRpcHandler exportRpcHandler;
  // private final Set<MetricDescriptor> registeredDescriptors = new HashSet<>();

  OcAgentMetricsExporterWorker(
      String endPoint,
      boolean useInsecure,
      Duration exportInterval,
      Duration retryInterval,
      String serviceName,
      MetricProducerManager metricProducerManager) {
    this.endPoint = endPoint;
    this.useInsecure = useInsecure;
    this.exportIntervalMillis = exportInterval.toMillis();
    this.retryIntervalMillis = retryInterval.toMillis();
    this.serviceName = serviceName;
    this.metricProducerManager = metricProducerManager;
  }

  @Override
  public void run() {
    while (true) {
      connect();
      while (exportRpcHandler != null && !exportRpcHandler.isCompleted()) {
        export();
        sleep(exportIntervalMillis);
      }
      if (exportRpcHandler != null && exportRpcHandler.getTerminateStatus() != null) {
        TerminateStatusRunnable runnable =
            new TerminateStatusRunnable(exportRpcHandler.getTerminateStatus(), "Export");
        new Thread(runnable).start();
      }
      sleep(retryIntervalMillis);
    }
  }

  private void connect() {
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(endPoint);
    if (useInsecure) {
      channelBuilder.usePlaintext();
    }
    ManagedChannel channel = channelBuilder.build();
    MetricsServiceGrpc.MetricsServiceStub stub = MetricsServiceGrpc.newStub(channel);
    exportRpcHandler = OcAgentMetricsServiceExportRpcHandler.create(stub);
    ExportMetricsServiceRequest firstRequest =
        ExportMetricsServiceRequest.newBuilder()
            .setNode(OcAgentNodeUtils.getNodeInfo(serviceName))
            .setResource(OcAgentNodeUtils.getAutoDetectedResourceProto())
            .build();
    exportRpcHandler.onExport(firstRequest);
  }

  // Polls MetricProducerManager from Metrics library for all registered MetricDescriptors,
  // converts them to proto, then exports them to OC-Agent.
  private void export() {
    if (exportRpcHandler == null || exportRpcHandler.isCompleted()) {
      return;
    }

    ArrayList<Metric> metricsList = Lists.newArrayList();
    for (MetricProducer metricProducer : metricProducerManager.getAllMetricProducer()) {
      metricsList.addAll(metricProducer.getMetrics());
    }

    List<io.opencensus.proto.metrics.v1.Metric> metricProtos = Lists.newArrayList();
    for (Metric metric : metricsList) {
      // TODO(songya): determine if we should make the optimization on not sending already-existed
      // MetricDescriptors.
      // boolean registered = true;
      // if (!registeredDescriptors.contains(metric.getMetricDescriptor())) {
      //   registered = false;
      //   registeredDescriptors.add(metric.getMetricDescriptor());
      // }
      metricProtos.add(MetricsProtoUtils.toMetricProto(metric, null));
    }

    exportRpcHandler.onExport(
        ExportMetricsServiceRequest.newBuilder()
            // TODO(songya): resource proto may not be necessary for following requests.
            .setResource(OcAgentNodeUtils.getAutoDetectedResourceProto())
            .addAllMetrics(metricProtos)
            .build());
  }

  private static void sleep(long timeInMillis) {
    try {
      Thread.sleep(timeInMillis);
    } catch (InterruptedException e) {
      logger.log(Level.INFO, "OcAgentMetricsExporterWorker is interrupted.", e);
      Thread.currentThread().interrupt();
    }
  }

  private static final class TerminateStatusRunnable implements Runnable {
    private final io.grpc.Status status;
    private final String rpcName;

    TerminateStatusRunnable(io.grpc.Status status, String rpcName) {
      this.status = status;
      this.rpcName = rpcName;
    }

    @Override
    public void run() {
      logger.log(Level.INFO, "RPC " + rpcName + " terminated with Status ", status);
    }
  }
}
