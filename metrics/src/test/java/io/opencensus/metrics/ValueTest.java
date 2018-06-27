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
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.metrics.Distribution.Bucket;
import io.opencensus.metrics.Distribution.Range;
import io.opencensus.metrics.Value.ValueDistribution;
import io.opencensus.metrics.Value.ValueDouble;
import io.opencensus.metrics.Value.ValueLong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Value}. */
@RunWith(JUnit4.class)
public class ValueTest {

  private static final Distribution DISTRIBUTION =
      Distribution.create(
          10,
          10,
          1,
          Range.create(1, 5),
          Arrays.asList(-5.0, 0.0, 5.0),
          Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)));

  @Test
  public void createAndGet_ValueDouble() {
    Value value = Value.doubleValue(-34.56);
    assertThat(value).isInstanceOf(ValueDouble.class);
    assertThat(((ValueDouble) value).getValue()).isEqualTo(-34.56);
  }

  @Test
  public void createAndGet_ValueLong() {
    Value value = Value.longValue(123456789);
    assertThat(value).isInstanceOf(ValueLong.class);
    assertThat(((ValueLong) value).getValue()).isEqualTo(123456789);
  }

  @Test
  public void createAndGet_ValueDistribution() {
    Value value = Value.distributionValue(DISTRIBUTION);
    assertThat(value).isInstanceOf(ValueDistribution.class);
    assertThat(((ValueDistribution) value).getValue()).isEqualTo(DISTRIBUTION);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(Value.doubleValue(1.0), Value.doubleValue(1.0))
        .addEqualityGroup(Value.doubleValue(2.0))
        .addEqualityGroup(Value.longValue(1L))
        .addEqualityGroup(Value.longValue(2L))
        .addEqualityGroup(
            Value.distributionValue(
                Distribution.create(
                    -7,
                    10,
                    23.456,
                    Range.create(-19.1, 19.2),
                    Arrays.asList(-5.0, 0.0, 5.0),
                    Arrays.asList(
                        Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)))))
        .testEquals();
  }

  @Test
  public void testMatch() {
    List<Value> values =
        Arrays.asList(
            ValueDouble.create(1.0), ValueLong.create(-1), ValueDistribution.create(DISTRIBUTION));
    List<Number> expected =
        Arrays.<Number>asList(1.0, -1L, 10.0, 10L, 1.0, 1.0, 5.0, -5.0, 0.0, 5.0, 3L, 1L, 2L, 4L);
    final List<Number> actual = new ArrayList<Number>();
    for (Value value : values) {
      value.match(
          new Function<Double, Object>() {
            @Override
            public Object apply(Double arg) {
              actual.add(arg);
              return null;
            }
          },
          new Function<Long, Object>() {
            @Override
            public Object apply(Long arg) {
              actual.add(arg);
              return null;
            }
          },
          new Function<Distribution, Object>() {
            @Override
            public Object apply(Distribution arg) {
              actual.add(arg.getMean());
              actual.add(arg.getCount());
              actual.add(arg.getSumOfSquaredDeviations());
              actual.add(arg.getRange().getMin());
              actual.add(arg.getRange().getMax());
              actual.addAll(arg.getBucketBoundaries());
              for (Bucket bucket : arg.getBuckets()) {
                actual.add(bucket.getCount());
              }
              return null;
            }
          },
          Functions.throwAssertionError());
    }
    assertThat(actual).containsExactlyElementsIn(expected).inOrder();
  }
}
