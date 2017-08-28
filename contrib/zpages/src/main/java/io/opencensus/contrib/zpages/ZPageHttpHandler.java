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
import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** An {@link HttpHandler} that can be used to render HTML pages using any {@code ZPageHandler}. */
final class ZPageHttpHandler implements HttpHandler {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String HTTP_SERVER = "HttpServer";
  private final ZPageHandler zpageHandler;
  private final String httpServerSpanName;

  /** Constructs a new {@code ZPageHttpHandler}. */
  ZPageHttpHandler(ZPageHandler zpageHandler) {
    this.zpageHandler = zpageHandler;
    this.httpServerSpanName = HTTP_SERVER + zpageHandler.getUrlPath();
    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(Arrays.asList(httpServerSpanName));
  }

  @Override
  public final void handle(HttpExchange httpExchange) throws IOException {
    try (Scope ss =
        tracer
            .spanBuilderWithExplicitParent(httpServerSpanName, null)
            .setRecordEvents(true)
            .startScopedSpan()) {
      tracer
          .getCurrentSpan()
          .addAttributes(
              ImmutableMap.<String, AttributeValue>builder()
                  .put(
                      "RequestMethod",
                      AttributeValue.stringAttributeValue(httpExchange.getRequestMethod()))
                  .build());
      httpExchange.sendResponseHeaders(200, 0);
      zpageHandler.emitHtml(
          uriQueryToMap(httpExchange.getRequestURI()), httpExchange.getResponseBody());
    } finally {
      httpExchange.close();
    }
  }

  @VisibleForTesting
  static Map<String, String> uriQueryToMap(URI uri) {
    String query = uri.getQuery();
    if (query == null) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<String, String>();
    for (String param : query.split("&")) {
      String[] pair = param.split("=");
      if (pair.length > 1) {
        result.put(pair[0], pair[1]);
      } else {
        result.put(pair[0], "");
      }
    }
    return result;
  }
}
