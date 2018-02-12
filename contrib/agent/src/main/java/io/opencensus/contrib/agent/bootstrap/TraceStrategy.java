/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent.bootstrap;

import com.google.errorprone.annotations.MustBeClosed;
import java.io.Closeable;
import javax.annotation.Nullable;

/**
 * Strategy interface for creating and manipulating trace spans.
 *
 * @since 0.9
 */
public interface TraceStrategy {

  /**
   * Starts a new span and sets it as the current span.
   *
   * <p>Enters the scope of code where the newly created {@code Span} is in the current Context, and
   * returns an object that represents that scope. When the returned object is closed, the scope is
   * exited, the previous Context is restored, and the newly created {@code Span} is ended using
   * {@link io.opencensus.trace.Span#end}.
   *
   * <p>Callers must eventually close the returned object to avoid leaking the Context.
   *
   * <p>Supports the try-with-resource idiom.
   *
   * <p>NB: The return type of this method is intentionally {@link Closeable} and not the more
   * specific {@link io.opencensus.common.Scope} because the latter would not be visible from
   * classes loaded by the bootstrap classloader.
   *
   * @param spanName the name of the returned {@link io.opencensus.trace.Span}
   * @return an object that defines a scope where the newly created {@code Span} will be set to the
   *     current Context
   * @see io.opencensus.trace.Tracer#spanBuilder(java.lang.String)
   * @see io.opencensus.trace.SpanBuilder#startScopedSpan()
   * @since 0.9
   */
  @MustBeClosed
  Closeable startScopedSpan(String spanName);

  /**
   * Ends the current span with a status derived from the given (optional) Throwable, and closes the
   * given scope.
   *
   * @param scope an object representing the scope
   * @param throwable an optional Throwable
   * @since 0.9
   */
  void endScope(Closeable scope, @Nullable Throwable throwable);
}
