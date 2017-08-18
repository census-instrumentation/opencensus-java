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

package io.opencensus.implcore.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opencensus.common.Clock;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableHistogram;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableRange;
import io.opencensus.implcore.stats.MutableAggregation.MutableStdDev;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.implcore.tags.TagContextImpl;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Range;
import io.opencensus.stats.Aggregation.StdDev;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Window.Cumulative;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.WindowData.CumulativeData;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * A mutable version of {@link ViewData}, used for recording stats and start/end time.
 */
final class MutableViewData {

  private static final Function<TagString, Object> GET_STRING_TAG_VALUE =
      new Function<TagString, Object>() {
        @Override
        public Object apply(TagString tag) {
          return tag.getValue();
        }
      };

  private static final Function<TagLong, Object> GET_LONG_TAG_VALUE =
      new Function<TagLong, Object>() {
        @Override
        public Object apply(TagLong tag) {
          return tag.getValue();
        }
      };

  private static final Function<TagBoolean, Object> GET_BOOLEAN_TAG_VALUE =
      new Function<TagBoolean, Object>() {
        @Override
        public Object apply(TagBoolean tag) {
          return tag.getValue();
        }
      };

  // TODO(sebright): Change this field to type TagValue once we have a TagValue class.
  @VisibleForTesting
  static final Object UNKNOWN_TAG_VALUE = null;

  private final View view;
  private final Map<List<Object>, List<MutableAggregation>> tagValueAggregationMap =
      Maps.newHashMap();
  @Nullable private final Timestamp start;

  private MutableViewData(View view, @Nullable Timestamp start) {
    this.view = view;
    this.start = start;
  }

  /**
   * Constructs a new {@link MutableViewData}.
   *
   * @param view the {@code View} linked with this {@code MutableViewData}.
   * @param start the start {@code Timestamp}. Not needed for {@code Interval} based {@code View}s.
   * @return a {@code MutableViewData}.
   */
  static MutableViewData create(final View view, @Nullable final Timestamp start) {
    return view.getWindow().match(
        new Function<Cumulative, MutableViewData>() {
          @Override
          public MutableViewData apply(Cumulative arg) {
            return new MutableViewData(view, start);
          }
        },
        new Function<Interval, MutableViewData>() {
          @Override
          public MutableViewData apply(Interval arg) {
            // TODO(songya): support IntervalView.
            throw new UnsupportedOperationException("Interval views not supported yet.");
          }
        },
        Functions.<MutableViewData>throwIllegalArgumentException());
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
  void record(TagContext context, double value) {
    List<Object> tagValues = getTagValues(getTagMap(context), view.getColumns());
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
  void record(TagContext tags, long value) {
    // TODO(songya): modify MutableDistribution to support long values.
    throw new UnsupportedOperationException("Not implemented.");
  }

  private static Map<TagKey, Object> getTagMap(TagContext ctx) {
    if (ctx instanceof TagContextImpl) {
      return ((TagContextImpl) ctx).getTags();
    } else {
      Map<TagKey, Object> tags = Maps.newHashMap();
      for (Iterator<Tag> i = ctx.unsafeGetIterator(); i.hasNext(); ) {
        Tag tag = i.next();
        tags.put(
            tag.getKey(),
            tag.match(
                GET_STRING_TAG_VALUE,
                GET_LONG_TAG_VALUE,
                GET_BOOLEAN_TAG_VALUE,
                Functions.throwAssertionError()));
      }
      return tags;
    }
  }

  /**
   * Convert this {@link MutableViewData} to {@link ViewData}.
   */
  ViewData toViewData(Clock clock) {
    Map<List<Object>, List<AggregationData>> map = Maps.newHashMap();
    for (Entry<List<Object>, List<MutableAggregation>> entry :
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
  static List<Object> getTagValues(
      Map<? extends TagKey, ? extends Object> tags, List<? extends TagKey> columns) {
    List<Object> tagValues = new ArrayList<Object>(columns.size());
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
        CreateMutableSum.INSTANCE,
        CreateMutableCount.INSTANCE,
        CreateMutableHistogram.INSTANCE,
        CreateMutableRange.INSTANCE,
        CreateMutableMean.INSTANCE,
        CreateMutableStdDev.INSTANCE,
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
        CreateSumData.INSTANCE,
        CreateCountData.INSTANCE,
        CreateHistogramData.INSTANCE,
        CreateRangeData.INSTANCE,
        CreateMeanData.INSTANCE,
        CreateStdDevData.INSTANCE);
  }
  
  // static inner Function classes
  
  private static final class CreateMutableSum implements Function<Sum, MutableAggregation> {
    @Override
    public MutableAggregation apply(Sum arg) {
      return MutableSum.create();
    }
    
    private static final CreateMutableSum INSTANCE = new CreateMutableSum();
  }

  private static final class CreateMutableCount implements Function<Count, MutableAggregation> {
    @Override
    public MutableAggregation apply(Count arg) {
      return MutableCount.create();
    }

    private static final CreateMutableCount INSTANCE = new CreateMutableCount();
  }

  private static final class CreateMutableHistogram 
      implements Function<Histogram, MutableAggregation> {
    @Override
    public MutableAggregation apply(Histogram arg) {
      return MutableHistogram.create(arg.getBucketBoundaries());
    }

    private static final CreateMutableHistogram INSTANCE = new CreateMutableHistogram();
  }

  private static final class CreateMutableRange implements Function<Range, MutableAggregation> {
    @Override
    public MutableAggregation apply(Range arg) {
      return MutableRange.create();
    }

    private static final CreateMutableRange INSTANCE = new CreateMutableRange();
  }

  private static final class CreateMutableMean implements Function<Mean, MutableAggregation> {
    @Override
    public MutableAggregation apply(Mean arg) {
      return MutableMean.create();
    }

    private static final CreateMutableMean INSTANCE = new CreateMutableMean();
  }

  private static final class CreateMutableStdDev implements Function<StdDev, MutableAggregation> {
    @Override
    public MutableAggregation apply(StdDev arg) {
      return MutableStdDev.create();
    }

    private static final CreateMutableStdDev INSTANCE = new CreateMutableStdDev();
  }


  private static final class CreateSumData implements Function<MutableSum, AggregationData> {
    @Override
    public AggregationData apply(MutableSum arg) {
      return SumData.create(arg.getSum());
    }

    private static final CreateSumData INSTANCE = new CreateSumData();
  }

  private static final class CreateCountData implements Function<MutableCount, AggregationData> {
    @Override
    public AggregationData apply(MutableCount arg) {
      return CountData.create(arg.getCount());
    }

    private static final CreateCountData INSTANCE = new CreateCountData();
  }

  private static final class CreateHistogramData
      implements Function<MutableHistogram, AggregationData> {
    @Override
    public AggregationData apply(MutableHistogram arg) {
      return HistogramData.create(arg.getBucketCounts());
    }

    private static final CreateHistogramData INSTANCE = new CreateHistogramData();
  }

  private static final class CreateRangeData implements Function<MutableRange, AggregationData> {
    @Override
    public AggregationData apply(MutableRange arg) {
      return RangeData.create(arg.getMin(), arg.getMax());
    }

    private static final CreateRangeData INSTANCE = new CreateRangeData();
  }

  private static final class CreateMeanData implements Function<MutableMean, AggregationData> {
    @Override
    public AggregationData apply(MutableMean arg) {
      return MeanData.create(arg.getMean());
    }

    private static final CreateMeanData INSTANCE = new CreateMeanData();
  }

  private static final class CreateStdDevData implements Function<MutableStdDev, AggregationData> {
    @Override
    public AggregationData apply(MutableStdDev arg) {
      return StdDevData.create(arg.getStdDev());
    }

    private static final CreateStdDevData INSTANCE = new CreateStdDevData();
  }
}
