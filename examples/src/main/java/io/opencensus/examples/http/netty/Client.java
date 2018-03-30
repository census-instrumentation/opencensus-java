/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.examples.http.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;

/**
 * A simple instrumented HTTP client that sends a GET to server root path and print out the result.
 */
public final class Client {
  private static final String HOST = System.getProperty("host", "127.0.0.1");
  private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

  /** Main entry for client. */
  public static void main(String[] args) throws Exception {
    LoggingTraceExporter.register();
    int expectedStatusCode = 200;
    if (args.length > 0) {
      try {
        expectedStatusCode = Integer.parseInt(args[0]);
      } catch (NumberFormatException nfe) {
        // ignore
      }
    }

    // Configure the client.
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class).handler(new ClientInitializer());

      Channel ch = b.connect(HOST, PORT).sync().channel();
      HttpRequest request =
          new DefaultFullHttpRequest(
              HttpVersion.HTTP_1_1, HttpMethod.GET, "?expected=" + expectedStatusCode);
      ch.writeAndFlush(request);
      ch.closeFuture().sync();

      try {
        System.err.println("Waiting for span export...");
        Thread.sleep(5000);
      } catch (InterruptedException ie) {
        // ignore
      }
    } finally {
      // Shut down executor threads to exit.
      group.shutdownGracefully();
    }
  }
}
