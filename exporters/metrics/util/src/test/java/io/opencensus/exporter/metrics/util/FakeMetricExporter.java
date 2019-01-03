/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.metrics.util;

import io.opencensus.metrics.export.Metric;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

class FakeMetricExporter extends MetricExporter {

  private final Object monitor = new Object();

  // TODO: Decide whether to use a different class instead of LinkedList.
  @GuardedBy("monitor")
  private List<List<Metric>> exportedMetrics = new ArrayList<>();

  @Override
  public void export(Collection<Metric> metricList) {
    synchronized (monitor) {
      this.exportedMetrics.add(new ArrayList<>(metricList));
      monitor.notifyAll();
    }
  }

  /**
   * Waits until export is called for numberOfExports times. Returns the list of exported lists of
   * metrics
   */
  @Nullable
  List<List<Metric>> waitForNumberOfExports(int numberOfExports) {
    List<List<Metric>> ret;
    synchronized (monitor) {
      while (exportedMetrics.size() < numberOfExports) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          // Preserve the interruption status as per guidance.
          Thread.currentThread().interrupt();
          return null;
        }
      }
      ret = exportedMetrics;
      exportedMetrics = new ArrayList<>();
    }
    return ret;
  }
}
