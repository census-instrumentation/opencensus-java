/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.examples.trace;

import static io.opencensus.examples.trace.Utils.sleep;

import io.opencensus.exporter.trace.logging.LoggingTraceExporter;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;

/** Example showing how to directly create a child {@link Span} and add annotations. */
public final class MultiSpansTracing {
  // Per class Tracer.
  private static final Tracer tracer = Tracing.getTracer();

  private MultiSpansTracing() {}

  private static void doWork() {
    Span rootSpan = tracer.spanBuilderWithExplicitParent("MyRootSpan", null).startSpan();
    rootSpan.addAnnotation("Annotation to the root Span before child is created.");
    Span childSpan = tracer.spanBuilderWithExplicitParent("MyChildSpan", rootSpan).startSpan();
    childSpan.addAnnotation("Annotation to the child Span");
    childSpan.end();
    rootSpan.addAnnotation("Annotation to the root Span after child is ended.");
    rootSpan.end();
  }

  /**
   * Main method.
   *
   * @param args the main arguments.
   */
  public static void main(String[] args) {

    // WARNING: Be careful before you set sampler value to always sample, especially in production environment.
    // Trace data is often very large in size and is expensive to collect. This is why rather than collecting traces
    // for every request(i.e. alwaysSample), downsampling is prefered.
    // By default, OpenCensus provides a probabilistic sampler that will trace once in every 10,000 requests.
    // If you prefer to use probabilistic sampler, you might not see trace data printed or exported and this is
    // expected behavior.

    TraceConfig traceConfig = Tracing.getTraceConfig();
    traceConfig.updateActiveTraceParams(
            traceConfig
                    .getActiveTraceParams()
                    .toBuilder()
                    .setSampler(Samplers.alwaysSample()).build());

    LoggingTraceExporter.register();
    doWork();

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    // Spans are exported every 5 seconds
    Utils.sleep(5100);
  }
}
