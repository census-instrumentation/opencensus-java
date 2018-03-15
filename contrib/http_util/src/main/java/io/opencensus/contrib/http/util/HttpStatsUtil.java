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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import java.util.Arrays;
import java.util.Collections;

/**
 * A utility class that allows users to access OpenCensus default HTTP {@link Measure}s, {@link
 * TagKey}s and {@link View}s, and provides convenience to register HTTP views easily.
 *
 * <p>{@link Measure}s, {@link TagKey}s and {@link View}s in this class are all public for other
 * libraries/frameworks to reference and use.
 *
 * @since 0.13
 */
public class HttpStatsUtil {

  private HttpStatsUtil() {}

  private static final String UNIT_COUNT = "1";
  private static final String UNIT_SIZE = "By";
  private static final String UNIT_LATENCY = "ms";

  @VisibleForTesting
  static String getName(String clientOrServer, String shortName) {
    return String.format("opencensus.io/http/%s/%s", clientOrServer, shortName);
  }

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
   * {@link Measure} for the number of client-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_REQUEST_COUNT =
      Measure.MeasureLong.create(
          getName("client", "request_count"),
          "Number of client-side HTTP requests started",
          UNIT_COUNT);

  /**
   * {@link Measure} for the client-side HTTP request body size if set as ContentLength
   * (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_REQUEST_BYTES =
      Measure.MeasureLong.create(
          getName("client", "request_bytes"),
          "Client-side HTTP request body size if set as ContentLength (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the client-side HTTP response body size (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_RESPONSE_BYTES =
      Measure.MeasureLong.create(
          getName("client", "response_bytes"),
          "Client-side HTTP response body size (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the client-side end-to-end latency.
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_CLIENT_LATENCY =
      Measure.MeasureDouble.create(
          getName("client", "latency"), "Client-side end-to-end latency", UNIT_LATENCY);

  /**
   * {@link Measure} for the number of server-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_REQUEST_COUNT =
      Measure.MeasureLong.create(
          getName("server", "request_count"),
          "Number of server-side HTTP requests started",
          UNIT_COUNT);

  /**
   * {@link Measure} for the server-side HTTP request body size if set as ContentLength
   * (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_REQUEST_BYTES =
      Measure.MeasureLong.create(
          getName("server", "request_bytes"),
          "Server-side HTTP request body size if set as ContentLength (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the server-side HTTP response body size (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_RESPONSE_BYTES =
      Measure.MeasureLong.create(
          getName("server", "response_bytes"),
          "Server-side HTTP response body size (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the server-side end-to-end latency.
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          getName("server", "latency"), "Server-side end-to-end latency", UNIT_LATENCY);

  /**
   * {@link TagKey} for the value of the HTTP host header.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_HOST = TagKey.create("http.host");

  /**
   * {@link TagKey} for the numeric HTTP response status code. If a transport error occurred and no
   * status code was read, use "error" as the {@code TagValue}.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_STATUS_CODE = TagKey.create("http.status");

  /**
   * {@link TagKey} for the URL path (not including query string) in the request.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_PATH = TagKey.create("http.path");

  /**
   * {@link TagKey} for the HTTP method of the request, capitalized (GET, POST, etc.).
   *
   * @since 0.13
   */
  public static final TagKey HTTP_METHOD = TagKey.create("http.method");

  /**
   * {@link View} for count of client-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create(getName("client", "request_count")),
          "Count of client-side HTTP requests started",
          HTTP_CLIENT_REQUEST_COUNT,
          COUNT,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for size distribution of client-side HTTP request body.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create(getName("client", "request_bytes")),
          "Size distribution of client-side HTTP request body",
          HTTP_CLIENT_REQUEST_BYTES,
          SIZE_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for size distribution of client-side HTTP response body.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create(getName("client", "response_bytes")),
          "Size distribution of client-side HTTP response body",
          HTTP_CLIENT_RESPONSE_BYTES,
          SIZE_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for latency distribution of client-side HTTP requests.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_LATENCY_VIEW =
      View.create(
          View.Name.create(getName("client", "latency")),
          "Latency distribution of client-side HTTP requests",
          HTTP_CLIENT_LATENCY,
          LATENCY_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for client request count by HTTP method.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_REQUEST_COUNT_BY_METHOD_VIEW =
      View.create(
          View.Name.create(getName("client", "request_count_by_method")),
          "Client request count by HTTP method",
          HTTP_CLIENT_REQUEST_COUNT,
          COUNT,
          Arrays.asList(HTTP_METHOD));

  /**
   * {@link View} for client response count by status code.
   *
   * @since 0.13
   */
  public static final View HTTP_CLIENT_RESPONSE_COUNT_BY_STATUS_CODE_VIEW =
      View.create(
          View.Name.create(getName("client", "response_count_by_status_code")),
          "Client response count by status code",
          HTTP_CLIENT_LATENCY,
          COUNT,
          Arrays.asList(HTTP_STATUS_CODE));

  /**
   * {@link View} for count of server-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_REQUEST_COUNT_VIEW =
      View.create(
          View.Name.create(getName("server", "request_count")),
          "Count of HTTP server-side requests started",
          HTTP_SERVER_REQUEST_COUNT,
          COUNT,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for size distribution of server-side HTTP request body.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_REQUEST_BYTES_VIEW =
      View.create(
          View.Name.create(getName("server", "request_bytes")),
          "Size distribution of server-side HTTP request body",
          HTTP_SERVER_REQUEST_BYTES,
          SIZE_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for size distribution of server-side HTTP response body.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_RESPONSE_BYTES_VIEW =
      View.create(
          View.Name.create(getName("server", "response_bytes")),
          "Size distribution of server-side HTTP response body",
          HTTP_SERVER_RESPONSE_BYTES,
          SIZE_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for latency distribution of server-side HTTP requests.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_LATENCY_VIEW =
      View.create(
          View.Name.create(getName("server", "latency")),
          "Latency distribution of HTTP requests",
          HTTP_SERVER_LATENCY,
          LATENCY_DISTRIBUTION,
          Collections.<TagKey>emptyList());

  /**
   * {@link View} for server request count by HTTP method.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_REQUEST_COUNT_BY_METHOD_VIEW =
      View.create(
          View.Name.create(getName("server", "request_count_by_method")),
          "Server request count by HTTP method",
          HTTP_SERVER_REQUEST_COUNT,
          COUNT,
          Arrays.asList(HTTP_METHOD));

  /**
   * {@link View} for server response count by status code.
   *
   * @since 0.13
   */
  public static final View HTTP_SERVER_RESPONSE_COUNT_BY_STATUS_CODE_VIEW =
      View.create(
          View.Name.create(getName("server", "response_count_by_status_code")),
          "Server response count by status code",
          HTTP_SERVER_LATENCY,
          COUNT,
          Arrays.asList(HTTP_STATUS_CODE));

  @VisibleForTesting
  static final ImmutableSet<View> HTTP_SERVER_VIEWS_SET =
      ImmutableSet.of(
          HTTP_SERVER_REQUEST_COUNT_VIEW,
          HTTP_SERVER_REQUEST_BYTES_VIEW,
          HTTP_SERVER_RESPONSE_BYTES_VIEW,
          HTTP_SERVER_LATENCY_VIEW,
          HTTP_SERVER_REQUEST_COUNT_BY_METHOD_VIEW,
          HTTP_SERVER_RESPONSE_COUNT_BY_STATUS_CODE_VIEW);

  @VisibleForTesting
  static final ImmutableSet<View> HTTP_CLIENT_VIEWS_SET =
      ImmutableSet.of(
          HTTP_CLIENT_REQUEST_COUNT_VIEW,
          HTTP_CLIENT_REQUEST_BYTES_VIEW,
          HTTP_CLIENT_RESPONSE_BYTES_VIEW,
          HTTP_CLIENT_LATENCY_VIEW,
          HTTP_CLIENT_REQUEST_COUNT_BY_METHOD_VIEW,
          HTTP_CLIENT_RESPONSE_COUNT_BY_STATUS_CODE_VIEW);

  /**
   * Register all default client views.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllClientViews() {
    registerAllClientViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllClientViews(ViewManager viewManager) {
    for (View view : HTTP_CLIENT_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Register all default server views.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllServerViews() {
    registerAllServerViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllServerViews(ViewManager viewManager) {
    for (View view : HTTP_SERVER_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Register all default views. Equivalent with calling {@link #registerAllClientViews()} and
   * {@link #registerAllServerViews()}.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllViews() {
    registerAllViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllViews(ViewManager viewManager) {
    registerAllClientViews(viewManager);
    registerAllServerViews(viewManager);
  }
}
