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

package io.opencensus.trace;

import io.opencensus.common.NonThrowingCloseable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * {@link SpanBuilder} is used to construct {@link Span} instances which define arbitrary scopes of
 * code that are sampled for distributed tracing as a single atomic unit.
 *
 * <p>This is a simple example where all the work is being done within a single scope and a single
 * thread and the Context is automatically propagated:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracing.getTracer();
 *   void doWork {
 *     // Create a Span as a child of the current Span.
 *     try (NonThrowingCloseable ss = tracer.spanBuilder("MyChildSpan").startScopedSpan()) {
 *       tracer.getCurrentSpan().addAnnotation("my annotation");
 *       doSomeWork();  // Here the new span is in the current Context, so it can be used
 *                      // implicitly anywhere down the stack.
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>There might be cases where you do not perform all the work inside one static scope and the
 * Context is automatically propagated:
 *
 * <pre>{@code
 * class MyRpcServerInterceptorListener implements RpcServerInterceptor.Listener {
 *   private static final Tracer tracer = Tracing.getTracer();
 *   private Span mySpan;
 *
 *   public MyRpcInterceptor() {}
 *
 *   public void onRequest(String rpcName, Metadata metadata) {
 *     // Create a Span as a child of the remote Span.
 *     mySpan = tracer.spanBuilderWithRemoteParent(
 *         getTraceContextFromMetadata(metadata), rpcName).startSpan();
 *   }
 *
 *   public void onExecuteHandler(ServerCallHandler serverCallHandler) {
 *     try (NonThrowingCloseable ws = tracer.withSpan(mySpan)) {
 *       tracer.getCurrentSpan().addAnnotation("Start rpc execution.");
 *       serverCallHandler.run();  // Here the new span is in the current Context, so it can be
 *                                 // used implicitly anywhere down the stack.
 *     }
 *   }
 *
 *   // Called when the RPC is canceled and guaranteed onComplete will not be called.
 *   public void onCancel() {
 *     // IMPORTANT: DO NOT forget to ended the Span here as the work is done.
 *     mySpan.end(EndSpanOptions.builder().setStatus(Status.CANCELLED));
 *   }
 *
 *   // Called when the RPC is done and guaranteed onCancel will not be called.
 *   public void onComplete(RpcStatus rpcStatus) {
 *     // IMPORTANT: DO NOT forget to ended the Span here as the work is done.
 *     mySpan.end(EndSpanOptions.builder().setStatus(rpcStatusToCanonicalTraceStatus(status));
 *   }
 * }
 * }</pre>
 *
 * <p>This is a simple example where all the work is being done within a single scope and the
 * Context is manually propagated:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Tracer tracer = Tracing.getTracer();
 *   void DoWork() {
 *     Span span = tracer.spanBuilder(null, "MyRootSpan").startSpan();
 *     span.addAnnotation("my annotation");
 *     try {
 *       doSomeWork(span); // Manually propagate the new span down the stack.
 *     } finally {
 *       // To make sure we end the span even in case of an exception.
 *       span.end();  // Manually end the span.
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>If your Java version is less than Java SE 7, see {@link SpanBuilder#startSpan} and {@link
 * SpanBuilder#startScopedSpan} for usage examples.
 */
public final class SpanBuilder {
  private final SpanFactory spanFactory;
  private final String name;
  private final StartSpanOptions startSpanOption = new StartSpanOptions();
  private Span parentSpan;
  private SpanContext parentSpanContext;
  private boolean remoteParent;

  static SpanBuilder builder(SpanFactory spanFactory, Span parentSpan, String name) {
    return new SpanBuilder(spanFactory, parentSpan, null, false, name);
  }

  static SpanBuilder builderWithRemoteParent(
      SpanFactory spanFactory, SpanContext parentSpanContext, String name) {
    return new SpanBuilder(spanFactory, null, parentSpanContext, true, name);
  }

  /**
   * Sets the {@link Sampler} to use. If a {@code null} value is passed, the implementation will
   * provide a default.
   *
   * @param sampler The {@code Sampler} to use when determining sampling for a {@code Span}.
   * @return this.
   */
  public SpanBuilder setSampler(@Nullable Sampler sampler) {
    startSpanOption.setSampler(sampler);
    return this;
  }

  /**
   * Sets the {@code List} of parent links. Links are used to link {@link Span}s in different
   * traces. Used (for example) in batching operations, where a single batch handler processes
   * multiple requests from different traces.
   *
   * @param parentLinks New links to be added.
   * @return this.
   */
  public SpanBuilder setParentLinks(@Nullable List<Span> parentLinks) {
    startSpanOption.setParentLinks(parentLinks);
    return this;
  }

  /**
   * Sets the option {@link Span.Options#RECORD_EVENTS} for the newly created {@code Span}. If not
   * called, the implementation will provide a default.
   *
   * @param recordEvents New value determining if this {@code Span} should have events recorded.
   * @return this.
   */
  public SpanBuilder setRecordEvents(boolean recordEvents) {
    startSpanOption.setRecordEvents(recordEvents);
    return this;
  }

  /**
   * If called this will force the newly created {@code Span} to be a root span. As a consequence,
   * any parent specified (or inherited from the Context) will be ignored (N.B. does not apply to
   * linked parents set through {@link #setParentLinks}).
   *
   * <p>This is useful when {@link Tracer#spanBuilder(String)} is used and the newly created {@code
   * Span} needs to be decoupled from the parent {@code Span}.
   *
   * @return this.
   */
  public SpanBuilder becomeRoot() {
    parentSpan = null;
    parentSpanContext = null;
    remoteParent = false;
    return this;
  }

  /**
   * Starts a new {@link Span}.
   *
   * <p>Users <b>must</b> manually call {@link Span#end()} or {@link Span#end(EndSpanOptions)} to
   * end this {@code Span}.
   *
   * <p>Does not install the newly created {@code Span} to the current Context.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * class MyClass {
   *   private static final Tracer tracer = Tracing.getTracer();
   *   void DoWork() {
   *     Span span = tracer.spanBuilder(null, "MyRootSpan").startSpan();
   *     span.addAnnotation("my annotation");
   *     try {
   *       doSomeWork(span); // Manually propagate the new span down the stack.
   *     } finally {
   *       // To make sure we end the span even in case of an exception.
   *       span.end();  // Manually end the span.
   *     }
   *   }
   * }
   * }</pre>
   *
   * @return the newly created {@code Span}.
   */
  public Span startSpan() {
    return start();
  }

  // TODO(bdrutu): Add error_prone annotation @MustBeClosed when the 2.0.16 jar is fixed.
  /**
   * Starts a new new span and sets it as the {@link Tracer#getCurrentSpan current span}.
   *
   * <p>Enters the scope of code where the newly created {@code Span} is in the current Context, and
   * returns an object that represents that scope. The scope is exited when the returned object is
   * closed then the previous Context is restored and the newly created {@code Span} is ended using
   * {@link Span#end}.
   *
   * <p>Supports try-with-resource idiom.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * class MyClass {
   *   private static final Tracer tracer = Tracing.getTracer();
   *   void doWork {
   *     // Create a Span as a child of the current Span.
   *     try (NonThrowingCloseable ss = tracer.spanBuilder("MyChildSpan").startScopedSpan()) {
   *       tracer.getCurrentSpan().addAnnotation("my annotation");
   *       doSomeWork();  // Here the new span is in the current Context, so it can be used
   *                      // implicitly anywhere down the stack. Anytime in this closure the span
   *                      // can be accessed via tracer.getCurrentSpan().
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p>Prior to Java SE 7, you can use a finally block to ensure that a resource is closed (the
   * {@code Span} is ended and removed from the Context) regardless of whether the try statement
   * completes normally or abruptly.
   *
   * <p>Example of usage prior to Java SE7:
   *
   * <pre>{@code
   * class MyClass {
   *   private static Tracer tracer = Tracing.getTracer();
   *   void doWork {
   *     // Create a Span as a child of the current Span.
   *     NonThrowingCloseable ss = tracer.spanBuilder("MyChildSpan").startScopedSpan();
   *     try {
   *       tracer.getCurrentSpan().addAnnotation("my annotation");
   *       doSomeWork();  // Here the new span is in the current Context, so it can be used
   *                      // implicitly anywhere down the stack. Anytime in this closure the span
   *                      // can be accessed via tracer.getCurrentSpan().
   *     } finally {
   *       ss.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @return an object that defines a scope where the newly created {@code Span} will be set to the
   *     current Context.
   */
  public NonThrowingCloseable startScopedSpan() {
    return new ScopedSpanHandle(start());
  }

  private SpanBuilder(
      SpanFactory spanFactory,
      @Nullable Span parentSpan,
      @Nullable SpanContext parentSpanContext,
      boolean remoteParent,
      String name) {
    this.parentSpan = parentSpan;
    this.parentSpanContext = parentSpanContext;
    this.remoteParent = remoteParent;
    this.name = name;
    this.spanFactory = spanFactory;
  }

  // Utility method to start a Span.
  private Span start() {
    return remoteParent
        ? spanFactory.startSpanWithRemoteParent(parentSpanContext, name, startSpanOption)
        : spanFactory.startSpan(parentSpan, name, startSpanOption);
  }
}
