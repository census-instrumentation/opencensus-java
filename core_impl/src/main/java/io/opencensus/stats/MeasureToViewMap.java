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

package io.opencensus.stats;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.opencensus.common.Clock;
import io.opencensus.common.Function;
import io.opencensus.stats.Measurement.DoubleMeasurement;
import io.opencensus.stats.Measurement.LongMeasurement;
import io.opencensus.stats.MutableView.MutableDistributionView;
import io.opencensus.stats.MutableView.MutableIntervalView;
import io.opencensus.stats.ViewDescriptor.DistributionViewDescriptor;
import io.opencensus.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * A class that stores a singleton map from {@code MeasureName}s to {@link
 * MutableView}s.
 */
final class MeasureToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from Measures
   * to MutableViews.
   */
  @GuardedBy("this")
  private final Multimap<String, MutableView> mutableMap =
      HashMultimap.<String, MutableView>create();

  @GuardedBy("this")
  private final Map<ViewDescriptor.Name, ViewDescriptor> registeredViews =
      new HashMap<ViewDescriptor.Name, ViewDescriptor>();

  /** Returns a {@link View} corresponding to the given {@link ViewDescriptor.Name}. */
  synchronized View getView(ViewDescriptor.Name viewName, Clock clock) {
    MutableView view = getMutableView(viewName);
    return view == null ? null : view.toView(clock);
  }

  @Nullable
  private synchronized MutableView getMutableView(ViewDescriptor.Name viewName) {
    ViewDescriptor viewDescriptor = registeredViews.get(viewName);
    if (viewDescriptor == null) {
      return null;
    }
    Collection<MutableView> views =
        mutableMap.get(viewDescriptor.getMeasure().getName());
    for (MutableView view : views) {
      if (view.getViewDescriptor().getViewDescriptorName().equals(viewName)) {
        return view;
      }
    }
    throw new AssertionError("Internal error: Not recording stats for view: \"" + viewName
        + "\" registeredViews=" + registeredViews + ", mutableMap=" + mutableMap);
  }

  /** Enable stats collection for the given {@link ViewDescriptor}. */
  synchronized void registerView(ViewDescriptor viewDescriptor, Clock clock) {
    ViewDescriptor existing = registeredViews.get(viewDescriptor.getViewDescriptorName());
    if (existing != null) {
      if (existing.equals(viewDescriptor)) {
        // Ignore views that are already registered.
        return;
      } else {
        throw new IllegalArgumentException(
            "A different view with the same name is already registered: " + existing);
      }
    }
    registeredViews.put(viewDescriptor.getViewDescriptorName(), viewDescriptor);
    MutableView mutableView =
        viewDescriptor.match(
            new CreateMutableDistributionViewFunction(clock),
            new CreateMutableIntervalViewFunction());
    mutableMap.put(
        viewDescriptor.getMeasure().getName(), mutableView);
  }

  // Records stats with a set of tags.
  synchronized void record(StatsContextImpl tags, MeasureMap stats) {
    Iterator<Measurement> iterator = stats.iterator();
    while (iterator.hasNext()) {
      Measurement measurement = iterator.next();
      Collection<MutableView> views =
          mutableMap.get(measurement.getMeasure().getName());
      Object value = measurement.match(new GetDoubleValueFunc(), new GetLongValueFunc());

      for (MutableView view : views) {
        if (value instanceof Double) {
          view.record(tags, (Double) value);
        } else if (value instanceof Long) {
          // TODO: determine if we want to support LongMeasure in v0.1
          throw new UnsupportedOperationException("Long measurements not supported.");
        } else {
          throw new UnsupportedOperationException("Unknown measure value type.");
        }
      }
    }
  }

  private static final class CreateMutableDistributionViewFunction
      implements Function<DistributionViewDescriptor, MutableView> {

    private final Clock clock;

    CreateMutableDistributionViewFunction(Clock clock) {
      this.clock = clock;
    }

    @Override
    public MutableView apply(DistributionViewDescriptor viewDescriptor) {
      return MutableDistributionView.create(viewDescriptor, clock.now());
    }
  }

  private static final class CreateMutableIntervalViewFunction
      implements Function<IntervalViewDescriptor, MutableView> {

    @Override
    public MutableView apply(IntervalViewDescriptor viewDescriptor) {
      // TODO(songya): Create Interval Aggregations from internal Distributions.
      return MutableIntervalView.create(viewDescriptor);
    }
  }

  private static final class GetDoubleValueFunc implements Function<DoubleMeasurement, Object> {
    @Override
    public Double apply(DoubleMeasurement arg) {
      return arg.getValue();
    }
  }

  private static final class GetLongValueFunc implements Function<LongMeasurement, Object> {
    @Override
    public Long apply(LongMeasurement arg) {
      return arg.getValue();
    }
  }
}
