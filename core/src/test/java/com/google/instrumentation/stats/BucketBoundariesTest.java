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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link com.google.instrumentation.stats.BucketBoundaries}.
 */
@RunWith(JUnit4.class)
public class BucketBoundariesTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstructBoundaries() {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 2.0);
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(buckets);
    assertThat(bucketBoundaries.getBoundaries()).isEqualTo(buckets);
  }

  @Test
  public void testBoundariesDoesNotChangeWithOriginalList() {
    List<Double> original = new ArrayList<Double>();
    original.add(0.0);
    original.add(1.0);
    original.add(2.0);
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(original);
    original.set(2, 3.0);
    original.add(4.0);
    List<Double> expected = Arrays.asList(0.0, 1.0, 2.0);
    assertThat(bucketBoundaries.getBoundaries()).isNotEqualTo(original);
    assertThat(bucketBoundaries.getBoundaries()).isEqualTo(expected);
  }

  @Test
  public void testNullBoundaries() throws Exception {
    thrown.expect(NullPointerException.class);
    BucketBoundaries.create(null);
  }

  @Test
  public void testUnsortedBoundaries() throws Exception {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 1.0);
    thrown.expect(IllegalArgumentException.class);
    BucketBoundaries.create(buckets);
  }

  @Test
  public void testNoBoundaries() {
    List<Double> buckets = Arrays.asList();
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(buckets);
    assertThat(bucketBoundaries.getBoundaries()).isEqualTo(buckets);
  }

  @Test
  public void testBucketBoundariesEquals() {
    new EqualsTester()
        .addEqualityGroup(
            BucketBoundaries.create(Arrays.asList(-1.0, 2.0)),
            BucketBoundaries.create(Arrays.asList(-1.0, 2.0)))
        .addEqualityGroup(BucketBoundaries.create(Arrays.asList(-1.0)))
        .testEquals();
  }
}
