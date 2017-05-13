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

package com.google.instrumentation.examples.trace;

import com.google.instrumentation.common.NonThrowingCloseable;
import com.google.instrumentation.trace.Span;
import com.google.instrumentation.trace.TraceExporter;
import com.google.instrumentation.trace.Tracer;
import com.google.instrumentation.trace.Tracing;

/**
 * Example showing how to create a child {@link Span} using scoped Spans, install it in the current
 * context, and add annotations.
 */
public final class MultiSpansScopedTracing {
  // Per class Tracer.
  private static final Tracer tracer = Tracing.getTracer();

  private static void doSomeOtherWork() {
    tracer.getCurrentSpan().addAnnotation("Annotation to the child Span");
  }

  private static void doSomeMoreWork() {
    // Create a child Span of the current Span.
    try (NonThrowingCloseable ss = tracer.spanBuilder("MyChildSpan").startScopedSpan()) {
      doSomeOtherWork();
    }
  }

  private static void doWork() {
    tracer.getCurrentSpan().addAnnotation("Annotation to the root Span before child is created.");
    doSomeMoreWork();
    tracer.getCurrentSpan().addAnnotation("Annotation to the root Span after child is ended.");
  }

  /** Main method. */
  public static void main(String[] args) {
    TraceExporter.LoggingServiceHandler.registerService(Tracing.getTraceExporter());
    try (NonThrowingCloseable ss =
        tracer.spanBuilder("MyRootSpan").becomeRoot().startScopedSpan()) {
      doWork();
    }
  }
}
