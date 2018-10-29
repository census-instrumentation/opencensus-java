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

package io.opencensus.examples.stats;

import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * StackdriverExample is an example of exporting a custom metric from OpenCensus to Stackdriver.
 */
public class StackdriverExample {

  // The task latency in milliseconds.
  private static final MeasureDouble LATENCY_MS = MeasureDouble.create("task_latency",
    "The task latency in milliseconds", "ms");
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  public static void main(String ...args) throws IOException, InterruptedException {

    // Defining the distribution aggregations
    Aggregation latencyDistribution = Distribution.create(BucketBoundaries.create(
      Arrays.asList(
        // Latency in buckets: [>=0ms, >=100ms, >=200ms, >=400ms, >=1s, >=2s, >=4s]
        0.0, 100.0, 200.0, 400.0, 1000.0, 2000.0, 4000.0)
    ));

    View view = View.create(Name.create("task_latency_distribution"),
      "The distribution of the task latencies", LATENCY_MS, latencyDistribution,
      Collections.<TagKey>emptyList());

    // Create the view manager
    ViewManager vmgr = Stats.getViewManager();

    // Then finally register the views
    vmgr.registerView(view);

    // Enable OpenCensus exporters to export metrics to Stackdriver Monitoring.
    // Exporters use Application Default Credentials to authenticate.
    // See https://developers.google.com/identity/protocols/application-default-credentials
    // for more details.
    // The minimum reporting period for Stackdriver is 1 minute.
    StackdriverStatsExporter.createAndRegister();

    // Record 100 fake latency values between 0 and 5 seconds.
    for (int i = 0; i < 100; i++) {
      double ms = Math.random() * 5 * 1000;
      System.out.println(String.format("Latency %d: %f", i, ms));
      statsRecorder.newMeasureMap().put(LATENCY_MS, ms).record();
      Thread.sleep(1000);
    }

    System.out.println("Done recording metrics");
  }
}
