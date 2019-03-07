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

import io.opencensus.common.AttachmentValue;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.tags.TagValue;
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
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure1", "description", "1");
  private static final Duration MINUTE = Duration.create(60, 0);
  private static final Duration NEGATIVE_TEN_SEC = Duration.create(-10, 0);
  private static final Timestamp START = Timestamp.create(60, 0);
  private static final Mean MEAN = Mean.create();

  @Test
  public void preventNullStartTime() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(null, MINUTE, MEAN, MEASURE_DOUBLE);
  }

  @Test
  public void preventNullDuration() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(START, null, MEAN, MEASURE_DOUBLE);
  }

  @Test
  public void preventNegativeDuration() {
    thrown.expect(IllegalArgumentException.class);
    new IntervalBucket(START, NEGATIVE_TEN_SEC, MEAN, MEASURE_DOUBLE);
  }

  @Test
  public void preventNullAggregation() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(START, MINUTE, null, MEASURE_DOUBLE);
  }

  @Test
  public void preventNullMeasure() {
    thrown.expect(NullPointerException.class);
    new IntervalBucket(START, MINUTE, MEAN, null);
  }

  @Test
  public void testGetTagValueAggregationMap_empty() {
    assertThat(new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE).getTagValueAggregationMap())
        .isEmpty();
  }

  @Test
  public void testGetStart() {
    assertThat(new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE).getStart()).isEqualTo(START);
  }

  @Test
  public void testRecord() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE);
    List<TagValue> tagValues1 = Arrays.<TagValue>asList(TagValue.create("VALUE1"));
    List<TagValue> tagValues2 = Arrays.<TagValue>asList(TagValue.create("VALUE2"));
    bucket.record(tagValues1, 5.0, Collections.<String, AttachmentValue>emptyMap(), START);
    bucket.record(tagValues1, 15.0, Collections.<String, AttachmentValue>emptyMap(), START);
    bucket.record(tagValues2, 10.0, Collections.<String, AttachmentValue>emptyMap(), START);
    assertThat(bucket.getTagValueAggregationMap().keySet()).containsExactly(tagValues1, tagValues2);
    MutableMean mutableMean1 = (MutableMean) bucket.getTagValueAggregationMap().get(tagValues1);
    MutableMean mutableMean2 = (MutableMean) bucket.getTagValueAggregationMap().get(tagValues2);
    assertThat(mutableMean1.getSum()).isWithin(TOLERANCE).of(20);
    assertThat(mutableMean2.getSum()).isWithin(TOLERANCE).of(10);
    assertThat(mutableMean1.getCount()).isEqualTo(2);
    assertThat(mutableMean2.getCount()).isEqualTo(1);
  }

  @Test
  public void testGetFraction() {
    Timestamp thirtySecondsAfterStart = Timestamp.create(90, 0);
    assertThat(
            new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE)
                .getFraction(thirtySecondsAfterStart))
        .isWithin(TOLERANCE)
        .of(0.5);
  }

  @Test
  public void preventCallingGetFractionOnPastBuckets() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE);
    Timestamp twoMinutesAfterStart = Timestamp.create(180, 0);
    thrown.expect(IllegalArgumentException.class);
    bucket.getFraction(twoMinutesAfterStart);
  }

  @Test
  public void preventCallingGetFractionOnFutureBuckets() {
    IntervalBucket bucket = new IntervalBucket(START, MINUTE, MEAN, MEASURE_DOUBLE);
    Timestamp thirtySecondsBeforeStart = Timestamp.create(30, 0);
    thrown.expect(IllegalArgumentException.class);
    bucket.getFraction(thirtySecondsBeforeStart);
  }
}
