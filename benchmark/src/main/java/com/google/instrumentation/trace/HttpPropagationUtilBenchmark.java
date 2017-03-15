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

import com.google.common.io.BaseEncoding;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link HttpPropagationUtil}. */
@State(Scope.Benchmark)
public class HttpPropagationUtilBenchmark {
  private static final byte[] traceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private byte[] spanContextBytes;
  private String spanContextString;
  private static final TraceId traceId = TraceId.fromBytes(traceIdBytes);
  private static final byte[] spanIdBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0};
  private static final SpanId spanId = SpanId.fromBytes(spanIdBytes);
  private static final byte[] traceOptionsBytes = new byte[] {0, 0, 0, 1};
  private static final TraceOptions traceOptions = TraceOptions.fromBytes(traceOptionsBytes);
  private SpanContext spanContext;
  private String spanContextStringHttp;

  @Setup
  public void setUp() throws Exception {
    spanContext = new SpanContext(traceId, spanId, traceOptions);
    spanContextStringHttp = HttpPropagationUtil.toHttpHeaderValue(spanContext);
    spanContextBytes = new byte[16 + 8 + 4];
    traceId.copyBytesTo(spanContextBytes, 0);
    spanId.copyBytesTo(spanContextBytes, 16);
    traceOptions.copyBytesTo(spanContextBytes, 24);
    spanContextString = BaseEncoding.base16().lowerCase().encode(spanContextBytes);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * HttpPropagationUtil#toHttpHeaderValue(SpanContext)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String toHttpHeaderValueSpanContext() {
    return HttpPropagationUtil.toHttpHeaderValue(spanContext);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * HttpPropagationUtil#fromHttpHeaderValue(CharSequence)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext fromHttpHeaderValueSpanContext() {
    return HttpPropagationUtil.fromHttpHeaderValue(spanContextStringHttp);
  }

  /**
   * This benchmark attempts to measure performance of {@link
   * HttpPropagationUtil#toHttpHeaderValue(SpanContext)} then {@link
   * HttpPropagationUtil#fromHttpHeaderValue(CharSequence)}.
   */
  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public SpanContext toFromHttpFormatSpanContext() {
    return HttpPropagationUtil.fromHttpHeaderValue(
        HttpPropagationUtil.toHttpHeaderValue(spanContext));
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String base16EncodeSpanContext() {
    return BaseEncoding.base16().lowerCase().encode(spanContextBytes);
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] base16DecodeSpanContext() {
    return BaseEncoding.base16().lowerCase().decode(spanContextString);
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] base16EncodeDecodeSpanContext() {
    return BaseEncoding.base16()
        .lowerCase()
        .decode(BaseEncoding.base16().lowerCase().encode(spanContextBytes));
  }
}
