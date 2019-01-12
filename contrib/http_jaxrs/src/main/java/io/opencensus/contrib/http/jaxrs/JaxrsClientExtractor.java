package io.opencensus.contrib.http.jaxrs;

import io.opencensus.contrib.http.HttpExtractor;
import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

public class JaxrsClientExtractor extends
    HttpExtractor<ClientRequestContext, ClientResponseContext> {

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
