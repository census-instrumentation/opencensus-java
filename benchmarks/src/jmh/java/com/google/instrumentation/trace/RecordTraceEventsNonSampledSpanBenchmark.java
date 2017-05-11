/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link SpanImpl} to record trace events. */
@State(Scope.Benchmark)
public class RecordTraceEventsNonSampledSpanBenchmark {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";
  private Span linkedSpan =
      tracer.spanBuilder(SPAN_NAME).becomeRoot().setSampler(Samplers.alwaysSample()).startSpan();
  private Span span =
      tracer.spanBuilder(SPAN_NAME).becomeRoot().setSampler(Samplers.alwaysSample()).startSpan();

  /** TearDown method. */
  @TearDown
  public void doTearDown() {
    span.end();
    linkedSpan.end();
  }

  /** This benchmark attempts to measure performance of {@link Span#addAttributes(Map)}. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAttributes() {
    HashMap<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(ATTRIBUTE_KEY, AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE));
    span.addAttributes(attributes);
    return span;
  }

  /** This benchmark attempts to measure performance of {@link Span#addAnnotation(String)}. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotation() {
    span.addAnnotation(ANNOTATION_DESCRIPTION);
    return span;
  }

  /**
   * This benchmark attempts to measure performance of {@link Span#addNetworkEvent(NetworkEvent)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addNetworkEvent() {
    span.addNetworkEvent(NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build());
    return span;
  }

  /**
   * This benchmark attempts to measure performance of {@link Span#addNetworkEvent(NetworkEvent)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addLink() {
    span.addLink(Link.fromSpanContext(linkedSpan.getContext(), Link.Type.PARENT));
    return span;
  }
}
