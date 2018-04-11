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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.opencensus.contrib.http.HttpHandler;
import io.opencensus.trace.Span;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ChannelDuplexHandler} that handles exceptions during read/write and ends current span
 * when channel is closed.
 */
class InstrumentDuplexHandler extends ChannelDuplexHandler {
  private static final Logger logger = Logger.getLogger(InstrumentDuplexHandler.class.getName());

  HttpHandler<HttpRequest, HttpResponse> handler;

  /** Listener that handles write failure. */
  private static ChannelFutureListener WRITE_FAILURE_LISTENER =
      new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
          if (!future.isSuccess()) {
            logger.log(Level.SEVERE, "Error writing data", future.cause());
            NettyUtils.setError(future.channel(), future.cause());
          }
        }
      };

  InstrumentDuplexHandler(HttpHandler handler) {
    this.handler = handler;
  }

  private void endSpan(Channel channel, Span span) {
    HttpResponse response = NettyUtils.getResponse(channel);
    Throwable error = NettyUtils.getError(channel);
    handler.handleEnd(response, error, span);
    NettyUtils.resetChannel(channel, null, null);
  }

  /** Called when write channel is closed. */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    Channel channel = ctx.channel();
    Span span = NettyUtils.getSpan(channel);
    if (span != null) {
      endSpan(channel, span);
    }
    ctx.fireChannelInactive();
  }

  /** Add write failure listener. */
  @Override
  public void write(ChannelHandlerContext ctx, Object m, ChannelPromise promise) throws Exception {
    promise.addListener(WRITE_FAILURE_LISTENER);
  }

  /** Method that handles read failure. */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.log(Level.SEVERE, "Error reading data", cause);
    NettyUtils.setError(ctx.channel(), cause);
    ctx.close();
  }
}
