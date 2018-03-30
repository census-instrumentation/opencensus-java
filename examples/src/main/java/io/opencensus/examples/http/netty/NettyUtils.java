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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.AttributeKey;
import io.opencensus.trace.Span;
import io.opencensus.trace.propagation.TextFormat;
import java.util.logging.Logger;

/** Utilities for netty instrumentation. */
// TODO(hailongwen): consider extract this to opencensus-contrib-http-netty for reuse.
public class NettyUtils {
  private static final Logger logger = Logger.getLogger(NettyUtils.class.getName());

  private NettyUtils() {}

  /** Customized {@link TextFormat.Getter} for netty. */
  public static final TextFormat.Getter<HttpRequest> HTTP_REQUEST_GETTER =
      new TextFormat.Getter<HttpRequest>() {
        @Override
        public String get(HttpRequest carrier, String key) {
          return carrier.headers().get(key);
        }
      };

  /** Customized {@link TextFormat.Setter} for netty. */
  public static final TextFormat.Setter<HttpRequest> HTTP_REQUEST_SETTER =
      new TextFormat.Setter<HttpRequest>() {
        @Override
        public void put(HttpRequest carrier, String key, String value) {
          carrier.headers().set(key, value);
        }
      };

  /** Active {@link Span} in current {@link Channel}. */
  public static final AttributeKey<Span> OPENCENSUS_SPAN =
      AttributeKey.<Span>valueOf("OpenCensus.Netty.Span");

  /** {@link Throwable} in the {@link Channel} if any. */
  public static final AttributeKey<Throwable> OPENCENSUS_ERROR =
      AttributeKey.<Throwable>valueOf("OpenCensus.Netty.Error");

  /** {@link Response} in the {@link Channel} if any. */
  public static final AttributeKey<HttpResponse> OPENCENSUS_RESPONSE =
      AttributeKey.<HttpResponse>valueOf("OpenCensus.Netty.HttpResponse");

  /** Accumulated request size in the {@link Channel}. */
  public static final AttributeKey<Integer> OPENCENSUS_REQUEST_SIZE =
      AttributeKey.<Integer>valueOf("OpenCensus.Netty.RequestSize");

  /** Accumulated response size in the {@link Channel}. */
  public static final AttributeKey<Integer> OPENCENSUS_RESPONSE_SIZE =
      AttributeKey.<Integer>valueOf("OpenCensus.Netty.ResponseSize");

  /** Resets a {@link Channel}. */
  public static void resetChannel(Channel channel, Span span, HttpResponse response) {
    channel.attr(OPENCENSUS_SPAN).set(span);
    channel.attr(OPENCENSUS_REQUEST_SIZE).set(0);
    channel.attr(OPENCENSUS_RESPONSE_SIZE).set(0);
    channel.attr(OPENCENSUS_RESPONSE).set(response);
    channel.attr(OPENCENSUS_ERROR).set(null);
  }

  /** Adds size of the {@code content} to the accumulated request size. */
  public static void incrementRequestSize(Channel channel, HttpContent content) {
    ByteBuf buf = content.content();
    int size =
        buf.isReadable() ? buf.readableBytes() : (buf.isWritable() ? buf.writableBytes() : 0);
    int newSize = channel.attr(OPENCENSUS_REQUEST_SIZE).get() + size;
    channel.attr(OPENCENSUS_REQUEST_SIZE).set(newSize);
  }

  /** Adss size of the {@code content} to the accumulated response size. */
  public static void incrementResponseSize(Channel channel, HttpContent content) {
    ByteBuf buf = content.content();
    int size =
        buf.isReadable() ? buf.readableBytes() : (buf.isWritable() ? buf.writableBytes() : 0);
    int newSize = channel.attr(OPENCENSUS_RESPONSE_SIZE).get() + size;
    channel.attr(OPENCENSUS_RESPONSE_SIZE).set(newSize);
  }

  /** Sets current error in the {@link Channel}. */
  public static void setError(Channel channel, Throwable error) {
    channel.attr(OPENCENSUS_ERROR).set(error);
  }

  /** Returns current span in the {@link Channel}. */
  public static Span getSpan(Channel channel) {
    return channel.attr(OPENCENSUS_SPAN).get();
  }

  /** Returns current error in the {@link Channel}. */
  public static Throwable getError(Channel channel) {
    return channel.attr(OPENCENSUS_ERROR).get();
  }

  /** Returns current response in the {@link Channel}. */
  public static HttpResponse getResponse(Channel channel) {
    return channel.attr(OPENCENSUS_RESPONSE).get();
  }

  /** Returns current request size in the {@link Channel}. */
  public static int getRequestSize(Channel channel) {
    return channel.attr(NettyUtils.OPENCENSUS_REQUEST_SIZE).get();
  }

  /** Returns current response size in the {@link Channel}. */
  public static int getResponseSize(Channel channel) {
    return channel.attr(NettyUtils.OPENCENSUS_RESPONSE_SIZE).get();
  }
}
