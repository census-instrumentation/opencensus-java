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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.metrics.export.Metric;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measurement;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** A class that stores a singleton map from {@code MeasureName}s to {@link MutableViewData}s. */
@SuppressWarnings("deprecation")
final class MeasureToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from Measures
   * to MutableViewDatas.
   */
  @GuardedBy("this")
  private final Multimap<String, MutableViewData> mutableMap =
      HashMultimap.<String, MutableViewData>create();

  @GuardedBy("this")
  private final Map<View.Name, View> registeredViews = new HashMap<View.Name, View>();

  // TODO(songya): consider adding a Measure.Name class
  @GuardedBy("this")
  private final Map<String, Measure> registeredMeasures = Maps.newHashMap();

  // Cached set of exported views. It must be set to null whenever a view is registered or
  // unregistered.
  @javax.annotation.Nullable private volatile Set<View> exportedViews;

  /** Returns a {@link ViewData} corresponding to the given {@link View.Name}. */
  @javax.annotation.Nullable
  synchronized ViewData getView(View.Name viewName, Clock clock, State state) {
    MutableViewData view = getMutableViewData(viewName);
    return view == null ? null : view.toViewData(clock.now(), state);
  }

  Set<View> getExportedViews() {
    Set<View> views = exportedViews;
    if (views == null) {
      synchronized (this) {
        exportedViews = views = filterExportedViews(registeredViews.values());
      }
    }
    return views;
  }

  // Returns the subset of the given views that should be exported
  private static Set<View> filterExportedViews(Collection<View> allViews) {
    Set<View> views = Sets.newHashSet();
    for (View view : allViews) {
      if (view.getWindow() instanceof View.AggregationWindow.Cumulative) {
        views.add(view);
      }
    }
    return Collections.unmodifiableSet(views);
  }

  /** Enable stats collection for the given {@link View}. */
  synchronized void registerView(View view, Clock clock) {
    exportedViews = null;
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
    Measure measure = view.getMeasure();
    Measure registeredMeasure = registeredMeasures.get(measure.getName());
    if (registeredMeasure != null && !registeredMeasure.equals(measure)) {
      throw new IllegalArgumentException(
          "A different measure with the same name is already registered: " + registeredMeasure);
    }
    registeredViews.put(view.getName(), view);
    if (registeredMeasure == null) {
      registeredMeasures.put(measure.getName(), measure);
    }
    Timestamp now = clock.now();
    mutableMap.put(view.getMeasure().getName(), MutableViewData.create(view, now));
  }

  @javax.annotation.Nullable
  private synchronized MutableViewData getMutableViewData(View.Name viewName) {
    View view = registeredViews.get(viewName);
    if (view == null) {
      return null;
    }
    Collection<MutableViewData> views = mutableMap.get(view.getMeasure().getName());
    for (MutableViewData viewData : views) {
      if (viewData.getView().getName().equals(viewName)) {
        return viewData;
      }
    }
    throw new AssertionError(
        "Internal error: Not recording stats for view: \""
            + viewName
            + "\" registeredViews="
            + registeredViews
            + ", mutableMap="
            + mutableMap);
  }

  // Records stats with a set of tags.
  synchronized void record(
      Map<TagKey, TagValue> tags, MeasureMapInternal stats, Timestamp timestamp) {
    Iterator<Measurement> iterator = stats.iterator();
    Map<String, String> attachments = stats.getAttachments();
    while (iterator.hasNext()) {
      Measurement measurement = iterator.next();
      Measure measure = measurement.getMeasure();
      if (!measure.equals(registeredMeasures.get(measure.getName()))) {
        // unregistered measures will be ignored.
        continue;
      }
      Collection<MutableViewData> viewDataCollection = mutableMap.get(measure.getName());
      for (MutableViewData viewData : viewDataCollection) {
        viewData.record(
            tags, RecordUtils.getDoubleValueFromMeasurement(measurement), timestamp, attachments);
      }
    }
  }

  synchronized List<Metric> getMetrics(Clock clock, State state) {
    List<Metric> metrics = new ArrayList<Metric>();
    Timestamp now = clock.now();
    for (Entry<String, MutableViewData> entry : mutableMap.entries()) {
      Metric metric = entry.getValue().toMetric(now, state);
      if (metric != null) {
        metrics.add(metric);
      }
    }
    return metrics;
  }

  // Clear stats for all the current MutableViewData
  synchronized void clearStats() {
    for (Entry<String, Collection<MutableViewData>> entry : mutableMap.asMap().entrySet()) {
      for (MutableViewData mutableViewData : entry.getValue()) {
        mutableViewData.clearStats();
      }
    }
  }

  // Resume stats collection for all MutableViewData.
  synchronized void resumeStatsCollection(Timestamp now) {
    for (Entry<String, Collection<MutableViewData>> entry : mutableMap.asMap().entrySet()) {
      for (MutableViewData mutableViewData : entry.getValue()) {
        mutableViewData.resumeStatsCollection(now);
      }
    }
  }
}
