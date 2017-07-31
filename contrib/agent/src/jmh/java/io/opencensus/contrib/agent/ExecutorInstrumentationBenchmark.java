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

package io.opencensus.contrib.agent;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Context;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

/** Benchmarks for automatic context propagation added by {@link ExecutorInstrumentation}. */
public class ExecutorInstrumentationBenchmark {

  private static class MyRunnable implements Runnable {

    private final Blackhole blackhole;

    private MyRunnable(Blackhole blackhole) {
      this.blackhole = blackhole;
    }

    @Override
    public void run() {
      blackhole.consume(Context.current());
    }
  }

  /**
   * This benchmark attempts to measure the performance without any context propagation.
   *
   * @param blackhole a {@link Blackhole} object supplied by JMH
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork
  public void none(final Blackhole blackhole) {
    MoreExecutors.directExecutor().execute(new MyRunnable(blackhole));
  }

  /**
   * This benchmark attempts to measure the performance with manual context propagation.
   *
   * @param blackhole a {@link Blackhole} object supplied by JMH
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork
  public void manual(final Blackhole blackhole) {
    MoreExecutors.directExecutor().execute(Context.current().wrap(new MyRunnable(blackhole)));
  }

  /**
   * This benchmark attempts to measure the performance with automatic context propagation.
   *
   * @param blackhole a {@link Blackhole} object supplied by JMH
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(jvmArgsAppend = "-javaagent:contrib/agent/build/libs/agent.jar")
  public void automatic(final Blackhole blackhole) {
    MoreExecutors.directExecutor().execute(new MyRunnable(blackhole));
  }
}
