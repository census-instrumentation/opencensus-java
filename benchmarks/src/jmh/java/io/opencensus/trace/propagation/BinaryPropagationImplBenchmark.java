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

package io.opencensus.trace.propagation;

import io.opencensus.impl.trace.propagation.BinaryFormatImpl;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link BinaryFormatImpl}. */
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
  private static final BinaryFormat BINARY_PROPAGATION = new BinaryFormatImpl();
  private static final byte[] spanContextBinary = BINARY_PROPAGATION.toBinaryValue(spanContext);

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormatImpl#toBinaryValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] toBinaryValueSpanContext() {
    return BINARY_PROPAGATION.toBinaryValue(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormatImpl#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromBinaryValueSpanContext() throws ParseException {
    return BINARY_PROPAGATION.fromBinaryValue(spanContextBinary);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * BinaryFormatImpl#toBinaryValue(SpanContext)} then {@link
   * BinaryFormatImpl#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromBinarySpanContext() throws ParseException {
    return BINARY_PROPAGATION.fromBinaryValue(BINARY_PROPAGATION.toBinaryValue(spanContext));
  }
}
