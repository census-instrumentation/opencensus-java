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

import io.opencensus.common.ExperimentalApi;
import javax.annotation.Nullable;

/**
 * An adaptor to extract information from request and response.
 *
 * <p>This class provides no-op implementations by default.
 *
 * <p>Please refer to this <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">document</a>
 * for more information about the HTTP attributes recorded in Open Census.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @since 0.19
 */
@ExperimentalApi
public abstract class HttpExtractor<Q, P> {

  /**
   * Returns the request route.
   *
   * @param request the HTTP request.
   * @return the request route.
   * @since 0.19
   */
  @Nullable
  public abstract String getRoute(Q request);

  /**
   * Returns the request URL.
   *
   * @param request the HTTP request.
   * @return the request URL.
   * @since 0.19
   */
  @Nullable
  public abstract String getUrl(Q request);

  /**
   * Returns the request URL host.
   *
   * @param request the HTTP request.
   * @return the request URL host.
   * @since 0.19
   */
  @Nullable
  public abstract String getHost(Q request);

  /**
   * Returns the request method.
   *
   * @param request the HTTP request.
   * @return the request method.
   * @since 0.19
   */
  @Nullable
  public abstract String getMethod(Q request);

  /**
   * Returns the request URL path.
   *
   * @param request the HTTP request.
   * @return the request URL path.
   * @since 0.19
   */
  @Nullable
  public abstract String getPath(Q request);

  /**
   * Returns the request user agent.
   *
   * @param request the HTTP request.
   * @return the request user agent.
   * @since 0.19
   */
  @Nullable
  public abstract String getUserAgent(Q request);

  /**
   * Returns the response status code. If the response is null, this method should return {@code 0}.
   *
   * @param response the HTTP response.
   * @return the response status code.
   * @since 0.19
   */
  public abstract int getStatusCode(@Nullable P response);
}
