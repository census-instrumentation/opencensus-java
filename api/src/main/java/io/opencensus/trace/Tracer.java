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

package io.opencensus.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.trace.internal.SpanFactory;
import javax.annotation.Nullable;

/**
 * Tracer is a simple, thin class for {@link Span} creation and in-process context interaction.
 *
 * <p>Users may choose to use manual or automatic Context propagation. Because of that this class
 * offers APIs to facilitate both usages.
 *
 * <p>The automatic context propagation is done using {@link io.grpc.Context} which is a gRPC
 * independent implementation for in-process Context propagation mechanism which can carry
 * scoped-values across API boundaries and between threads. Users of the library must propagate the
 * {@link io.grpc.Context} between different threads.
 *
 * <p>Example usage with automatic context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracing.getTracer();
 *   void doWork() {
 *     try(NonThrowingCloseable ss = tracer.spanBuilder("MyClass.DoWork").startScopedSpan) {
 *       tracer.getCurrentSpan().addAnnotation("Starting the work.");
 *       doWorkInternal();
 *       tracer.getCurrentSpan().addAnnotation("Finished working.");
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with manual context propagation:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracing.getTracer();
 *   void doWork() {
 *     Span span = tracer.spanBuilder(null, "MyRootSpan").startSpan();
 *     span.addAnnotation("Starting the work.");
 *     try {
 *       doSomeWork(span); // Manually propagate the new span down the stack.
 *     } finally {
 *       span.addAnnotation("Finished working.");
 *       // To make sure we end the span even in case of an exception.
 *       span.end();  // Manually end the span.
 *     }
 *   }
 * }
 * }</pre>
 */
public abstract class Tracer {
  private static final NoopTracer noopTracer = new NoopTracer();
  private final SpanFactory spanFactory;

  /**
   * Returns the no-op implementation of the {@code Tracer}.
   *
   * @return the no-op implementation of the {@code Tracer}.
   */
  static Tracer getNoopTracer() {
    return noopTracer;
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
  public final Span getCurrentSpan() {
    Span currentSpan = ContextUtils.getCurrentSpan();
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
   * private static Tracer tracer = Tracing.getTracer();
   * void doWork() {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.spanBuilder("my span").startSpan();
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
   * private static Tracer tracer = Tracing.getTracer();
   * void doWork() {
   *   // Create a Span as a child of the current Span.
   *   Span span = tracer.spanBuilder("my span").startSpan();
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
  public final NonThrowingCloseable withSpan(Span span) {
    return ContextUtils.withSpan(checkNotNull(span, "span"));
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
  public final SpanBuilder spanBuilder(String name) {
    return spanBuilder(ContextUtils.getCurrentSpan(), name);
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
  public final SpanBuilder spanBuilder(@Nullable Span parent, String name) {
    return SpanBuilder.builder(spanFactory, parent, checkNotNull(name, "name"));
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
  public final SpanBuilder spanBuilderWithRemoteParent(
      @Nullable SpanContext remoteParent, String name) {
    return SpanBuilder.builderWithRemoteParent(
        spanFactory, remoteParent, checkNotNull(name, "name"));
  }

  // No-Op implementation of the Tracer.
  private static final class NoopTracer extends Tracer {
    private NoopTracer() {
      super(new NoopSpanFactory());
    }

    // No-op implementation of the SpanFactory
    private static final class NoopSpanFactory extends SpanFactory {
      @Override
      public Span startSpan(@Nullable Span parent, String name, StartSpanOptions options) {
        return BlankSpan.INSTANCE;
      }

      @Override
      public Span startSpanWithRemoteParent(
          @Nullable SpanContext remoteParent, String name, StartSpanOptions options) {
        return BlankSpan.INSTANCE;
      }
    }
  }

  protected Tracer(SpanFactory spanFactory) {
    this.spanFactory = checkNotNull(spanFactory, "spanFactory");
  }
}
