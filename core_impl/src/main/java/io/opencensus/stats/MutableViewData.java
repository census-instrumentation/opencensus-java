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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opencensus.common.Clock;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Range;
import io.opencensus.stats.Aggregation.StdDev;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.MutableAggregation.MutableCount;
import io.opencensus.stats.MutableAggregation.MutableHistogram;
import io.opencensus.stats.MutableAggregation.MutableMean;
import io.opencensus.stats.MutableAggregation.MutableRange;
import io.opencensus.stats.MutableAggregation.MutableStdDev;
import io.opencensus.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.stats.ViewData.WindowData.CumulativeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * A mutable version of {@link ViewData}, used for recording stats and start/end time.
 */
final class MutableViewData {

  // TODO(songya): might want to update the default tag value later.
  @VisibleForTesting
  static final TagValue UNKNOWN_TAG_VALUE = TagValue.create("unknown/not set");

  private final View view;
  private final Map<List<TagValue>, List<MutableAggregation>> tagValueAggregationMap =
      Maps.newHashMap();
  @Nullable private final Timestamp start;

  private MutableViewData(View view, @Nullable Timestamp start) {
    this.view = view;
    this.start = start;
  }

  /**
   * Constructs a new {@link MutableViewData}.
   */
  static MutableViewData create(View view, Timestamp start) {
    if (view.getWindow() instanceof Interval) {
      // TODO(songya): support IntervalView.
      throw new UnsupportedOperationException("Interval views not supported yet.");
    }
    return new MutableViewData(view, start);
  }

  /**
   * The {@link View} associated with this {@link ViewData}.
   */
  View getView() {
    return view;
  }

  /**
   * Record double stats with the given tags.
   */
  void record(StatsContextImpl context, double value) {
    List<TagValue> tagValues = getTagValues(context.tags, view.getColumns());
    if (!tagValueAggregationMap.containsKey(tagValues)) {
      List<MutableAggregation> aggregations = Lists.newArrayList();
      for (Aggregation aggregation : view.getAggregations()) {
        aggregations.add(createMutableAggregation(aggregation));
      }
      tagValueAggregationMap.put(tagValues, aggregations);
    }
    for (MutableAggregation aggregation : tagValueAggregationMap.get(tagValues)) {
      aggregation.add(value);
    }
  }

  /**
   * Record long stats with the given tags.
   */
  void record(StatsContextImpl tags, long value) {
    // TODO(songya): modify MutableDistribution to support long values.
    throw new UnsupportedOperationException("Not implemented.");
  }

  /**
   * Convert this {@link MutableViewData} to {@link ViewData}.
   */
  ViewData toViewData(Clock clock) {
    Map<List<TagValue>, List<AggregationData>> map = Maps.newHashMap();
    for (Entry<List<TagValue>, List<MutableAggregation>> entry :
        tagValueAggregationMap.entrySet()) {
      List<AggregationData> aggregates = Lists.newArrayList();
      for (MutableAggregation aggregation : entry.getValue()) {
        aggregates.add(createAggregationData(aggregation));
      }
      map.put(entry.getKey(), aggregates);
    }
    return ViewData.create(view, map, CumulativeData.create(start, clock.now()));
  }

  /**
   * Returns start timestamp for this aggregation.
   */
  @Nullable
  Timestamp getStart() {
    return start;
  }

  @VisibleForTesting
  static List<TagValue> getTagValues(Map<TagKey, TagValue> tags, List<TagKey> columns) {
    List<TagValue> tagValues = new ArrayList<TagValue>(columns.size());
    // Record all the measures in a "Greedy" way.
    // Every view aggregates every measure. This is similar to doing a GROUPBY view’s keys.
    for (int i = 0; i < columns.size(); ++i) {
      TagKey tagKey = columns.get(i);
      if (!tags.containsKey(tagKey)) {
        // replace not found key values by “unknown/not set”.
        tagValues.add(UNKNOWN_TAG_VALUE);
      } else {
        tagValues.add(tags.get(tagKey));
      }
    }
    return tagValues;
  }

  /**
   * Create an empty {@link MutableAggregation} based on the given {@link Aggregation}.
   *
   * @param aggregation {@code Aggregation}.
   * @return an empty {@code MutableAggregation}.
   */
  @VisibleForTesting
  static MutableAggregation createMutableAggregation(Aggregation aggregation) {
    return aggregation.match(
        new Function<Sum, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Sum arg) {
            return MutableSum.create();
          }
        },
        new Function<Count, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Count arg) {
            return MutableCount.create();
          }
        },
        new Function<Histogram, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Histogram arg) {
            return MutableHistogram.create(arg.getBucketBoundaries());
          }
        },
        new Function<Range, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Range arg) {
            return MutableRange.create();
          }
        },
        new Function<Mean, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Mean arg) {
            return MutableMean.create();
          }
        },
        new Function<StdDev, MutableAggregation>() {
          @Override
          public MutableAggregation apply(StdDev arg) {
            return MutableStdDev.create();
          }
        },
        Functions.<MutableAggregation>throwIllegalArgumentException());
  }

  /**
   * Create an {@link AggregationData} snapshot based on the given {@link MutableAggregation}.
   *
   * @param aggregation {@code MutableAggregation}
   * @return an {@code AggregationData} which is the snapshot of current summary statistics.
   */
  @VisibleForTesting
  static AggregationData createAggregationData(MutableAggregation aggregation) {
    return aggregation.match(
        new Function<MutableSum, AggregationData>() {
          @Override
          public AggregationData apply(MutableSum arg) {
            return SumData.create(arg.getSum());
          }
        },
        new Function<MutableCount, AggregationData>() {
          @Override
          public AggregationData apply(MutableCount arg) {
            return CountData.create(arg.getCount());
          }
        },
        new Function<MutableHistogram, AggregationData>() {
          @Override
          public AggregationData apply(MutableHistogram arg) {
            return HistogramData.create(arg.getBucketCounts());
          }
        },
        new Function<MutableRange, AggregationData>() {
          @Override
          public AggregationData apply(MutableRange arg) {
            return RangeData.create(arg.getMin(), arg.getMax());
          }
        },
        new Function<MutableMean, AggregationData>() {
          @Override
          public AggregationData apply(MutableMean arg) {
            return MeanData.create(arg.getMean());
          }
        },
        new Function<MutableStdDev, AggregationData>() {
          @Override
          public AggregationData apply(MutableStdDev arg) {
            return StdDevData.create(arg.getStdDev());
          }
        });
  }
}
