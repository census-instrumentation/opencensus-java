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

import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagKey;

/**
 * A helper class which holds OpenCensus's default HTTP {@link Measure}s and {@link TagKey}s.
 *
 * <p>{@link Measure}s and {@link TagKey}s in this class are all public for other
 * libraries/frameworks to reference and use.
 *
 * @since 0.13
 */
public class HttpMeasureConstants {

  private HttpMeasureConstants() {}

  private static final String UNIT_COUNT = "1";
  private static final String UNIT_SIZE = "By";
  private static final String UNIT_LATENCY = "ms";

  /**
   * {@link Measure} for the number of client-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_REQUEST_COUNT =
      Measure.MeasureLong.create(
          "opencensus.io/http/client/request_count",
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
          "opencensus.io/http/client/request_bytes",
          "Client-side HTTP request body size if set as ContentLength (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the client-side HTTP response body size (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_RESPONSE_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/client/response_bytes",
          "Client-side HTTP response body size (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the client-side roundtrip latency (from first byte of request headers sent
   * to last byte of response received).
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_CLIENT_LATENCY =
      Measure.MeasureDouble.create(
          "opencensus.io/http/client/latency",
          "Client-side roundtrip latency (from first byte of request headers sent to last byte "
              + "of response received)",
          UNIT_LATENCY);

  /**
   * {@link Measure} for the number of server-side HTTP requests started.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_REQUEST_COUNT =
      Measure.MeasureLong.create(
          "opencensus.io/http/server/request_count",
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
          "opencensus.io/http/server/request_bytes",
          "Server-side HTTP request body size if set as ContentLength (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the server-side HTTP response body size (uncompressed).
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_RESPONSE_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/server/response_bytes",
          "Server-side HTTP response body size (uncompressed)",
          UNIT_SIZE);

  /**
   * {@link Measure} for the server-side roundtrip latency (from first byte of request headers
   * received to last byte of response sent).
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "opencensus.io/http/server/latency",
          "Server-side roundtrip latency (from first byte of request headers received to last "
              + "byte of response sent)",
          UNIT_LATENCY);

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
}
