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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.opencensus.contrib.http.HttpExtractor;
import javax.annotation.Nullable;

/** {@link HttpExtractor} customized for Netty. */
public class NettyHttpExtractor extends HttpExtractor<HttpRequest, HttpResponse> {

  @Override
  public String getHost(HttpRequest request) {
    return request.headers().get(HttpHeaderNames.HOST);
  }

  @Override
  public String getMethod(HttpRequest request) {
    return request.getMethod().name();
  }

  @Override
  public String getPath(HttpRequest request) {
    return new QueryStringDecoder(request.uri()).path();
  }

  @Override
  public String getUserAgent(HttpRequest request) {
    return request.headers().get(HttpHeaderNames.USER_AGENT);
  }

  @Override
  public int getStatusCode(@Nullable HttpResponse response) {
    return response == null ? 0 : response.status().code();
  }
}
