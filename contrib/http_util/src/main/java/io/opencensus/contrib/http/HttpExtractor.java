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

package io.opencensus.contrib.http;

import javax.annotation.Nullable;

/**
 * An adaptor to extract information from request and response.
 *
 * <p>Please refer to this <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTPAttributes.md">document</a>
 * for more information about the HTTP attributes recorded in Open Census.
 *
 * @since 0.13
 */
public abstract class HttpExtractor<Q, P> {

  /**
   * Returns the request URL host.
   *
   * @param request the HTTP request.
   * @return the request URL host.
   * @since 0.13
   */
  public abstract String getHost(Q request);

  /**
   * Returns the request method.
   *
   * @param request the HTTP request.
   * @return the request method.
   * @since 0.13
   */
  public abstract String getMethod(Q request);

  /**
   * Returns the request URL path.
   *
   * @param request the HTTP request.
   * @return the request URL path.
   * @since 0.13
   */
  public abstract String getPath(Q request);

  /**
   * Returns the matched request URL route. This is for server-side instrumentation to determine
   * route and handler.
   *
   * @param request the HTTP request.
   * @return the matched request URL route.
   * @since 0.13
   */
  public abstract String getRoute(Q request);

  /**
   * Returns the request user agent.
   *
   * @param request the HTTP request.
   * @return the request user agent.
   * @since 0.13
   */
  public abstract String getUserAgent(Q request);

  /**
   * Returns the response status code. If the response is null, this method should return {@code
   * null}.
   *
   * @param response the HTTP response.
   * @return the response status code.
   * @since 0.13
   */
  @Nullable
  public abstract Integer getStatusCode(@Nullable P response);

  /**
   * Returns the request message size.
   *
   * @param request the HTTP request.
   * @return the request message size.
   * @since 0.13
   */
  public abstract long getRequestSize(Q request);

  /**
   * Returns the response message size.
   *
   * @param response the HTTP response. If the response is null, this method should return {@code
   *     0}.
   * @return the response message size.
   * @since 0.13
   */
  public abstract long getResponseSize(@Nullable P response);
}
