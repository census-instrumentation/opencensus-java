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

package com.google.instrumentation.stats;

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.common.EventQueue;

/**
 * Base implementation of {@link StatsManager}.
 */
class StatsManagerImplBase extends StatsManager {

  // StatsManagerImplBase delegates all operations related to stats to ViewManager in order to keep
  // StatsManagerImplBase simple.
  private final ViewManager viewManager;

  // The StatsContextFactoryImpl is lazily initialized because it references "this" and cannot be
  // created in the constructor.  Multiple initializations are okay.
  private volatile StatsContextFactoryImpl statsContextFactory;

  StatsManagerImplBase(EventQueue queue, Clock clock) {
    this.viewManager = new ViewManager(queue, clock);
  }

  @Override
  public void registerView(ViewDescriptor viewDescriptor) {
    viewManager.registerView(viewDescriptor);
  }

  @Override
  public View getView(ViewDescriptor viewDescriptor) {
    return viewManager.getView(viewDescriptor);
  }

  @Override
  StatsContextFactoryImpl getStatsContextFactory() {
    StatsContextFactoryImpl factory = statsContextFactory;
    if (factory == null) {
      statsContextFactory = factory = new StatsContextFactoryImpl(this);
    }
    return factory;
  }

  /**
   * Records a set of measurements with a set of tags.
   *
   * @param tags the tags associated with the measurements
   * @param measurementValues the measurements to record
   */
  // TODO(sebright): Add this method to StatsManager.
  void record(StatsContextImpl tags, MeasurementMap measurementValues) {
    viewManager.record(tags, measurementValues);
  }
}
