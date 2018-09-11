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

package io.opencensus.implcore.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link BinaryFormat}. */
@State(Scope.Benchmark)
public class BinaryFormatImplBenchmark {
  @State(Scope.Thread)
  public static class Data {
    private BinaryFormat binaryFormat;
    private SpanContext spanContext;
    private byte[] spanContextBinary;

    @Setup
    public void setup() {
      binaryFormat = new BinaryFormatImpl();
      Random random = new Random(1234);
      spanContext =
          SpanContext.create(
              TraceId.generateRandomId(random),
              SpanId.generateRandomId(random),
              TraceOptions.builder().setIsSampled(random.nextBoolean()).build(),
              Tracestate.builder().build());
      spanContextBinary = binaryFormat.toByteArray(spanContext);
    }
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormat#toBinaryValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] toBinarySpanContext(Data data) {
    return data.binaryFormat.toByteArray(data.spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link BinaryFormat#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromBinarySpanContext(Data data) throws SpanContextParseException {
    return data.binaryFormat.fromByteArray(data.spanContextBinary);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormat#toBinaryValue(SpanContext)} then {@link BinaryFormat#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromBinarySpanContext(Data data) throws SpanContextParseException {
    return data.binaryFormat.fromByteArray(data.binaryFormat.toByteArray(data.spanContext));
  }
}
