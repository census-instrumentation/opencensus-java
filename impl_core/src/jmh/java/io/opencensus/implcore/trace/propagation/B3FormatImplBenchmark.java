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

package io.opencensus.implcore.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link io.opencensus.implcore.trace.propagation.B3Format}. */
@State(Scope.Benchmark)
public class B3FormatImplBenchmark {
  @State(Scope.Thread)
  public static class Data {
    private TextFormatBenchmarkBase textFormatBase;
    private SpanContext spanContext;
    private Map<String, String> spanContextHeaders;

    @Setup
    public void setup() {
      textFormatBase = new TextFormatBenchmarkBase(new B3Format());
      Random random = new Random(1234);
      spanContext =
          SpanContext.create(
              TraceId.generateRandomId(random),
              SpanId.generateRandomId(random),
              TraceOptions.builder().setIsSampled(random.nextBoolean()).build(),
              Tracestate.builder().build());
      spanContextHeaders = new HashMap<String, String>();
      textFormatBase.inject(spanContext, spanContextHeaders);
    }
  }

  /**
   * This benchmark attempts to measure performance of {@link TextFormat#inject(SpanContext, Object,
   * Setter)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Map<String, String> inject(Data data) {
    Map<String, String> carrier = new HashMap<String, String>();
    data.textFormatBase.inject(data.spanContext, carrier);
    return carrier;
  }

  /**
   * This benchmark attempts to measure performance of {@link TextFormat#extract(Object, Getter)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext extract(Data data) throws SpanContextParseException {
    return data.textFormatBase.extract(data.spanContextHeaders);
  }

  /**
   * This benchmark attempts to measure performance of {@link TextFormat#inject(SpanContext, Object,
   * Setter)} then {@link TextFormat#extract(Object, Getter)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext injectExtract(Data data) throws SpanContextParseException {
    Map<String, String> carrier = new HashMap<String, String>();
    data.textFormatBase.inject(data.spanContext, carrier);
    return data.textFormatBase.extract(carrier);
  }
}
