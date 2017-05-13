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

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.truth.Truth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Stats test utilities. */
public final class StatsTestUtil {

  private StatsTestUtil() {}

  /**
   * Creates a {@code StatsContextImpl} from a factory and a list of alternating tag keys and
   * values.
   *
   * @param factory the factory used to produce the {@code StatsContextImpl}.
   * @param tagKeysAndValues a list of alternating {@link TagKey}s and {@link TagValue}s. It must
   *     have an even length.
   * @return a {@code StatsContextImpl} with the given keys and values.
   */
  public static StatsContextImpl createContext(
      StatsContextFactoryImpl factory, Object... tagKeysAndValues) {
    StatsContextImpl.Builder builder = factory.getDefault().builder();
    for (Iterator<Object> i = Arrays.asList(tagKeysAndValues).iterator(); i.hasNext(); ) {
      TagKey key = (TagKey) i.next();
      TagValue value = (TagValue) i.next();
      builder.set(key, value);
    }
    return builder.build();
  }

  /**
   * Creates a {@code DistributionAggregation} by adding the given values to a new {@link
   * MutableDistribution}.
   *
   * @param tags the {@code DistributionAggregation}'s tags.
   * @param bucketBoundaries the bucket boundaries.
   * @param values the values to add to the distribution.
   * @return the new {@code DistributionAggregation}
   */
  public static DistributionAggregation createDistributionAggregation(
      List<Tag> tags, BucketBoundaries bucketBoundaries, List<Double> values) {
    MutableDistribution mdist = MutableDistribution.create(bucketBoundaries);
    for (double value : values) {
      mdist.add(value);
    }
    MutableDistribution.Range range = mdist.getRange();
    return DistributionAggregation.create(
        mdist.getCount(),
        mdist.getMean(),
        mdist.getSum(),
        DistributionAggregation.Range.create(range.getMin(), range.getMax()),
        tags,
        mdist.getBucketCounts());
  }

  /**
   * Asserts that the two sets of {@code DistributionAggregation}s are equivalent, with a given
   * tolerance. The tolerance is used when comparing the mean and sum of values. The order of the
   * {@code DistributionAggregation}s has no effect. The expected parameter is last, because it is
   * likely to be a larger expression.
   *
   * @param tolerance the tolerance used for {@code double} comparison.
   * @param actual the actual test result.
   * @param expected the expected value.
   * @throws AssertionError if the {@code DistributionAggregation}s don't match.
   */
  public static void assertDistributionAggregationsEquivalent(
      double tolerance,
      Collection<DistributionAggregation> actual,
      Collection<DistributionAggregation> expected) {
    Function<DistributionAggregation, List<Tag>> getTagsFunction =
        new Function<DistributionAggregation, List<Tag>>() {
          @Override
          public List<Tag> apply(DistributionAggregation agg) {
            return agg.getTags();
          }
        };
    Iterable<List<Tag>> expectedTags = Iterables.transform(expected, getTagsFunction);
    Iterable<List<Tag>> actualTags = Iterables.transform(actual, getTagsFunction);
    Truth.assertThat(actualTags).containsExactlyElementsIn(expectedTags);
    for (DistributionAggregation expectedAgg : expected) {
      DistributionAggregation actualAgg =
          Iterables.find(
              actual,
              Predicates.compose(Predicates.equalTo(expectedAgg.getTags()), getTagsFunction));
      assertDistributionAggregationValuesEquivalent(
          "DistributionAggregation tags=" + expectedAgg.getTags(),
          tolerance,
          expectedAgg,
          actualAgg);
    }
  }

  private static void assertDistributionAggregationValuesEquivalent(
      String msg, double tolerance, DistributionAggregation agg1, DistributionAggregation agg2) {
    Truth.assertWithMessage(msg + " count").that(agg1.getCount()).isEqualTo(agg2.getCount());
    Truth.assertWithMessage(msg + " mean")
        .that(agg1.getMean())
        .isWithin(tolerance)
        .of(agg2.getMean());
    Truth.assertWithMessage(msg + " sum").that(agg1.getSum()).isWithin(tolerance).of(agg2.getSum());
    Truth.assertWithMessage(msg + " range").that(agg1.getRange()).isEqualTo(agg2.getRange());
    Truth.assertWithMessage(msg + " bucket counts")
        .that(removeTrailingZeros(agg1.getBucketCounts()))
        .isEqualTo(removeTrailingZeros(agg2.getBucketCounts()));
  }

  private static List<Long> removeTrailingZeros(List<Long> longs) {
    List<Long> truncated = new ArrayList<Long>(longs);
    while (!truncated.isEmpty() && Iterables.getLast(truncated) == 0) {
      truncated.remove(truncated.size() - 1);
    }
    return truncated;
  }
}
