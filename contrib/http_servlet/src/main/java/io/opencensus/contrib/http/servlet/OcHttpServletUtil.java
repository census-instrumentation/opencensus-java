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

package io.opencensus.contrib.http.servlet;

import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Response;

class OcHttpServletUtil {
  static final String CONTENT_LENGTH = "Content-Length";

  static void recordMessageSentEvent(
      HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler,
      HttpRequestContext context,
      HttpServletResponse response) {
    if (response != null) {
      String length = response.getHeader(CONTENT_LENGTH);
      if (length != null && !length.isEmpty()) {
        try {
          handler.handleMessageSent(context, Integer.parseInt(length));
        } catch (NumberFormatException e) {
          return;
        }
      } else {
        if (response instanceof Response) {
          Response resp = (Response) response;
          handler.handleMessageSent(context, resp.getContentCount());
        }
      }
    }
  }
}
