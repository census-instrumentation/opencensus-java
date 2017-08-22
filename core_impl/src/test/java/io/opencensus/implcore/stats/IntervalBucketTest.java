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
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
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
  public void testGetTagValueAggregationMap() {
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
    List<TagValue> tagValues = Arrays.<TagValue>asList(TagValueString.create("VALUE"));
    bucket.record(tagValues, 5.0);
    bucket.record(tagValues, 15.0);
    assertThat(bucket.getTagValueAggregationMap()).containsKey(tagValues);
    for (MutableAggregation aggregation : bucket.getTagValueAggregationMap().get(tagValues)) {
      aggregation.match(
          new Function<MutableSum, Void>() {
            @Override
            public Void apply(MutableSum arg) {
              assertThat(arg.getSum()).isWithin(TOLERANCE).of(20.0);
              return null;
            }
          },
          new Function<MutableCount, Void>() {
            @Override
            public Void apply(MutableCount arg) {
              assertThat(arg.getCount()).isEqualTo(2);
              return null;
            }
          },
          new Function<MutableHistogram, Void>() {
            @Override
            public Void apply(MutableHistogram arg) {
              assertThat(arg.getBucketCounts()).isEqualTo(new long[]{0, 0, 1, 1});
              return null;
            }
          },
          Functions.<Void>throwAssertionError(),
          Functions.<Void>throwAssertionError(),
          Functions.<Void>throwAssertionError());
    }
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
}
