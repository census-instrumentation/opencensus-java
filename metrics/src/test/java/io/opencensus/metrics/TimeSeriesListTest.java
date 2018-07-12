/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.TimeSeriesList.TimeSeriesCumulativeList;
import io.opencensus.metrics.TimeSeriesList.TimeSeriesGaugeList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimeSeries}. */
@RunWith(JUnit4.class)
public class TimeSeriesListTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final LabelValue LABEL_VALUE_1 = LabelValue.create("value1");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("value2");
  private static final LabelValue LABEL_VALUE_EMPTY = LabelValue.create("");
  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Value VALUE_DOUBLE = Value.doubleValue(-345.77);
  private static final Timestamp TIMESTAMP_1 = Timestamp.fromMillis(1000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(2000);
  private static final Timestamp TIMESTAMP_3 = Timestamp.fromMillis(3000);
  private static final Point POINT_1 = Point.create(VALUE_DOUBLE, TIMESTAMP_2);
  private static final Point POINT_2 = Point.create(VALUE_LONG, TIMESTAMP_3);
  private static final TimeSeriesGauge TIME_SERIES_GAUGE =
      TimeSeriesGauge.create(
          Arrays.asList(LABEL_VALUE_1, LABEL_VALUE_2), Arrays.asList(POINT_1, POINT_2));
  private static final TimeSeriesCumulative TIME_SERIES_CUMULATIVE =
      TimeSeriesCumulative.create(
          Arrays.asList(LABEL_VALUE_1, LABEL_VALUE_EMPTY), Arrays.asList(POINT_1), TIMESTAMP_1);

  @Test
  public void testGet_TimeSeriesGaugeList() {
    TimeSeriesGaugeList gaugeTimeSeriesList =
        TimeSeriesGaugeList.create(Collections.singletonList(TIME_SERIES_GAUGE));
    assertThat(gaugeTimeSeriesList.getList()).containsExactly(TIME_SERIES_GAUGE);
  }

  @Test
  public void testGet_TimeSeriesCumulativeList() {
    TimeSeriesCumulativeList cumulativeTimeSeriesList =
        TimeSeriesCumulativeList.create(Collections.singletonList(TIME_SERIES_CUMULATIVE));
    assertThat(cumulativeTimeSeriesList.getList()).containsExactly(TIME_SERIES_CUMULATIVE);
  }

  @Test
  public void createGaugeList_WithNullList() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("list"));
    TimeSeriesGaugeList.create(null);
  }

  @Test
  public void createGaugeList_WithNullTimeSeries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("timeSeriesGauge"));
    TimeSeriesGaugeList.create(Arrays.asList(TIME_SERIES_GAUGE, null));
  }

  @Test
  public void createCumulativeList_WithNullList() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("list"));
    TimeSeriesCumulativeList.create(null);
  }

  @Test
  public void createCumulativeList_WithNullTimeSeries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("timeSeriesCumulative"));
    TimeSeriesCumulativeList.create(Arrays.asList(TIME_SERIES_CUMULATIVE, null));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            TimeSeriesGaugeList.create(Collections.singletonList(TIME_SERIES_GAUGE)),
            TimeSeriesGaugeList.create(Collections.singletonList(TIME_SERIES_GAUGE)))
        .addEqualityGroup(
            TimeSeriesCumulativeList.create(Collections.singletonList(TIME_SERIES_CUMULATIVE)),
            TimeSeriesCumulativeList.create(Collections.singletonList(TIME_SERIES_CUMULATIVE)))
        .addEqualityGroup(TimeSeriesGaugeList.create(Collections.<TimeSeriesGauge>emptyList()))
        .addEqualityGroup(
            TimeSeriesCumulativeList.create(Collections.<TimeSeriesCumulative>emptyList()))
        .testEquals();
  }

  @Test
  public void testMatch() {
    TimeSeriesList gaugeTimeSeriesList =
        TimeSeriesGaugeList.create(Collections.singletonList(TIME_SERIES_GAUGE));
    TimeSeriesList cumulativeTimeSeriesList =
        TimeSeriesCumulativeList.create(Collections.singletonList(TIME_SERIES_CUMULATIVE));

    final List<String> actual = new ArrayList<String>();
    for (TimeSeriesList timeSeriesList :
        Arrays.asList(cumulativeTimeSeriesList, gaugeTimeSeriesList)) {
      actual.add(
          timeSeriesList.match(
              Functions.returnConstant("TimeSeriesGaugeList"),
              Functions.returnConstant("TimeSeriesCumulativeList"),
              Functions.<String>throwAssertionError()));
    }
    assertThat(actual).containsExactly("TimeSeriesCumulativeList", "TimeSeriesGaugeList").inOrder();
  }
}
