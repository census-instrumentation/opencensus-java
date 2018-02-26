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

package io.opencensus.benchmarks.trace;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.samplers.Samplers;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link Span} to record trace events. */
@State(Scope.Benchmark)
public class RecordTraceEventsBenchmark {
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";

  @State(Scope.Benchmark)
  public static class Data {

    private Span linkedSpan = BlankSpan.INSTANCE;
    private Span span = BlankSpan.INSTANCE;

    @Param({"impl", "impl-lite"})
    String implementation;

    @Param({"true", "false"})
    boolean sampled;

    @Setup
    public void setup() {
      Tracer tracer = BenchmarksUtil.getTracer(implementation);
      linkedSpan =
          tracer
              .spanBuilderWithExplicitParent(SPAN_NAME, null)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();
      span =
          tracer
              .spanBuilderWithExplicitParent(SPAN_NAME, null)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();
    }

    @TearDown
    public void doTearDown() {
      linkedSpan.end();
      span.end();
    }
  }

  /** This benchmark attempts to measure performance of adding an attribute to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span putAttribute(Data data) {
    data.span.putAttribute(ATTRIBUTE_KEY, AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE));
    return data.span;
  }

  /** This benchmark attempts to measure performance of adding an annotation to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotation(Data data) {
    data.span.addAnnotation(ANNOTATION_DESCRIPTION);
    return data.span;
  }

  /** This benchmark attempts to measure performance of adding a network event to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addMessageEvent(Data data) {
    data.span.addMessageEvent(
        io.opencensus.trace.MessageEvent.builder(Type.RECEIVED, 1)
            .setUncompressedMessageSize(3)
            .build());
    return data.span;
  }

  /** This benchmark attempts to measure performance of adding a link to the span. */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addLink(Data data) {
    data.span.addLink(
        Link.fromSpanContext(data.linkedSpan.getContext(), Link.Type.PARENT_LINKED_SPAN));
    return data.span;
  }
}
