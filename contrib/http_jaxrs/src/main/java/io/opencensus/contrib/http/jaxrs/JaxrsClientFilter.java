package io.opencensus.contrib.http.jaxrs;

import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS client request and response filter to provide instrumentation of client calls with
 * OpenCensus.
 *
 * @since 0.19
 */
@Provider
public class JaxrsClientFilter implements ClientRequestFilter, ClientResponseFilter {

  private static final String OPENCENSUS_CONTEXT = "opencensus.context";
  private static final Setter<ClientRequestContext> SETTER = new Setter<ClientRequestContext>() {
    @Override
    public void put(ClientRequestContext carrier, String key, String value) {
      carrier.getHeaders().putSingle(key, value);
    }
  };

  private final HttpClientHandler<ClientRequestContext, ClientResponseContext, ClientRequestContext> handler;

  public JaxrsClientFilter() {
    handler = new HttpClientHandler<ClientRequestContext, ClientResponseContext, ClientRequestContext>(
        Tracing.getTracer(),
        new JaxrsClientExtractor(),
        Tracing.getPropagationComponent().getTraceContextFormat(),
        SETTER
    );
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    HttpRequestContext context = handler.handleStart(null, requestContext, requestContext);
    requestContext.setProperty(OPENCENSUS_CONTEXT, context);
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {
    HttpRequestContext context = (HttpRequestContext) requestContext
        .getProperty(OPENCENSUS_CONTEXT);
    handler.handleEnd(context, requestContext, responseContext, null);
  }
}
