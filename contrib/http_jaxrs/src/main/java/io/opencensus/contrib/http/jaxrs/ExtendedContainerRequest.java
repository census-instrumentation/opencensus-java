package io.opencensus.contrib.http.jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

/**
 * Contains both {@link ContainerRequestContext} and {@link ResourceInfo}.
 */
class ExtendedContainerRequest {
  private final ContainerRequestContext requestContext;
  private final ResourceInfo resourceInfo;

  public ExtendedContainerRequest(ContainerRequestContext requestContext,
      ResourceInfo resourceInfo) {
    this.requestContext = requestContext;
    this.resourceInfo = resourceInfo;
  }

  public ContainerRequestContext getRequestContext() {
    return requestContext;
  }

  public ResourceInfo getResourceInfo() {
    return resourceInfo;
  }
}
