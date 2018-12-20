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

package io.opencensus.contrib.http.jetty.client;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.trace.Span;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;

/** This class extracts attributes from Http Client Request and Response. */
@ExperimentalApi
public final class HttpRequestListener
    implements Request.Listener, Response.ContentListener, Response.CompleteListener {

  private final Span parent;
  @VisibleForTesting final HttpClientHandler<Request, Response, Request> handler;
  @VisibleForTesting @Nullable HttpRequestContext context;

  HttpRequestListener(Span parent, HttpClientHandler<Request, Response, Request> handler) {
    this.parent = parent;
    this.handler = handler;
    this.context = null;
  }

  @Override
  public void onComplete(Result result) {
    if (context == null) {
      return;
    }
    if (result != null) {
      handler.handleEnd(context, result.getRequest(), result.getResponse(), result.getFailure());
    } else {
      handler.handleEnd(context, null, null, null);
    }
  }

  @Override
  public void onBegin(Request request) {
    context = handler.handleStart(parent, request, request);
  }

  @Override
  public void onContent(Request request, ByteBuffer content) {
    if (context != null) {
      handler.handleMessageSent(context, content.capacity());
    }
  }

  @Override
  public void onContent(Response response, ByteBuffer content) {
    if (context != null) {
      handler.handleMessageReceived(context, content.capacity());
    }
  }

  @Override
  public void onCommit(Request request) {}

  @Override
  public void onFailure(Request request, Throwable failure) {}

  @Override
  public void onHeaders(Request request) {}

  @Override
  public void onQueued(Request request) {}

  @Override
  public void onSuccess(Request request) {}
}
