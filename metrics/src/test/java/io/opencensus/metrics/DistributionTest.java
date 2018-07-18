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

package io.opencensus.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.Distribution.Bucket;
import io.opencensus.metrics.Distribution.Exemplar;
import io.opencensus.metrics.Distribution.Range;
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

  private static final List<Bucket> EMPTY_BUCKET_LIST = Collections.<Bucket>emptyList();
  private static final List<Exemplar> EMPTY_EXEMPLAR_LIST = Collections.<Exemplar>emptyList();
  private static final Timestamp TIMESTAMP_1 = Timestamp.create(1, 0);
  private static final Timestamp TIMESTAMP_2 = Timestamp.create(2, 0);
  private static final Map<String, String> ATTACHMENTS = Collections.singletonMap("key", "value");

  @Test
  public void createAndGet_Range() {
    Range range = Range.create(-5.5, 6.6);
    assertThat(range.getMax()).isEqualTo(6.6);
    assertThat(range.getMin()).isEqualTo(-5.5);
  }

  @Test
  public void createAndGet_Bucket() {
    Bucket bucket = Bucket.create(98);
    assertThat(bucket.getCount()).isEqualTo(98);
  }

  @Test
  public void createAndGet_Exemplar() {
    Exemplar exemplar = Exemplar.create(-9.9, TIMESTAMP_1, ATTACHMENTS);
    assertThat(exemplar.getValue()).isEqualTo(-9.9);
    assertThat(exemplar.getTimestamp()).isEqualTo(TIMESTAMP_1);
    assertThat(exemplar.getAttachments()).isEqualTo(ATTACHMENTS);
  }

  @Test
  public void createAndGet_Distribution() {
    Range range = Range.create(-3.4, 5.6);
    Exemplar exemplar = Exemplar.create(15.0, TIMESTAMP_1, ATTACHMENTS);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    Distribution distribution =
        Distribution.create(
            6.6, 10, 678.54, range, bucketBounds, buckets, Collections.singletonList(exemplar));
    assertThat(distribution.getMean()).isEqualTo(6.6);
    assertThat(distribution.getCount()).isEqualTo(10);
    assertThat(distribution.getSumOfSquaredDeviations()).isEqualTo(678.54);
    assertThat(distribution.getRange()).isEqualTo(range);
    assertThat(distribution.getBucketBoundaries())
        .containsExactlyElementsIn(bucketBounds)
        .inOrder();
    assertThat(distribution.getBuckets()).containsExactlyElementsIn(buckets).inOrder();
    assertThat(distribution.getExemplars()).containsExactly(exemplar);
  }

  @Test
  public void createRange_MinGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    Range.create(1.0, -1.0);
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
    Exemplar.create(15, TIMESTAMP_1, null);
  }

  @Test
  public void createExemplar_PreventNullAttachmentKey() {
    Map<String, String> attachments = Collections.singletonMap(null, "value");
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key of attachment");
    Exemplar.create(15, TIMESTAMP_1, attachments);
  }

  @Test
  public void createExemplar_PreventNullAttachmentValue() {
    Map<String, String> attachments = Collections.singletonMap("key", null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value of attachment");
    Exemplar.create(15, TIMESTAMP_1, attachments);
  }

  @Test
  public void createDistribution_NegativeCount() {
    Range range = Range.create(-3.4, 5.6);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("count should be non-negative.");
    Distribution.create(6.6, -10, 678.54, range, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_NegativeSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be non-negative.");
    Distribution.create(6.6, 0, -678.54, null, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_ZeroCountAndNonNullRange() {
    Range range = Range.create(-3.4, 5.6);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("range should not be present if count is 0.");
    Distribution.create(0, 0, 0, range, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_ZeroCountAndPositiveMean() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("mean should be 0 if count is 0.");
    Distribution.create(6.6, 0, 0, null, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_ZeroCountAndSumOfSquaredDeviations() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(0), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum of squared deviations should be 0 if count is 0.");
    Distribution.create(0, 0, 678.54, null, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_PositiveCountAndNullRange() {
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(0), Bucket.create(1), Bucket.create(0), Bucket.create(0));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("range should be present if count is not 0.");
    Distribution.create(6.6, 1, 0, null, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_NullBucketBounds() {
    Range range = Range.create(-3.4, 5.6);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries list should not be null.");
    Distribution.create(6.6, 10, 678.54, range, null, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_UnorderedBucketBounds() {
    Range range = Range.create(-3.4, 5.6);
    List<Double> bucketBounds = Arrays.asList(0.0, -1.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("bucket boundaries not sorted.");
    Distribution.create(6.6, 10, 678.54, range, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_NullBucketList() {
    Range range = Range.create(-3.4, 5.6);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket list should not be null.");
    Distribution.create(6.6, 10, 678.54, range, bucketBounds, null, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void createDistribution_NullBucket() {
    Range range = Range.create(-3.4, 5.6);
    List<Double> bucketBounds = Arrays.asList(-1.0, 0.0, 1.0);
    List<Bucket> buckets =
        Arrays.asList(Bucket.create(3), Bucket.create(1), null, Bucket.create(4));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket should not be null.");
    Distribution.create(6.6, 10, 678.54, range, bucketBounds, buckets, EMPTY_EXEMPLAR_LIST);
  }

  @Test
  public void preventNullExemplarList() {
    Range range = Range.create(1, 1);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exemplar list should not be null.");
    Distribution.create(1, 1, 1, range, Collections.<Double>emptyList(), EMPTY_BUCKET_LIST, null);
  }

  @Test
  public void preventNullExemplar() {
    Range range = Range.create(1, 1);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exemplar should not be null.");
    Distribution.create(
        1,
        1,
        1,
        range,
        Collections.<Double>emptyList(),
        EMPTY_BUCKET_LIST,
        Collections.<Exemplar>singletonList(null));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Distribution.create(
                10,
                10,
                1,
                Range.create(1, 5),
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)),
                EMPTY_EXEMPLAR_LIST),
            Distribution.create(
                10,
                10,
                1,
                Range.create(1, 5),
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)),
                EMPTY_EXEMPLAR_LIST))
        .addEqualityGroup(
            Distribution.create(
                -7,
                10,
                23.456,
                Range.create(-19.1, 19.2),
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)),
                EMPTY_EXEMPLAR_LIST))
        .addEqualityGroup(
            Distribution.create(
                -7,
                10,
                23.456,
                Range.create(-19.1, 19.2),
                Arrays.asList(-5.0, 0.0, 5.0),
                Arrays.asList(
                    Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)),
                Collections.singletonList(Exemplar.create(1.0, TIMESTAMP_2, ATTACHMENTS))))
        .testEquals();
  }
}
