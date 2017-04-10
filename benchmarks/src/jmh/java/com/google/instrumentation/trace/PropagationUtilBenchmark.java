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

package com.google.instrumentation.trace;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link PropagationUtil}. */
@State(Scope.Benchmark)
public class PropagationUtilBenchmark {
  private static final byte[] traceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId traceId = TraceId.fromBytes(traceIdBytes);
  private static final byte[] spanIdBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0};
  private static final SpanId spanId = SpanId.fromBytes(spanIdBytes);
  private static final byte[] traceOptionsBytes = new byte[] {1};
  private static final TraceOptions traceOptions = TraceOptions.fromBytes(traceOptionsBytes);
  private SpanContext spanContext;
  private String spanContextStringHttp;
  private byte[] spanContextBinary;


  /**
   * Setup function for benchmarks.
   */
  @Setup
  public void setUp() {
    spanContext = new SpanContext(traceId, spanId, traceOptions);
    spanContextStringHttp = PropagationUtil.toHttpHeaderValue(spanContext);
    spanContextBinary = PropagationUtil.toBinaryValue(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#toHttpHeaderValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String toHttpHeaderValueSpanContext() {
    return PropagationUtil.toHttpHeaderValue(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#fromHttpHeaderValue(CharSequence)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromHttpHeaderValueSpanContext() {
    return PropagationUtil.fromHttpHeaderValue(spanContextStringHttp);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#toHttpHeaderValue(SpanContext)} then {@link
   * PropagationUtil#fromHttpHeaderValue(CharSequence)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromHttpFormatSpanContext() {
    return PropagationUtil.fromHttpHeaderValue(
        PropagationUtil.toHttpHeaderValue(spanContext));
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#toBinaryValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] toBinaryValueSpanContext() {
    return PropagationUtil.toBinaryValue(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromBinaryValueSpanContext() {
    return PropagationUtil.fromBinaryValue(spanContextBinary);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * PropagationUtil#toBinaryValue(SpanContext)} then
   * {@link PropagationUtil#fromBinaryValue(byte[])}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromBinarySpanContext() {
    return PropagationUtil.fromBinaryValue(
        PropagationUtil.toBinaryValue(spanContext));
  }
}
