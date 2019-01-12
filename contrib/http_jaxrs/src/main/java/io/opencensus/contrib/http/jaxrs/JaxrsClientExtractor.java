/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.contrib.http.jaxrs;

import io.opencensus.contrib.http.HttpExtractor;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

/**
 * Extracts information from JAX-RS client request and response.
 *
 * @since 0.19
 */
public class JaxrsClientExtractor
    extends HttpExtractor<ClientRequestContext, ClientResponseContext> {

  @Nullable
  @Override
  public String getRoute(ClientRequestContext request) {
    return null;
  }

  @Nullable
  @Override
  public String getUrl(ClientRequestContext request) {
    return request.getUri().toString();
  }

  @Nullable
  @Override
  public String getHost(ClientRequestContext request) {
    return request.getUri().getHost();
  }

  @Nullable
  @Override
  public String getMethod(ClientRequestContext request) {
    return request.getMethod();
  }

  @Nullable
  @Override
  public String getPath(ClientRequestContext request) {
    return request.getUri().getPath();
  }

  @Nullable
  @Override
  public String getUserAgent(ClientRequestContext request) {
    return request.getHeaderString("user-agent");
  }

  @Override
  public int getStatusCode(@Nullable ClientResponseContext response) {
    return response != null ? response.getStatus() : 0;
  }
}
