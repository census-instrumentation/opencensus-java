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
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.LABEL_NAME_QUANTILE;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_BUCKET;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_COUNT;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.SAMPLE_SUFFIX_SUM;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.convertToLabelNames;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link PrometheusExportUtils}. */
@RunWith(JUnit4.class)
public class PrometheusExportUtilsTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "my_metric";
  private static final String METRIC_NAME2 = "my_metric2";
  private static final String METRIC_NAME3 = "my_metric3";
  private static final String METRIC_DESCRIPTION = "metric description";
  private static final String METRIC_UNIT = "us";
  private static final String KEY_DESCRIPTION = "key description";

  private static final LabelKey K1_LABEL_KEY = LabelKey.create("k1", KEY_DESCRIPTION);
  private static final LabelKey K2_LABEL_KEY = LabelKey.create("k2", KEY_DESCRIPTION);
  private static final LabelKey K3_LABEL_KEY = LabelKey.create("k-3", KEY_DESCRIPTION);
  private static final LabelValue V1_LABEL_VALUE = LabelValue.create("v1");
  private static final LabelValue V2_LABEL_VALUE = LabelValue.create("v2");
  private static final LabelValue V3_LABEL_VALUE = LabelValue.create("v-3");
  private static final List<LabelKey> LABEL_KEY = Arrays.asList(K1_LABEL_KEY, K2_LABEL_KEY);
  private static final List<LabelValue> LABEL_VALUE = Arrays.asList(V1_LABEL_VALUE, V2_LABEL_VALUE);
  private static final List<LabelKey> LE_LABEL_KEY =
      Arrays.asList(K1_LABEL_KEY, LabelKey.create(LABEL_NAME_BUCKET_BOUND, KEY_DESCRIPTION));
  private static final List<LabelKey> QUNATILE_LABEL_KEY =
      Arrays.asList(K1_LABEL_KEY, LabelKey.create(LABEL_NAME_QUANTILE, KEY_DESCRIPTION));

  private static final io.opencensus.metrics.export.Distribution DISTRIBUTION =
      io.opencensus.metrics.export.Distribution.create(
          5,
          22,
          135.22,
          BucketOptions.explicitOptions(Arrays.asList(1.0, 2.0, 5.0)),
          Arrays.asList(Bucket.create(0), Bucket.create(2), Bucket.create(2), Bucket.create(1)));
  private static final Summary SUMMARY =
      Summary.create(
          22L,
          74.8,
          Snapshot.create(
              10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99, 10.2))));
  private static final Summary SUMMARY_2 =
      Summary.create(
          22L,
          74.8,
          Snapshot.create(
              10L,
              87.07,
              Arrays.asList(
                  ValueAtPercentile.create(99.5, 8.2), ValueAtPercentile.create(99, 10.2))));
  private static final Value DOUBLE_VALUE = Value.doubleValue(-5.5);
  private static final Value LONG_VALUE = Value.longValue(123456789);
  private static final Value DISTRIBUTION_VALUE = Value.distributionValue(DISTRIBUTION);
  private static final Value SUMMARY_VALUE = Value.summaryValue(SUMMARY);
  private static final Value SUMMARY_VALUE_2 = Value.summaryValue(SUMMARY_2);

  private static final MetricDescriptor CUMULATIVE_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_INT64,
          LABEL_KEY);
  private static final MetricDescriptor SUMMARY_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME2,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.SUMMARY,
          Collections.singletonList(K3_LABEL_KEY));
  private static final MetricDescriptor HISTOGRAM_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME3,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
          Collections.singletonList(K1_LABEL_KEY));
  private static final MetricDescriptor LE_LABEL_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
          LE_LABEL_KEY);
  private static final MetricDescriptor QUANTILE_LABEL_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.SUMMARY,
          QUNATILE_LABEL_KEY);

  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Point LONG_POINT = Point.create(LONG_VALUE, TIMESTAMP);
  private static final Point DISTRIBUTION_POINT = Point.create(DISTRIBUTION_VALUE, TIMESTAMP);
  private static final Point SUMMARY_POINT = Point.create(SUMMARY_VALUE, TIMESTAMP);

  private static final io.opencensus.metrics.export.TimeSeries LONG_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, LONG_POINT, null);
  private static final io.opencensus.metrics.export.TimeSeries DISTRIBUTION_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          Collections.singletonList(V3_LABEL_VALUE), DISTRIBUTION_POINT, null);
  private static final io.opencensus.metrics.export.TimeSeries SUMMARY_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          Collections.singletonList(V1_LABEL_VALUE), SUMMARY_POINT, null);

  private static final Metric LONG_METRIC =
      Metric.createWithOneTimeSeries(CUMULATIVE_METRIC_DESCRIPTOR, LONG_TIME_SERIES);
  private static final Metric DISTRIBUTION_METRIC =
      Metric.createWithOneTimeSeries(HISTOGRAM_METRIC_DESCRIPTOR, DISTRIBUTION_TIME_SERIES);
  private static final Metric SUMMARY_METRIC =
      Metric.createWithOneTimeSeries(SUMMARY_METRIC_DESCRIPTOR, SUMMARY_TIME_SERIES);

  @Test
  public void testConstants() {
    assertThat(SAMPLE_SUFFIX_BUCKET).isEqualTo("_bucket");
    assertThat(SAMPLE_SUFFIX_COUNT).isEqualTo("_count");
    assertThat(SAMPLE_SUFFIX_SUM).isEqualTo("_sum");
    assertThat(LABEL_NAME_BUCKET_BOUND).isEqualTo("le");
  }

  @Test
  public void getType() {
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.CUMULATIVE_INT64))
        .isEqualTo(Type.COUNTER);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.CUMULATIVE_DOUBLE))
        .isEqualTo(Type.COUNTER);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION))
        .isEqualTo(Type.HISTOGRAM);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.SUMMARY))
        .isEqualTo(Type.SUMMARY);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.GAUGE_INT64))
        .isEqualTo(Type.GAUGE);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.GAUGE_DOUBLE))
        .isEqualTo(Type.GAUGE);
    assertThat(PrometheusExportUtils.getType(MetricDescriptor.Type.GAUGE_DISTRIBUTION))
        .isEqualTo(Type.HISTOGRAM);
  }

  @Test
  public void createDescribableMetricFamilySamples() {
    assertThat(
            PrometheusExportUtils.createDescribableMetricFamilySamples(
                CUMULATIVE_METRIC_DESCRIPTOR, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME, Type.COUNTER, METRIC_DESCRIPTION, Collections.<Sample>emptyList()));
    assertThat(
            PrometheusExportUtils.createDescribableMetricFamilySamples(
                SUMMARY_METRIC_DESCRIPTOR, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME2, Type.SUMMARY, METRIC_DESCRIPTION, Collections.<Sample>emptyList()));
    assertThat(
            PrometheusExportUtils.createDescribableMetricFamilySamples(
                HISTOGRAM_METRIC_DESCRIPTOR, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME3, Type.HISTOGRAM, METRIC_DESCRIPTION, Collections.<Sample>emptyList()));
  }

  @Test
  public void createDescribableMetricFamilySamples_WithNamespace() {
    String namespace1 = "myorg";
    assertThat(
            PrometheusExportUtils.createDescribableMetricFamilySamples(
                CUMULATIVE_METRIC_DESCRIPTOR, namespace1))
        .isEqualTo(
            new MetricFamilySamples(
                namespace1 + '_' + METRIC_NAME,
                Type.COUNTER,
                METRIC_DESCRIPTION,
                Collections.<Sample>emptyList()));

    String namespace2 = "opencensus/";
    assertThat(
            PrometheusExportUtils.createDescribableMetricFamilySamples(
                CUMULATIVE_METRIC_DESCRIPTOR, namespace2))
        .isEqualTo(
            new MetricFamilySamples(
                "opencensus_" + METRIC_NAME,
                Type.COUNTER,
                METRIC_DESCRIPTION,
                Collections.<Sample>emptyList()));
  }

  @Test
  public void getSamples() {
    assertThat(
            PrometheusExportUtils.getSamples(
                METRIC_NAME, convertToLabelNames(LABEL_KEY), LABEL_VALUE, DOUBLE_VALUE))
        .containsExactly(
            new Sample(METRIC_NAME, Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"), -5.5));
    assertThat(
            PrometheusExportUtils.getSamples(
                METRIC_NAME,
                convertToLabelNames(Collections.singletonList(K3_LABEL_KEY)),
                Collections.singletonList(V3_LABEL_VALUE),
                LONG_VALUE))
        .containsExactly(
            new Sample(
                METRIC_NAME,
                Collections.singletonList("k_3"),
                Collections.singletonList("v-3"),
                123456789));
    assertThat(
            PrometheusExportUtils.getSamples(
                METRIC_NAME,
                convertToLabelNames(Arrays.asList(K1_LABEL_KEY, K3_LABEL_KEY)),
                Arrays.asList(V1_LABEL_VALUE, null),
                LONG_VALUE))
        .containsExactly(
            new Sample(
                METRIC_NAME, Arrays.asList("k1", "k_3"), Arrays.asList("v1", ""), 123456789));
    assertThat(
            PrometheusExportUtils.getSamples(
                METRIC_NAME,
                convertToLabelNames(Collections.singletonList(K3_LABEL_KEY)),
                Collections.singletonList(V3_LABEL_VALUE),
                SUMMARY_VALUE_2))
        .containsExactly(
            new Sample(
                METRIC_NAME + "_count",
                Collections.singletonList("k_3"),
                Collections.singletonList("v-3"),
                22),
            new Sample(
                METRIC_NAME + "_sum",
                Collections.singletonList("k_3"),
                Collections.singletonList("v-3"),
                74.8),
            new Sample(
                METRIC_NAME,
                Arrays.asList("k_3", LABEL_NAME_QUANTILE),
                Arrays.asList("v-3", "0.995"),
                8.2),
            new Sample(
                METRIC_NAME,
                Arrays.asList("k_3", LABEL_NAME_QUANTILE),
                Arrays.asList("v-3", "0.99"),
                10.2))
        .inOrder();
    assertThat(
            PrometheusExportUtils.getSamples(
                METRIC_NAME,
                convertToLabelNames(Collections.singletonList(K1_LABEL_KEY)),
                Collections.singletonList(V1_LABEL_VALUE),
                DISTRIBUTION_VALUE))
        .containsExactly(
            new Sample(
                METRIC_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "1.0"), 0),
            new Sample(
                METRIC_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "2.0"), 2),
            new Sample(
                METRIC_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "5.0"), 4),
            new Sample(
                METRIC_NAME + "_bucket", Arrays.asList("k1", "le"), Arrays.asList("v1", "+Inf"), 5),
            new Sample(
                METRIC_NAME + "_count",
                Collections.singletonList("k1"),
                Collections.singletonList("v1"),
                5),
            new Sample(
                METRIC_NAME + "_sum",
                Collections.singletonList("k1"),
                Collections.singletonList("v1"),
                22.0))
        .inOrder();
  }

  @Test
  public void getSamples_KeysAndValuesHaveDifferentSizes() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Keys and Values don't have same size.");
    PrometheusExportUtils.getSamples(
        METRIC_NAME,
        convertToLabelNames(Arrays.asList(K1_LABEL_KEY, K3_LABEL_KEY, K3_LABEL_KEY)),
        Arrays.asList(V1_LABEL_VALUE, V2_LABEL_VALUE),
        DISTRIBUTION_VALUE);
  }

  @Test
  public void createDescribableMetricFamilySamples_Histogram_DisallowLeLabelName() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(
        "Prometheus Histogram cannot have a label named 'le', "
            + "because it is a reserved label for bucket boundaries. "
            + "Please remove this key from your view.");
    PrometheusExportUtils.createDescribableMetricFamilySamples(LE_LABEL_METRIC_DESCRIPTOR, "");
  }

  @Test
  public void createDescribableMetricFamilySamples_Summary_DisallowQuantileLabelName() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(
        "Prometheus Summary cannot have a label named 'quantile', "
            + "because it is a reserved label. Please remove this key from your view.");
    PrometheusExportUtils.createDescribableMetricFamilySamples(
        QUANTILE_LABEL_METRIC_DESCRIPTOR, "");
  }

  @Test
  public void createMetricFamilySamples() {
    assertThat(PrometheusExportUtils.createMetricFamilySamples(LONG_METRIC, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME,
                Type.COUNTER,
                METRIC_DESCRIPTION,
                Collections.singletonList(
                    new Sample(
                        METRIC_NAME,
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        123456789))));
    assertThat(PrometheusExportUtils.createMetricFamilySamples(SUMMARY_METRIC, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME2,
                Type.SUMMARY,
                METRIC_DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        METRIC_NAME2 + "_count",
                        Collections.singletonList("k_3"),
                        Collections.singletonList("v1"),
                        22),
                    new Sample(
                        METRIC_NAME2 + "_sum",
                        Collections.singletonList("k_3"),
                        Collections.singletonList("v1"),
                        74.8),
                    new Sample(
                        METRIC_NAME2,
                        Arrays.asList("k_3", LABEL_NAME_QUANTILE),
                        Arrays.asList("v1", "0.99"),
                        10.2))));
    assertThat(PrometheusExportUtils.createMetricFamilySamples(DISTRIBUTION_METRIC, ""))
        .isEqualTo(
            new MetricFamilySamples(
                METRIC_NAME3,
                Type.HISTOGRAM,
                METRIC_DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        METRIC_NAME3 + "_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "1.0"),
                        0),
                    new Sample(
                        METRIC_NAME3 + "_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "2.0"),
                        2),
                    new Sample(
                        METRIC_NAME3 + "_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "5.0"),
                        4),
                    new Sample(
                        METRIC_NAME3 + "_bucket",
                        Arrays.asList("k1", "le"),
                        Arrays.asList("v-3", "+Inf"),
                        5),
                    new Sample(
                        METRIC_NAME3 + "_count",
                        Collections.singletonList("k1"),
                        Collections.singletonList("v-3"),
                        5),
                    new Sample(
                        METRIC_NAME3 + "_sum",
                        Collections.singletonList("k1"),
                        Collections.singletonList("v-3"),
                        22.0))));
  }

  @Test
  public void createMetricFamilySamples_WithNamespace() {
    String namespace = "opencensus_";
    assertThat(PrometheusExportUtils.createMetricFamilySamples(LONG_METRIC, namespace))
        .isEqualTo(
            new MetricFamilySamples(
                namespace + METRIC_NAME,
                Type.COUNTER,
                METRIC_DESCRIPTION,
                Collections.singletonList(
                    new Sample(
                        namespace + METRIC_NAME,
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        123456789))));
  }
}
