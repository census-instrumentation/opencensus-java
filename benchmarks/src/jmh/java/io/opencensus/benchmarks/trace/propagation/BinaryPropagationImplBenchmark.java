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

package io.opencensus.benchmarks.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.BinaryFormat;
import io.opencensus.trace.propagation.SpanContextParseException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link BinaryFormat}. */
@State(Scope.Benchmark)
public class BinaryPropagationImplBenchmark {
  private static final byte[] traceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId traceId = TraceId.fromBytes(traceIdBytes);
  private static final byte[] spanIdBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0};
  private static final SpanId spanId = SpanId.fromBytes(spanIdBytes);
  private static final byte[] traceOptionsBytes = new byte[] {1};
  private static final TraceOptions traceOptions = TraceOptions.fromBytes(traceOptionsBytes);
  private static final SpanContext spanContext = SpanContext.create(traceId, spanId, traceOptions);
  private static final BinaryFormat binaryFormat =
      Tracing.getPropagationComponent().getBinaryFormat();
  private static final byte[] spanContextBinary = binaryFormat.toByteArray(spanContext);

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormat#toBinaryValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] toBinarySpanContext() {
    return binaryFormat.toByteArray(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormat#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromBinarySpanContext() throws SpanContextParseException {
    return binaryFormat.fromByteArray(spanContextBinary);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormat#toBinaryValue(SpanContext)} then {@link
   * BinaryFormat#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromBinarySpanContext() throws SpanContextParseException {
    return binaryFormat.fromByteArray(binaryFormat.toByteArray(spanContext));
  }
}
