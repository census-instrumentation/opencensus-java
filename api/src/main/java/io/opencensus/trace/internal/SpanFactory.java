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

package io.opencensus.trace.internal;

import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.StartSpanOptions;
import javax.annotation.Nullable;

/** Factory class to create and start a {@link Span}. */
public abstract class SpanFactory {
  /**
   * Creates and starts a new child {@link Span} (or root if parent is {@code null}), with parent
   * being the designated {@code Span} and the given options.
   *
   * @param parent The parent of the returned {@code Span}.
   * @param name The name of the returned {@code Span}.
   * @param options The options for the start of the {@code Span}.
   * @return A child {@code Span} that will have the name provided.
   */
  public abstract Span startSpan(@Nullable Span parent, String name, StartSpanOptions options);

  /**
   * Creates and starts a new child {@link Span} (or root if parent is {@code null}), with parent
   * being the {@code Span} designated by the {@link SpanContext} and the given options.
   *
   * <p>This must be used to create a {@code Span} when the parent is on a different process.
   *
   * @param remoteParent The remote parent of the returned {@code Span}.
   * @param name The name of the returned {@code Span}.
   * @param options The options for the start of the {@code Span}.
   * @return A child {@code Span} that will have the name provided.
   */
  public abstract Span startSpanWithRemoteParent(
      @Nullable SpanContext remoteParent, String name, StartSpanOptions options);
}
