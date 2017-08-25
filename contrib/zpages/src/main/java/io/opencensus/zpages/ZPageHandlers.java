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

package io.opencensus.zpages;

import com.sun.net.httpserver.HttpServer;
import io.opencensus.trace.Tracing;

/**
 * A collection of HTML pages to display stats and trace data and allow library configuration
 * control.
 *
 * <p>Example usage with {@link HttpServer}:
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
 *     ZPageHandlers.registerAllToHttpServer(server);
 *     server.start();
 *     ... // do work
 *   }
 * }
 * }</pre>
 */
public final class ZPageHandlers {
  private static final ZPageHandler tracezZPageHandler =
      TracezZPageHandler.create(
          Tracing.getExportComponent().getRunningSpanStore(),
          Tracing.getExportComponent().getSampledSpanStore());

  /**
   * Returns a {@code ZPageHandler} for tracing debug. The page displays information about all
   * active spans and all sampled spans based on latency and errors.
   *
   * <p>It prints a summary table which contains one row for each span name and data about number of
   * active and sampled spans.
   *
   * <p>To
   */
  public static ZPageHandler getTracezZPageHandler() {
    return tracezZPageHandler;
  }

  /**
   * Registers all pages to the given {@code HttpServer}.
   *
   * @param server the server that exports the tracez page.
   */
  public static void registerAllToHttpServer(HttpServer server) {
    server.createContext(tracezZPageHandler.getUrlPath(), new ZPageHttpHandler(tracezZPageHandler));
  }

  private ZPageHandlers() {}
}
