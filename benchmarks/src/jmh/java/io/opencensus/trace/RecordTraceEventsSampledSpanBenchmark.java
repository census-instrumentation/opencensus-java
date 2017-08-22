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

package io.opencensus.trace;

import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.trace.samplers.Samplers;
import java.util.HashMap;
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
public class RecordTraceEventsSampledSpanBenchmark {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";
  private final Span linkedSpan =
      tracer
          .spanBuilderWithExplicitParent(SPAN_NAME, null)
          .setSampler(Samplers.alwaysSample())
          .startSpan();
  private final Span span =
      tracer
          .spanBuilderWithExplicitParent(SPAN_NAME, null)
          .setSampler(Samplers.alwaysSample())
          .startSpan();

  /** TearDown method. */
  @TearDown
  public void doTearDown() {
    span.end();
    linkedSpan.end();
  }

  /** This benchmark attempts to measure performance of adding an attribute to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAttributes() {
    HashMap<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(ATTRIBUTE_KEY, AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE));
    span.addAttributes(attributes);
    return span;
  }

  /** This benchmark attempts to measure performance of adding an annotation to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotation() {
    span.addAnnotation(ANNOTATION_DESCRIPTION);
    return span;
  }

  /** This benchmark attempts to measure performance of adding a network event to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addNetworkEvent() {
    span.addNetworkEvent(NetworkEvent.builder(NetworkEvent.Type.RECV, 1).setMessageSize(3).build());
    return span;
  }

  /** This benchmark attempts to measure performance of adding a link to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addLink() {
    span.addLink(Link.fromSpanContext(linkedSpan.getContext(), Link.Type.PARENT_LINKED_SPAN));
    return span;
  }
}
