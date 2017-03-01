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

/** Example showing how to directly create a child {@link Span} and add annotations. */
public final class MultiSpansTracing {
  // Per class Tracer.
  private static final Tracer tracer = Tracer.getTracer();

  private static void doWork() {
    try (Span rootSpan = tracer.startSpan(null, "MyRootSpan", StartSpanOptions.getDefault())) {
      rootSpan.addAnnotation("Annotation to the root Span before child is created.");
      try (Span childSpan =
          tracer.startSpan(rootSpan, "MyChildSpan", StartSpanOptions.getDefault())) {
        childSpan.addAnnotation("Annotation to the child Span");
      }
      rootSpan.addAnnotation("Annotation to the root Span after child is ended.");
    }
  }

  public static void main(String[] args) {
    doWork();
  }
}
