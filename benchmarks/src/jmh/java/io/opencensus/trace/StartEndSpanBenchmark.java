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

package io.opencensus.trace;

import io.opencensus.trace.samplers.Samplers;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link SpanFactoryImpl} and {@link SpanImpl}. */
@State(Scope.Benchmark)
public class StartEndSpanBenchmark {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String SPAN_NAME = "MySpanName";
  private Span rootSpan =
      tracer.spanBuilder(SPAN_NAME).becomeRoot().setSampler(Samplers.neverSample()).startSpan();

  @TearDown
  public void doTearDown() {
    rootSpan.end();
  }

  /**
   * This benchmark attempts to measure performance of start/end for a non-sampled root {@code
   * Span}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndNonSampledRootSpan() {
    Span span =
        tracer.spanBuilder(SPAN_NAME).becomeRoot().setSampler(Samplers.neverSample()).startSpan();
    span.end();
    return span;
  }

  /**
   * This benchmark attempts to measure performance of start/end for a root {@code Span} with record
   * events option.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndRecordEventsRootSpan() {
    Span span =
        tracer
            .spanBuilder(SPAN_NAME)
            .becomeRoot()
            .setSampler(Samplers.neverSample())
            .setRecordEvents(true)
            .startSpan();
    span.end();
    return span;
  }

  /**
   * This benchmark attempts to measure performance of start/end for a sampled root {@code Span}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndSampledRootSpan() {
    Span span = tracer.spanBuilder(SPAN_NAME).setSampler(Samplers.alwaysSample()).startSpan();
    span.end();
    return span;
  }

  /**
   * This benchmark attempts to measure performance of start/end for a non-sampled child {@code
   * Span}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndNonSampledChildSpan() {
    Span span =
        tracer.spanBuilder(rootSpan, SPAN_NAME).setSampler(Samplers.neverSample()).startSpan();
    span.end();
    return span;
  }

  /**
   * This benchmark attempts to measure performance of start/end for a child {@code Span} with
   * record events option.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndRecordEventsChildSpan() {
    Span span =
        tracer
            .spanBuilder(rootSpan, SPAN_NAME)
            .setSampler(Samplers.neverSample())
            .setRecordEvents(true)
            .startSpan();
    span.end();
    return span;
  }

  /**
   * This benchmark attempts to measure performance of start/end for a sampled child {@code Span}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span startEndSampledChildSpan() {
    Span span =
        tracer.spanBuilder(rootSpan, SPAN_NAME).setSampler(Samplers.alwaysSample()).startSpan();
    span.end();
    return span;
  }
}
