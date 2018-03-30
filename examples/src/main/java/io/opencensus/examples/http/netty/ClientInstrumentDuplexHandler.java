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

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.propagation.TextFormat;
import java.util.logging.Logger;

/**
 * A {@link ChannelDuplexHandler} that process both inbound and outbound messages for HTTP clients.
 *
 * <p>For outbound messages, it sets the propagation information into the {@link HttpRequest} and
 * starts a new {@link Span}.
 *
 * <p>For inbound messages, it closes the created {@link Span} after {@link HttpResponse} is
 * received. The {@link Span} will also be ended if no response is received.
 *
 * <p>This handler should be placed after the {@link io.netty.handler.codec.http.HttpClientCodec}.
 */
public final class ClientInstrumentDuplexHandler extends InstrumentDuplexHandler {
  private static final Logger logger =
      Logger.getLogger(ClientInstrumentDuplexHandler.class.getName());

  private final TextFormat textFormat;

  /**
   * Creates a handler.
   *
   * @param textFormat the {@link TextFormat} used in propagation.
   * @param handler the {@link HttpClientHandler} used to instrument calls.
   */
  public ClientInstrumentDuplexHandler(
      TextFormat textFormat, HttpClientHandler<HttpRequest, HttpResponse> handler) {
    super(handler);
    this.textFormat = textFormat;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object m, ChannelPromise promise) throws Exception {
    super.write(ctx, m, promise);
    Channel channel = ctx.channel();
    Span span = NettyUtils.getSpan(channel);
    if (m instanceof HttpRequest) {
      final HttpRequest request = (HttpRequest) m;
      span =
          ((HttpClientHandler) handler)
              .handleStart(textFormat, NettyUtils.HTTP_REQUEST_SETTER, request, request);
      NettyUtils.resetChannel(channel, span, null);
    }
    if (span != null) {
      if (m instanceof HttpContent) {
        NettyUtils.incrementRequestSize(channel, (HttpContent) m);
        if (m instanceof LastHttpContent) {
          handler.handleMessageSent(span, /* messageid = */ 0, NettyUtils.getRequestSize(channel));
        }
      }
    }
    // Pass on to next handler.
    ctx.write(m, promise);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel channel = ctx.channel();
    Span span = NettyUtils.getSpan(channel);
    if (span != null) {
      if (msg instanceof HttpResponse) {
        HttpResponse response = (HttpResponse) msg;
        NettyUtils.resetChannel(channel, span, response);
      }
      if (msg instanceof HttpContent) {
        NettyUtils.incrementResponseSize(channel, (HttpContent) msg);
        if (msg instanceof LastHttpContent) {
          handler.handleMessageReceived(
              span, /* messageId = */ 0, NettyUtils.getResponseSize(channel));
        }
      }
    }
    // Pass on to next handler.
    ctx.fireChannelRead(msg);
  }
}
