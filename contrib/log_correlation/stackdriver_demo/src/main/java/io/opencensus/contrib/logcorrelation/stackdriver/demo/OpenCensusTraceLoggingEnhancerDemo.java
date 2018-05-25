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

package io.opencensus.contrib.logcorrelation.stackdriver.demo;

import io.opencensus.common.Scope;
import io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Demo for {@link OpenCensusTraceLoggingEnhancer}. */
public final class OpenCensusTraceLoggingEnhancerDemo {

  // Use a high sampling probability to demonstrate tracing.
  private static final Sampler SAMPLER = Samplers.probabilitySampler(0.7);

  private static final Logger logger =
      LoggerFactory.getLogger(OpenCensusTraceLoggingEnhancerDemo.class.getName());

  private static final Tracer tracer = Tracing.getTracer();

  private OpenCensusTraceLoggingEnhancerDemo() {}

  /** Runs the demo. */
  public static void main(String[] args) throws IOException {
    StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
    try (Scope scope = tracer.spanBuilder("Demo.Main").setSampler(SAMPLER).startScopedSpan()) {
      pause();
      logger.warn("parent span log message 1");
      pause();
      doWork();
      pause();
      logger.info("parent span log message 2");
      pause();
    }
    Tracing.getExportComponent().shutdown();
  }

  private static void doWork() {
    try (Scope scope = tracer.spanBuilder("Demo.DoWork").startScopedSpan()) {
      pause();
      logger.info("child span log message 1");
      pause();
      logger.error("child span log message 2");
      pause();
    }
  }

  /** Sleeps for 500 ms to spread out the events on the trace. */
  private static void pause() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }
  }
}
