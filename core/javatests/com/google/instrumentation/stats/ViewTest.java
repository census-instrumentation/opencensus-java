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

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.Aggregation.DistributionAggregation;
import com.google.instrumentation.stats.Aggregation.DistributionAggregation.Range;
import com.google.instrumentation.stats.Aggregation.IntervalAggregation;
import com.google.instrumentation.stats.Aggregation.IntervalAggregation.Interval;
import com.google.instrumentation.stats.AggregationDescriptor.DistributionAggregationDescriptor;
import com.google.instrumentation.stats.AggregationDescriptor.IntervalAggregationDescriptor;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
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
    DistributionAggregationDescriptor distributionAggrDescriptor =
        DistributionAggregationDescriptor.create();
    ViewDescriptor distributionViewDescriptor =
        ViewDescriptor.create(
            name, description, measurementDescriptor, distributionAggrDescriptor, tagKeys);
    List<Aggregation> aggrs = Arrays.asList(new Aggregation[] {
          DistributionAggregation.create(tags1, 5, 5.0, 15.0, Range.create(1.0, 5.0),
              Arrays.asList(new Long[] { 1L, 1L, 1L, 1L, 1L })),
          DistributionAggregation.create(tags2, 10, 5.0, 30.0, Range.create(1.0, 5.0),
              Arrays.asList(new Long[] { 2L, 2L, 2L, 2L, 2L }))
        });

    View view = View.create(distributionViewDescriptor, aggrs, start, end);
    assertThat(view.getViewDescriptor()).isEqualTo(distributionViewDescriptor);
    assertThat(view.getAggregations().size()).isEqualTo(aggrs.size());
    for (int i = 0; i < aggrs.size(); i++) {
      assertThat(view.getAggregations().get(i)).isEqualTo(aggrs.get(i));
    }
    assertThat(view.getStart()).isEqualTo(start);
    assertThat(view.getEnd()).isEqualTo(end);
  }

  @Test
  public void testIntervalView() {
    IntervalAggregationDescriptor intervalAggrDescriptor = IntervalAggregationDescriptor.create();
    ViewDescriptor intervalViewDescriptor =
        ViewDescriptor.create(
            name, description, measurementDescriptor, intervalAggrDescriptor, tagKeys);
    List<Aggregation> aggrs = Arrays.asList(new Aggregation[] {
          IntervalAggregation.create(tags1, Arrays.asList(
              new Interval[] { Interval.create(Duration.fromMillis(111), 10, 100) })),
          IntervalAggregation.create(tags2, Arrays.asList(
              new Interval[] { Interval.create(Duration.fromMillis(111), 10, 100) }))
        });

    View view = View.create(intervalViewDescriptor, aggrs, start, end);
    assertThat(view.getViewDescriptor()).isEqualTo(intervalViewDescriptor);
    assertThat(view.getAggregations().size()).isEqualTo(aggrs.size());
    for (int i = 0; i < aggrs.size(); i++) {
      assertThat(view.getAggregations().get(i)).isEqualTo(aggrs.get(i));
    }
    assertThat(view.getStart()).isEqualTo(start);
    assertThat(view.getEnd()).isEqualTo(end);
  }

  // tag keys
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private final List<TagKey> tagKeys = Arrays.asList(new TagKey[] { K1, K2 });

  // tag values
  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V10 = new TagValue("v10");
  private static final TagValue V20 = new TagValue("v20");

  // tags
  List<Tag> tags1 = Arrays.asList(new Tag[] { Tag.create(K1, V1), Tag.create(K2, V2) });
  List<Tag> tags2 = Arrays.asList(new Tag[] { Tag.create(K1, V10), Tag.create(K2, V20) });

  // name
  private final String name = "test-view-descriptor";
  // description
  private final String description = "test-view-descriptor description";
  // measurement descriptor
  private final MeasurementDescriptor measurementDescriptor = MeasurementDescriptor.create(
      "measurement-descriptor",
      "measurement-descriptor description",
      MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR })));
  // time stamps
  private final Timestamp start = Timestamp.fromMillis(1000);
  private final Timestamp end = Timestamp.fromMillis(2000);
}
