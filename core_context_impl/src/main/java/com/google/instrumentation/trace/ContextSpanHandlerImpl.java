/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import com.google.instrumentation.common.NonThrowingCloseable;

import io.grpc.Context;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This is an implementation of the {@link ContextSpanHandler} using {@link io.grpc.Context}. Users
 * may interact directly with the {@code io.grpc.Context} using the key {@link
 * GrpcTraceUtils#getContextSpanKey}.
 *
 * <p>Users of the instrumentation library must propagate the {@code io.grpc.Context} between
 * different threads. See {@link io.grpc.Context}.
 *
 * <p>{@code io.grpc.Context} is a gRPC independent implementation for in-process Context
 * propagation mechanism which can carry scoped-values across API boundaries and between threads.
 */
@Immutable
public final class ContextSpanHandlerImpl extends ContextSpanHandler {
  /**
   * Constructor for {@code ContextSpanHandlerImpl}. Needs to be public to allow loading with
   * reflection.
   */
  public ContextSpanHandlerImpl() {}

  @Override
  @Nullable
  public Span getCurrentSpan() {
    return GrpcTraceUtils.getContextSpanKey().get(Context.current());
  }

  @Override
  public NonThrowingCloseable withSpan(Span span) {
    return new WithSpan(span, GrpcTraceUtils.getContextSpanKey());
  }

  // Defines an arbitrary scope of code as a traceable operation. Supports try-with-resources idiom.
  private static final class WithSpan implements NonThrowingCloseable {
    private final Context origContext;

    /**
     * Constructs a new {@link WithSpan}.
     *
     * @param span is the {@code Span} to be added to the current {@code io.grpc.Context}.
     * @param contextKey is the {@code Context.Key} used to set/get {@code Span} from the {@code
     *     Context}.
     */
    WithSpan(Span span, Context.Key<Span> contextKey) {
      origContext = Context.current().withValue(contextKey, span).attach();
    }

    @Override
    public void close() {
      Context.current().detach(origContext);
    }
  }
}
