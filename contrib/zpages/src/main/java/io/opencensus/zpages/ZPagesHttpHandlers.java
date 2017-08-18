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
import io.opencensus.trace.export.ExportComponent;

/**
 * A collection of HTML pages to display stats and trace data and allows library configuration
 * control.
 *
 * <p>Example usage with {@link HttpServer}:
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
 *     ZPagesHttpHandlers.register(server);
 *     server.start();
 *   }
 * }
 * }</pre>
 */
public final class ZPagesHttpHandlers {
  private static final String TRACEZ_URL = "/tracez";

  /**
   * Registers all pages to the given {@code HttpServer}.
   *
   * @param server the server that exports the tracez page.
   */
  public static void register(HttpServer server) {
    ExportComponent exportComponent = Tracing.getExportComponent();
    PageFormatter tracezPageFormatter =
        TracezPageFormatter.create(
            exportComponent.getRunningSpanStore(), exportComponent.getSampledSpanStore());
    server.createContext(TRACEZ_URL, new ZPageHttpHandler<>(tracezPageFormatter, TRACEZ_URL));
  }
}
