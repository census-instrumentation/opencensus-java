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
import static io.opencensus.implcore.stats.StatsTestUtil.assertAggregationDataEquals;

import com.google.common.collect.ImmutableList;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValueDouble;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValueLong;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSumDouble;
import io.opencensus.implcore.stats.MutableAggregation.MutableSumLong;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.DistributionData.Exemplar;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.AttachmentValue;
import io.opencensus.stats.AttachmentValue.AttachmentValueString;
import io.opencensus.stats.BucketBoundaries;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.implcore.stats.MutableAggregation}. */
@RunWith(JUnit4.class)
public class MutableAggregationTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0));
  private static final BucketBoundaries BUCKET_BOUNDARIES_EMPTY =
      BucketBoundaries.create(Collections.<Double>emptyList());
  private static final Timestamp TIMESTAMP = Timestamp.create(60, 0);
  private static final AttachmentValue ATTACHMENT_VALUE_1 = AttachmentValueString.create("v1");
  private static final AttachmentValue ATTACHMENT_VALUE_2 = AttachmentValueString.create("v2");
  private static final AttachmentValue ATTACHMENT_VALUE_3 = AttachmentValueString.create("v3");
  private static final AttachmentValue ATTACHMENT_VALUE_4 = AttachmentValueString.create("v4");
  private static final AttachmentValue ATTACHMENT_VALUE_5 = AttachmentValueString.create("v5");

  @Test
  public void testCreateEmpty() {
    assertThat(MutableSumDouble.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableSumLong.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableCount.create().getCount()).isEqualTo(0);
    assertThat(MutableMean.create().getMean()).isWithin(TOLERANCE).of(0);
    assertThat(MutableLastValueDouble.create().getLastValue()).isNaN();
    assertThat(MutableLastValueLong.create().getLastValue()).isNaN();

    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));
    MutableDistribution mutableDistribution = MutableDistribution.create(bucketBoundaries);
    assertThat(mutableDistribution.getMean()).isWithin(TOLERANCE).of(0);
    assertThat(mutableDistribution.getCount()).isEqualTo(0);
    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of(0);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[4]);
    assertThat(mutableDistribution.getExemplars()).isEqualTo(new Exemplar[4]);

    MutableDistribution mutableDistributionNoHistogram =
        MutableDistribution.create(BUCKET_BOUNDARIES_EMPTY);
    assertThat(mutableDistributionNoHistogram.getExemplars()).isNull();
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    MutableDistribution.create(null);
  }

  @Test
  public void testNoBoundaries() {
    MutableDistribution noBoundaries =
        MutableDistribution.create(BucketBoundaries.create(Collections.<Double>emptyList()));
    assertThat(noBoundaries.getBucketCounts().length).isEqualTo(1);
    assertThat(noBoundaries.getBucketCounts()[0]).isEqualTo(0);
  }

  @Test
  public void testAdd() {
    List<MutableAggregation> aggregations =
        Arrays.asList(
            MutableSumDouble.create(),
            MutableSumLong.create(),
            MutableCount.create(),
            MutableMean.create(),
            MutableDistribution.create(BUCKET_BOUNDARIES),
            MutableLastValueDouble.create(),
            MutableLastValueLong.create());

    List<Double> values = Arrays.asList(-1.0, 1.0, -5.0, 20.0, 5.0);

    for (double value : values) {
      for (MutableAggregation aggregation : aggregations) {
        aggregation.add(value, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
      }
    }

    assertAggregationDataEquals(
        aggregations.get(0).toAggregationData(),
        AggregationData.SumDataDouble.create(20.0),
        TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(1).toAggregationData(), AggregationData.SumDataLong.create(20), TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(2).toAggregationData(), AggregationData.CountData.create(5), TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(3).toAggregationData(),
        AggregationData.MeanData.create(4.0, 5),
        TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(4).toAggregationData(),
        AggregationData.DistributionData.create(4.0, 5, 372, Arrays.asList(4L, 1L)),
        TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(5).toAggregationData(),
        AggregationData.LastValueDataDouble.create(5.0),
        TOLERANCE);
    assertAggregationDataEquals(
        aggregations.get(6).toAggregationData(),
        AggregationData.LastValueDataLong.create(5),
        TOLERANCE);
  }

  @Test
  public void testAdd_DistributionWithExemplarAttachments() {
    MutableDistribution mutableDistribution = MutableDistribution.create(BUCKET_BOUNDARIES);
    MutableDistribution mutableDistributionNoHistogram =
        MutableDistribution.create(BUCKET_BOUNDARIES_EMPTY);
    List<Double> values = Arrays.asList(-1.0, 1.0, -5.0, 20.0, 5.0);
    List<Map<String, AttachmentValue>> attachmentsList =
        ImmutableList.<Map<String, AttachmentValue>>of(
            Collections.<String, AttachmentValue>singletonMap("k1", ATTACHMENT_VALUE_1),
            Collections.<String, AttachmentValue>singletonMap("k2", ATTACHMENT_VALUE_2),
            Collections.<String, AttachmentValue>singletonMap("k3", ATTACHMENT_VALUE_3),
            Collections.<String, AttachmentValue>singletonMap("k4", ATTACHMENT_VALUE_4),
            Collections.<String, AttachmentValue>singletonMap("k5", ATTACHMENT_VALUE_5));
    List<Timestamp> timestamps =
        Arrays.asList(
            Timestamp.fromMillis(500),
            Timestamp.fromMillis(1000),
            Timestamp.fromMillis(2000),
            Timestamp.fromMillis(3000),
            Timestamp.fromMillis(4000));
    for (int i = 0; i < values.size(); i++) {
      mutableDistribution.add(values.get(i), attachmentsList.get(i), timestamps.get(i));
      mutableDistributionNoHistogram.add(values.get(i), attachmentsList.get(i), timestamps.get(i));
    }

    // Each bucket can only have up to one exemplar. If there are more than one exemplars in a
    // bucket, only the last one will be kept.
    List<Exemplar> expected =
        Arrays.<Exemplar>asList(
            Exemplar.create(values.get(4), timestamps.get(4), attachmentsList.get(4)),
            Exemplar.create(values.get(3), timestamps.get(3), attachmentsList.get(3)));
    assertThat(mutableDistribution.getExemplars())
        .asList()
        .containsExactlyElementsIn(expected)
        .inOrder();
    assertThat(mutableDistributionNoHistogram.getExemplars()).isNull();
  }

  @Test
  public void testCombine_SumCountMean() {
    // combine() for Mutable Sum, Count and Mean will pick up fractional stats
    List<MutableAggregation> aggregations1 =
        Arrays.asList(
            MutableSumDouble.create(),
            MutableSumLong.create(),
            MutableCount.create(),
            MutableMean.create());
    List<MutableAggregation> aggregations2 =
        Arrays.asList(
            MutableSumDouble.create(),
            MutableSumLong.create(),
            MutableCount.create(),
            MutableMean.create());

    for (double val : Arrays.asList(-1.0, -5.0)) {
      for (MutableAggregation aggregation : aggregations1) {
        aggregation.add(val, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
      }
    }
    for (double val : Arrays.asList(10.0, 50.0)) {
      for (MutableAggregation aggregation : aggregations2) {
        aggregation.add(val, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
      }
    }

    List<MutableAggregation> combined =
        Arrays.asList(
            MutableSumDouble.create(),
            MutableSumLong.create(),
            MutableCount.create(),
            MutableMean.create());
    double fraction1 = 1.0;
    double fraction2 = 0.6;
    for (int i = 0; i < combined.size(); i++) {
      combined.get(i).combine(aggregations1.get(i), fraction1);
      combined.get(i).combine(aggregations2.get(i), fraction2);
    }

    assertThat(((MutableSumDouble) combined.get(0)).getSum()).isWithin(TOLERANCE).of(30);
    assertThat(((MutableSumLong) combined.get(1)).getSum()).isWithin(TOLERANCE).of(30);
    assertThat(((MutableCount) combined.get(2)).getCount()).isEqualTo(3);
    assertThat(((MutableMean) combined.get(3)).getMean()).isWithin(TOLERANCE).of(10);
  }

  @Test
  public void testCombine_Distribution() {
    // combine() for Mutable Distribution will ignore fractional stats
    MutableDistribution distribution1 = MutableDistribution.create(BUCKET_BOUNDARIES);
    MutableDistribution distribution2 = MutableDistribution.create(BUCKET_BOUNDARIES);
    MutableDistribution distribution3 = MutableDistribution.create(BUCKET_BOUNDARIES);

    for (double val : Arrays.asList(5.0, -5.0)) {
      distribution1.add(val, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
    }
    for (double val : Arrays.asList(10.0, 20.0)) {
      distribution2.add(val, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
    }
    for (double val : Arrays.asList(-10.0, 15.0, -15.0, -20.0)) {
      distribution3.add(val, Collections.<String, AttachmentValue>emptyMap(), TIMESTAMP);
    }

    MutableDistribution combined = MutableDistribution.create(BUCKET_BOUNDARIES);
    combined.combine(distribution1, 1.0); // distribution1 will be combined
    combined.combine(distribution2, 0.6); // distribution2 will be ignored
    verifyMutableDistribution(combined, 0, 2, 50.0, new long[] {2, 0});

    combined.combine(distribution2, 1.0); // distribution2 will be combined
    verifyMutableDistribution(combined, 7.5, 4, 325.0, new long[] {2, 2});
    combined.combine(distribution3, 1.0); // distribution3 will be combined
    verifyMutableDistribution(combined, 0, 8, 1500.0, new long[] {5, 3});
  }

  @Test
  public void mutableAggregation_ToAggregationData() {
    assertThat(MutableSumDouble.create().toAggregationData()).isEqualTo(SumDataDouble.create(0));
    assertThat(MutableSumLong.create().toAggregationData()).isEqualTo(SumDataLong.create(0));
    assertThat(MutableCount.create().toAggregationData()).isEqualTo(CountData.create(0));
    assertThat(MutableMean.create().toAggregationData()).isEqualTo(MeanData.create(0, 0));
    assertThat(MutableDistribution.create(BUCKET_BOUNDARIES).toAggregationData())
        .isEqualTo(DistributionData.create(0, 0, 0, Arrays.asList(0L, 0L)));
    assertThat(MutableLastValueDouble.create().toAggregationData())
        .isEqualTo(LastValueDataDouble.create(Double.NaN));
    assertThat(MutableLastValueLong.create().toAggregationData())
        .isEqualTo(LastValueDataLong.create(0));
  }

  @Test
  public void mutableAggregation_ToPoint() {
    assertThat(MutableSumDouble.create().toPoint(TIMESTAMP))
        .isEqualTo(Point.create(Value.doubleValue(0), TIMESTAMP));
    assertThat(MutableSumLong.create().toPoint(TIMESTAMP))
        .isEqualTo(Point.create(Value.longValue(0), TIMESTAMP));
    assertThat(MutableCount.create().toPoint(TIMESTAMP))
        .isEqualTo(Point.create(Value.longValue(0), TIMESTAMP));
    assertThat(MutableMean.create().toPoint(TIMESTAMP))
        .isEqualTo(Point.create(Value.doubleValue(0), TIMESTAMP));

    assertThat(MutableDistribution.create(BUCKET_BOUNDARIES).toPoint(TIMESTAMP))
        .isEqualTo(
            Point.create(
                Value.distributionValue(
                    Distribution.create(
                        0,
                        0,
                        0,
                        BucketOptions.explicitOptions(BUCKET_BOUNDARIES.getBoundaries()),
                        Arrays.asList(Bucket.create(0), Bucket.create(0)))),
                TIMESTAMP));
  }

  private static void verifyMutableDistribution(
      MutableDistribution mutableDistribution,
      double mean,
      long count,
      double sumOfSquaredDeviations,
      long[] bucketCounts) {
    assertThat(mutableDistribution.getMean()).isWithin(MutableAggregationTest.TOLERANCE).of(mean);
    assertThat(mutableDistribution.getCount()).isEqualTo(count);
    assertThat(mutableDistribution.getSumOfSquaredDeviations())
        .isWithin(MutableAggregationTest.TOLERANCE)
        .of(sumOfSquaredDeviations);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(bucketCounts);
  }
}
