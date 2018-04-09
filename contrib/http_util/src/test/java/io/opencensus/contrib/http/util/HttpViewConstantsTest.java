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

import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link HttpViewConstants}. */
@RunWith(JUnit4.class)
public class HttpViewConstantsTest {

  @Test
  public void constants() {
    // Test aggregations, and their bucket boundaries (if they are Distribution).
    assertThat(HttpViewConstants.COUNT).isEqualTo(Count.create());
    assertThat(HttpViewConstants.SIZE_DISTRIBUTION).isInstanceOf(Distribution.class);
    assertThat(
            ((Distribution) HttpViewConstants.SIZE_DISTRIBUTION)
                .getBucketBoundaries()
                .getBoundaries())
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
    assertThat(HttpViewConstants.LATENCY_DISTRIBUTION).isInstanceOf(Distribution.class);
    assertThat(
            ((Distribution) HttpViewConstants.LATENCY_DISTRIBUTION)
                .getBucketBoundaries()
                .getBoundaries())
        .containsExactly(
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0, 40.0, 50.0,
            65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0, 500.0, 650.0, 800.0,
            1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0, 100000.0)
        .inOrder();

    // Test views.
    assertThat(HttpViewConstants.HTTP_CLIENT_COMPLETED_COUNT_VIEW.getName().asString())
        .contains("opencensus.io/http/client");
    assertThat(HttpViewConstants.HTTP_CLIENT_SENT_BYTES_VIEW.getName().asString())
        .contains("opencensus.io/http/client");
    assertThat(HttpViewConstants.HTTP_CLIENT_RECEIVED_BYTES_VIEW.getName().asString())
        .contains("opencensus.io/http/client");
    assertThat(HttpViewConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW.getName().asString())
        .contains("opencensus.io/http/client");
    assertThat(HttpViewConstants.HTTP_SERVER_COMPLETED_COUNT_VIEW.getName().asString())
        .contains("opencensus.io/http/server");
    assertThat(HttpViewConstants.HTTP_SERVER_RECEIVED_BYTES_VIEW.getName().asString())
        .contains("opencensus.io/http/server");
    assertThat(HttpViewConstants.HTTP_SERVER_SENT_BYTES_VIEW.getName().asString())
        .contains("opencensus.io/http/server");
    assertThat(HttpViewConstants.HTTP_SERVER_LATENCY_VIEW.getName().asString())
        .contains("opencensus.io/http/server");

    assertThat(HttpViewConstants.HTTP_CLIENT_COMPLETED_COUNT_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY);
    assertThat(HttpViewConstants.HTTP_CLIENT_SENT_BYTES_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES);
    assertThat(HttpViewConstants.HTTP_CLIENT_RECEIVED_BYTES_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES);
    assertThat(HttpViewConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY);
    assertThat(HttpViewConstants.HTTP_SERVER_COMPLETED_COUNT_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_SERVER_LATENCY);
    assertThat(HttpViewConstants.HTTP_SERVER_RECEIVED_BYTES_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_SERVER_RECEIVED_BYTES);
    assertThat(HttpViewConstants.HTTP_SERVER_SENT_BYTES_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_SERVER_SENT_BYTES);
    assertThat(HttpViewConstants.HTTP_SERVER_LATENCY_VIEW.getMeasure())
        .isEqualTo(HttpMeasureConstants.HTTP_SERVER_LATENCY);

    assertThat(HttpViewConstants.HTTP_CLIENT_COMPLETED_COUNT_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.COUNT);
    assertThat(HttpViewConstants.HTTP_CLIENT_SENT_BYTES_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.SIZE_DISTRIBUTION);
    assertThat(HttpViewConstants.HTTP_CLIENT_RECEIVED_BYTES_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.SIZE_DISTRIBUTION);
    assertThat(HttpViewConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.LATENCY_DISTRIBUTION);
    assertThat(HttpViewConstants.HTTP_SERVER_COMPLETED_COUNT_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.COUNT);
    assertThat(HttpViewConstants.HTTP_SERVER_RECEIVED_BYTES_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.SIZE_DISTRIBUTION);
    assertThat(HttpViewConstants.HTTP_SERVER_SENT_BYTES_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.SIZE_DISTRIBUTION);
    assertThat(HttpViewConstants.HTTP_SERVER_LATENCY_VIEW.getAggregation())
        .isEqualTo(HttpViewConstants.LATENCY_DISTRIBUTION);

    assertThat(HttpViewConstants.HTTP_CLIENT_COMPLETED_COUNT_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_CLIENT_METHOD, HttpMeasureConstants.HTTP_CLIENT_PATH);
    assertThat(HttpViewConstants.HTTP_CLIENT_SENT_BYTES_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_CLIENT_METHOD, HttpMeasureConstants.HTTP_CLIENT_PATH);
    assertThat(HttpViewConstants.HTTP_CLIENT_RECEIVED_BYTES_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_CLIENT_METHOD, HttpMeasureConstants.HTTP_CLIENT_PATH);
    assertThat(HttpViewConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_CLIENT_METHOD,
            HttpMeasureConstants.HTTP_CLIENT_PATH,
            HttpMeasureConstants.HTTP_CLIENT_STATUS);
    assertThat(HttpViewConstants.HTTP_SERVER_COMPLETED_COUNT_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_SERVER_METHOD, HttpMeasureConstants.HTTP_SERVER_PATH);
    assertThat(HttpViewConstants.HTTP_SERVER_RECEIVED_BYTES_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_SERVER_METHOD, HttpMeasureConstants.HTTP_SERVER_PATH);
    assertThat(HttpViewConstants.HTTP_SERVER_SENT_BYTES_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_SERVER_METHOD, HttpMeasureConstants.HTTP_SERVER_PATH);
    assertThat(HttpViewConstants.HTTP_SERVER_LATENCY_VIEW.getColumns())
        .containsExactly(
            HttpMeasureConstants.HTTP_SERVER_METHOD,
            HttpMeasureConstants.HTTP_SERVER_PATH,
            HttpMeasureConstants.HTTP_SERVER_STATUS);
  }
}
