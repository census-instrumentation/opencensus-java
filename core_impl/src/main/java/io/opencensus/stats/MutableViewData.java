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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opencensus.common.Clock;
import io.opencensus.common.Duration;
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
import io.opencensus.stats.View.Window.Cumulative;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.stats.ViewData.WindowData.CumulativeData;
import io.opencensus.stats.ViewData.WindowData.IntervalData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** A mutable version of {@link ViewData}, used for recording stats and start/end time. */
abstract class MutableViewData {
  
  // TODO(songya): might want to update the default tag value later.
  @VisibleForTesting
  static final TagValue UNKNOWN_TAG_VALUE = TagValue.create("unknown/not set");

  private final View view;
  private final Map<List<TagValue>, List<MutableAggregation>> tagValueAggregationMap =
      Maps.newHashMap();

  private MutableViewData(View view) {
    this.view = view;
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
        new CreateCumulative(view, start),
        new CreateInterval(view),
        Functions.<MutableViewData>throwAssertionError());
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
  abstract void record(StatsContextImpl context, double value, Timestamp timestamp);

  /**
   * Record long stats with the given tags.
   */
  abstract void record(StatsContextImpl tags, long value, Timestamp timestamp);

  /**
   * Convert this {@link MutableViewData} to {@link ViewData}.
   */
  abstract ViewData toViewData(Clock clock);

  private void recordInternal(List<TagValue> tagValues, double value) {
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

  private Map<List<TagValue>, List<AggregationData>> getAggregationMap() {
    Map<List<TagValue>, List<AggregationData>> map = Maps.newHashMap();
    for (Entry<List<TagValue>, List<MutableAggregation>> entry :
        tagValueAggregationMap.entrySet()) {
      List<AggregationData> aggregates = Lists.newArrayList();
      for (MutableAggregation aggregation : entry.getValue()) {
        aggregates.add(createAggregationData(aggregation));
      }
      map.put(entry.getKey(), aggregates);
    }
    return map;
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

  private static final class CumulativeMutableViewData extends MutableViewData {

    private final Timestamp start;

    private CumulativeMutableViewData(View view, Timestamp start) {
      super(view);
      checkNotNull(start, "Start Timestamp");
      this.start = start;
    }

    @Override
    void record(StatsContextImpl context, double value, Timestamp timestamp) {
      List<TagValue> tagValues = getTagValues(context.tags, super.view.getColumns());
      super.recordInternal(tagValues, value);
    }

    @Override
    void record(StatsContextImpl tags, long value, Timestamp timestamp) {
      // TODO(songya): implement this.
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    ViewData toViewData(Clock clock) {
      return ViewData.create(
          super.view, super.getAggregationMap(), CumulativeData.create(start, clock.now()));
    }
  }

  private static final class IntervalMutableViewData extends MutableViewData {

    // The queue of TimestampedValues for interval aggregations.
    private final LinkedList<TimestampedValue> valueQueue = new LinkedList<TimestampedValue>();

    private IntervalMutableViewData(View view) {
      super(view);
    }

    @Override
    void record(StatsContextImpl context, double value, Timestamp timestamp) {
      removeExpiredValues(timestamp);
      List<TagValue> tagValues = getTagValues(context.tags, super.view.getColumns());
      addValueToQueue(value, timestamp, tagValues);
      super.recordInternal(tagValues, value);
    }

    @Override
    void record(StatsContextImpl tags, long value, Timestamp timestamp) {
      // TODO(songya): implement this.
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    ViewData toViewData(Clock clock) {
      removeExpiredValues(clock.now());
      return ViewData.create(
          super.view, super.getAggregationMap(), IntervalData.create(clock.now()));
    }

    // Add the measure value associated with the given timestamp and tag values to valueQueue.
    private void addValueToQueue(double value, Timestamp now, List<TagValue> tagValues) {
      checkNotNull(now, "Timestamp");
      checkNotNull(tagValues, "TagValues");
      valueQueue.addLast(new TimestampedValue(value, now, tagValues));
    }

    // Dequeue expired values from valueQueue against current timestamp, and update summary stats
    // by eliminating expired values.
    private void removeExpiredValues(Timestamp now) {
      checkNotNull(now, "Timestamp");
      Interval interval = (Interval) super.view.getWindow();
      while (!valueQueue.isEmpty() && valueQueue.peek().isExpired(interval.getDuration(), now)) {
        TimestampedValue expired = valueQueue.poll();
        List<MutableAggregation> mutableAggregations = super.tagValueAggregationMap
            .get(expired.tagValues);
        for (MutableAggregation mutableAggregation : mutableAggregations) {
          mutableAggregation.remove(expired.value);
        }
      }
    }
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

  private static final class CreateCumulative implements Function<Cumulative, MutableViewData> {
    @Override
    public MutableViewData apply(Cumulative arg) {
      return new CumulativeMutableViewData(view, start);
    }

    private final View view;
    private final Timestamp start;

    private CreateCumulative(View view, Timestamp start) {
      this.view = view;
      this.start = start;
    }
  }

  private static final class CreateInterval implements Function<Interval, MutableViewData> {
    @Override
    public MutableViewData apply(Interval arg) {
      return new IntervalMutableViewData(view);
    }

    private final View view;

    private CreateInterval(View view) {
      this.view = view;
    }
  }

  /** Value associated with a {@code TimeStamp} and a list of {@code TagValue}s. */
  @Immutable
  private static final class TimestampedValue {

    private final double value;
    private final Timestamp timestamp;
    private final List<TagValue> tagValues;

    private TimestampedValue(double value, Timestamp timestamp, List<TagValue> tagValues) {
      this.value = value;
      this.timestamp = timestamp;
      this.tagValues = tagValues;
    }

    private boolean isExpired(Duration duration, Timestamp now) {
      return now.compareTo(timestamp.addDuration(duration)) > 0;
    }
  }
}
