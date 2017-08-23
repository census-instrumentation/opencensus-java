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

package io.opencensus.contrib.agent.instrumentation;

import io.grpc.Context;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

/** Naive benchmarks for automatic context propagation added by {@link ThreadInstrumentation}.*/
public class ThreadInstrumentationBenchmark {

  private static final class MyRunnable implements Runnable {

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
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Fork
  public void none(Blackhole blackhole) throws InterruptedException {
    Thread t = new Thread(new MyRunnable(blackhole));
    t.start();
    t.join();
  }

  /**
   * This benchmark attempts to measure the performance with manual context propagation.
   *
   * @param blackhole a {@link Blackhole} object supplied by JMH
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Fork
  public void manual(Blackhole blackhole) throws InterruptedException {
    Thread t = new Thread((Context.current().wrap(new MyRunnable(blackhole))));
    t.start();
    t.join();
  }

  /**
   * This benchmark attempts to measure the performance with automatic context propagation.
   *
   * @param blackhole a {@link Blackhole} object supplied by JMH
   */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Fork(jvmArgsAppend = "-javaagent:contrib/agent/build/libs/agent.jar")
  public void automatic(Blackhole blackhole) throws InterruptedException {
    Thread t = new Thread(new MyRunnable(blackhole));
    t.start();
    t.join();
  }
}
