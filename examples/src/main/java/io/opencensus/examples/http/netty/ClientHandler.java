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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.CharsetUtil;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;

/** A handler that prints out whatever received from server. */
public final class ClientHandler extends SimpleChannelInboundHandler<HttpContent> {

  @Override
  public void channelRead0(ChannelHandlerContext ctx, HttpContent msg) {
    Span span = NettyUtils.getSpan(ctx.channel());
    SpanContext spanContext = span == null ? SpanContext.INVALID : span.getContext();
    if (msg.content().isReadable()) {
      String response = msg.content().toString(CharsetUtil.UTF_8);
      System.out.println(response);
    }
    ctx.close();
  }
}
