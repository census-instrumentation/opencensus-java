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

package io.opencensus.impl.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opencensus.impl.stats.MutableAggregation.MutableCount;
import io.opencensus.impl.stats.MutableAggregation.MutableHistogram;
import io.opencensus.impl.stats.MutableAggregation.MutableMean;
import io.opencensus.impl.stats.MutableAggregation.MutableRange;
import io.opencensus.impl.stats.MutableAggregation.MutableStdDev;
import io.opencensus.impl.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.UnreleasedApiAccessor;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
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

  private static final double EPSILON = 1e-7;

  private static final TagKeyString ORIGINATOR = TagKeyString.create("originator");
  private static final TagKeyString CALLER = TagKeyString.create("caller");
  private static final TagKeyString METHOD = TagKeyString.create("method");
  private static final TagValueString CALLER_V = TagValueString.create("some caller");
  private static final TagValueString METHOD_V = TagValueString.create("some method");

  @Test
  public void testConstants() {
    assertThat(MutableViewData.UNKNOWN_TAG_VALUE).isNull();
  }

  @Test
  public void testGetTagValues() {
    List<TagKeyString> columns = Arrays.asList(CALLER, METHOD, ORIGINATOR);
    Map<TagKeyString, TagValueString> tags =
        ImmutableMap.of(CALLER, CALLER_V, METHOD, METHOD_V);

    assertThat(MutableViewData.getTagValues(tags, columns))
        .containsExactly(CALLER_V, METHOD_V, MutableViewData.UNKNOWN_TAG_VALUE)
        .inOrder();
  }

  @Test
  public void createMutableAggregation() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));

    assertThat(
        ((MutableSum) MutableViewData.createMutableAggregation(Sum.create())).getSum())
        .isWithin(EPSILON).of(0.0);
    assertThat(
        ((MutableCount) MutableViewData.createMutableAggregation(Count.create())).getCount())
        .isEqualTo(0L);
    assertThat(
        ((MutableHistogram) MutableViewData.createMutableAggregation(
            Histogram.create(bucketBoundaries))).getBucketCounts())
        .isEqualTo(new long[]{0, 0, 0, 0});
    assertThat(
        ((MutableRange) MutableViewData
            .createMutableAggregation(UnreleasedApiAccessor.createRange())).getMin())
        .isPositiveInfinity();
    assertThat(
        ((MutableRange) MutableViewData
            .createMutableAggregation(UnreleasedApiAccessor.createRange())).getMax())
        .isNegativeInfinity();
    assertThat(
        ((MutableMean) MutableViewData.createMutableAggregation(UnreleasedApiAccessor.createMean()))
            .getMean())
        .isWithin(EPSILON).of(0D);
    assertThat(
        ((MutableStdDev) MutableViewData
            .createMutableAggregation(UnreleasedApiAccessor.createStdDev())).getStdDev())
        .isWithin(EPSILON).of(0D);
  }

  @Test
  public void createAggregationData() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));
    List<MutableAggregation> mutableAggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableHistogram.create(bucketBoundaries),
        MutableRange.create(),
        MutableMean.create(),
        MutableStdDev.create());
    List<AggregationData> aggregates = new ArrayList<AggregationData>();
    for (MutableAggregation mutableAggregation : mutableAggregations) {
      aggregates.add(MutableViewData.createAggregationData(mutableAggregation));
    }
    assertThat(aggregates).containsExactly(
        SumData.create(0),
        CountData.create(0),
        HistogramData.create(0, 0, 0, 0),
        RangeData.create(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
        MeanData.create(0),
        StdDevData.create(0))
        .inOrder();
  }
}
