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

package io.opencensus.examples.stats;

import io.opencensus.common.Clock;
import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.MeasurementMap;
import io.opencensus.stats.RpcMeasurementConstants;
import io.opencensus.stats.RpcViewConstants;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsContextFactory;
import io.opencensus.stats.StatsManager;
import io.opencensus.stats.TagValue;
import io.opencensus.testing.common.TestClock;
import io.opencensus.testing.stats.StatsTester;

/** Simple program that demonstrates the use of a test {@code StatsManager}. */
public final class StatsTesting {

  /**
   * This main method runs {@code MyProgram.run()} twice, once with a test {@code StatsManager}, and
   * again with the default {@code StatsManager}.
   */
  public static void main(String[] args) throws InterruptedException {
    System.out.println("Test run:");
    Clock testClock = TestClock.create(Timestamp.create(1L, 0));
    MyProgram test = new MyProgram(StatsTester.createTestStatsManager(testClock));
    test.run();

    System.out.println();
    System.out.println("Non-test run:");
    MyProgram nonTest = new MyProgram(Stats.getStatsManager());
    nonTest.run();

    // TODO(sebright): Prevent EventQueue from blocking shutdown and then remove this call to exit.
    System.exit(0);
  }

  /** A simple instrumented program. */
  public static final class MyProgram {
    private final StatsManager statsManager;
    private final StatsContextFactory ctxFactory;

    public MyProgram(StatsManager statsManager) {
      this.statsManager = statsManager;
      this.ctxFactory = statsManager.getStatsContextFactory();
    }

    /** Runs the example instrumented program. */
    public void run() throws InterruptedException {
      statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
      try (NonThrowingCloseable ctx =
          ctxFactory.withStatsContext(
              ctxFactory
                  .getDefault()
                  .with(RpcMeasurementConstants.RPC_CLIENT_METHOD, TagValue.create("my method")))) {
        ctxFactory
            .getCurrentStatsContext()
            .record(MeasurementMap.of(RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 0.5));
      }

      // TODO(sebright): Add a method to shut down or flush the EventQueue, and remove this call to
      // sleep().
      Thread.sleep(1);  // Wait for EventQueue to process the measurement.

      System.out.println(statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW));
    }
  }
}
