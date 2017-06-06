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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.internal.TestClock;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.View.IntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasurementDescriptorToViewMap}. */
@RunWith(JUnit4.class)
public class MeasurementDescriptorToViewMapTest {

  private static final MeasurementDescriptor MEASUREMENT_DESCRIPTOR =
      MeasurementDescriptor.create(
          "my measurement",
          "measurement description",
          MeasurementUnit.create(0, Arrays.asList(BasicUnit.BYTES)));

  private static final ViewDescriptor.Name VIEW_NAME = ViewDescriptor.Name.create("my view");

  private static final ViewDescriptor VIEW_DESCRIPTOR =
      DistributionViewDescriptor.create(
          VIEW_NAME,
          "view description",
          MEASUREMENT_DESCRIPTOR,
          DistributionAggregationDescriptor.create(),
          Arrays.asList(TagKey.create("my key")));

  @Test
  public void testRegisterAndGetDistributionView() {
    MeasurementDescriptorToViewMap measurementDescriptorToViewMap =
        new MeasurementDescriptorToViewMap();
    TestClock clock = TestClock.create(Timestamp.create(10, 20));
    measurementDescriptorToViewMap.registerView(VIEW_DESCRIPTOR, clock);
    clock.setTime(Timestamp.create(30, 40));
    View actual = measurementDescriptorToViewMap.getView(VIEW_NAME, clock);
    actual.match(
        new Function<View.DistributionView, Void>() {
          @Override
          public Void apply(DistributionView view) {
            assertThat(view.getViewDescriptor()).isEqualTo(VIEW_DESCRIPTOR);
            assertThat(view.getStart()).isEqualTo(Timestamp.create(10, 20));
            assertThat(view.getEnd()).isEqualTo(Timestamp.create(30, 40));
            assertThat(view.getDistributionAggregations()).isEmpty();
            return null;
          }
        },
        new Function<View.IntervalView, Void>() {
          @Override
          public Void apply(IntervalView view) {
            fail("Wrong view type.");
            return null;
          }
        });
  }
}
