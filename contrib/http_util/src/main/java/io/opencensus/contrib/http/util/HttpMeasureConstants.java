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
public final class HttpMeasureConstants {

  private HttpMeasureConstants() {}

  private static final String UNIT_SIZE_BYTE = "By";
  private static final String UNIT_LATENCY_MS = "ms";

  /**
   * {@link Measure} for the client-side total bytes sent in request body (not including headers).
   * This is uncompressed bytes.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_SENT_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/client/sent_bytes",
          "Client-side total bytes sent in request body (uncompressed)",
          UNIT_SIZE_BYTE);

  /**
   * {@link Measure} for the client-side total bytes received in response bodies (not including
   * headers but including error responses with bodies). Should be measured from actual bytes
   * received and read, not the value of the Content-Length header. This is uncompressed bytes.
   * Responses with no body should record 0 for this value.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_CLIENT_RECEIVED_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/client/received_bytes",
          "Client-side total bytes received in response bodies (uncompressed)",
          UNIT_SIZE_BYTE);

  /**
   * {@link Measure} for the client-side time between first byte of request headers sent to last
   * byte of response received, or terminal error.
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_CLIENT_ROUNDTRIP_LATENCY =
      Measure.MeasureDouble.create(
          "opencensus.io/http/client/roundtrip_latency",
          "Client-side time between first byte of request headers sent to last byte of response "
              + "received, or terminal error",
          UNIT_LATENCY_MS);

  /**
   * {@link Measure} for the server-side total bytes received in request body (not including
   * headers). This is uncompressed bytes.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_RECEIVED_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/server/received_bytes",
          "Server-side total bytes received in request body (uncompressed)",
          UNIT_SIZE_BYTE);

  /**
   * {@link Measure} for the server-side total bytes sent in response bodies (not including headers
   * but including error responses with bodies). Should be measured from actual bytes written and
   * sent, not the value of the Content-Length header. This is uncompressed bytes. Responses with no
   * body should record 0 for this value.
   *
   * @since 0.13
   */
  public static final MeasureLong HTTP_SERVER_SENT_BYTES =
      Measure.MeasureLong.create(
          "opencensus.io/http/server/sent_bytes",
          "Server-side total bytes sent in response bodies (uncompressed)",
          UNIT_SIZE_BYTE);

  /**
   * {@link Measure} for the server-side time between first byte of request headers received to last
   * byte of response sent, or terminal error.
   *
   * @since 0.13
   */
  public static final MeasureDouble HTTP_SERVER_LATENCY =
      Measure.MeasureDouble.create(
          "opencensus.io/http/server/server_latency",
          "Server-side time between first byte of request headers received to last byte of "
              + "response sent, or terminal error",
          UNIT_LATENCY_MS);

  /**
   * {@link TagKey} for the value of the client-side HTTP host header.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_CLIENT_HOST = TagKey.create("http_client_host");

  /**
   * {@link TagKey} for the value of the server-side HTTP host header.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_SERVER_HOST = TagKey.create("http_server_host");

  /**
   * {@link TagKey} for the numeric client-side HTTP response status code (e.g. 200, 404, 500). If a
   * transport error occurred and no status code was read, use "error" as the {@code TagValue}.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_CLIENT_STATUS = TagKey.create("http_client_status");

  /**
   * {@link TagKey} for the numeric server-side HTTP response status code (e.g. 200, 404, 500). If a
   * transport error occurred and no status code was written, use "error" as the {@code TagValue}.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_SERVER_STATUS = TagKey.create("http_server_status");

  /**
   * {@link TagKey} for the client-side URL path (not including query string) in the request.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_CLIENT_PATH = TagKey.create("http_client_path");

  /**
   * {@link TagKey} for the server-side URL path (not including query string) in the request.
   *
   * @since 0.13
   */
  public static final TagKey HTTP_SERVER_PATH = TagKey.create("http_server_path");

  /**
   * {@link TagKey} for the client-side HTTP method of the request, capitalized (GET, POST, etc.).
   *
   * @since 0.13
   */
  public static final TagKey HTTP_CLIENT_METHOD = TagKey.create("http_client_method");

  /**
   * {@link TagKey} for the server-side HTTP method of the request, capitalized (GET, POST, etc.).
   *
   * @since 0.13
   */
  public static final TagKey HTTP_SERVER_METHOD = TagKey.create("http_server_method");

  /**
   * {@link TagKey} for the server-side logical route, a pattern that matched the URL, of a handler
   * that processed the request.
   *
   * @since 0.19
   */
  public static final TagKey HTTP_SERVER_ROUTE = TagKey.create("http_server_route");
}
