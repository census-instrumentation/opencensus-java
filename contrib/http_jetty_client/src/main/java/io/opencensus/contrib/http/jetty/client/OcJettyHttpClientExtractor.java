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

package io.opencensus.contrib.http.jetty.client;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.HttpExtractor;
import java.net.MalformedURLException;
import javax.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;

/** This class extracts attributes from {@link Request} and {@link Response}. */
@ExperimentalApi
final class OcJettyHttpClientExtractor extends HttpExtractor<Request, Response> {
  @Override
  public String getHost(Request request) {
    return request.getHost();
  }

  @Override
  public String getMethod(Request request) {
    return request.getMethod();
  }

  @Override
  public String getPath(Request request) {
    return request.getPath();
  }

  @Override
  public String getUserAgent(Request request) {
    return request.getHeaders().get("User-Agent");
  }

  @Override
  public int getStatusCode(@Nullable Response response) {
    if (response != null) {
      return response.getStatus();
    }
    return 0;
  }

  // TODO[rghetia] : make this configurable for user.
  @Override
  public String getRoute(Request request) {
    return "";
  }

  @Override
  public String getUrl(Request request) {
    try {
      return request.getURI().toURL().toString();
    } catch (MalformedURLException e) {
      return "";
    }
  }
}
