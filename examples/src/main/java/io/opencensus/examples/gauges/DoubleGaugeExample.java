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

package io.opencensus.examples.gauges;

import io.opencensus.metrics.DoubleGauge;
import io.opencensus.metrics.DoubleGauge.DoublePoint;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.Metrics;
import java.util.Collections;
import java.util.List;

/**
 * Example showing how to create a {@link DoubleGauge} and manually set or add value of the gauge.
 */
public class DoubleGaugeExample {
  private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();

  // The label keys and values are used to uniquely identify timeseries.
  private static final List<LabelKey> labelKeys =
      Collections.singletonList(LabelKey.create("Name", "desc"));
  private static final List<LabelValue> labelValues =
      Collections.singletonList(LabelValue.create("Inbound"));

  private static final DoubleGauge doubleGauge =
      metricRegistry.addDoubleGauge("queue_size", "Pending jobs", "1", labelKeys);
  // It is recommended to keep a reference of a point for manual operations.
  private static final DoublePoint pendingJobs = doubleGauge.getOrCreateTimeSeries(labelValues);

  // Tracks the number of pending jobs in the queue.
  private static void doWork() {
    addJob();
    // Your code here.
    removeJob();
  }

  private static void addJob() {
    pendingJobs.add(1);
    // Your code here.
  }

  private static void removeJob() {
    // Your code here.
    pendingJobs.add(-1);
  }

  /** Main launcher for the DoubleGaugeExample. */
  public static void main(String[] args) {
    // Double Gauge metric is used to report instantaneous measurement of a double value. This is
    // more convenient form when you want to manually increase and decrease values as per your
    // service requirements.

    doWork();
  }
}
