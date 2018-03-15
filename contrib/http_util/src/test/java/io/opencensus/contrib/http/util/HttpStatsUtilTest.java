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

package io.opencensus.contrib.http.util;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Test for {@link HttpStatsUtil}. */
@RunWith(JUnit4.class)
public class HttpStatsUtilTest {

  @Mock ViewManager viewManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getName() {
    assertThat(HttpStatsUtil.getName("client", "request_count"))
        .isEqualTo("opencensus.io/http/client/request_count");
    assertThat(HttpStatsUtil.getName("server", "request_bytes"))
        .isEqualTo("opencensus.io/http/server/request_bytes");
  }

  @Test
  public void constants() {
    // Test Tags
    assertThat(HttpStatsUtil.HTTP_STATUS_CODE).isNotNull();
    assertThat(HttpStatsUtil.HTTP_METHOD).isNotNull();
    assertThat(HttpStatsUtil.HTTP_PATH).isNotNull();
    assertThat(HttpStatsUtil.HTTP_HOST).isNotNull();

    // Test aggregations, and their bucket boundaries (if they are Distribution).
    assertThat(HttpStatsUtil.COUNT).isEqualTo(Count.create());
    assertThat(HttpStatsUtil.SIZE_DISTRIBUTION).isInstanceOf(Distribution.class);
    assertThat(
            ((Distribution) HttpStatsUtil.SIZE_DISTRIBUTION).getBucketBoundaries().getBoundaries())
        .containsExactly(
            0.0,
            1024.0,
            2048.0,
            4096.0,
            16384.0,
            65536.0,
            262144.0,
            1048576.0,
            4194304.0,
            16777216.0,
            67108864.0,
            268435456.0,
            1073741824.0,
            4294967296.0)
        .inOrder();
    assertThat(HttpStatsUtil.LATENCY_DISTRIBUTION).isInstanceOf(Distribution.class);
    assertThat(
            ((Distribution) HttpStatsUtil.LATENCY_DISTRIBUTION)
                .getBucketBoundaries()
                .getBoundaries())
        .containsExactly(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0, 40.0, 50.0,
            65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0, 500.0, 650.0, 800.0,
            1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0)
        .inOrder();

    // Test measurement descriptors.
    assertThat(HttpStatsUtil.HTTP_CLIENT_REQUEST_COUNT).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_REQUEST_BYTES).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_RESPONSE_BYTES).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_LATENCY).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_REQUEST_COUNT).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_REQUEST_BYTES).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_RESPONSE_BYTES).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_LATENCY).isNotNull();

    // Test view descriptors.
    assertThat(HttpStatsUtil.HTTP_CLIENT_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_LATENCY_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_REQUEST_COUNT_BY_METHOD_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_CLIENT_RESPONSE_COUNT_BY_STATUS_CODE_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_REQUEST_COUNT_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_REQUEST_BYTES_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_RESPONSE_BYTES_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_LATENCY_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_REQUEST_COUNT_BY_METHOD_VIEW).isNotNull();
    assertThat(HttpStatsUtil.HTTP_SERVER_RESPONSE_COUNT_BY_STATUS_CODE_VIEW).isNotNull();
  }

  @Test
  public void registerClientViews() {
    HttpStatsUtil.registerAllClientViews(viewManager);
    for (View view : HttpStatsUtil.HTTP_CLIENT_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }

  @Test
  public void registerServerViews() {
    HttpStatsUtil.registerAllServerViews(viewManager);
    for (View view : HttpStatsUtil.HTTP_SERVER_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }

  @Test
  public void registerAll() {
    HttpStatsUtil.registerAllViews(viewManager);
    for (View view : HttpStatsUtil.HTTP_CLIENT_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
    for (View view : HttpStatsUtil.HTTP_SERVER_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }
}
