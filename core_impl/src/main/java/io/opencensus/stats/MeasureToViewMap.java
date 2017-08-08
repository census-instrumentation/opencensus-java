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
import io.opencensus.stats.Measurement.MeasurementDouble;
import io.opencensus.stats.Measurement.MeasurementLong;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * A class that stores a singleton map from {@code MeasureName}s to {@link
 * MutableViewData}s.
 */
final class MeasureToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from Measures
   * to MutableViewDatas.
   */
  @GuardedBy("this")
  private final Multimap<String, MutableViewData> mutableMap =
      HashMultimap.<String, MutableViewData>create();

  @GuardedBy("this")
  private final Map<View.Name, View> registeredViews =
      new HashMap<View.Name, View>();

  /** Returns a {@link ViewData} corresponding to the given {@link View.Name}. */
  synchronized ViewData getView(View.Name viewName, Clock clock) {
    MutableViewData view = getMutableViewData(viewName);
    return view == null ? null : view.toViewData(clock);
  }

  @Nullable
  private synchronized MutableViewData getMutableViewData(View.Name viewName) {
    View view = registeredViews.get(viewName);
    if (view == null) {
      return null;
    }
    Collection<MutableViewData> views =
        mutableMap.get(view.getMeasure().getName());
    for (MutableViewData viewData : views) {
      if (viewData.getView().getName().equals(viewName)) {
        return viewData;
      }
    }
    throw new AssertionError("Internal error: Not recording stats for view: \"" + viewName
        + "\" registeredViews=" + registeredViews + ", mutableMap=" + mutableMap);
  }

  /** Enable stats collection for the given {@link View}. */
  synchronized void registerView(View view, Clock clock) {
    View existing = registeredViews.get(view.getName());
    if (existing != null) {
      if (existing.equals(view)) {
        // Ignore views that are already registered.
        return;
      } else {
        throw new IllegalArgumentException(
            "A different view with the same name is already registered: " + existing);
      }
    }
    registeredViews.put(view.getName(), view);
    mutableMap.put(view.getMeasure().getName(), MutableViewData.create(view, clock.now()));
  }

  // Records stats with a set of tags.
  synchronized void record(StatsContextImpl tags, MeasureMap stats) {
    Iterator<Measurement> iterator = stats.iterator();
    while (iterator.hasNext()) {
      Measurement measurement = iterator.next();
      Collection<MutableViewData> views = mutableMap.get(measurement.getMeasure().getName());
      for (MutableViewData view : views) {
        measurement.match(
            new RecordDoubleValueFunc(tags, view), new RecordLongValueFunc(tags, view));
      }
    }
  }

  private static final class RecordDoubleValueFunc implements Function<MeasurementDouble, Void> {
    @Override
    public Void apply(MeasurementDouble arg) {
      view.record(tags, arg.getValue());
      return null;
    }

    private final StatsContextImpl tags;
    private final MutableViewData view;

    private RecordDoubleValueFunc(StatsContextImpl tags, MutableViewData view) {
      this.tags = tags;
      this.view = view;
    }
  }

  private static final class RecordLongValueFunc implements Function<MeasurementLong, Void> {
    @Override
    public Void apply(MeasurementLong arg) {
      view.record(tags, arg.getValue());
      return null;
    }

    private final StatsContextImpl tags;
    private final MutableViewData view;

    private RecordLongValueFunc(StatsContextImpl tags, MutableViewData view) {
      this.tags = tags;
      this.view = view;
    }
  }
}
