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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.MustBeClosed;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Tracing;
import java.io.Closeable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class implements {@link AsyncListener} to handle span completion for async request handling.
 */
@ExperimentalApi
public final class OcHttpServletListener implements Closeable, AsyncListener {
  private final HttpRequestContext context;
  private final HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>
      handler;

  OcHttpServletListener(
      HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler,
      HttpRequestContext context) {
    checkNotNull(context, "context");
    checkNotNull(handler, "handler");
    this.context = context;
    this.handler = handler;
  }

  @Override
  public void close() {}

  @Override
  public void onComplete(AsyncEvent event) {
    ServletResponse response = event.getSuppliedResponse();
    if (response instanceof HttpServletResponse) {
      OcHttpServletUtil.recordMessageSentEvent(handler, context, (HttpServletResponse) response);
    }
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        null);
    this.close();
  }

  @Override
  public void onError(AsyncEvent event) {
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        event.getThrowable());
  }

  @Override
  public void onStartAsync(AsyncEvent event) {
    AsyncContext eventAsyncContext = event.getAsyncContext();
    if (eventAsyncContext != null) {
      eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
    }
  }

  @Override
  public void onTimeout(AsyncEvent event) {
    handler.handleEnd(
        context,
        (HttpServletRequest) event.getSuppliedRequest(),
        (HttpServletResponse) event.getSuppliedResponse(),
        null);
  }

  @MustBeClosed
  Scope withSpan() {
    return Tracing.getTracer().withSpan(handler.getSpanFromContext(context));
  }
}
