/*
 * Copyright 2016, Google Inc.
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

import static org.junit.Assert.assertTrue;

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.common.Function;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.DistributionAggregation.Range;
import com.google.instrumentation.stats.IntervalAggregation.Interval;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.View.IntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for class {@link View}.
 */
@RunWith(JUnit4.class)
public final class ViewTest {
  @Test
  public void testDistributionView() {
    DistributionAggregationDescriptor aggregationDescriptor =
        DistributionAggregationDescriptor.create(Arrays.asList(0.0, 10.0, 20.0, 30.0, 40.0));
    final DistributionViewDescriptor viewDescriptor =
        DistributionViewDescriptor.create(
            name, description, measurementDescriptor, aggregationDescriptor, tagKeys);
    List<DistributionAggregation> aggregations = Arrays.asList(
        DistributionAggregation.create(5, 5.0, 15.0, Range.create(1.0, 5.0), tags1,
            Arrays.asList(1L, 1L, 1L, 1L, 1L)),
        DistributionAggregation.create(10, 5.0, 30.0, Range.create(1.0, 5.0), tags2,
            Arrays.asList(2L, 2L, 2L, 2L, 2L)));
    final Timestamp start = Timestamp.fromMillis(1000);
    final Timestamp end = Timestamp.fromMillis(2000);
    final View view = DistributionView.create(viewDescriptor, aggregations, start, end);

    assertThat(view.getViewDescriptor()).isEqualTo(viewDescriptor);
    assertTrue(view.match(
        new Function<DistributionView, Boolean> () {
          @Override public Boolean apply(DistributionView dView) {
            return dView == view
                && dView.getViewDescriptor().equals(viewDescriptor)
                && shallowListEquals(dView.getDistributionAggregations(), aggregations)
                && dView.getStart().equals(start)
                && dView.getEnd().equals(end);
          }
        },
        new Function<IntervalView, Boolean> () {
          @Override public Boolean apply(IntervalView iView) {
            return false;
          }
        }));
  }

  @Test
  public void testIntervalView() {
    IntervalAggregationDescriptor aggregationDescriptor =
        IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(111)));
    final IntervalViewDescriptor viewDescriptor =
        IntervalViewDescriptor.create(
            name, description, measurementDescriptor, aggregationDescriptor, tagKeys);
    final List<IntervalAggregation> aggregations = Arrays.asList(
        IntervalAggregation.create(tags1, Arrays.asList(
            Interval.create(Duration.fromMillis(111), 10, 100))),
        IntervalAggregation.create(tags2, Arrays.asList(
            Interval.create(Duration.fromMillis(111), 10, 100))));

    final View view = IntervalView.create(viewDescriptor, aggregations);
    assertThat(view.getViewDescriptor()).isEqualTo(viewDescriptor);
    assertTrue(view.match(
        new Function<DistributionView, Boolean> () {
          @Override public Boolean apply(DistributionView dView) {
            return false;
          }
        },
        new Function<IntervalView, Boolean> () {
          @Override public Boolean apply(IntervalView iView) {
            return iView == view
                && iView.getViewDescriptor().equals(viewDescriptor)
                && shallowListEquals(iView.getIntervalAggregations(), aggregations);
          }
        }));
  }

  // tag keys
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private final List<TagKey> tagKeys = Arrays.asList(K1, K2);

  // tag values
  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V10 = new TagValue("v10");
  private static final TagValue V20 = new TagValue("v20");

  // tags
  List<Tag> tags1 = Arrays.asList(Tag.create(K1, V1), Tag.create(K2, V2));
  List<Tag> tags2 = Arrays.asList(Tag.create(K1, V10), Tag.create(K2, V20));

  // name
  private final String name = "test-view-descriptor";
  // description
  private final String description = "test-view-descriptor description";
  // measurement descriptor
  private final MeasurementDescriptor measurementDescriptor = MeasurementDescriptor.create(
      "measurement-descriptor",
      "measurement-descriptor description",
      MeasurementUnit.create(1, Arrays.asList(BasicUnit.SCALAR)));

  private static final <T> boolean shallowListEquals(List<T> l1, List <T> l2) {
    if (l1.size() != l2.size()) {
      return false;
    }
    for (int i = 0; i < l1.size(); i++) {
      if (l1.get(i) != l2.get(i)) {
        return false;
      }
    }
    return true;
  }
}
