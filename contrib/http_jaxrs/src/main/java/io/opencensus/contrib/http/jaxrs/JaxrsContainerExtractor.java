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
import java.util.List;
import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

/**
 * Extracts information from JAX-RS container request and response.
 *
 * @since 0.19
 */
public class JaxrsContainerExtractor extends
    HttpExtractor<ContainerRequestContext, ContainerResponseContext> {

  @Nullable
  @Override
  public String getRoute(ContainerRequestContext request) {
    List<String> uris = request.getUriInfo().getMatchedURIs();
    return uris.isEmpty() ? null : uris.get(0);
  }

  @Nullable
  @Override
  public String getUrl(ContainerRequestContext request) {
    return request.getUriInfo().getRequestUri().toString();
  }

  @Nullable
  @Override
  public String getHost(ContainerRequestContext request) {
    return request.getHeaderString("host");
  }

  @Nullable
  @Override
  public String getMethod(ContainerRequestContext request) {
    return request.getMethod();
  }

  @Nullable
  @Override
  public String getPath(ContainerRequestContext request) {
    return request.getUriInfo().getPath();
  }

  @Nullable
  @Override
  public String getUserAgent(ContainerRequestContext request) {
    return request.getHeaderString("user-agent");
  }

  @Override
  public int getStatusCode(@Nullable ContainerResponseContext response) {
    return response != null ? response.getStatus() : 0;
  }
}
