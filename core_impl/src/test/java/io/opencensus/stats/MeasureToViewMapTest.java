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

import io.opencensus.common.Timestamp;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureToViewMap}. */
@RunWith(JUnit4.class)
public class MeasureToViewMapTest {

  private static final Measure MEASURE =
      Measure.MeasureDouble.create(
          "my measurement",
          "measurement description",
          "By");

  private static final View.Name VIEW_NAME = View.Name.create("my view");

  private static final View VIEW =
      View.create(VIEW_NAME, "view description", MEASURE,
          RpcViewConstants.AGGREGATION_NO_HISTOGRAM, Arrays.asList(TagKey.create("my key")),
          RpcViewConstants.CUMULATIVE);

  @Test
  public void testRegisterAndGetView() {
    MeasureToViewMap measureToViewMap = new MeasureToViewMap();
    TestClock clock = TestClock.create(Timestamp.create(10, 20));
    measureToViewMap.registerView(VIEW, clock);
    clock.setTime(Timestamp.create(30, 40));
    ViewData viewData = measureToViewMap.getView(VIEW_NAME, clock);
    assertThat(viewData.getView()).isEqualTo(VIEW);
    StatsTestUtil.assertWindowDataEquals(
        viewData.getWindowData(), Timestamp.create(10, 20), Timestamp.create(30, 40));
    assertThat(viewData.getAggregationMap()).isEmpty();
  }
}
