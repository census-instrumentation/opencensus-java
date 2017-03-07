/*
 * Copyright 2016, Google Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.instrumentation.common.NonThrowingCloseable;
import com.google.instrumentation.common.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Tracer is a simple, singleton, thin class for {@link Span} creation and in-process context
 * interaction.
 *
 * <p>This can be used similarly to {@link java.util.logging.Logger}.
 *
 * <p>Users may choose to use manual or automatic Context propagation. Because of that this class
 * offers APIs to facilitate both usages.
 *
 * <p>Example usage with manual context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracer.getTracer();
 *   void DoWork(Span parent) {
 *     try(NonThrowingCloseable ss = tracer.spanBuilder(parent, "MyClass.DoWork").startScopedSpan) {
 *       tracer.getCurrentSpan().addAnnotation("We did the work.");
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracer.getTracer();
 *   void DoWork() {
 *     try(NonThrowingCloseable ss = tracer.spanBuilder("MyClass.DoWork").startScopedSpan) {
 *       tracer.getCurrentSpan().addAnnotation("We did the work.");
 *     }
 *   }
 * }
 * }</pre>
 */
public final class Tracer {
  private static final Logger logger = Logger.getLogger(Tracer.class.getName());
  private static final Tracer INSTANCE =
      new Tracer(
          loadContextSpanHandler(Provider.getCorrectClassLoader(ContextSpanHandler.class)),
          loadSpanFactory(Provider.getCorrectClassLoader(SpanFactory.class)));
  private final ContextSpanHandler contextSpanHandler;
  private final SpanFactory spanFactory;

  /**
   * Returns the {@link Tracer} with the provided implementations for {@link ContextSpanHandler} and
   * {@link SpanFactory}.
   *
   * <p>If no implementation is provided for any of the {@link Tracer} modules then no-op
   * implementations will be used.
   *
   * @return the {@link Tracer}.
   */
  public static Tracer getTracer() {
    return INSTANCE;
  }

  /**
   * Gets the current Span from the current Context.
   *
   * <p>To install a {@link Span} to the current Context use {@link #withSpan(Span)} OR use {@link
   * SpanBuilder#startScopedSpan} methods to start a new {@code Span}.
   *
   * <p>startSpan methods do NOT modify the current Context {@code Span}.
   *
   * @return a default {@code Span} that does nothing and has an invalid {@link SpanContext} if no
   *     {@code Span} is associated with the current Context, otherwise the current {@code Span}
   *     from the Context.
   */
  public Span getCurrentSpan() {
    Span currentSpan = contextSpanHandler.getCurrentSpan();
    return currentSpan != null ? currentSpan : BlankSpan.INSTANCE;
  }

  /**
   * Enters the scope of code where the given {@link Span} is in the current Context, and returns an
   * object that represents that scope. The scope is exited when the returned object is closed.
   *
   * <p>Supports try-with-resource idiom.
   *
   * <p>Can be called with {@link BlankSpan} to enter a scope of code where tracing is stopped.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * private static Tracer tracer = Tracer.getTracer();
   * void doWork {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.startSpan("my span");
   *   try (NonThrowingCloseable ws = tracer.withSpan(span)) {
   *     tracer.getCurrentSpan().addAnnotation("my annotation");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   }
   *   span.end();
   * }
   * }</pre>
   *
   * <p>Prior to Java SE 7, you can use a finally block to ensure that a resource is closed
   * regardless of whether the try statement completes normally or abruptly.
   *
   * <p>Example of usage prior to Java SE7:
   *
   * <pre>{@code
   * private static Tracer tracer = Tracer.getTracer();
   * void doWork {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.startSpan("my span");
   *   NonThrowingCloseable ws = tracer.withSpan(span);
   *   try {
   *     tracer.getCurrentSpan().addAnnotation("my annotation");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   } finally {
   *     ws.close();
   *   }
   *   span.end();
   * }
   * }</pre>
   *
   * @param span The {@link Span} to be set to the current Context.
   * @return an object that defines a scope where the given {@link Span} will be set to the current
   *     Context.
   * @throws NullPointerException if span is null.
   */
  public NonThrowingCloseable withSpan(Span span) {
    return contextSpanHandler.withSpan(checkNotNull(span, "span"));
  }

  /**
   * Returns a {@link SpanBuilder} to create and start a new child {@link Span} as a child of to the
   * current {@code Span} if any, otherwise create a root Span with the default options.
   *
   * <p>See {@link SpanBuilder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when automatic Context propagation is
   * used.
   *
   * @param name The name of the returned Span.
   * @return a {@code SpanBuilder} to create and start a new {@code Span}.
   * @throws NullPointerException if name is null.
   */
  public SpanBuilder spanBuilder(String name) {
    return spanBuilder(contextSpanHandler.getCurrentSpan(), name);
  }

  /**
   * Returns a {@link SpanBuilder} to create and start a new child {@link Span} (or root if parent
   * is null), with parent being the designated {@code Span}.
   *
   * <p>See {@link SpanBuilder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when manual Context propagation is used.
   *
   * @param parent The parent of the returned Span. If null the {@code SpanBuilder} will build a
   *     root {@code Span}.
   * @param name The name of the returned Span.
   * @return a {@code SpanBuilder} to create and start a new {@code Span}.
   * @throws NullPointerException if name is null.
   */
  public SpanBuilder spanBuilder(@Nullable Span parent, String name) {
    return new SpanBuilder(
        spanFactory,
        contextSpanHandler,
        parent == null ? null : parent.getContext(),
        /* hasRemoteParent = */ false,
        checkNotNull(name, "name"));
  }

  /**
   * Returns a {@link SpanBuilder} to create and start a new child {@link Span} (or root if parent
   * is null), with parent being the {@link Span} designated by the {@link SpanContext}.
   *
   * <p>See {@link SpanBuilder} for usage examples.
   *
   * <p>This <b>must</b> be used to create a {@code Span} when the parent is in a different process.
   * This is only intended for use by RPC systems or similar.
   *
   * @param remoteParent The remote parent of the returned Span.
   * @param name The name of the returned Span.
   * @return a {@code SpanBuilder} to create and start a new {@code Span}.
   * @throws NullPointerException if name is null.
   */
  public SpanBuilder spanBuilderWithRemoteParent(@Nullable SpanContext remoteParent, String name) {
    return new SpanBuilder(
        spanFactory,
        contextSpanHandler,
        remoteParent,
        /* hasRemoteParent = */ true,
        checkNotNull(name, "name"));
  }

  @VisibleForTesting
  Tracer(ContextSpanHandler contextSpanHandler, SpanFactory spanFactory) {
    this.contextSpanHandler = checkNotNull(contextSpanHandler, "contextSpanHandler");
    this.spanFactory = checkNotNull(spanFactory, "spanFactory");
  }

  // No-op implementation of the ContextSpanHandler
  private static final class NoopContextSpanHandler extends ContextSpanHandler {
    private static final NonThrowingCloseable defaultWithSpan =
        new NonThrowingCloseable() {
          @Override
          public void close() {
            // Do nothing.
          }
        };

    @Override
    @Nullable
    public Span getCurrentSpan() {
      return null;
    }

    @Override
    public NonThrowingCloseable withSpan(Span span) {
      return defaultWithSpan;
    }
  }

  // No-op implementation of the SpanFactory
  private static final class NoopSpanFactory extends SpanFactory {
    @Override
    public Span startSpan(
        @Nullable SpanContext parent,
        boolean hasRemoteParent,
        String name,
        StartSpanOptions options) {
      return BlankSpan.INSTANCE;
    }
  }

  // Any provider that may be used for SpanFactory can be added here.
  @VisibleForTesting
  static SpanFactory loadSpanFactory(ClassLoader classLoader) {
    try {
      // Because of shading tools we must call Class.forName with the literal string name of the
      // class.
      return Provider.createInstance(
          Class.forName("com.google.instrumentation.trace.SpanFactoryImpl", true, classLoader),
          SpanFactory.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Using default implementation for SpanFactory.", e);
    }
    return new NoopSpanFactory();
  }

  // Any provider that may be used for ContextSpanHandler can be added here.
  @VisibleForTesting
  static ContextSpanHandler loadContextSpanHandler(ClassLoader classLoader) {
    try {
      // Because of shading tools we must call Class.forName with the literal string name of the
      // class.
      return Provider.createInstance(
          Class.forName(
              "com.google.instrumentation.trace.ContextSpanHandlerImpl", true, classLoader),
          ContextSpanHandler.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Using default implementation for ContextSpanHandler.", e);
    }
    return new NoopContextSpanHandler();
  }
}
