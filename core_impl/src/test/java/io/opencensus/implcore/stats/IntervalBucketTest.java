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

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableHistogram;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link IntervalBucket}. */
@RunWith(JUnit4.class)
public class IntervalBucketTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;
  private static final Duration MINUTE = Duration.create(60, 0);
  private static final Timestamp START = Timestamp.create(60, 0);
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0));
  private static final List<Aggregation> AGGREGATIONS_V1 =
      Collections.unmodifiableList(Arrays.asList(
          Sum.create(), Count.create(), Histogram.create(BUCKET_BOUNDARIES)));

  @Test
  public void preventNullStartTime() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(null, MINUTE, AGGREGATIONS_V1);
  }

  @Test
  public void preventNullDuration() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(START, null, AGGREGATIONS_V1);
  }

  @Test
  public void preventNullAggregationList() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(START, MINUTE, null);
  }
  
  @Test
  public void testGetTagValueAggregationMap_empty() {
    assertThat(new IntervalBucket(START, MINUTE, AGGREGATIONS_V1).getTagValueAggregationMap())
        .isEmpty();
  }

  @Test
  public void testGetStart() {
    assertThat(new IntervalBucket(START, MINUTE, AGGREGATIONS_V1).getStart()).isEqualTo(START);
  }

  @Test
  public void testRecord() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, AGGREGATIONS_V1);
    List<TagValue> tagValues1 = Arrays.<TagValue>asList(TagValueString.create("VALUE1"));
    List<TagValue> tagValues2 = Arrays.<TagValue>asList(TagValueString.create("VALUE2"));
    bucket.record(tagValues1, 5.0);
    bucket.record(tagValues1, 15.0);
    bucket.record(tagValues2, 10.0);
    assertThat(bucket.getTagValueAggregationMap()).containsKey(tagValues1);
    verifyMutableAggregations(bucket.getTagValueAggregationMap().get(tagValues1),
        20.0, 2, new long[]{0, 0, 1, 1}, TOLERANCE);
    assertThat(bucket.getTagValueAggregationMap()).containsKey(tagValues2);
    verifyMutableAggregations(bucket.getTagValueAggregationMap().get(tagValues2),
        10.0, 1, new long[]{0, 0, 0, 1}, TOLERANCE);
  }

  private static void verifyMutableAggregations(
      List<MutableAggregation> aggregations, double sum, long count, long[] bucketCounts,
      double tolerance) {
    assertThat(aggregations).hasSize(3);
    assertThat(aggregations.get(0)).isInstanceOf(MutableSum.class);
    assertThat(((MutableSum) aggregations.get(0)).getSum()).isWithin(tolerance).of(sum);
    assertThat(aggregations.get(1)).isInstanceOf(MutableCount.class);
    assertThat(((MutableCount) aggregations.get(1)).getCount()).isEqualTo(count);
    assertThat(aggregations.get(2)).isInstanceOf(MutableHistogram.class);
    assertThat(((MutableHistogram) aggregations.get(2)).getBucketCounts()).isEqualTo(bucketCounts);
  }

  @Test
  public void testGetFraction() {
    Timestamp thirtySecondsAfterStart = Timestamp.create(90, 0);
    assertThat(
        new IntervalBucket(START, MINUTE, AGGREGATIONS_V1).getFraction(thirtySecondsAfterStart))
        .isWithin(TOLERANCE).of(0.5);
  }

  @Test
  public void preventCallingGetFractionOnPastBuckets() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, AGGREGATIONS_V1);
    Timestamp twoMinutesAfterStart = Timestamp.create(180, 0);
    thrown.expect(IllegalArgumentException.class);
    bucket.getFraction(twoMinutesAfterStart);
  }

  @Test
  public void preventCallingGetFractionOnFutureBuckets() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, AGGREGATIONS_V1);
    Timestamp thirtySecondsBeforeStart = Timestamp.create(30, 0);
    thrown.expect(IllegalArgumentException.class);
    bucket.getFraction(thirtySecondsBeforeStart);
  }
}
