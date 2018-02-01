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
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ERROR_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_VIEW;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagValue;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Unit tests for {@link RpczZPageHandler}. */
@RunWith(JUnit4.class)
public class RpczZPageHandlerTest {

  @Mock private final ViewManager mockViewManager = Mockito.mock(ViewManager.class);

  private static final TagValue METHOD_1 = TagValue.create("method1");
  private static final TagValue METHOD_2 = TagValue.create("method2");
  private static final MeanData MEAN_DATA_1 = MeanData.create(5.5, 11);
  private static final MeanData MEAN_DATA_2 = MeanData.create(1, 3);
  private static final MeanData MEAN_DATA_3 = MeanData.create(1, 2);
  private static final DistributionData DISTRIBUTION_DATA =
      DistributionData.create(4.2, 5, 0.2, 16.3, 234.56, Arrays.asList(1L, 0L, 1L, 2L, 1L));
  private static final CumulativeData CUMULATIVE_DATA =
      CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(5000));
  private static final IntervalData INTERVAL_DATA = IntervalData.create(Timestamp.fromMillis(8000));

  @Test
  public void getUrl() {
    RpczZPageHandler handler = RpczZPageHandler.create(mockViewManager);
    assertThat(handler.getUrlPath()).isEqualTo("/rpcz");
  }

  @Test
  public void emitSummaryTableForEachMethod() {
    doReturn(
            ViewData.create(
                RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW,
                ImmutableMap.of(Arrays.asList(METHOD_1), MEAN_DATA_1),
                INTERVAL_DATA))
        .when(mockViewManager)
        .getView(RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW.getName());
    doReturn(
            ViewData.create(
                RPC_CLIENT_ERROR_COUNT_VIEW,
                ImmutableMap.of(
                    Arrays.asList(METHOD_1), MEAN_DATA_2, Arrays.asList(METHOD_2), MEAN_DATA_3),
                CUMULATIVE_DATA))
        .when(mockViewManager)
        .getView(RPC_CLIENT_ERROR_COUNT_VIEW.getName());
    doReturn(
            ViewData.create(
                RPC_CLIENT_REQUEST_BYTES_VIEW,
                ImmutableMap.of(Arrays.asList(METHOD_1), DISTRIBUTION_DATA),
                CUMULATIVE_DATA))
        .when(mockViewManager)
        .getView(RPC_CLIENT_REQUEST_BYTES_VIEW.getName());
    OutputStream output = new ByteArrayOutputStream();
    RpczZPageHandler handler = RpczZPageHandler.create(mockViewManager);
    handler.emitHtml(Maps.newHashMap(), output);
    assertThat(output.toString()).contains(METHOD_1.asString());
    assertThat(output.toString()).contains(METHOD_2.asString());
  }
}
