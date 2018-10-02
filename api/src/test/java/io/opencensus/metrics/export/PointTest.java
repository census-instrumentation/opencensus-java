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
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.metrics.export.Point}. */
@RunWith(JUnit4.class)
public class PointTest {

  private static final Value DOUBLE_VALUE = Value.doubleValue(55.5);
  private static final Value LONG_VALUE = Value.longValue(9876543210L);
  private static final Value DISTRIBUTION_VALUE =
      Value.distributionValue(
          Distribution.create(
              10,
              6.6,
              678.54,
              Arrays.asList(-1.0, 0.0, 1.0),
              Arrays.asList(
                  Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4))));
  private static final Timestamp TIMESTAMP_1 = Timestamp.create(1, 2);
  private static final Timestamp TIMESTAMP_2 = Timestamp.create(3, 4);
  private static final Timestamp TIMESTAMP_3 = Timestamp.create(5, 6);

  @Test
  public void testGet() {
    Point point = Point.create(DOUBLE_VALUE, TIMESTAMP_1);
    assertThat(point.getValue()).isEqualTo(DOUBLE_VALUE);
    assertThat(point.getTimestamp()).isEqualTo(TIMESTAMP_1);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Point.create(DOUBLE_VALUE, TIMESTAMP_1), Point.create(DOUBLE_VALUE, TIMESTAMP_1))
        .addEqualityGroup(Point.create(LONG_VALUE, TIMESTAMP_1))
        .addEqualityGroup(Point.create(LONG_VALUE, TIMESTAMP_2))
        .addEqualityGroup(
            Point.create(DISTRIBUTION_VALUE, TIMESTAMP_2),
            Point.create(DISTRIBUTION_VALUE, TIMESTAMP_2))
        .addEqualityGroup(Point.create(DISTRIBUTION_VALUE, TIMESTAMP_3))
        .testEquals();
  }
}
