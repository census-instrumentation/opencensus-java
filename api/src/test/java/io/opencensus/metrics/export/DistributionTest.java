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

package io.opencensus.metrics.export;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Distribution.Exemplar;
import io.opencensus.metrics.export.Distribution.Explicit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Distribution}. */
@RunWith(JUnit4.class)
public class DistributionTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Timestamp TIMESTAMP = Timestamp.create(1, 0);
  private static final Map<String, String> ATTACHMENTS = Collections.singletonMap("key", "value");
  private static final double TOLERANCE = 1e-6;

  @Test
  public void createAndGet_Bucket() {
    Bucket bucket = Bucket.create(98);
    assertThat(bucket.getCount()).isEqualTo(98);
    assertThat(bucket.getExemplar()).isNull();
  }

  @Test
  public void createAndGet_BucketWithExemplar() {
    Exemplar exemplar = Exemplar.create(12.2, TIMESTAMP, ATTACHMENTS);
    Bucket bucket = Bucket.create(7, exemplar);
    assertThat(bucket.getCount()).isEqualTo(7);
    assertThat(bucket.getExemplar()).isEqualTo(exemplar);
  }

  @Test
  public void createBucket_preventNullExemplar() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exemplar");
    Bucket.create(1, null);
  }

  @Test
  public void createAndGet_Exemplar() {
    Exemplar exemplar = Exemplar.create(-9.9, TIMESTAMP, ATTACHMENTS);
    assertThat(exemplar.getValue()).isWithin(TOLERANCE).of(-9.9);
    assertThat(exemplar.getTimestamp()).isEqualTo(TIMESTAMP);
    assertThat(exemplar.getAttachments()).isEqualTo(ATTACHMENTS);
  }

  @Test
  public void createAndGet_ExplicitBuckets() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 3.0);
    Explicit explicitBuckets = Explicit.create(bucketBounds);
    assertThat(explicitBuckets.getBucketBoundaries())
        .containsExactlyElementsIn(bucketBounds)
        .inOrder();
  }

  @Test
  public void createAndGet_PreventNullExplicitBuckets() {
    thrown.expect(NullPointerException.class);
    Explicit.create(Arrays.asList(1.0, null, 3.0));
  }

  @Test
  public void createBucketOptions_UnorderedBucketBounds() {
    List<Double> bucketBounds = Arrays.asList(1.0, 5.0, 2.0);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("bucket boundaries not sorted.");
    BucketOptions.create(Explicit.create(bucketBounds));
  }

  @Test
  public void createAndGet_Distribution() {
    Exemplar exemplar = Exemplar.create(15.0, TIMESTAMP, ATTACHMENTS);
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    Explicit explicitBuckets = Explicit.create(bucketBounds);
    BucketOptions bucketOptions = BucketOptions.create(explicitBuckets);
    List<Bucket> buckets =
        Arrays.asList(
            Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4, exemplar));
    Distribution distribution = Distribution.create(10, 6.6, 678.54, bucketOptions, buckets);
    assertThat(distribution.getCount()).isEqualTo(10);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(6.6);
    assertThat(distribution.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of(678.54);
    assertThat(distribution.getBucketOptions().getExplicitBuckets().getBucketBoundaries())
        .containsExactlyElementsIn(bucketBounds)
        .inOrder();
    assertThat(distribution.getBuckets()).containsExactlyElementsIn(buckets).inOrder();
  }

  @Test
  public void createBucket_NegativeCount() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("bucket count should be non-negative.");
    Bucket.create(-5);
  }

  @Test
  public void createExemplar_PreventNullAttachments() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("attachments");
    Exemplar.create(15, TIMESTAMP, null);
  }

  @Test
  public void createExemplar_PreventNullAttachmentKey() {
    Map<String, String> attachments = Collections.singletonMap(null, "value");
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key of attachment");
    Exemplar.create(15, TIMESTAMP, attachments);
  }

  @Test
  public void createExemplar_PreventNullAttachmentValue() {
    Map<String, String> attachments = Collections.singletonMap("key", null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value of attachment");
    Exemplar.create(15, TIMESTAMP, attachments);
  }

  @Test
  public void createDistribution_NegativeCount() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));

    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("count should be non-negative.");
    Distribution.create(-10, 6.6, 678.54, bucketOptions, buckets);
  }

  @Test
  public void createDistribution_NegativeSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));

    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be non-negative.");
    Distribution.create(0, 6.6, -678.54, bucketOptions, buckets);
  }

  @Test
  public void createDistribution_ZeroCountAndPositiveMean() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));

    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum should be 0 if count is 0.");
    Distribution.create(0, 6.6, 0, bucketOptions, buckets);
  }

  @Test
  public void createDistribution_ZeroCountAndSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be 0 if count is 0.");
    Distribution.create(0, 0, 678.54, bucketOptions, buckets);
  }

  @Test
  public void createDistribution_NullBucketBounds() {
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries list should not be null.");
    Distribution.create(10, 6.6, 678.54, BucketOptions.create(Explicit.create(null)), buckets);
  }

  @Test
  public void createDistribution_NullBucketList() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket list should not be null.");
    Distribution.create(10, 6.6, 678.54, bucketOptions, null);
  }

  @Test
  public void createDistribution_NullBucket() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 5.0);
    BucketOptions bucketOptions = BucketOptions.create(Explicit.create(bucketBounds));
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), null, Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket should not be null.");
    Distribution.create(10, 6.6, 678.54, bucketOptions, buckets);
  }

  @Test
  public void testEquals() {
    List<Double> bucketBounds = Arrays.asList(1.0, 2.0, 2.5);
    new EqualsTester()
        .addEqualityGroup(
            Distribution.create(
                10,
                10,
                1,
                BucketOptions.create(Explicit.create(bucketBounds)),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))),
            Distribution.create(
                10,
                10,
                1,
                BucketOptions.create(Explicit.create(bucketBounds)),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))))
        .addEqualityGroup(
            Distribution.create(
                7,
                10,
                23.456,
                BucketOptions.create(Explicit.create(bucketBounds)),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))))
        .testEquals();
  }
}
