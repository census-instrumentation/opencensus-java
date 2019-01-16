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
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;

/**
 * Extracts information from JAX-RS container request and response.
 *
 * @since 0.19
 */
public class JaxrsContainerExtractor
    extends HttpExtractor<ExtendedContainerRequest, ContainerResponseContext> {

  @Nullable
  @Override
  public String getRoute(ExtendedContainerRequest request) {
    return resolveRoute(request.getResourceInfo());
  }

  @Nullable
  @Override
  public String getUrl(ExtendedContainerRequest request) {
    return request.getRequestContext().getUriInfo().getRequestUri().toString();
  }

  @Nullable
  @Override
  public String getHost(ExtendedContainerRequest request) {
    return request.getRequestContext().getHeaderString("host");
  }

  @Nullable
  @Override
  public String getMethod(ExtendedContainerRequest request) {
    return request.getRequestContext().getMethod();
  }

  @Nullable
  @Override
  public String getPath(ExtendedContainerRequest request) {
    return request.getRequestContext().getUriInfo().getPath();
  }

  @Nullable
  @Override
  public String getUserAgent(ExtendedContainerRequest request) {
    return request.getRequestContext().getHeaderString("user-agent");
  }

  @Override
  public int getStatusCode(@Nullable ContainerResponseContext response) {
    return response != null ? response.getStatus() : 0;
  }

  @Nullable
  @SuppressWarnings("dereference.of.nullable") // The annotations are checked
  private static String resolveRoute(ResourceInfo info) {
    StringBuilder path = new StringBuilder();

    Class<?> c = info.getResourceClass();
    if (c != null && c.isAnnotationPresent(Path.class)) {
      Path p = c.getAnnotation(Path.class);
      path.append(p.value());
    }

    Method m = info.getResourceMethod();
    if (m != null && m.isAnnotationPresent(Path.class)) {
      Path p = m.getAnnotation(Path.class);
      if (!endsWithSlash(path) && !p.value().startsWith("/")) {
        path.append("/");
      }
      if (endsWithSlash(path) && p.value().startsWith("/")) {
        path.deleteCharAt(path.lastIndexOf("/"));
      }
      path.append(p.value());
    }

    return path.length() == 0 ? null : path.toString();
  }

  private static boolean endsWithSlash(StringBuilder path) {
    return path.lastIndexOf("/") == (path.length() - 1);
  }
}
