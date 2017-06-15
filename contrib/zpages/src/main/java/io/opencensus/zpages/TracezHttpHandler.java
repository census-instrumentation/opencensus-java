/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.zpages;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.ExportComponent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@code HttpHandler} that displays information about active and sampled spans recorded using
 * the OpenCensus library.
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
 *     TracezHttpHandler.register(server);
 *     server.start();
 *   }
 * }
 * }</pre>
 */
public final class TracezHttpHandler implements HttpHandler {
  private static final Tracer tracer = Tracing.getTracer();
  private static final String TRACEZ_URL = "/tracez";
  private static final String HTTP_SERVER_SPAN_NAME = "HttpServer/tracez";
  private final TracezPageFormatter pageFormatter;

  /** Constructs a new {@code TracezHttpHandler}. */
  private TracezHttpHandler() {
    ExportComponent exportComponent = Tracing.getExportComponent();
    this.pageFormatter =
        TracezPageFormatter.create(
            exportComponent.getRunningSpanStore(), exportComponent.getSampledSpanStore());
    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(Arrays.asList(HTTP_SERVER_SPAN_NAME));
  }

  /**
   * Registers the tracez {@code HttpHandler} to the given {@code HttpServer}.
   *
   * @param server the server that exports the tracez page.
   */
  public static void register(HttpServer server) {
    server.createContext(TRACEZ_URL, new TracezHttpHandler());
  }

  @Override
  public final void handle(HttpExchange httpExchange) throws IOException {
    try (Scope ss =
        tracer
            .spanBuilderWithExplicitParent(HTTP_SERVER_SPAN_NAME, null)
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
      pageFormatter.emitHtml(queryToMap(httpExchange), httpExchange.getResponseBody());
      httpExchange.close();
    }
  }

  private static Map<String, String> queryToMap(HttpExchange httpExchange) {
    String query = httpExchange.getRequestURI().getQuery();
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
    tracer.getCurrentSpan().addAnnotation("Finish to parse query components.");
    return result;
  }
}
