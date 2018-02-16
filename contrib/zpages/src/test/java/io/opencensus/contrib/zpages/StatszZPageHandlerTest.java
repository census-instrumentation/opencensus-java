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

package io.opencensus.contrib.zpages;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_ERROR_COUNT;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_REQUEST_BYTES;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcMeasureConstants.RPC_SERVER_SERVER_LATENCY;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ERROR_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_LATENCY_VIEW;
import static io.opencensus.contrib.zpages.StatszZPageHandler.QUERY_PATH;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Unit tests for {@link StatszZPageHandler}. */
@RunWith(JUnit4.class)
public class StatszZPageHandlerTest {

  @Mock private final ViewManager mockViewManager = Mockito.mock(ViewManager.class);

  private static final View MY_VIEW =
      View.create(
          View.Name.create("my_view"),
          "My view",
          RPC_CLIENT_REQUEST_BYTES,
          Sum.create(),
          Arrays.asList(TagKey.create("my_key")),
          Cumulative.create());
  private static final TagValue METHOD_1 = TagValue.create("method1");
  private static final TagValue METHOD_2 = TagValue.create("method2");
  private static final TagValue METHOD_3 = TagValue.create("method3");
  private static final AggregationData.MeanData MEAN_DATA = AggregationData.MeanData.create(1, 3);
  private static final AggregationData.DistributionData DISTRIBUTION_DATA_1 =
      AggregationData.DistributionData.create(
          4.2,
          5,
          0.2,
          16.3,
          234.56,
          Arrays.asList(0L, 1L, 1L, 2L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
  private static final AggregationData.DistributionData DISTRIBUTION_DATA_2 =
      AggregationData.DistributionData.create(
          7.9,
          11,
          5.1,
          12.2,
          123.88,
          Arrays.asList(0L, 0L, 3L, 5L, 3L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
  private static final ViewData.AggregationWindowData.CumulativeData CUMULATIVE_DATA =
      ViewData.AggregationWindowData.CumulativeData.create(
          Timestamp.fromMillis(1000), Timestamp.fromMillis(5000));
  private static final ViewData VIEW_DATA_1 =
      ViewData.create(
          RPC_CLIENT_REQUEST_BYTES_VIEW,
          ImmutableMap.of(
              Arrays.asList(METHOD_1), DISTRIBUTION_DATA_1,
              Arrays.asList(METHOD_2), DISTRIBUTION_DATA_2),
          CUMULATIVE_DATA);
  private static final ViewData VIEW_DATA_2 =
      ViewData.create(
          RPC_CLIENT_ERROR_COUNT_VIEW,
          ImmutableMap.of(Arrays.asList(METHOD_3), MEAN_DATA),
          CUMULATIVE_DATA);

  @Before
  public void setUp() {
    doReturn(
            ImmutableSet.of(
                RPC_CLIENT_REQUEST_BYTES_VIEW,
                RPC_CLIENT_ERROR_COUNT_VIEW,
                RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW,
                RPC_SERVER_SERVER_LATENCY_VIEW,
                MY_VIEW))
        .when(mockViewManager)
        .getAllExportedViews();
    doReturn(VIEW_DATA_1)
        .when(mockViewManager)
        .getView(RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW.getName());
    doReturn(VIEW_DATA_2).when(mockViewManager).getView(RPC_CLIENT_ERROR_COUNT_VIEW.getName());
  }

  @Test
  public void getUrl() {
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);
    assertThat(handler.getUrlPath()).isEqualTo("/statsz");
  }

  @Test
  public void emitMeasures() {
    OutputStream output = new ByteArrayOutputStream();
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);
    handler.emitHtml(Maps.newHashMap(), output);
    assertContainsMeasure(output, RPC_CLIENT_REQUEST_BYTES);
    assertContainsMeasure(output, RPC_CLIENT_ERROR_COUNT);
    assertContainsMeasure(output, RPC_CLIENT_ROUNDTRIP_LATENCY);
    assertContainsMeasure(output, RPC_SERVER_SERVER_LATENCY);
  }

  @Test
  public void emitDirectoriesAndViews() {
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);

    OutputStream output1 = new ByteArrayOutputStream();
    handler.emitHtml(Maps.newHashMap(), output1);
    assertThat(output1.toString()).contains("grpc.io");
    assertThat(output1.toString()).contains("(4 views)");
    assertThat(output1.toString()).contains("my_view");

    OutputStream output2 = new ByteArrayOutputStream();
    handler.emitHtml(ImmutableMap.of(QUERY_PATH, "/grpc.io"), output2);
    assertThat(output2.toString()).contains("client");
    assertThat(output2.toString()).contains("(3 views)");
    assertThat(output2.toString()).contains("server");
    assertThat(output2.toString()).contains("(1 view)");

    OutputStream output3 = new ByteArrayOutputStream();
    handler.emitHtml(ImmutableMap.of(QUERY_PATH, "/grpc.io/client"), output3);
    assertThat(output3.toString()).contains("request_bytes");
    assertThat(output3.toString()).contains("error_count");
    assertThat(output3.toString()).contains("roundtrip_latency");
    assertThat(output3.toString()).contains("(1 view)");
  }

  @Test
  public void emitViewData() {
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);

    OutputStream output1 = new ByteArrayOutputStream();
    handler.emitHtml(
        ImmutableMap.of(QUERY_PATH, "/grpc.io/client/roundtrip_latency/cumulative"), output1);
    assertContainsViewData(output1, VIEW_DATA_1);

    OutputStream output2 = new ByteArrayOutputStream();
    handler.emitHtml(
        ImmutableMap.of(QUERY_PATH, "/grpc.io/client/error_count/cumulative"), output2);
    assertContainsViewData(output2, VIEW_DATA_2);
  }

  @Test
  public void nonExistingPath() {
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);
    OutputStream output = new ByteArrayOutputStream();
    handler.emitHtml(ImmutableMap.of(QUERY_PATH, "/unknown/unknown_view"), output);
    assertThat(output.toString())
        .contains("Directory not found: /unknown/unknown_view. Return to root.");
  }

  @Test
  public void viewWithNoStats() {
    StatszZPageHandler handler = StatszZPageHandler.create(mockViewManager);
    OutputStream output = new ByteArrayOutputStream();
    handler.emitHtml(ImmutableMap.of(QUERY_PATH, "/my_view"), output);
    assertThat(output.toString()).contains("No Stats found for View my_view.");
  }

  private static void assertContainsMeasure(OutputStream output, Measure measure) {
    assertThat(output.toString()).contains(measure.getName());
    assertThat(output.toString()).contains(measure.getDescription());
    assertThat(output.toString()).contains(measure.getUnit());
    String type =
        measure.match(
            Functions.returnConstant("Double"),
            Functions.returnConstant("Long"),
            Functions.throwAssertionError());
    assertThat(output.toString()).contains(type);
  }

  private static void assertContainsViewData(OutputStream output, ViewData viewData) {
    View view = viewData.getView();
    assertThat(output.toString()).contains(view.getName().asString());
    assertThat(output.toString()).contains(view.getDescription());
    assertThat(output.toString()).contains(view.getMeasure().getName());
    for (TagKey tagKey : view.getColumns()) {
      assertThat(output.toString()).contains(tagKey.getName());
    }
    String aggregationType =
        view.getAggregation()
            .match(
                Functions.returnConstant("Sum"),
                Functions.returnConstant("Count"),
                Functions.returnConstant("Mean"),
                Functions.returnConstant("Distribution"),
                Functions.<String>throwAssertionError());
    assertThat(output.toString()).contains(aggregationType);
    for (Map.Entry<List</*@Nullable*/ TagValue>, AggregationData> entry :
        viewData.getAggregationMap().entrySet()) {
      List<TagValue> tagValues = entry.getKey();
      for (TagValue tagValue : tagValues) {
        String tagValueStr = tagValue == null ? "" : tagValue.asString();
        assertThat(output.toString()).contains(tagValueStr);
      }
      entry
          .getValue()
          .match(
              Functions.</*@Nullable*/ Void>throwAssertionError(),
              Functions.</*@Nullable*/ Void>throwAssertionError(),
              Functions.</*@Nullable*/ Void>throwAssertionError(),
              new Function<AggregationData.MeanData, Void>() {
                @Override
                public Void apply(AggregationData.MeanData arg) {
                  assertThat(output.toString()).contains(String.valueOf(arg.getCount()));
                  assertThat(output.toString()).contains(String.valueOf(arg.getMean()));
                  return null;
                }
              },
              new Function<AggregationData.DistributionData, Void>() {
                @Override
                public Void apply(AggregationData.DistributionData arg) {
                  assertThat(output.toString()).contains(String.valueOf(arg.getCount()));
                  assertThat(output.toString()).contains(String.valueOf(arg.getMax()));
                  assertThat(output.toString()).contains(String.valueOf(arg.getMin()));
                  assertThat(output.toString()).contains(String.valueOf(arg.getMean()));
                  assertThat(output.toString())
                      .contains(String.valueOf(arg.getSumOfSquaredDeviations()));
                  return null;
                }
              },
              Functions.</*@Nullable*/ Void>throwAssertionError());
    }
  }
}
