/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.zpages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An {@link HttpHandler} that can be used to render HTML pages using any {@code ZPageHandler}. */
final class ZPageHttpHandler implements HttpHandler {

  private static final Tracer tracer = Tracing.getTracer();
  private static final String HTTP_SERVER = "HttpServer";
  private static final EndSpanOptions END_SPAN_OPTIONS =
      EndSpanOptions.builder().setSampleToLocalSpanStore(true).build();
  private final ZPageHandler zpageHandler;
  private final String httpServerSpanName;

  /** Constructs a new {@code ZPageHttpHandler}. */
  ZPageHttpHandler(ZPageHandler zpageHandler) {
    this.zpageHandler = zpageHandler;
    this.httpServerSpanName = HTTP_SERVER + zpageHandler.getUrlPath();
  }

  @Override
  public final void handle(HttpExchange httpExchange) throws IOException {
    Span span =
        tracer
            .spanBuilderWithExplicitParent(httpServerSpanName, null)
            .setRecordEvents(true)
            .startSpan();
    try (Scope ss = tracer.withSpan(span)) {
      span.putAttribute(
          "/http/method ", AttributeValue.stringAttributeValue(httpExchange.getRequestMethod()));
      httpExchange.sendResponseHeaders(200, 0);
      zpageHandler.emitHtml(
          uriQueryToMap(httpExchange.getRequestURI()), httpExchange.getResponseBody());
    } finally {
      httpExchange.close();
      span.end(END_SPAN_OPTIONS);
    }
  }

  @VisibleForTesting
  static Map<String, String> uriQueryToMap(URI uri) {
    String query = uri.getQuery();
    if (query == null) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<String, String>();
    for (String param : Splitter.on("&").split(query)) {
      List<String> splits = Splitter.on("=").splitToList(param);
      if (splits.size() > 1) {
        result.put(splits.get(0), splits.get(1));
      } else {
        result.put(splits.get(0), "");
      }
    }
    return result;
  }
}
