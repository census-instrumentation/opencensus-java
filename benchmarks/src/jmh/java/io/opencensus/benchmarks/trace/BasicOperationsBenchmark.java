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

import io.opencensus.common.Scope;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.PropagationComponent;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import io.opencensus.trace.samplers.Samplers;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for basic trace operations. */
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class BasicOperationsBenchmark {
  private static final String TRACEPARENT_KEY = "traceparent";
  private static final Status STATUS_OK = Status.OK;

  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    private Span span;
    private byte[] spanToDecodeBinary;
    private String spanToDecodeText;
    private Span spanToEncode;
    private Span spanToScope;
    private Span spanToSet;
    private Span spanToEnd;

    private Tracer tracer;
    private PropagationComponent propagation;

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
      propagation = BenchmarksUtil.getPropagationComponent(implementation);
      span =
          tracer
              .spanBuilderWithExplicitParent("TopLevelSpan", null)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();

      spanToEncode =
          tracer
              .spanBuilderWithExplicitParent("SpanToEncode", span)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();

      spanToScope =
          tracer
              .spanBuilderWithExplicitParent("SpanToScope", span)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();

      spanToSet =
          tracer
              .spanBuilderWithExplicitParent("SpanToSet", span)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();

      spanToEnd =
          tracer
              .spanBuilderWithExplicitParent("SpanToEnd", span)
              .setRecordEvents(recorded)
              .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
              .startSpan();

      spanToDecodeBinary = propagation.getBinaryFormat().toByteArray(spanToEncode.getContext());

      spanToDecodeText =
          encodeSpanContextText(propagation.getTraceContextFormat(), spanToEncode.getContext());
    }

    @TearDown
    public void doTearDown() {
      span.end();
      spanToEncode.end();
      spanToScope.end();
      spanToSet.end();
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

  /** Scope/Unscope a trace span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Scope scopeSpan(Data data) {
    try (Scope scope = data.tracer.withSpan(data.spanToScope)) {
      return scope;
    }
  }

  /** Get current trace span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Scope getCurrentSpan(Data data) {
    try (Scope scope = data.tracer.withSpan(data.spanToScope)) {
      return scope;
    }
  }

  /** Encode a span using binary format. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] encodeSpanBinary(Data data) {
    return data.propagation.getBinaryFormat().toByteArray(data.spanToEncode.getContext());
  }

  /** Decode a span using binary format. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext decodeSpanBinary(Data data) throws SpanContextParseException {
    return data.propagation.getBinaryFormat().fromByteArray(data.spanToDecodeBinary);
  }

  /** Encode a span using text format. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String encodeSpanText(Data data) {
    return encodeSpanContextText(
        data.propagation.getTraceContextFormat(), data.spanToEncode.getContext());
  }

  /** Decode a span using text format. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext decodeSpanText(Data data) throws SpanContextParseException {
    return data.propagation.getTraceContextFormat().extract(data.spanToDecodeText, textGetter);
  }

  /** Set status on a span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span setStatus(Data data) {
    data.spanToSet.setStatus(STATUS_OK);
    return data.spanToSet;
  }

  /** End a span. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span endSpan(Data data) {
    data.spanToEnd.end();
    return data.spanToEnd;
  }

  private static String encodeSpanContextText(TextFormat format, SpanContext context) {
    StringBuilder builder = new StringBuilder();
    format.inject(context, builder, textSetter);
    return builder.toString();
  }

  private static final Setter<StringBuilder> textSetter =
      new Setter<StringBuilder>() {
        @Override
        public void put(StringBuilder carrier, String key, String value) {
          if (key.equals(TRACEPARENT_KEY)) {
            carrier.append(value);
          }
        }
      };

  private static final Getter<String> textGetter =
      new Getter<String>() {
        @Override
        public String get(String carrier, String key) {
          return key.equals(TRACEPARENT_KEY) ? carrier : null;
        }
      };
}
