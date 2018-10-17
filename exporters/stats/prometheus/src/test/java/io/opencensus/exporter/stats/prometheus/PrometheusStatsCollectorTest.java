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
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.ExportComponent;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link PrometheusStatsCollector}. */
@RunWith(JUnit4.class)
public class PrometheusStatsCollectorTest {
  private static final String METRIC_NAME = "my_metric";
  private static final String METRIC_DESCRIPTION = "metric description";
  private static final String METRIC_UNIT = "us";
  private static final String KEY_DESCRIPTION = "key description";
  private static final LabelKey K1_LABEL_KEY = LabelKey.create("k1", KEY_DESCRIPTION);
  private static final LabelKey K2_LABEL_KEY = LabelKey.create("k2", KEY_DESCRIPTION);
  private static final LabelValue V1_LABEL_VALUE = LabelValue.create("v1");
  private static final LabelValue V2_LABEL_VALUE = LabelValue.create("v2");
  private static final List<LabelKey> LABEL_KEY = Arrays.asList(K1_LABEL_KEY, K2_LABEL_KEY);
  private static final List<LabelValue> LABEL_VALUE = Arrays.asList(V1_LABEL_VALUE, V2_LABEL_VALUE);
  private static final List<LabelKey> LE_LABEL_KEY =
      Arrays.asList(K1_LABEL_KEY, LabelKey.create(LABEL_NAME_BUCKET_BOUND, KEY_DESCRIPTION));
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
          LABEL_KEY);
  private static final MetricDescriptor LE_LABEL_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
          LE_LABEL_KEY);
  private static final Distribution DISTRIBUTION =
      Distribution.create(
          5,
          22,
          135.22,
          BucketOptions.explicitOptions(Arrays.asList(1.0, 2.0, 5.0)),
          Arrays.asList(Bucket.create(0), Bucket.create(2), Bucket.create(2), Bucket.create(1)));
  private static final Value DISTRIBUTION_VALUE = Value.distributionValue(DISTRIBUTION);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Point DISTRIBUTION_POINT = Point.create(DISTRIBUTION_VALUE, TIMESTAMP);
  private static final io.opencensus.metrics.export.TimeSeries DISTRIBUTION_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          LABEL_VALUE, DISTRIBUTION_POINT, null);
  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, DISTRIBUTION_TIME_SERIES);
  private static final Metric LE_LABEL_METRIC =
      Metric.createWithOneTimeSeries(LE_LABEL_METRIC_DESCRIPTOR, DISTRIBUTION_TIME_SERIES);

  @Mock private MetricProducerManager mockMetricProducerManager;
  @Mock private MetricProducer mockMetricProducer;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(ImmutableSet.of(mockMetricProducer))
        .when(mockMetricProducerManager)
        .getAllMetricProducer();
    doReturn(Collections.singletonList(METRIC)).when(mockMetricProducer).getMetrics();
  }

  @Test
  public void testCollect() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(mockMetricProducerManager);
    assertThat(collector.collect())
        .containsExactly(
            new MetricFamilySamples(
                METRIC_NAME,
                Type.HISTOGRAM,
                METRIC_DESCRIPTION,
                Arrays.asList(
                    new Sample(
                        METRIC_NAME + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "1.0"),
                        0),
                    new Sample(
                        METRIC_NAME + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "2.0"),
                        2),
                    new Sample(
                        METRIC_NAME + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "5.0"),
                        4),
                    new Sample(
                        METRIC_NAME + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "+Inf"),
                        5),
                    new Sample(
                        METRIC_NAME + "_count",
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        5),
                    new Sample(
                        METRIC_NAME + "_sum",
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        22.0))));
  }

  @Test
  public void testCollect_SkipDistributionMetricWithLeLabelKey() {
    doReturn(Collections.singletonList(LE_LABEL_METRIC)).when(mockMetricProducer).getMetrics();
    PrometheusStatsCollector collector = new PrometheusStatsCollector(mockMetricProducerManager);
    assertThat(collector.collect()).isEmpty();
  }

  @Test
  public void testDescribe() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(mockMetricProducerManager);
    assertThat(collector.describe())
        .containsExactly(
            new MetricFamilySamples(
                METRIC_NAME, Type.HISTOGRAM, METRIC_DESCRIPTION, Collections.<Sample>emptyList()));
  }

  @Test
  public void testCollect_WithNoopViewManager() {
    PrometheusStatsCollector collector =
        new PrometheusStatsCollector(
            ExportComponent.newNoopExportComponent().getMetricProducerManager());
    assertThat(collector.collect()).isEmpty();
  }

  @Test
  public void testDescribe_WithNoopViewManager() {
    PrometheusStatsCollector collector =
        new PrometheusStatsCollector(
            ExportComponent.newNoopExportComponent().getMetricProducerManager());
    assertThat(collector.describe()).isEmpty();
  }
}
