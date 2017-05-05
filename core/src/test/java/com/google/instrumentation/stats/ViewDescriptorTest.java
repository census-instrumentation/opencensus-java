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

import com.google.common.testing.EqualsTester;
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
    DistributionAggregationDescriptor dAggrDescriptor = DistributionAggregationDescriptor.create();
    final ViewDescriptor viewDescriptor = DistributionViewDescriptor.create(
        name, description, measurementDescriptor, dAggrDescriptor, keys);

    assertThat(viewDescriptor.getViewDescriptorName()).isEqualTo(name);
    assertThat(viewDescriptor.getName()).isEqualTo(name.asString());
    assertThat(viewDescriptor.getDescription()).isEqualTo(description);
    assertThat(viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName())
        .isEqualTo(measurementDescriptor.getMeasurementDescriptorName());
    assertThat(viewDescriptor.getTagKeys()).hasSize(2);
    assertThat(viewDescriptor.getTagKeys().get(0).toString()).isEqualTo("foo");
    assertThat(viewDescriptor.getTagKeys().get(1).toString()).isEqualTo("bar");
    assertTrue(viewDescriptor.match(
        new Function<DistributionViewDescriptor, Boolean> () {
          @Override public Boolean apply(DistributionViewDescriptor dViewDescriptor) {
            return dViewDescriptor == viewDescriptor;
          }
        },
        new Function<IntervalViewDescriptor, Boolean> () {
          @Override public Boolean apply(IntervalViewDescriptor iViewDescriptor) {
            return false;
          }
        }));
  }

  @Test
  public void testIntervalViewDescriptor() {
    IntervalAggregationDescriptor iAggrDescriptor = IntervalAggregationDescriptor.create(
        Arrays.asList(Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)));
    final ViewDescriptor viewDescriptor = IntervalViewDescriptor.create(
        name, description, measurementDescriptor, iAggrDescriptor, keys);

    assertThat(viewDescriptor.getViewDescriptorName()).isEqualTo(name);
    assertThat(viewDescriptor.getName()).isEqualTo(name.asString());
    assertThat(viewDescriptor.getDescription()).isEqualTo(description);
    assertThat(viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName())
        .isEqualTo(measurementDescriptor.getMeasurementDescriptorName());
    assertThat(viewDescriptor.getTagKeys()).hasSize(2);
    assertThat(viewDescriptor.getTagKeys().get(0).toString()).isEqualTo("foo");
    assertThat(viewDescriptor.getTagKeys().get(1).toString()).isEqualTo("bar");
    assertTrue(viewDescriptor.match(
        new Function<DistributionViewDescriptor, Boolean> () {
          @Override public Boolean apply(DistributionViewDescriptor dViewDescriptor) {
            return false;
          }
        },
        new Function<IntervalViewDescriptor, Boolean> () {
          @Override public Boolean apply(IntervalViewDescriptor iViewDescriptor) {
            return iViewDescriptor == viewDescriptor;
          }
        }));
  }

  @Test
  public void testViewDescriptorEquals() {
    DistributionAggregationDescriptor dAggrDescriptor = DistributionAggregationDescriptor.create();
    IntervalAggregationDescriptor iAggrDescriptor = IntervalAggregationDescriptor.create(
        Arrays.asList(Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)));
    new EqualsTester()
        .addEqualityGroup(
            DistributionViewDescriptor.create(
                name, description, measurementDescriptor, dAggrDescriptor, keys),
            DistributionViewDescriptor.create(
                name, description, measurementDescriptor, dAggrDescriptor, keys))
        .addEqualityGroup(
            DistributionViewDescriptor.create(
                name, description + 2, measurementDescriptor, dAggrDescriptor, keys))
        .addEqualityGroup(
            IntervalViewDescriptor.create(
                name, description, measurementDescriptor, iAggrDescriptor, keys),
            IntervalViewDescriptor.create(
                name, description, measurementDescriptor, iAggrDescriptor, keys))
        .addEqualityGroup(
            IntervalViewDescriptor.create(
                name, description + 2, measurementDescriptor, iAggrDescriptor, keys))
        .testEquals();
  }

  @Test
  public void testViewDescriptorName() {
    assertThat(ViewDescriptor.Name.create("my name").asString()).isEqualTo("my name");
  }

  @Test
  public void testViewDescriptorNameEquals() {
    new EqualsTester()
        .addEqualityGroup(
            ViewDescriptor.Name.create("view-1"), ViewDescriptor.Name.create("view-1"))
        .addEqualityGroup(ViewDescriptor.Name.create("view-2"))
        .testEquals();
  }

  private final ViewDescriptor.Name name = ViewDescriptor.Name.create("test-view-name");
  private final String description = "test-view-name description";
  private final MeasurementDescriptor measurementDescriptor = MeasurementDescriptor.create(
      "measurement",
      "measurement description",
      MeasurementUnit.create(1, Arrays.asList(BasicUnit.SCALAR)));
  private final List<TagKey> keys = Arrays.asList(TagKey.create("foo"), TagKey.create("bar"));
}
