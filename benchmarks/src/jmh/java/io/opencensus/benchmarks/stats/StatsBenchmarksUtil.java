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

package io.opencensus.benchmarks.stats;

import static io.opencensus.benchmarks.tags.TagsBenchmarksUtil.TAG_KEYS;
import static io.opencensus.benchmarks.tags.TagsBenchmarksUtil.TAG_VALUES;

import io.opencensus.impl.stats.StatsComponentImpl;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.impllite.stats.StatsComponentImplLite;
import io.opencensus.impllite.tags.TagsComponentImplLite;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.propagation.TagContextBinarySerializer;

import java.util.Arrays;

/** Util class for Benchmarks. */
final class StatsBenchmarksUtil {
  private static final StatsComponentImpl statsComponentImpl = new StatsComponentImpl();
  private static final StatsComponentImplLite statsComponentImplLite = new StatsComponentImplLite();

  private static final int MEASURES = 8;
  private static final int VIEWS = 8;

  static final Aggregation.Distribution DISTRIBUTION = Aggregation.Distribution.create(
      BucketBoundaries.create(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)));

  static final Measure.MeasureDouble[] DOUBLE_COUNT_MEASURES = createMeasureDoubles(MEASURES, "Count");
  static final Measure.MeasureLong[] LONG_COUNT_MEASURES = createMeasureLongs(MEASURES, "Count");

  static final Measure.MeasureDouble[] DOUBLE_SUM_MEASURES = createMeasureDoubles(MEASURES, "Sum");
  static final Measure.MeasureLong[] LONG_SUM_MEASURES = createMeasureLongs(MEASURES, "Sum");

  static final Measure.MeasureDouble[] DOUBLE_DISTRIBUTION_MEASURES =
      createMeasureDoubles(MEASURES, "Distribution");
  static final Measure.MeasureLong[] LONG_DISTRIBUTION_MEASURES =
      createMeasureLongs(MEASURES, "Distribution");

  static final Measure.MeasureDouble[] DOUBLE_LASTVALUE_MEASURES =
      createMeasureDoubles(MEASURES, "LastValue");
  static final Measure.MeasureLong[] LONG_LASTVALUE_MEASURES =
      createMeasureLongs(MEASURES, "LastValue");

  static final View[] DOUBLE_COUNT_VIEWS =
      createViews(VIEWS, DOUBLE_COUNT_MEASURES, Aggregation.Count.create(), TAG_KEYS[0]);
  static final View[] LONG_COUNT_VIEWS =
      createViews(VIEWS, LONG_COUNT_MEASURES, Aggregation.Count.create(), TAG_KEYS[0]);

  static final View[] DOUBLE_SUM_VIEWS =
      createViews(VIEWS, DOUBLE_SUM_MEASURES, Aggregation.Sum.create(), TAG_KEYS[0]);
  static final View[] LONG_SUM_VIEWS =
      createViews(VIEWS, LONG_SUM_MEASURES, Aggregation.Sum.create(), TAG_KEYS[0]);

  static final View[] DOUBLE_DISTRIBUTION_VIEWS =
      createViews(VIEWS, DOUBLE_DISTRIBUTION_MEASURES, DISTRIBUTION, TAG_KEYS[0]);
  static final View[] LONG_DISTRIBUTION_VIEWS =
      createViews(VIEWS, LONG_DISTRIBUTION_MEASURES, DISTRIBUTION, TAG_KEYS[0]);

  static final View[] DOUBLE_LASTVALUE_VIEWS =
      createViews(VIEWS, DOUBLE_LASTVALUE_MEASURES, Aggregation.LastValue.create(), TAG_KEYS[0]);
  static final View[] LONG_LASTVALUE_VIEWS =
      createViews(VIEWS, LONG_LASTVALUE_MEASURES, Aggregation.LastValue.create(), TAG_KEYS[0]);

  static StatsRecorder getStatsRecorder(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return statsComponentImpl.getStatsRecorder();
    } else if (implementation.equals("impl-lite")) {
      return statsComponentImplLite.getStatsRecorder();
    } else {
      throw new RuntimeException("Invalid stats recorder implementation specified.");
    }
  }

  static ViewManager getViewManager(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return statsComponentImpl.getViewManager();
    } else if (implementation.equals("impl-lite")) {
      return statsComponentImplLite.getViewManager();
    } else {
      throw new RuntimeException("Invalid view manager implementation specified.");
    }
  }

  private static View[] createViews(int size, Measure[] measures, Aggregation aggregation, TagKey... keys) {
    View[] views = new View[size];
    for (int i = 0; i < size; i++) {
      views[i] =  createView(measures[i].getName(), measures[i], aggregation, keys);
    }
    return views;
  }

  static View createView(String name, Measure measure, Aggregation aggregation, TagKey... keys) {
    return View.create(View.Name.create(name), "", measure, aggregation, Arrays.asList(keys));
  }

  private static Measure.MeasureDouble[] createMeasureDoubles(int size, String name) {
    Measure.MeasureDouble[] measures = new Measure.MeasureDouble[size];
    for (int i = 0; i < size; i++) {
      measures[i] = Measure.MeasureDouble.create(name + "_MD" + i, "", "ns");
    }
    return measures;
  }

  private static Measure.MeasureLong[] createMeasureLongs(int size, String name) {
    Measure.MeasureLong[] measures = new Measure.MeasureLong[size];
    for (int i = 0; i < size; i++) {
      measures[i] = Measure.MeasureLong.create(name + "_ML" + i, "", "ns");
    }
    return measures;
  }

  // Avoid instances of this class.
  private StatsBenchmarksUtil() {}
}
