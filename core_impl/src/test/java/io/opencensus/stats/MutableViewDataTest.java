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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MutableViewData}. */
@RunWith(JUnit4.class)
public class MutableViewDataTest {

  @Test
  public void testConstants() {
    assertThat(MutableViewData.UNKNOWN_TAG_VALUE.asString()).isEqualTo("unknown/not set");
  }

  @Test
  public void testGetTagValues() {
    TagKey originator = TagKey.create("originator");
    TagKey caller = TagKey.create("caller");
    TagKey method = TagKey.create("method");
    TagValue callerV = TagValue.create("some caller");
    TagValue methodV = TagValue.create("some method");
    List<TagKey> columns = Arrays.asList(originator, caller, method);
    Map<TagKey, TagValue> tags = ImmutableMap.of(caller, callerV, method, methodV);

    assertThat(MutableViewData.getTagValues(tags, columns))
        .containsExactly(callerV, methodV, MutableViewData.UNKNOWN_TAG_VALUE);
  }

  @Test
  public void testCreateAggr() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));
    List<Aggregation> aggregations = Arrays.asList(
        Sum.create(),
        Count.create(),
        Histogram.create(bucketBoundaries),
        Range.create(),
        Mean.create(),
        StdDev.create());
    List<MutableAggregation> mutableAggregations = new ArrayList<MutableAggregation>();
    List<AggregationData> aggregates = new ArrayList<AggregationData>();

    for (Aggregation aggregation : aggregations) {
      MutableAggregation mAggregation = MutableViewData.createMutableAggregation(aggregation);
      mutableAggregations.add(mAggregation);
      StatsTestUtil.assertMutableAggregationEquals(mAggregation, 0D, 0L, new long[]{0, 0, 0, 0},
          Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0D, 0D, 1e-6);
    }

    for (MutableAggregation mutableAggregation : mutableAggregations) {
      aggregates.add(MutableViewData.createAggregationData(mutableAggregation));
    }
    assertThat(aggregates).containsExactly(
        SumData.create(0),
        CountData.create(0),
        HistogramData.create(0, 0, 0, 0),
        RangeData.create(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
        MeanData.create(0),
        StdDevData.create(0));
  }
}
