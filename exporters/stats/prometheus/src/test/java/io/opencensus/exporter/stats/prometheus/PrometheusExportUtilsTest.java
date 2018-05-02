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

package io.opencensus.exporter.stats.prometheus;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.LABEL_NAME_BUCKET_BOUND;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.OPENCENSUS_HELP_MSG;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.OPENCENSUS_NAMESPACE;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_BUCKET;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_COUNT;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_SUM;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.convertToLabelNames;

import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link PrometheusExportUtils}. */
@RunWith(JUnit4.class)
public class PrometheusExportUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Duration ONE_SECOND = Duration.create(1, 0);
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Interval INTERVAL = Interval.create(ONE_SECOND);
  private static final Sum SUM = Sum.create();
  private static final Count COUNT = Count.create();
  private static final Mean MEAN = Mean.create();
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-5.0, 0.0, 5.0));
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final LastValue LAST_VALUE = LastValue.create();
  private static final View.Name VIEW_NAME_1 = View.Name.create("view1");
  private static final View.Name VIEW_NAME_2 = View.Name.create("view2");
  private static final View.Name VIEW_NAME_3 = View.Name.create("view-3");
  private static final View.Name VIEW_NAME_4 = View.Name.create("-view4");
  private static final String DESCRIPTION = "View description";
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure", "description", "1");
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k-3");
  private static final TagKey TAG_KEY_LE = TagKey.create(LABEL_NAME_BUCKET_BOUND);
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v-3");
  private static final SumDataDouble SUM_DATA_DOUBLE = SumDataDouble.create(-5.5);
  private static final SumDataLong SUM_DATA_LONG = SumDataLong.create(123456789);
  private static final CountData COUNT_DATA = CountData.create(12345);
  private static final MeanData MEAN_DATA = MeanData.create(3.4, 22);
  private static final DistributionData DISTRIBUTION_DATA =
      DistributionData.create(4.4, 5, -3.2, 15.7, 135.22, Arrays.asList(0L, 2L, 2L, 1L));
  private static final LastValueDataDouble LAST_VALUE_DATA_DOUBLE = LastValueDataDouble.create(7.9);
  private static final LastValueDataLong LAST_VALUE_DATA_LONG = LastValueDataLong.create(66666666);
  private static final View VIEW1 =
      View.create(
          VIEW_NAME_1, DESCRIPTION, MEASURE_DOUBLE, COUNT, Arrays.asList(K1, K2), CUMULATIVE);
  private static final View VIEW2 =
      View.create(VIEW_NAME_2, DESCRIPTION, MEASURE_DOUBLE, MEAN, Arrays.asList(K3), CUMULATIVE);
  private static final View VIEW3 =
      View.create(
          VIEW_NAME_3, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(K1), CUMULATIVE);
  private static final View VIEW4 =
      View.create(VIEW_NAME_4, DESCRIPTION, MEASURE_DOUBLE, COUNT, Arrays.asList(K1), INTERVAL);
  private static final View DISTRIBUTION_VIEW_WITH_LE_KEY =
      View.create(
          VIEW_NAME_1,
          DESCRIPTION,
          MEASURE_DOUBLE,
          DISTRIBUTION,
          Arrays.asList(K1, TAG_KEY_LE),
          CUMULATIVE);
  private static final CumulativeData CUMULATIVE_DATA =
      CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000));
  private static final IntervalData INTERVAL_DATA = IntervalData.create(Timestamp.fromMillis(1000));
  private static final String SAMPLE_NAME = OPENCENSUS_NAMESPACE + "_view";

  @Test
  public void testConstants() {
    assertThat(OPENCENSUS_NAMESPACE).isEqualTo("opencensus");
    assertThat(OPENCENSUS_HELP_MSG).isEqualTo("Opencensus Prometheus metrics: ");
    assertThat(SAMPLE_SUFFIX_BUCKET).isEqualTo("_bucket");
    assertThat(SAMPLE_SUFFIX_COUNT).isEqualTo("_count");
    assertThat(SAMPLE_SUFFIX_SUM).isEqualTo("_sum");
    assertThat(LABEL_NAME_BUCKET_BOUND).isEqualTo("le");
  }

  @Test
  public void getType() {
    assertThat(PrometheusExportUtils.getType(COUNT, INTERVAL)).isEqualTo(Type.UNTYPED);
    assertThat(PrometheusExportUtils.getType(COUNT, CUMULATIVE)).isEqualTo(Type.COUNTER);
    assertThat(PrometheusExportUtils.getType(DISTRIBUTION, CUMULATIVE)).isEqualTo(Type.HISTOGRAM);
    assertThat(PrometheusExportUtils.getType(SUM, CUMULATIVE)).isEqualTo(Type.UNTYPED);
    assertThat(PrometheusExportUtils.getType(MEAN, CUMULATIVE)).isEqualTo(Type.SUMMARY);
    assertThat(PrometheusExportUtils.getType(LAST_VALUE, CUMULATIVE)).isEqualTo(Type.GAUGE);
  }

  @Test
  public void createDescribableMetricFamilySamples() {
    assertThat(PrometheusExportUtils.createDescribableMetricFamilySamples(VIEW1))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view1",
                Type.COUNTER,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Collections.<Sample>emptyList()));
    assertThat(PrometheusExportUtils.createDescribableMetricFamilySamples(VIEW2))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view2",
                Type.SUMMARY,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Collections.<Sample>emptyList()));
    assertThat(PrometheusExportUtils.createDescribableMetricFamilySamples(VIEW3))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view_3",
                Type.HISTOGRAM,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Collections.<Sample>emptyList()));
    assertThat(PrometheusExportUtils.createDescribableMetricFamilySamples(VIEW4))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "__view4",
                Type.UNTYPED,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Collections.<Sample>emptyList()));
  }

  @Test
  public void getSamples() {
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K1, K2)),
                Arrays.asList(V1, V2),
                SUM_DATA_DOUBLE,
                SUM))
        .containsExactly(
            new Sample(SAMPLE_NAME, Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"), -5.5));
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K3)),
                Arrays.asList(V3),
                SUM_DATA_LONG,
                SUM))
        .containsExactly(
            new Sample(SAMPLE_NAME, Arrays.asList("k_3"), Arrays.asList("v-3"), 123456789));
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K1, K3)),
                Arrays.asList(V1, null),
                COUNT_DATA,
                COUNT))
        .containsExactly(
            new Sample(SAMPLE_NAME, Arrays.asList("k1", "k_3"), Arrays.asList("v1", ""), 12345));
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K3)),
                Arrays.asList(V3),
                MEAN_DATA,
                MEAN))
        .containsExactly(
            new Sample(SAMPLE_NAME + "_count", Arrays.asList("k_3"), Arrays.asList("v-3"), 22),
            new Sample(SAMPLE_NAME + "_sum", Arrays.asList("k_3"), Arrays.asList("v-3"), 74.8))
        .inOrder();
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K1)),
                Arrays.asList(V1),
                DISTRIBUTION_DATA,
                DISTRIBUTION))
        .containsExactly(
            new Sample(
                SAMPLE_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "-5.0"), 0),
            new Sample(
                SAMPLE_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "0.0"), 2),
            new Sample(
                SAMPLE_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "5.0"), 2),
            new Sample(
                SAMPLE_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "+Inf"), 1),
            new Sample(SAMPLE_NAME + "_count", Arrays.asList("k1"), Arrays.asList("v1"), 5),
            new Sample(SAMPLE_NAME + "_sum", Arrays.asList("k1"), Arrays.asList("v1"), 22.0))
        .inOrder();
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K1, K2)),
                Arrays.asList(V1, V2),
                LAST_VALUE_DATA_DOUBLE,
                LAST_VALUE))
        .containsExactly(
            new Sample(SAMPLE_NAME, Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"), 7.9));
    assertThat(
            PrometheusExportUtils.getSamples(
                SAMPLE_NAME,
                convertToLabelNames(Arrays.asList(K3)),
                Arrays.asList(V3),
                LAST_VALUE_DATA_LONG,
                LAST_VALUE))
        .containsExactly(
            new Sample(SAMPLE_NAME, Arrays.asList("k_3"), Arrays.asList("v-3"), 66666666));
  }

  @Test
  public void getSamples_KeysAndValuesHaveDifferentSizes() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label names and tag values have different sizes.");
    PrometheusExportUtils.getSamples(
        SAMPLE_NAME,
        convertToLabelNames(Arrays.asList(K1, K2, K3)),
        Arrays.asList(V1, V2),
        DISTRIBUTION_DATA,
        DISTRIBUTION);
  }

  @Test
  public void createDescribableMetricFamilySamples_Histogram_DisallowLeLabelName() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(
        "Prometheus Histogram cannot have a label named 'le', "
            + "because it is a reserved label for bucket boundaries. "
            + "Please remove this tag key from your view.");
    PrometheusExportUtils.createDescribableMetricFamilySamples(DISTRIBUTION_VIEW_WITH_LE_KEY);
  }

  @Test
  public void createMetricFamilySamples() {
    assertThat(
            PrometheusExportUtils.createMetricFamilySamples(
                ViewData.create(
                    VIEW1, ImmutableMap.of(Arrays.asList(V1, V2), COUNT_DATA), CUMULATIVE_DATA)))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view1",
                Type.COUNTER,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view1",
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        12345))));
    assertThat(
            PrometheusExportUtils.createMetricFamilySamples(
                ViewData.create(
                    VIEW2, ImmutableMap.of(Arrays.asList(V1), MEAN_DATA), CUMULATIVE_DATA)))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view2",
                Type.SUMMARY,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view2_count",
                        Arrays.asList("k_3"),
                        Arrays.asList("v1"),
                        22),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view2_sum",
                        Arrays.asList("k_3"),
                        Arrays.asList("v1"),
                        74.8))));
    assertThat(
            PrometheusExportUtils.createMetricFamilySamples(
                ViewData.create(
                    VIEW3, ImmutableMap.of(Arrays.asList(V3), DISTRIBUTION_DATA), CUMULATIVE_DATA)))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "_view_3",
                Type.HISTOGRAM,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "-5.0"),
                        0),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "0.0"),
                        2),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "5.0"),
                        2),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "+Inf"),
                        1),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_count",
                        Arrays.asList("k1"),
                        Arrays.asList("v-3"),
                        5),
                    new Sample(
                        OPENCENSUS_NAMESPACE + "_view_3_sum",
                        Arrays.asList("k1"),
                        Arrays.asList("v-3"),
                        22.0))));
    assertThat(
            PrometheusExportUtils.createMetricFamilySamples(
                ViewData.create(
                    VIEW4, ImmutableMap.of(Arrays.asList(V1), COUNT_DATA), INTERVAL_DATA)))
        .isEqualTo(
            new MetricFamilySamples(
                OPENCENSUS_NAMESPACE + "__view4",
                Type.UNTYPED,
                OPENCENSUS_HELP_MSG + DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        OPENCENSUS_NAMESPACE + "__view4",
                        Arrays.asList("k1"),
                        Arrays.asList("v1"),
                        12345))));
  }
}
