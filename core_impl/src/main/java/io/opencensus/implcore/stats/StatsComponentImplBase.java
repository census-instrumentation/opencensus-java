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

package io.opencensus.implcore.stats;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.implcore.stats.export.ExportComponentImpl;
import io.opencensus.stats.StatsComponent;
import io.opencensus.stats.export.ExportComponent;

/** Base implementation of {@link StatsComponent}. */
public class StatsComponentImplBase extends StatsComponent {

  private final ViewManagerImpl viewManager;
  private final StatsRecorderImpl statsRecorder;
  private final ExportComponentImpl exportComponent;

  /**
   * Creates a new {@code StatsComponentImplBase}.
   *
   * @param queue the queue implementation.
   * @param clock the clock to use when recording stats.
   */
  public StatsComponentImplBase(
      EventQueue queue, Clock clock, ExportComponentImpl exportComponent) {
    this.exportComponent = checkNotNull(exportComponent, "ExportComponent");
    StatsManager statsManager = new StatsManager(queue, clock, exportComponent.getStatsExporter());
    this.viewManager = new ViewManagerImpl(statsManager);
    this.statsRecorder = new StatsRecorderImpl(statsManager);
  }

  @Override
  public ViewManagerImpl getViewManager() {
    return viewManager;
  }

  @Override
  public StatsRecorderImpl getStatsRecorder() {
    return statsRecorder;
  }

  @Override
  public ExportComponent getExportComponent() {
    return exportComponent;
  }
}
