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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import io.opencensus.trace.samplers.Samplers;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The implementation of the OpenCensus Agent (OC-Agent) Trace Exporter.
 *
 * <p>Example of usage:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   OcAgentTraceExporter.createAndRegister();
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.20
 */
@ThreadSafe
public final class OcAgentTraceExporter {

  private static final Object monitor = new Object();
  private static final String REGISTER_NAME = OcAgentTraceExporter.class.getName();
  private static final double DEAFULT_SAMPLING_RATE = 0.0001;

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  private OcAgentTraceExporter() {}

  /**
   * Creates a {@code OcAgentTraceExporterHandler} with default configurations and registers it to
   * the OpenCensus library.
   *
   * @since 0.20
   */
  public static void createAndRegister() {
    createAndRegister(OcAgentTraceExporterConfiguration.builder().build());
  }

  /**
   * Creates a {@code OcAgentTraceExporterHandler} with the given configurations and registers it to
   * the OpenCensus library.
   *
   * @param configuration the {@code OcAgentTraceExporterConfiguration}.
   * @since 0.20
   */
  public static void createAndRegister(OcAgentTraceExporterConfiguration configuration) {
    synchronized (monitor) {
      checkState(handler == null, "OC-Agent exporter is already registered.");
      OcAgentTraceExporterHandler newHandler =
          new OcAgentTraceExporterHandler(
              configuration.getEndPoint(),
              configuration.getServiceName(),
              configuration.getUseInsecure(),
              configuration.getSslContext(),
              configuration.getRetryInterval(),
              configuration.getEnableConfig(),
              configuration.getDeadline());
      registerInternal(newHandler);
    }
  }

  /**
   * Enables OpenCensus traces.
   *
   * <p>This will set sampling rate and register Ocagent Trace Exporter. When coupled with an agent,
   * it allows users to monitor application behavior.
   *
   * <p>Example usage for maven:
   *
   * <pre>{@code
   * <dependency>
   *   <groupId>io.opencensus</groupId>
   *   <artifactId>opencensus-exporter-trace-ocagent</artifactId>
   *   <version>${opencensus.version}</version>
   * </dependency>
   * }</pre>
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing traces.
   *
   * <pre>{@code
   * OcAgentTraceExporter.basicSetup(OcAgentTraceExporterConfiguration.builder().build(), 1);
   * }</pre>
   *
   * @param configuration the {@code OcAgentTraceExporterConfiguration}.
   * @param probability the desired probability of sampling. Must be within [0.0, 1.0].
   * @since 0.25
   */
  public static void basicSetup(
      OcAgentTraceExporterConfiguration configuration, double probability) {
    // set sampling rate
    TraceConfig traceConfig = Tracing.getTraceConfig();
    TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
    traceConfig.updateActiveTraceParams(
        activeTraceParams.toBuilder().setSampler(Samplers.probabilitySampler(probability)).build());

    // create and register Trace Agent Exporter
    createAndRegister(configuration);
  }

  /**
   * Enables OpenCensus traces with default sampling rate {@code DEAFULT_SAMPLING_RATE}.
   *
   * <p>This will set sampling rate and register Ocagent Trace Exporter. When coupled with an agent,
   * it allows users to monitor application behavior.
   *
   * <p>Example usage for maven:
   *
   * <pre>{@code
   * <dependency>
   *   <groupId>io.opencensus</groupId>
   *   <artifactId>opencensus-exporter-trace-ocagent</artifactId>
   *   <version>${opencensus.version}</version>
   * </dependency>
   * }</pre>
   *
   * <p>It is recommended to call this method before doing any RPC call to avoid missing traces.
   *
   * <pre>{@code
   * OcAgentTraceExporter.basicSetup(OcAgentTraceExporterConfiguration.builder().build());
   * }</pre>
   *
   * @param configuration the {@code OcAgentTraceExporterConfiguration}.
   * @since 0.25
   */
  public static void basicSetup(OcAgentTraceExporterConfiguration configuration) {
    basicSetup(configuration, DEAFULT_SAMPLING_RATE);
  }

  /**
   * Registers the {@code OcAgentTraceExporterHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  private static void registerInternal(Handler newHandler) {
    synchronized (monitor) {
      handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  /**
   * Unregisters the OC-Agent exporter from the OpenCensus library.
   *
   * @since 0.20
   */
  public static void unregister() {
    synchronized (monitor) {
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@code OcAgentTraceExporterHandler}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }
}
