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
import com.google.instrumentation.trace.Trace;
import com.google.instrumentation.trace.Tracer;

/**
 * Example showing how to create a {@link Span} using scoped Span, install it in the current
 * context, and add annotations.
 */
public final class BasicScopedTracing {
  // Per class Tracer.
  private static final Tracer tracer = Trace.getTracer();

  private static void doWork() {
    // Add an annotation to the current Span.
    tracer.getCurrentSpan().addAnnotation("This is a doWork() annotation.");
  }

  /** Main method. */
  public static void main(String[] args) {
    try (NonThrowingCloseable ss =
        tracer.spanBuilder("MyRootSpan").becomeRoot().startScopedSpan()) {
      doWork();
    }
  }
}
