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

/** Strategy interface for creating and manipulating trace spans. */
public interface TraceStrategy {

  /**
   * Starts a new span and sets it as the current span.
   *
   * @param spanName the name of the returned {@link io.opencensus.trace.Span}
   * @return an object that defines a scope where the newly created {@code Span} will be set to the
   *     current Context
   * @see io.opencensus.trace.Tracer#spanBuilder(java.lang.String)
   * @see io.opencensus.trace.SpanBuilder#startScopedSpan()
   */
  @MustBeClosed
  Closeable startScopedSpan(String spanName);
}
