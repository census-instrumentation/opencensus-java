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

import static com.google.common.base.Preconditions.checkState;

import com.sun.net.httpserver.HttpServer;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A collection of HTML pages to display stats and trace data and allow library configuration
 * control.
 *
 * <p>Example usage with private {@link HttpServer}:
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     ZPageHandlers.startHttpServerAndRegisterAll(8000);
 *     ... // do work
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with shared {@link HttpServer}:
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
@ThreadSafe
public final class ZPageHandlers {
  // The HttpServer listening socket backlog (maximum number of queued incoming connections).
  private static final int BACKLOG = 5;
  // How many seconds to wait for the HTTP server to stop.
  private static final int STOP_DELAY = 1;
  private static final Logger logger = Logger.getLogger(ZPageHandler.class.getName());
  private static final ZPageHandler tracezZPageHandler =
      TracezZPageHandler.create(
          Tracing.getExportComponent().getRunningSpanStore(),
          Tracing.getExportComponent().getSampledSpanStore());
  private static final ZPageHandler traceConfigzZPageHandler =
      TraceConfigzZPageHandler.create(Tracing.getTraceConfig());

  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  private static HttpServer server;

  /**
   * Returns a {@code ZPageHandler} for tracing debug. The page displays information about all
   * active spans and all sampled spans based on latency and errors.
   *
   * <p>It prints a summary table which contains one row for each span name and data about number of
   * active and sampled spans.
   *
   * <p>If no sampled spans based on latency and error codes are available for a given name, make
   * sure that the span name is registered to the {@code SampledSpanStore}.
   *
   * @return a {@code ZPageHandler} for tracing debug.
   */
  public static ZPageHandler getTracezZPageHandler() {
    return tracezZPageHandler;
  }

  /**
   * Returns a {@code ZPageHandler} for tracing config. The page displays information about all
   * active configuration and allow changing the active configuration.
   *
   * @return a {@code ZPageHandler} for tracing config.
   */
  public static ZPageHandler getTraceConfigzZPageHandler() {
    return traceConfigzZPageHandler;
  }

  /**
   * Registers all pages to the given {@code HttpServer}.
   *
   * @param server the server that exports the tracez page.
   */
  public static void registerAllToHttpServer(HttpServer server) {
    server.createContext(tracezZPageHandler.getUrlPath(), new ZPageHttpHandler(tracezZPageHandler));
    server.createContext(
        traceConfigzZPageHandler.getUrlPath(), new ZPageHttpHandler(traceConfigzZPageHandler));
  }

  /**
   * Starts an {@code HttpServer} and registers all pages to it. When the JVM shuts down the server
   * is stopped.
   *
   * <p>Users must call this function only once per process.
   *
   * @param port the port used to bind the {@code HttpServer}.
   * @throws IllegalStateException if the server is already started.
   * @throws IOException if the server cannot bind to the requested address.
   */
  public static void startHttpServerAndRegisterAll(int port) throws IOException {
    synchronized (monitor) {
      checkState(server == null, "The HttpServer is already started.");
      server = HttpServer.create(new InetSocketAddress(port), BACKLOG);
      ZPageHandlers.registerAllToHttpServer(server);
      server.start();
      logger.fine("HttpServer started on address " + server.getAddress().toString());
    }

    // This does not need to be mutex protected because it is guaranteed that only one thread will
    // get ever here.
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.fine("*** Shutting down gRPC server (JVM shutting down)");
                ZPageHandlers.stop();
                logger.fine("*** Server shut down");
              }
            });
  }

  private static void stop() {
    synchronized (monitor) {
      // This should never happen because we register the shutdown hook only if we start the server.
      checkState(server != null, "The HttpServer is already stopped.");
      server.stop(STOP_DELAY);
      server = null;
    }
  }

  private ZPageHandlers() {}
}
