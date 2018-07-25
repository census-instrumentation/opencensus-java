/*
 * Copyright 2018, OpenCensus Authors
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

import com.google.common.collect.ImmutableMap;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValue;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link RecordUtils}. */
@RunWith(JUnit4.class)
public class RecordUtilsTest {

  private static final double EPSILON = 1e-7;
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure1", "description", "1");
  private static final MeasureLong MEASURE_LONG =
      MeasureLong.create("measure2", "description", "1");
  private static final TagKey ORIGINATOR = TagKey.create("originator");
  private static final TagKey CALLER = TagKey.create("caller");
  private static final TagKey METHOD = TagKey.create("method");
  private static final TagValue CALLER_V = TagValue.create("some caller");
  private static final TagValue METHOD_V = TagValue.create("some method");

  @Test
  public void testConstants() {
    assertThat(RecordUtils.UNKNOWN_TAG_VALUE).isNull();
  }

  @Test
  public void testGetTagValues() {
    List<TagKey> columns = Arrays.asList(CALLER, METHOD, ORIGINATOR);
    Map<TagKey, TagValue> tags = ImmutableMap.of(CALLER, CALLER_V, METHOD, METHOD_V);

    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(CALLER_V, METHOD_V, RecordUtils.UNKNOWN_TAG_VALUE)
        .inOrder();
  }

  @Test
  public void createMutableAggregation() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));

    assertThat(((MutableSum) RecordUtils.createMutableAggregation(Sum.create())).getSum())
        .isWithin(EPSILON)
        .of(0.0);
    assertThat(((MutableCount) RecordUtils.createMutableAggregation(Count.create())).getCount())
        .isEqualTo(0L);
    assertThat(((MutableMean) RecordUtils.createMutableAggregation(Mean.create())).getMean())
        .isWithin(EPSILON)
        .of(0D);
    assertThat(
            ((MutableLastValue) RecordUtils.createMutableAggregation(LastValue.create()))
                .getLastValue())
        .isNaN();

    MutableDistribution mutableDistribution =
        (MutableDistribution)
            RecordUtils.createMutableAggregation(Distribution.create(bucketBoundaries));
    assertThat(mutableDistribution.getMin()).isPositiveInfinity();
    assertThat(mutableDistribution.getMax()).isNegativeInfinity();
    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(EPSILON).of(0);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[4]);
  }

  @Test
  public void createAggregationData() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));
    List<AggregationData> aggregates = new ArrayList<AggregationData>();
    aggregates.add(RecordUtils.createAggregationData(MutableSum.create(), MEASURE_DOUBLE));
    aggregates.add(RecordUtils.createAggregationData(MutableSum.create(), MEASURE_LONG));
    aggregates.add(RecordUtils.createAggregationData(MutableLastValue.create(), MEASURE_DOUBLE));
    aggregates.add(RecordUtils.createAggregationData(MutableLastValue.create(), MEASURE_LONG));

    List<MutableAggregation> mutableAggregations =
        Arrays.asList(
            MutableCount.create(),
            MutableMean.create(),
            MutableDistribution.create(bucketBoundaries));
    for (MutableAggregation mutableAggregation : mutableAggregations) {
      aggregates.add(RecordUtils.createAggregationData(mutableAggregation, MEASURE_DOUBLE));
    }

    assertThat(aggregates)
        .containsExactly(
            SumDataDouble.create(0),
            SumDataLong.create(0),
            LastValueDataDouble.create(Double.NaN),
            LastValueDataLong.create(0),
            CountData.create(0),
            MeanData.create(0, 0),
            DistributionData.create(
                0,
                0,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                0,
                Arrays.asList(0L, 0L, 0L, 0L)))
        .inOrder();
  }
}
