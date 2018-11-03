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

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.View.Name;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MetricUtils}. */
@RunWith(JUnit4.class)
public class MetricUtilsTest {

  private static final TagKey KEY = TagKey.create("KEY");
  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE_2");
  private static final String MEASURE_NAME = "my measurement";
  private static final String MEASURE_NAME_2 = "my measurement 2";
  private static final String MEASURE_UNIT = "us";
  private static final String MEASURE_DESCRIPTION = "measure description";
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);
  private static final MeasureLong MEASURE_LONG =
      MeasureLong.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT);
  private static final Name VIEW_NAME = Name.create("my view");
  private static final Name VIEW_NAME_2 = Name.create("my view 2");
  private static final String VIEW_DESCRIPTION = "view description";
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Interval INTERVAL = Interval.create(TEN_SECONDS);
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0));
  private static final Sum SUM = Sum.create();
  private static final Count COUNT = Count.create();
  private static final Mean MEAN = Mean.create();
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final LastValue LAST_VALUE = LastValue.create();
  private static final View VIEW_1 =
      View.create(
          VIEW_NAME, VIEW_DESCRIPTION, MEASURE_DOUBLE, LAST_VALUE, Collections.singletonList(KEY));
  private static final View VIEW_2 =
      View.create(
          VIEW_NAME_2,
          VIEW_DESCRIPTION,
          MEASURE_DOUBLE,
          MEAN,
          Collections.singletonList(KEY),
          INTERVAL);

  @Test
  public void viewToMetricDescriptor() {
    MetricDescriptor metricDescriptor = MetricUtils.viewToMetricDescriptor(VIEW_1);
    assertThat(metricDescriptor).isNotNull();
    assertThat(metricDescriptor.getName()).isEqualTo(VIEW_NAME.asString());
    assertThat(metricDescriptor.getUnit()).isEqualTo(MEASURE_UNIT);
    assertThat(metricDescriptor.getType()).isEqualTo(Type.GAUGE_DOUBLE);
    assertThat(metricDescriptor.getDescription()).isEqualTo(VIEW_DESCRIPTION);
    assertThat(metricDescriptor.getLabelKeys()).containsExactly(LabelKey.create(KEY.getName(), ""));
  }

  @Test
  public void viewToMetricDescriptor_NoIntervalViews() {
    MetricDescriptor metricDescriptor = MetricUtils.viewToMetricDescriptor(VIEW_2);
    assertThat(metricDescriptor).isNull();
  }

  @Test
  public void getType() {
    assertThat(MetricUtils.getType(MEASURE_DOUBLE, LAST_VALUE)).isEqualTo(Type.GAUGE_DOUBLE);
    assertThat(MetricUtils.getType(MEASURE_LONG, LAST_VALUE)).isEqualTo(Type.GAUGE_INT64);
    assertThat(MetricUtils.getType(MEASURE_DOUBLE, SUM)).isEqualTo(Type.CUMULATIVE_DOUBLE);
    assertThat(MetricUtils.getType(MEASURE_LONG, SUM)).isEqualTo(Type.CUMULATIVE_INT64);
    assertThat(MetricUtils.getType(MEASURE_DOUBLE, MEAN)).isEqualTo(Type.CUMULATIVE_DOUBLE);
    assertThat(MetricUtils.getType(MEASURE_LONG, MEAN)).isEqualTo(Type.CUMULATIVE_DOUBLE);
    assertThat(MetricUtils.getType(MEASURE_DOUBLE, COUNT)).isEqualTo(Type.CUMULATIVE_INT64);
    assertThat(MetricUtils.getType(MEASURE_LONG, COUNT)).isEqualTo(Type.CUMULATIVE_INT64);
    assertThat(MetricUtils.getType(MEASURE_DOUBLE, DISTRIBUTION))
        .isEqualTo(Type.CUMULATIVE_DISTRIBUTION);
    assertThat(MetricUtils.getType(MEASURE_LONG, DISTRIBUTION))
        .isEqualTo(Type.CUMULATIVE_DISTRIBUTION);
  }

  @Test
  public void tagValuesToLabelValues() {
    List<TagValue> tagValues = Arrays.asList(VALUE, VALUE_2, null);
    assertThat(MetricUtils.tagValuesToLabelValues(tagValues))
        .containsExactly(
            LabelValue.create(VALUE.asString()),
            LabelValue.create(VALUE_2.asString()),
            LabelValue.create(null));
  }
}
