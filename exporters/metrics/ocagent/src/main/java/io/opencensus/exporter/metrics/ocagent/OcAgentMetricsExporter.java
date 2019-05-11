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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.netty.handler.ssl.SslContext;
import io.opencensus.common.Duration;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.MetricProducerManager;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The implementation of the OpenCensus Agent (OC-Agent) Metrics Exporter.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   OcAgentMetricsExporter.createAndRegister(
 *     OcAgentMetricsExporterConfiguration.builder().build());
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.20
 */
@ThreadSafe
public final class OcAgentMetricsExporter {

  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static OcAgentMetricsExporter exporter = null;

  private final Thread workerThread;

  /**
   * Creates a {@code OcAgentMetricsExporterHandler} with the given configurations and registers it
   * to the OpenCensus library.
   *
   * @param configuration the {@code OcAgentMetricsExporterConfiguration}.
   * @since 0.20
   */
  public static void createAndRegister(OcAgentMetricsExporterConfiguration configuration) {
    checkNotNull(configuration, "configuration");
    createInternal(
        configuration.getEndPoint(),
        configuration.getUseInsecure(),
        configuration.getSslContext(),
        configuration.getServiceName(),
        configuration.getExportInterval(),
        configuration.getRetryInterval());
  }

  private static void createInternal(
      String endPoint,
      boolean useInsecure,
      @Nullable SslContext sslContext,
      String serviceName,
      Duration exportInterval,
      Duration retryInterval) {
    checkArgument(
        useInsecure == (sslContext == null), "Either use insecure or provide a valid SslContext.");
    synchronized (monitor) {
      checkState(exporter == null, "OcAgent Metrics exporter is already created.");
      exporter =
          new OcAgentMetricsExporter(
              endPoint,
              useInsecure,
              sslContext,
              serviceName,
              exportInterval,
              retryInterval,
              Metrics.getExportComponent().getMetricProducerManager());
      exporter.workerThread.start();
    }
  }

  private OcAgentMetricsExporter(
      String endPoint,
      Boolean useInsecure,
      @Nullable SslContext sslContext,
      String serviceName,
      Duration exportInterval,
      Duration retryInterval,
      MetricProducerManager metricProducerManager) {
    OcAgentMetricsExporterWorker worker =
        new OcAgentMetricsExporterWorker(
            endPoint,
            useInsecure,
            sslContext,
            exportInterval,
            retryInterval,
            serviceName,
            metricProducerManager);
    workerThread = new Thread(worker);
    workerThread.setDaemon(true);
    workerThread.setName("OcAgentMetricsExporterWorker");
  }
}
