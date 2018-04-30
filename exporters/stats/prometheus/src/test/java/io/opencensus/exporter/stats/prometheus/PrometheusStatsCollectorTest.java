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
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link PrometheusStatsCollector}. */
@RunWith(JUnit4.class)
public class PrometheusStatsCollectorTest {

  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-5.0, 0.0, 5.0));
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final View.Name VIEW_NAME = View.Name.create("view1");
  private static final String DESCRIPTION = "View description";
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure", "description", "1");
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final DistributionData DISTRIBUTION_DATA =
      DistributionData.create(4.4, 5, -3.2, 15.7, 135.22, Arrays.asList(0L, 2L, 2L, 1L));
  private static final View VIEW =
      View.create(
          VIEW_NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(K1, K2), CUMULATIVE);
  private static final CumulativeData CUMULATIVE_DATA =
      CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000));
  private static final ViewData VIEW_DATA =
      ViewData.create(
          VIEW, ImmutableMap.of(Arrays.asList(V1, V2), DISTRIBUTION_DATA), CUMULATIVE_DATA);

  @Mock private ViewManager mockViewManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(ImmutableSet.of(VIEW)).when(mockViewManager).getAllExportedViews();
    doReturn(VIEW_DATA).when(mockViewManager).getView(VIEW_NAME);
  }

  @Test
  public void testCollect() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(mockViewManager);
    String name = "opencensus_view1";
    assertThat(collector.collect())
        .containsExactly(
            new MetricFamilySamples(
                "opencensus_view1",
                Type.HISTOGRAM,
                "Opencensus Prometheus metrics: View description",
                Arrays.asList(
                    new Sample(
                        name + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "-5.0"),
                        0),
                    new Sample(
                        name + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "0.0"),
                        2),
                    new Sample(
                        name + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "5.0"),
                        2),
                    new Sample(
                        name + "_bucket",
                        Arrays.asList("k1", "k2", "le"),
                        Arrays.asList("v1", "v2", "+Inf"),
                        1),
                    new Sample(
                        name + "_count", Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"), 5),
                    new Sample(
                        name + "_sum",
                        Arrays.asList("k1", "k2"),
                        Arrays.asList("v1", "v2"),
                        22.0))));
  }

  @Test
  public void testDescribe() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(mockViewManager);
    assertThat(collector.describe())
        .containsExactly(
            new MetricFamilySamples(
                "opencensus_view1",
                Type.HISTOGRAM,
                "Opencensus Prometheus metrics: View description",
                Collections.<Sample>emptyList()));
  }

  @Test
  public void testCollect_WithNoopViewManager() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(Stats.getViewManager());
    assertThat(collector.collect()).isEmpty();
  }

  @Test
  public void testDescribe_WithNoopViewManager() {
    PrometheusStatsCollector collector = new PrometheusStatsCollector(Stats.getViewManager());
    assertThat(collector.describe()).isEmpty();
  }
}
