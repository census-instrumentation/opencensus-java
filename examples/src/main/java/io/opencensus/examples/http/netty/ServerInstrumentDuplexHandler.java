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

import static io.opencensus.examples.http.netty.NettyUtils.HTTP_REQUEST_GETTER;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;

/**
 * A {@link ChannelDuplexHandler} that process both inbound and outbound messages for HTTP servers.
 *
 * <p>For inbound messages, it extracts propagation information from {@link HttpRequest} and set it
 * into current {@link ChannelHandlerContext} and starts a new {@link Span}.
 *
 * <p>For outbound messages, it closes the created {@link Span} after {@link HttpResponse} is sent.
 * The {@link Span} will also be ended if no response is sent.
 *
 * <p>This handler should be placed after {@link io.netty.handler.codec.http.HttpServerCodec}.
 */
public final class ServerInstrumentDuplexHandler extends InstrumentDuplexHandler {

  /**
   * Creates a handler.
   *
   * @param handler the {@link HttpServerHandler} used to instrument calls.
   */
  public ServerInstrumentDuplexHandler(HttpServerHandler<HttpRequest, HttpResponse> handler) {
    super(handler);
  }

  /**
   * Determine what {@link TextFormat} to use according to the context and request.
   *
   * <p>This allows client to explicitly specify what format is used, or allows server to infer the
   * format from headers.
   */
  public TextFormat getTextFormat(ChannelHandlerContext ctx, HttpRequest msg) {
    // For demonstration purpose, we simply return a B3Format.
    return Tracing.getPropagationComponent().getB3Format();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Channel channel = ctx.channel();
    Span span = NettyUtils.getSpan(channel);
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) msg;
      TextFormat textFormat = getTextFormat(ctx, request);
      span =
          ((HttpServerHandler) handler)
              .handleStart(textFormat, HTTP_REQUEST_GETTER, request, request);
      NettyUtils.resetChannel(channel, span, null);
    }
    if (span != null) {
      if (msg instanceof HttpContent) {
        NettyUtils.incrementRequestSize(channel, (HttpContent) msg);
        if (msg instanceof LastHttpContent) {
          handler.handleMessageReceived(
              span, /* messageId = */ 0, NettyUtils.getRequestSize(channel));
        }
      }
    }
    // Pass on to next handler.
    ctx.fireChannelRead(msg);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
    super.write(ctx, msg, promise);

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
          handler.handleMessageSent(span, /* messageId = */ 0, NettyUtils.getResponseSize(channel));
        }
      }
    }
    // Pass on to next handler.
    ctx.write(msg, promise);
  }
}
