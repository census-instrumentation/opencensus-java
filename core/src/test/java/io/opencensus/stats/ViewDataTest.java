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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.DistributionAggregation.Range;
import io.opencensus.stats.IntervalAggregation.Interval;
import io.opencensus.stats.ViewData.DistributionViewData;
import io.opencensus.stats.ViewData.IntervalViewData;
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for class {@link ViewData}. */
@RunWith(JUnit4.class)
public final class ViewDataTest {
  @Test
  public void testDistributionViewData() {
    DistributionAggregationDescriptor aggregationDescriptor =
        DistributionAggregationDescriptor.create(Arrays.asList(10.0, 20.0, 30.0, 40.0));
    final DistributionView view =
        DistributionView.create(
            name, description, measure, aggregationDescriptor, tagKeys);
    final List<DistributionAggregation> aggregations = Arrays.asList(
        DistributionAggregation.create(5, 5.0, 15.0, Range.create(1.0, 5.0), tags1,
            Arrays.asList(1L, 1L, 1L, 1L, 1L)),
        DistributionAggregation.create(10, 5.0, 30.0, Range.create(1.0, 5.0), tags2,
            Arrays.asList(2L, 2L, 2L, 2L, 2L)));
    final Timestamp start = Timestamp.fromMillis(1000);
    final Timestamp end = Timestamp.fromMillis(2000);
    final ViewData viewData = DistributionViewData.create(view, aggregations, start, end);

    assertThat(viewData.getView()).isEqualTo(view);
    assertTrue(viewData.match(
        new Function<DistributionViewData, Boolean> () {
          @Override public Boolean apply(DistributionViewData dViewData) {
            return dViewData == viewData
                && dViewData.getView().equals(view)
                && shallowListEquals(dViewData.getDistributionAggregations(), aggregations)
                && dViewData.getStart().equals(start)
                && dViewData.getEnd().equals(end);
          }
        },
        new Function<IntervalViewData, Boolean> () {
          @Override public Boolean apply(IntervalViewData iViewData) {
            return false;
          }
        }));
  }

  @Test
  public void testIntervalViewData() {
    IntervalAggregationDescriptor aggregationDescriptor =
        IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(111)));
    final IntervalView view =
        IntervalView.create(
            name, description, measure, aggregationDescriptor, tagKeys);
    final List<IntervalAggregation> aggregations = Arrays.asList(
        IntervalAggregation.create(tags1, Arrays.asList(
            Interval.create(Duration.fromMillis(111), 10, 100))),
        IntervalAggregation.create(tags2, Arrays.asList(
            Interval.create(Duration.fromMillis(111), 10, 100))));

    final ViewData viewData = IntervalViewData.create(view, aggregations);
    assertThat(viewData.getView()).isEqualTo(view);
    assertTrue(viewData.match(
        new Function<DistributionViewData, Boolean> () {
          @Override public Boolean apply(DistributionViewData dViewData) {
            return false;
          }
        },
        new Function<IntervalViewData, Boolean> () {
          @Override public Boolean apply(IntervalViewData iViewData) {
            return iViewData == viewData
                && iViewData.getView().equals(view)
                && shallowListEquals(iViewData.getIntervalAggregations(), aggregations);
          }
        }));
  }

  @Test
  public void testViewDataEquals() {
    DistributionView dView =
        DistributionView.create(
            name,
            description,
            measure,
            DistributionAggregationDescriptor.create(Arrays.asList(10.0)),
            tagKeys);
    List<DistributionAggregation> dAggregations =
        Arrays.asList(
            DistributionAggregation.create(
                5, 5.0, 15.0, Range.create(1.0, 5.0), tags1, Arrays.asList(1L)));
    IntervalView iView =
        IntervalView.create(
            name,
            description,
            measure,
            IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(111))),
            tagKeys);
    List<IntervalAggregation> iAggregations =
        Arrays.asList(
            IntervalAggregation.create(
                tags1, Arrays.asList(Interval.create(Duration.fromMillis(111), 10, 100))));

    new EqualsTester()
        .addEqualityGroup(
            DistributionViewData.create(
                dView,
                dAggregations,
                Timestamp.fromMillis(1000),
                Timestamp.fromMillis(2000)),
            DistributionViewData.create(
                dView,
                dAggregations,
                Timestamp.fromMillis(1000),
                Timestamp.fromMillis(2000)))
        .addEqualityGroup(
            DistributionViewData.create(
                dView,
                dAggregations,
                Timestamp.fromMillis(1000),
                Timestamp.fromMillis(3000)))
        .addEqualityGroup(
            IntervalViewData.create(iView, iAggregations),
            IntervalViewData.create(iView, iAggregations))
        .addEqualityGroup(
            IntervalViewData.create(iView, Collections.<IntervalAggregation>emptyList()))
        .testEquals();
  }

  // tag keys
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private final List<TagKey> tagKeys = Arrays.asList(K1, K2);

  // tag values
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V10 = TagValue.create("v10");
  private static final TagValue V20 = TagValue.create("v20");

  // tags
  List<Tag> tags1 = Arrays.asList(Tag.create(K1, V1), Tag.create(K2, V2));
  List<Tag> tags2 = Arrays.asList(Tag.create(K1, V10), Tag.create(K2, V20));

  // name
  private final String name = "test-view";
  // description
  private final String description = "test-view-descriptor description";
  // measurement descriptor
  private final Measure measure = Measure.MeasureDouble.create(
      "measure",
      "measure description",
      "1");

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
