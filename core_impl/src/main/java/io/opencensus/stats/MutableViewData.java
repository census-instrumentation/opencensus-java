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
import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import io.opencensus.stats.ViewData.DistributionViewData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A mutable version of {@link ViewData}, used for recording stats and start/end time.
 */
abstract class MutableViewData {

  // TODO(songya): might want to update the default tag value later.
  @VisibleForTesting
  static final TagValue UNKNOWN_TAG_VALUE = TagValue.create("unknown/not set");

  /**
   * The {@link View} associated with this {@link ViewData}.
   */
  abstract View getView();

  /**
   * Record stats with the given tags.
   */
  abstract void record(StatsContextImpl tags, double value);

  /**
   * Convert this {@link MutableViewData} to {@link ViewData}.
   */
  abstract ViewData toViewData(Clock clock);

  private MutableViewData() {
  }

  /**
   * A {@link MutableViewData} for recording stats on distribution-based aggregations.
   */
  static final class MutableDistributionViewData extends MutableViewData {

    /**
     * Constructs a new {@link MutableDistributionViewData}.
     */
    static MutableDistributionViewData create(
        DistributionView distributionView, Timestamp start) {
      return new MutableDistributionViewData(distributionView, start);
    }

    @Override
    View getView() {
      return distributionView;
    }

    @Override
    void record(StatsContextImpl context, double value) {
      Map<TagKey, TagValue> tags = context.tags;
      // TagKeys need to be unique within one view descriptor.
      final List<TagKey> tagKeys = this.distributionView.getDimensions();
      final List<TagValue> tagValues = new ArrayList<TagValue>(tagKeys.size());

      // Record all the measures in a "Greedy" way.
      // Every view aggregates every measure. This is similar to doing a GROUPBY view’s keys.
      for (int i = 0; i < tagKeys.size(); ++i) {
        TagKey tagKey = tagKeys.get(i);
        if (!tags.containsKey(tagKey)) {
          // replace not found key values by “unknown/not set”.
          tagValues.add(UNKNOWN_TAG_VALUE);
        } else {
          tagValues.add(tags.get(tagKey));
        }
      }

      if (!tagValueDistributionMap.containsKey(tagValues)) {
        final List<Double> bucketBoundaries =
            this.distributionView.getDistributionAggregationDescriptor()
                .getBucketBoundaries();
        final MutableDistribution distribution =
            bucketBoundaries == null ? MutableDistribution.create()
                : MutableDistribution.create(BucketBoundaries.create(bucketBoundaries));
        tagValueDistributionMap.put(tagValues, distribution);
      }
      tagValueDistributionMap.get(tagValues).add(value);
    }

    @Override
    final ViewData toViewData(Clock clock) {
      final List<DistributionAggregation> distributionAggregations =
          new ArrayList<DistributionAggregation>();
      for (Entry<List<TagValue>, MutableDistribution> entry : tagValueDistributionMap.entrySet()) {
        MutableDistribution distribution = entry.getValue();
        DistributionAggregation distributionAggregation = distribution.getBucketCounts() == null
            ? DistributionAggregation.create(distribution.getCount(), distribution.getMean(),
            distribution.getSum(), convertRange(distribution.getRange()),
            generateTags(entry.getKey()))
            : DistributionAggregation.create(distribution.getCount(), distribution.getMean(),
                distribution.getSum(), convertRange(distribution.getRange()),
                generateTags(entry.getKey()), distribution.getBucketCounts());
        distributionAggregations.add(distributionAggregation);
      }
      return DistributionViewData.create(distributionView, distributionAggregations, start,
          clock.now());
    }

    /**
     * Returns start timestamp for this aggregation.
     */
    Timestamp getStart() {
      return start;
    }

    private final DistributionView distributionView;
    private final Map<List<TagValue>, MutableDistribution> tagValueDistributionMap =
        new HashMap<List<TagValue>, MutableDistribution>();
    private final Timestamp start;

    private MutableDistributionViewData(
        DistributionView distributionView, Timestamp start) {
      this.distributionView = distributionView;
      this.start = start;
    }

    private final List<Tag> generateTags(List<TagValue> tagValues) {
      final List<Tag> tags = new ArrayList<Tag>(tagValues.size());
      int i = 0;
      for (TagKey tagKey : this.distributionView.getDimensions()) {
        tags.add(Tag.create(tagKey, tagValues.get(i)));
        ++i;
      }
      return tags;
    }

    // TODO(songya): remove DistributionAggregation.Range, then remove this method
    private static final DistributionAggregation.Range convertRange(
        MutableDistribution.Range range) {
      return DistributionAggregation.Range.create(range.getMin(), range.getMax());
    }
  }

  /**
   * A {@link MutableViewData} for recording stats on interval-based aggregations.
   */
  static final class MutableIntervalViewData extends MutableViewData {

    /**
     * Constructs a new {@link MutableIntervalViewData}.
     */
    static MutableIntervalViewData create(IntervalView view) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    View getView() {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    void record(StatsContextImpl tags, double value) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    final ViewData toViewData(Clock clock) {
      throw new UnsupportedOperationException("Not implemented.");
    }
  }
}
