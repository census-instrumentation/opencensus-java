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
import com.google.instrumentation.common.Function;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link ViewDescriptor}
 */
@RunWith(JUnit4.class)
public final class ViewDescriptorTest {
  @Test
  public void testDistributionViewDescriptor() {
    String name = "test-view-name";
    String description = "test-view-name description";
    MeasurementDescriptor mDescriptor = MeasurementDescriptor.create(
        "measurement",
        "measurement description",
        MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR })));
    DistributionAggregationDescriptor dAggrDescriptor = DistributionAggregationDescriptor.create();
    List<TagKey> keys = Arrays.asList(new TagKey[] { new TagKey("foo"), new TagKey("bar") });
    final DistributionViewDescriptor viewDescriptor =
        DistributionViewDescriptor.create(name, description, mDescriptor, dAggrDescriptor, keys);

    assertThat(viewDescriptor.getName()).isEqualTo(name);
    assertThat(viewDescriptor.getDescription()).isEqualTo(description);
    assertThat(viewDescriptor.getMeasurementDescriptor().getName()).isEqualTo(mDescriptor.getName());
    assertThat(viewDescriptor.getDistributionAggregationDescriptor()).isEqualTo(dAggrDescriptor);
    assertThat(viewDescriptor.getTagKeys().size()).isEqualTo(2);
    assertThat(viewDescriptor.getTagKeys().get(0).toString()).isEqualTo("foo");
    assertThat(viewDescriptor.getTagKeys().get(1).toString()).isEqualTo("bar");
    assertThat(viewDescriptor.match(
        new Function<DistributionViewDescriptor, Boolean> () {
          @Override public Boolean apply(DistributionViewDescriptor dViewDescriptor) {
            return dViewDescriptor == viewDescriptor;
          }
        },
        new Function<IntervalViewDescriptor, Boolean> () {
          @Override public Boolean apply(IntervalViewDescriptor iViewDescriptor) {
            return false;
          }
        })).isTrue();
  }

  @Test
  public void testIntervalViewDescriptor() {
    String name = "test-view-name";
    String description = "test-view-name description";
    MeasurementDescriptor mDescriptor = MeasurementDescriptor.create(
        "measurement",
        "measurement description",
        MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR })));
    List<Duration> intervals = Arrays.asList(new Duration[] {
          Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)});
    IntervalAggregationDescriptor iAggrDescriptor = IntervalAggregationDescriptor.create(intervals);
    List<TagKey> keys = Arrays.asList(new TagKey[] { new TagKey("foo"), new TagKey("bar") });
    final IntervalViewDescriptor viewDescriptor =
        IntervalViewDescriptor.create(name, description, mDescriptor, iAggrDescriptor, keys);

    assertThat(viewDescriptor.getName()).isEqualTo(name);
    assertThat(viewDescriptor.getDescription()).isEqualTo(description);
    assertThat(viewDescriptor.getMeasurementDescriptor().getName()).isEqualTo(mDescriptor.getName());
    assertThat(viewDescriptor.getIntervalAggregationDescriptor()).isEqualTo(iAggrDescriptor);
    assertThat(viewDescriptor.getTagKeys().size()).isEqualTo(2);
    assertThat(viewDescriptor.getTagKeys().get(0).toString()).isEqualTo("foo");
    assertThat(viewDescriptor.getTagKeys().get(1).toString()).isEqualTo("bar");
    assertThat(viewDescriptor.match(
        new Function<DistributionViewDescriptor, Boolean> () {
          @Override public Boolean apply(DistributionViewDescriptor dViewDescriptor) {
            return false;
          }
        },
        new Function<IntervalViewDescriptor, Boolean> () {
          @Override public Boolean apply(IntervalViewDescriptor iViewDescriptor) {
            return iViewDescriptor == viewDescriptor;
          }
        })).isTrue();
  }
}
