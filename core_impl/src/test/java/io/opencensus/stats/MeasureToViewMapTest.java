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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import io.opencensus.common.Function;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import io.opencensus.stats.ViewDescriptor.DistributionViewDescriptor;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureToViewMap}. */
@RunWith(JUnit4.class)
public class MeasureToViewMapTest {

  private static final Measure MEASUREMENT_DESCRIPTOR =
      Measure.DoubleMeasure.create(
          "my measurement",
          "measurement description",
          "By");

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
    MeasureToViewMap measureToViewMap = new MeasureToViewMap();
    TestClock clock = TestClock.create(Timestamp.create(10, 20));
    measureToViewMap.registerView(VIEW_DESCRIPTOR, clock);
    clock.setTime(Timestamp.create(30, 40));
    View actual = measureToViewMap.getView(VIEW_NAME, clock);
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
