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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.MonitoredResource;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code ThreadSafe} class that holds Stackdriver {@link MonitoredResource} for this {@link
 * StackdriverStatsExporter}.
 */
@ThreadSafe
final class StackdriverStatsMonitoredResource {

  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  private static MonitoredResource monitoredResource =
      MonitoredResource.newBuilder().setType("global").build();

  static void setMonitoredResource(MonitoredResource monitoredResource) {
    checkNotNull(monitoredResource);
    synchronized (monitor) {
      StackdriverStatsMonitoredResource.monitoredResource = monitoredResource;
    }
  }

  static MonitoredResource getMonitoredResource() {
    synchronized (monitor) {
      return monitoredResource;
    }
  }

  private StackdriverStatsMonitoredResource() {}
}
