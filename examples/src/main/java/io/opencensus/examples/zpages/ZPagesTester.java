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

package io.opencensus.examples.zpages;

import com.sun.net.httpserver.HttpServer;
import io.opencensus.contrib.zpages.ZPageHandlers;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Testing only class for the UI.
 */
public class ZPagesTester {
  private static final Logger logger = Logger.getLogger(ZPagesTester.class.getName());

  /** Main method. */
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
    ZPageHandlers.registerAllToHttpServer(server);
    server.start();
    logger.info(server.getAddress().toString());
  }

}
