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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.stats.MutableViewData.toMillis;

import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Duration;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
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
        ((MutableMean) MutableViewData.createMutableAggregation(Mean.create()))
            .getMean())
        .isWithin(EPSILON).of(0D);

    MutableDistribution mutableDistribution = (MutableDistribution) MutableViewData
        .createMutableAggregation(Distribution.create(bucketBoundaries));
    assertThat(mutableDistribution.getMin()).isPositiveInfinity();
    assertThat(mutableDistribution.getMax()).isNegativeInfinity();
    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(EPSILON).of(0);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[4]);
  }

  @Test
  public void createAggregationData() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));
    List<MutableAggregation> mutableAggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(bucketBoundaries));
    List<AggregationData> aggregates = new ArrayList<AggregationData>();
    for (MutableAggregation mutableAggregation : mutableAggregations) {
      aggregates.add(MutableViewData.createAggregationData(mutableAggregation));
    }
    assertThat(aggregates).containsExactly(
        SumData.create(0),
        CountData.create(0),
        MeanData.create(0, 0),
        DistributionData.create(
            0, 0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0, new long[4]))
        .inOrder();
  }

  @Test
  public void testDurationToMillis() {
    assertThat(toMillis(Duration.create(0, 0))).isEqualTo(0);
    assertThat(toMillis(Duration.create(0, 987000000))).isEqualTo(987);
    assertThat(toMillis(Duration.create(3, 456000000))).isEqualTo(3456);
    assertThat(toMillis(Duration.create(0, -1000000))).isEqualTo(-1);
    assertThat(toMillis(Duration.create(-1, 0))).isEqualTo(-1000);
    assertThat(toMillis(Duration.create(-3, -456000000))).isEqualTo(-3456);
  }
}
