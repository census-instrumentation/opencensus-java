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
import io.opencensus.trace.Status;
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
public class OperationsBenchmark {
  private static final String SPAN_NAME = "SpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";
  private static final long NETWORK_MESSAGE_ID = 1042;
  private static final Status STATUS_OK = Status.OK;

  @State(Scope.Benchmark)
  public static class Data {
    private Tracer tracer;
    private Span setSpan;
    private Span endSpan;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"true", "false"})
    boolean recorded;

    @Param({"true", "false"})
    boolean sampled;

    @Param({"0", "1", "4", "8", "16"})
    // @Param({"0", "1", "16"})
    int size;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      setSpan = createSpan("SetSpan");
      endSpan = createSpan("EndSpan");
    }

    @TearDown
    public void doTearDown() {
      setSpan.end();
    }

    private Span createSpan(String suffix) {
      return tracer
          .spanBuilderWithExplicitParent(SPAN_NAME + suffix, null)
          .setRecordEvents(recorded)
          .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
          .startSpan();
    }
  }

  /** Set status on a span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span setStatusOnSpans(Data data) {
    data.setSpan.setStatus(STATUS_OK);
    return data.setSpan;
  }

  /** End a span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span endSpan(Data data) {
    data.endSpan.end();
    return data.endSpan;
  }
}
