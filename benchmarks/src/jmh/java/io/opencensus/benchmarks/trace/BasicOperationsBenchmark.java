/*
 * Copyright 2019, OpenCensus Authors
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
public class BasicOperationsBenchmark {
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";

  @State(Scope.Benchmark)
  public static class Data {
    private Span span;
    private Tracer tracer;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"true", "false"})
    boolean recorded;

    @Param({"true", "false"})
    boolean sampled;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      span =
          tracer
              .spanBuilderWithExplicitParent("TopLevelSpan", null)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();
    }

    @TearDown
    public void doTearDown() {
      span.end();
    }
  }

  /** Create a root span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createRootSpan(Data data) {
    Span span =
        data.tracer
            .spanBuilderWithExplicitParent("RootSpan", null)
            .setRecordEvents(data.recorded)
            .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
            .startSpan();
    span.end();
    return span;
  }

  /** Create a child span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithExplicitParent(Data data) {
    Span span =
        data.tracer
            .spanBuilderWithExplicitParent("ChildSpan", data.span)
            .setRecordEvents(data.recorded)
            .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
            .startSpan();
    span.end();
    return span;
  }

  /** Create a child span from the current span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithCurrentSpan(Data data) {
    Span span =
        data.tracer
            .spanBuilder("ChildSpanFromCurrent")
            .setRecordEvents(data.recorded)
            .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
            .startSpan();
    span.end();
    return span;
  }

  /** Create a child span with a remote parent. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithRemoteParent(Data data) {
    Span span =
        data.tracer
            .spanBuilderWithRemoteParent("ChildSpanFromRemoteParent", data.span.getContext())
            .setRecordEvents(data.recorded)
            .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
            .startSpan();
    span.end();
    return span;
  }
}
