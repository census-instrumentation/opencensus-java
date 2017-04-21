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

import com.google.common.annotations.VisibleForTesting;
import com.google.instrumentation.common.Function;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A mutable version of {@link View}, used for recording stats and start/end time.
 */
// TODO(songya): remove or modify the methods of this class, since it's not part of the API.
abstract class MutableView {
  @VisibleForTesting static final TagValue UNKNOWN_TAG_VALUE = TagValue.create("unknown/not set");

  /**
   * The {@link ViewDescriptor} associated with this {@link View}.
   */
  abstract ViewDescriptor getViewDescriptor();

  /**
   * Applies the given match function to the underlying data type.
   */
  abstract <T> T match(
      Function<MutableDistributionView, T> p0,
      Function<MutableIntervalView, T> p1);

  /**
   * Record stats with the given tags.
   */
  abstract void record(StatsContextImpl tags, double value);

  /**
   * Convert this {@link MutableView} to {@link View}.
   */
  abstract View toView();

  private MutableView() {
  }

  /**
   * A {@link MutableView} for recording stats on distribution-based aggregations.
   */
  static final class MutableDistributionView extends MutableView {

    /**
     * Constructs a new {@link MutableDistributionView}.
     */
    static MutableDistributionView create(
        DistributionViewDescriptor distributionViewDescriptor, Timestamp start) {
      return new MutableDistributionView(distributionViewDescriptor, start);
    }

    @Override
    ViewDescriptor getViewDescriptor() {
      return distributionViewDescriptor;
    }

    @Override
    <T> T match(
        Function<MutableDistributionView, T> p0, Function<MutableIntervalView, T> p1) {
      return p0.apply(this);
    }

    @Override
    void record(StatsContextImpl context, double value) {
      Map<TagKey, TagValue> tags = context.tags;
      // TagKeys need to be unique within one view descriptor.
      final List<TagKey> tagKeys = this.distributionViewDescriptor.getTagKeys();
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
            this.distributionViewDescriptor.getDistributionAggregationDescriptor()
                .getBucketBoundaries();
        final MutableDistribution distribution =
            bucketBoundaries == null ? MutableDistribution.create()
                : MutableDistribution.create(BucketBoundaries.create(bucketBoundaries));
        tagValueDistributionMap.put(tagValues, distribution);
      }
      tagValueDistributionMap.get(tagValues).add(value);
    }

    @Override
    final View toView() {
      final List<DistributionAggregation> distributionAggregations =
          new ArrayList<DistributionAggregation>();
      for (Entry<List<TagValue>, MutableDistribution> entry : tagValueDistributionMap.entrySet()) {
        MutableDistribution distribution = entry.getValue();
        distributionAggregations.add(
            DistributionAggregation.create(distribution.getCount(), distribution.getMean(),
                distribution.getSum(), convertRange(distribution.getRange()),
                generateTags(entry.getKey()), distribution.getBucketCounts()));
      }
      return DistributionView.create(distributionViewDescriptor, distributionAggregations, start,
          Timestamp.fromMillis(System.currentTimeMillis()));
    }

    /**
     * Returns start timestamp for this aggregation.
     */
    Timestamp getStart() {
      return start;
    }

    private final DistributionViewDescriptor distributionViewDescriptor;
    private final Map<List<TagValue>, MutableDistribution> tagValueDistributionMap =
            new HashMap<List<TagValue>, MutableDistribution>();
    private final Timestamp start;

    private MutableDistributionView(
        DistributionViewDescriptor distributionViewDescriptor, Timestamp start) {
      this.distributionViewDescriptor = distributionViewDescriptor;
      this.start = start;
    }

    private final List<Tag> generateTags(List<TagValue> tagValues) {
      final List<Tag> tags = new ArrayList<Tag>(tagValues.size());
      int i = 0;
      for (TagKey tagKey : this.distributionViewDescriptor.getTagKeys()) {
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
   * A {@link MutableView} for recording stats on interval-based aggregations.
   */
  static final class MutableIntervalView extends MutableView {

    /**
     * Constructs a new {@link MutableIntervalView}.
     */
    static MutableIntervalView create(IntervalViewDescriptor viewDescriptor) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    ViewDescriptor getViewDescriptor() {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    <T> T match(
        Function<MutableDistributionView, T> p0, Function<MutableIntervalView, T> p1) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    void record(StatsContextImpl tags, double value) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    final View toView() {
      throw new UnsupportedOperationException("Not implemented.");
    }
  }
}
