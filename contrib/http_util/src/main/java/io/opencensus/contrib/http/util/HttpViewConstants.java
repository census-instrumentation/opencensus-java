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

import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_METHOD;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_STATUS;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_LATENCY;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_METHOD;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_RECEIVED_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_ROUTE;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_SENT_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_STATUS;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.View;
import java.util.Arrays;
import java.util.Collections;

/**
 * A helper class that holds OpenCensus's default HTTP {@link View}s.
 *
 * <p>{@link View}s in this class are all public for other libraries/frameworks to reference and
 * use.
 *
 * @since 0.13
 */
public final class HttpViewConstants {

  private HttpViewConstants() {}

  @VisibleForTesting static final Aggregation COUNT = Count.create();

  @VisibleForTesting
  static final Aggregation SIZE_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Collections.<Double>unmodifiableList(
                  Arrays.<Double>asList(
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
                      4294967296.0))));

  @VisibleForTesting
  static final Aggregation LATENCY_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Collections.<Double>unmodifiableList(
                  Arrays.<Double>asList(
                      0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0, 16.0, 20.0, 25.0, 30.0,
                      40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0, 300.0, 400.0,
                      500.0, 650.0, 800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0,
                      100000.0))));

  /**
   * {@link View} for count of client-side HTTP requests completed.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_COMPLETED_COUNT_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/client/completed_count"),
          "Count of client-side HTTP requests completed",
          HTTP_CLIENT_ROUNDTRIP_LATENCY,
          COUNT,
          Arrays.asList(HTTP_CLIENT_METHOD, HTTP_CLIENT_STATUS));

  /**
   * {@link View} for size distribution of client-side HTTP request body.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_SENT_BYTES_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/client/sent_bytes"),
          "Size distribution of client-side HTTP request body",
          HTTP_CLIENT_SENT_BYTES,
          SIZE_DISTRIBUTION,
          Arrays.asList(HTTP_CLIENT_METHOD, HTTP_CLIENT_STATUS));

  /**
   * {@link View} for size distribution of client-side HTTP response body.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_RECEIVED_BYTES_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/client/received_bytes"),
          "Size distribution of client-side HTTP response body",
          HTTP_CLIENT_RECEIVED_BYTES,
          SIZE_DISTRIBUTION,
          Arrays.asList(HTTP_CLIENT_METHOD, HTTP_CLIENT_STATUS));

  /**
   * {@link View} for roundtrip latency distribution of client-side HTTP requests.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/client/roundtrip_latency"),
          "Roundtrip latency distribution of client-side HTTP requests",
          HTTP_CLIENT_ROUNDTRIP_LATENCY,
          LATENCY_DISTRIBUTION,
          Arrays.asList(HTTP_CLIENT_METHOD, HTTP_CLIENT_STATUS));

  /**
   * {@link View} for count of server-side HTTP requests serving completed.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_COMPLETED_COUNT_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/server/completed_count"),
          "Count of HTTP server-side requests serving completed",
          HTTP_SERVER_LATENCY,
          COUNT,
          Arrays.asList(HTTP_SERVER_METHOD, HTTP_SERVER_ROUTE, HTTP_SERVER_STATUS));

  /**
   * {@link View} for size distribution of server-side HTTP request body.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_RECEIVED_BYTES_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/server/received_bytes"),
          "Size distribution of server-side HTTP request body",
          HTTP_SERVER_RECEIVED_BYTES,
          SIZE_DISTRIBUTION,
          Arrays.asList(HTTP_SERVER_METHOD, HTTP_SERVER_ROUTE, HTTP_SERVER_STATUS));

  /**
   * {@link View} for size distribution of server-side HTTP response body.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_SENT_BYTES_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/server/sent_bytes"),
          "Size distribution of server-side HTTP response body",
          HTTP_SERVER_SENT_BYTES,
          SIZE_DISTRIBUTION,
          Arrays.asList(HTTP_SERVER_METHOD, HTTP_SERVER_ROUTE, HTTP_SERVER_STATUS));

  /**
   * {@link View} for latency distribution of server-side HTTP requests serving.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create("opencensus.io/http/server/server_latency"),
          "Latency distribution of server-side HTTP requests serving",
          HTTP_SERVER_LATENCY,
          LATENCY_DISTRIBUTION,
          Arrays.asList(HTTP_SERVER_METHOD, HTTP_SERVER_ROUTE, HTTP_SERVER_STATUS));
}
