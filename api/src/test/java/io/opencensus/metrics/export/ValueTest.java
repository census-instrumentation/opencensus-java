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
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Distribution.BucketOptions.ExplicitOptions;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value.ValueDistribution;
import io.opencensus.metrics.export.Value.ValueDouble;
import io.opencensus.metrics.export.Value.ValueLong;
import io.opencensus.metrics.export.Value.ValueSummary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Value}. */
@RunWith(JUnit4.class)
public class ValueTest {
  private static final double TOLERANCE = 1e-6;

  private static final Distribution DISTRIBUTION =
      Distribution.create(
          10,
          10,
          1,
          BucketOptions.explicitOptions(Arrays.asList(1.0, 2.0, 5.0)),
          Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)));
  private static final Summary SUMMARY =
      Summary.create(
          10L,
          10.0,
          Snapshot.create(
              10L, 87.07, Collections.singletonList(ValueAtPercentile.create(0.98, 10.2))));

  @Test
  public void createAndGet_ValueDouble() {
    Value value = Value.doubleValue(-34.56);
    assertThat(value).isInstanceOf(ValueDouble.class);
    assertThat(((ValueDouble) value).getValue()).isWithin(TOLERANCE).of(-34.56);
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
  public void createAndGet_ValueSummary() {
    Value value = Value.summaryValue(SUMMARY);
    assertThat(value).isInstanceOf(ValueSummary.class);
    assertThat(((ValueSummary) value).getValue()).isEqualTo(SUMMARY);
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
                    7,
                    10,
                    23.456,
                    BucketOptions.explicitOptions(Arrays.asList(1.0, 2.0, 5.0)),
                    Arrays.asList(
                        Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)))))
        .testEquals();
  }

  @Test
  public void testMatch() {
    List<Value> values =
        Arrays.asList(
            ValueDouble.create(1.0),
            ValueLong.create(-1),
            ValueDistribution.create(DISTRIBUTION),
            ValueSummary.create(SUMMARY));
    List<Number> expected =
        Arrays.<Number>asList(1.0, -1L, 10.0, 10L, 1.0, 1.0, 2.0, 5.0, 3L, 1L, 2L, 4L);
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
              actual.add(arg.getSum());
              actual.add(arg.getCount());
              actual.add(arg.getSumOfSquaredDeviations());

              arg.getBucketOptions()
                  .match(
                      new Function<ExplicitOptions, Object>() {
                        @Override
                        public Object apply(ExplicitOptions arg) {
                          actual.addAll(arg.getBucketBoundaries());
                          return null;
                        }
                      },
                      Functions.throwAssertionError());

              for (Bucket bucket : arg.getBuckets()) {
                actual.add(bucket.getCount());
              }
              return null;
            }
          },
          new Function<Summary, Object>() {
            @Override
            public Object apply(Summary arg) {
              return null;
            }
          },
          Functions.throwAssertionError());
    }
    assertThat(actual).containsExactlyElementsIn(expected).inOrder();
  }
}
