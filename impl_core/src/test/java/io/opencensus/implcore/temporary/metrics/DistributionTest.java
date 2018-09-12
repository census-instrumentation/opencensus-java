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

package io.opencensus.implcore.temporary.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.temporary.metrics.Distribution.Bucket;
import io.opencensus.implcore.temporary.metrics.Distribution.Exemplar;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Value}. */
@RunWith(JUnit4.class)
public class DistributionTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Timestamp TIMESTAMP = Timestamp.create(1, 0);
  private static final Map<String, String> ATTACHMENTS = Collections.singletonMap("key", "value");

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
    assertThat(exemplar.getValue()).isEqualTo(-9.9);
    assertThat(exemplar.getTimestamp()).isEqualTo(TIMESTAMP);
    assertThat(exemplar.getAttachments()).isEqualTo(ATTACHMENTS);
  }

  @Test
  public void createAndGet_Distribution() {
    Exemplar exemplar = Exemplar.create(15.0, TIMESTAMP, ATTACHMENTS);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(
            Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4, exemplar));
    Distribution distribution = Distribution.create(6.6, 10, 678.54, bucketBounds, buckets);
    assertThat(distribution.getMean()).isEqualTo(6.6);
    assertThat(distribution.getCount()).isEqualTo(10);
    assertThat(distribution.getSumOfSquaredDeviations()).isEqualTo(678.54);
    assertThat(distribution.getBucketBoundaries())
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
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("count should be non-negative.");
    Distribution.create(6.6, -10, 678.54, bucketBounds, buckets);
  }

  @Test
  public void createDistribution_NegativeSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be non-negative.");
    Distribution.create(6.6, 0, -678.54, bucketBounds, buckets);
  }

  @Test
  public void createDistribution_ZeroCountAndPositiveMean() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("mean should be 0 if count is 0.");
    Distribution.create(6.6, 0, 0, bucketBounds, buckets);
  }

  @Test
  public void createDistribution_ZeroCountAndSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be 0 if count is 0.");
    Distribution.create(0, 0, 678.54, bucketBounds, buckets);
  }

  @Test
  public void createDistribution_NullBucketBounds() {
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries list should not be null.");
    Distribution.create(6.6, 10, 678.54, null, buckets);
  }

  @Test
  public void createDistribution_UnorderedBucketBounds() {
    List<Double> bucketBounds = Arrays.asList(0.0, -1.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("bucket boundaries not sorted.");
    Distribution.create(6.6, 10, 678.54, bucketBounds, buckets);
  }

  @Test
  public void createDistribution_NullBucketList() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket list should not be null.");
    Distribution.create(6.6, 10, 678.54, bucketBounds, null);
  }

  @Test
  public void createDistribution_NullBucket() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), null, Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket should not be null.");
    Distribution.create(6.6, 10, 678.54, bucketBounds, buckets);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Distribution.create(
                10,
                10,
                1,
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))),
            Distribution.create(
                10,
                10,
                1,
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))))
        .addEqualityGroup(
            Distribution.create(
                -7,
                10,
                23.456,
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))))
        .testEquals();
  }
}
