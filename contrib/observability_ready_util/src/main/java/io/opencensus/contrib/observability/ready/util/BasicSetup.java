/*
 * Copyright 2020, OpenCensus Authors
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

package io.opencensus.contrib.observability.ready.util;

import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.exporter.metrics.ocagent.OcAgentMetricsExporter;
import io.opencensus.exporter.metrics.ocagent.OcAgentMetricsExporterConfiguration;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporter;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporterConfiguration;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;

/**
 * Setup class to enable OpenCensus stats and metrics collections easily.
 *
 * @since 0.25
 */
public class BasicSetup {
  private static final int DEAFULT_SAMPLING_RATE = 1 / 10000;
  private static final String DEAFULT_ENDPOINT = "localhost:55678";

  private BasicSetup() {}

  /**
   * Enables OpenCensus metric and traces.
   *
   * <p>This will register all basic {@link io.opencensus.stats.View}s. When coupled with an agent,
   * it allows users to monitor client behavior.
   *
   * <p>Please note that in addition to calling this method, the application must:
   *
   * <ul>
   *   <li>Include opencensus-contrib-observability-ready-util dependency on the classpath
   *   <li>Configure the OpenCensus agent
   * </ul>
   *
   * <p>Example usage for maven:
   *
   * <pre>{@code
   * <dependency>
   *   <groupId>io.opencensus</groupId>
   *   <artifactId>opencensus-contrib-observability-ready-util</artifactId>
   *   <version>${opencensus.version}</version>
   * </dependency>
   * }</pre>
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing stats.
   *
   * <pre>{@code
   * BasicSetup.enableOpenCensus("with-service-name");
   * }</pre>
   *
   * @since 0.25
   */
  public static void enableOpenCensus(String serviceName, String endpoint, double probability) {
    // register basic rpc views
    RpcViews.registerAllGrpcBasicViews();

    // set sampling rate
    TraceConfig traceConfig = Tracing.getTraceConfig();
    TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
    traceConfig.updateActiveTraceParams(
        activeTraceParams.toBuilder().setSampler(Samplers.probabilitySampler(probability)).build());

    // create and register Trace Agent Exporter
    OcAgentTraceExporter.createAndRegister(
        OcAgentTraceExporterConfiguration.builder()
            .setEndPoint(endpoint)
            .setServiceName(serviceName)
            .setUseInsecure(true)
            .setEnableConfig(false)
            .build());

    // create and register Trace Metrics Exporter
    OcAgentMetricsExporter.createAndRegister(
        OcAgentMetricsExporterConfiguration.builder()
            .setEndPoint(endpoint)
            .setServiceName(serviceName)
            .setUseInsecure(true)
            .build());
  }

  public static void enableOpenCensus(String serviceName) {
    enableOpenCensus(serviceName, DEAFULT_ENDPOINT, DEAFULT_SAMPLING_RATE);
  }
}
