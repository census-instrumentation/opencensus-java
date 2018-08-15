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

import com.google.common.base.Preconditions;
import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.metrics.MetricProducer;
import io.opencensus.metrics.Metrics;
import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;

/** Base implementation of {@link StatsComponent}. */
public class StatsComponentImplBase extends StatsComponent {

  // The StatsCollectionState shared between the StatsComponent, StatsRecorder and ViewManager.
  private final CurrentStatsState state = new CurrentStatsState();

  private final ViewManagerImpl viewManager;
  private final StatsRecorderImpl statsRecorder;

  /**
   * Creates a new {@code StatsComponentImplBase}.
   *
   * @param queue the queue implementation.
   * @param clock the clock to use when recording stats.
   */
  public StatsComponentImplBase(EventQueue queue, Clock clock) {
    StatsManager statsManager = new StatsManager(queue, clock, state);
    this.viewManager = new ViewManagerImpl(statsManager);
    this.statsRecorder = new StatsRecorderImpl(statsManager);

    // Create a new MetricProducerImpl and register it to MetricProducerManager when
    // StatsComponentImplBase is initialized.
    MetricProducer metricProducer = new MetricProducerImpl(statsManager);
    Metrics.getExportComponent().getMetricProducerManager().add(metricProducer);
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
  public StatsCollectionState getState() {
    return state.get();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void setState(StatsCollectionState newState) {
    boolean stateChanged = state.set(Preconditions.checkNotNull(newState, "newState"));
    if (stateChanged) {
      if (newState == StatsCollectionState.DISABLED) {
        viewManager.clearStats();
      } else {
        viewManager.resumeStatsCollection();
      }
    }
  }
}
