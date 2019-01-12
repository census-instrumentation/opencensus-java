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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.tags.TagContext;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Status;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import org.junit.Test;

public class JaxrsClientFilterTest {

  JaxrsClientFilter filter = new JaxrsClientFilter();

  @Test
  public void testRequestFilter() throws Exception {
    URI uri = URI.create("https://mydomain/myresource");
    ClientRequestContext requestContext = mock(ClientRequestContext.class);
    when(requestContext.getUri()).thenReturn(uri);
    filter.filter(requestContext);
    verify(requestContext).getUri();
  }

  @Test
  public void testResponseFilter() throws Exception {
    Span span = new FakeSpan(SpanContext.INVALID, null);
    TagContext tagContext = mock(TagContext.class);

    HttpRequestContext context = createHttpRequestContext(span, tagContext);

    ClientRequestContext requestContext = mock(ClientRequestContext.class);
    when(requestContext.getProperty("opencensus.context")).thenReturn(context);

    ClientResponseContext responseContext = mock(ClientResponseContext.class);

    filter.filter(requestContext, responseContext);

    verify(requestContext).getProperty("opencensus.context");
    verify(responseContext, times(2)).getStatus();
  }

  static HttpRequestContext createHttpRequestContext(Span span, TagContext tagContext)
      throws Exception {
    Constructor<HttpRequestContext> constructor =
        HttpRequestContext.class.getDeclaredConstructor(Span.class, TagContext.class);
    constructor.setAccessible(true);
    return constructor.newInstance(span, tagContext);
  }

  static class FakeSpan extends Span {

    public FakeSpan(SpanContext context, EnumSet<Options> options) {
      super(context, options);
    }

    @Override
    public void putAttribute(String key, AttributeValue value) {}

    @Override
    public void putAttributes(Map<String, AttributeValue> attributes) {}

    @Override
    public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

    @Override
    public void addAnnotation(Annotation annotation) {}

    @Override
    public void addMessageEvent(MessageEvent messageEvent) {}

    @Override
    public void addLink(Link link) {}

    @Override
    public void setStatus(Status status) {}

    @Override
    public void end(EndSpanOptions options) {}
  }
}
