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

package io.opencensus.contrib.http.servlet;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.HttpExtractor;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class extracts attributes from {@link HttpServletRequest} and {@link HttpServletResponse}.
 */
@ExperimentalApi
class OcHttpServletExtractor extends HttpExtractor<HttpServletRequest, HttpServletResponse> {
  @Override
  public String getHost(HttpServletRequest request) {
    return request.getServerName();
  }

  @Override
  public String getMethod(HttpServletRequest request) {
    return request.getMethod();
  }

  @Override
  public String getPath(HttpServletRequest request) {
    // Path defined in the <a
    // href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md#attributes>spec</a>
    // is equivalent of URI in HttpServlet.
    return request.getRequestURI();
  }

  @Override
  public String getUserAgent(HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }

  @Override
  public int getStatusCode(@Nullable HttpServletResponse response) {
    if (response != null) {
      return response.getStatus();
    }
    return 0;
  }

  @Override
  public String getUrl(HttpServletRequest request) {
    // Url defined in the <a
    // href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md#attributes">spec</a>
    // is equivalent of URL + QueryString in HttpServlet.
    return request.getRequestURL().toString() + "?" + request.getQueryString();
  }

  @Override
  public String getRoute(HttpServletRequest request) {
    return "";
  }
}
