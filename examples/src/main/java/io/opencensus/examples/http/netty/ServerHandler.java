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

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;

/** A handler that replies the propagated context information. */
public final class ServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
    int responseStatusCode = 200;
    try {
      responseStatusCode = Integer.parseInt(queryStringDecoder.parameters().get("expected").get(0));
    } catch (Throwable error) {
      // ignore
    }

    Span span = NettyUtils.getSpan(ctx.channel());
    SpanContext spanContext = span == null ? SpanContext.INVALID : span.getContext();
    String result = spanContext.toString();
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            HTTP_1_1,
            HttpResponseStatus.valueOf(responseStatusCode),
            Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }
}
