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

import com.google.instrumentation.common.Function;
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
 * Tests for {@link ViewDescriptor}
 */
@RunWith(JUnit4.class)
public final class ViewDescriptorTest {
  @Test
  public void testViewDescriptor() {
    String name = "test-view-name";
    String description = "test-view-name description";
    MeasurementDescriptor mDescriptor = MeasurementDescriptor.create(
        "measurement",
        "measurement description",
        MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR })));
    AggregationDescriptor aDescriptor = IntervalAggregationDescriptor.create();
    List<TagKey> keys = Arrays.asList(new TagKey[] { new TagKey("foo"), new TagKey("bar") });
    ViewDescriptor view = ViewDescriptor.create(name, description, mDescriptor, aDescriptor, keys);

    assertThat(view.getName()).isEqualTo(name);
    assertThat(view.getDescription()).isEqualTo(description);
    assertThat(view.getMeasurementDescriptor().getName()).isEqualTo(mDescriptor.getName());
    assertThat(view.getAggregationDescriptor().match(
        new Function<DistributionAggregationDescriptor, Boolean> () {
          @Override public Boolean apply(DistributionAggregationDescriptor dDescriptor) {
            return false;
          }
        },
        new Function<IntervalAggregationDescriptor, Boolean> () {
          @Override public Boolean apply(IntervalAggregationDescriptor iDescriptor) {
            return iDescriptor.getIntervalSizes() == null;
          }
        })).isTrue();
    assertThat(view.getTagKeys().size()).isEqualTo(2);
    assertThat(view.getTagKeys().get(0).toString()).isEqualTo("foo");
    assertThat(view.getTagKeys().get(1).toString()).isEqualTo("bar");
  }
}
