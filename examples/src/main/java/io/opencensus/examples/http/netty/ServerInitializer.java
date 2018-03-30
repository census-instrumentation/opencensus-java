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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.contrib.http.HttpSpanCustomizer;
import io.opencensus.trace.Tracing;

/** The {@link ChannelInitializer} to initialize the {@link ChannelPipeline} of the server. */
public final class ServerInitializer extends ChannelInitializer<SocketChannel> {

  /** Framework specific HTTP information extractor */
  private static final NettyHttpExtractor extractor = new NettyHttpExtractor();

  /** Business specific span customizer. */
  private static final HttpSpanCustomizer<HttpRequest, HttpResponse> customizer =
      new HttpSpanCustomizer<HttpRequest, HttpResponse>();

  /** Handler used to instrument calls. */
  private static final HttpServerHandler<HttpRequest, HttpResponse> serverHandler =
      new HttpServerHandler(Tracing.getTracer(), extractor, customizer);

  @Override
  public void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(new HttpServerCodec());
    // Add the instrument layer
    pipeline.addLast(new ServerInstrumentDuplexHandler(serverHandler));
    pipeline.addLast(new ServerHandler());
  }
}
