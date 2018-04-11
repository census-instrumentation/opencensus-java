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
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpSpanCustomizer;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;

/** The {@link ChannelInitializer} to initialize the {@link ChannelPipeline} of the client. */
public final class ClientInitializer extends ChannelInitializer<SocketChannel> {
  /** Framework specific HTTP information extractor. */
  private static final NettyHttpExtractor extractor = new NettyHttpExtractor();

  /** Business specific span customizer. */
  private static final HttpSpanCustomizer<HttpRequest, HttpResponse> customizer =
      new HttpSpanCustomizer<HttpRequest, HttpResponse>() {
        @Override
        public void customizeSpanBuilder(
            HttpRequest request,
            SpanBuilder builder,
            HttpExtractor<HttpRequest, HttpResponse> extractor) {
          builder.setSampler(Samplers.alwaysSample());
        }
      };

  /** Handler used to instrument calls. */
  private static final HttpClientHandler<HttpRequest, HttpResponse> clientHandler =
      new HttpClientHandler(Tracing.getTracer(), extractor, customizer);

  @Override
  public void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(new HttpClientCodec());
    // Add the instrument layer
    pipeline.addLast(
        new ClientInstrumentDuplexHandler(
            Tracing.getPropagationComponent().getB3Format(), clientHandler));
    pipeline.addLast(new ClientHandler());
  }
}
