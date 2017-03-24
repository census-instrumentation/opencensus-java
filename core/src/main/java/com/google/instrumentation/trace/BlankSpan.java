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

import javax.annotation.concurrent.Immutable;

/**
 * The {@code BlankSpan} is a singleton class, which is the default {@link Span} that is used when
 * no {@code Span} implementation is available. All operations are no-op.
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 */
@Immutable
public final class BlankSpan extends Span {
  /** Singleton instance of this class. */
  public static final BlankSpan INSTANCE = new BlankSpan();

  private BlankSpan() {
    super(SpanContext.INVALID, null);
  }

  /** No-op implementation of the {@link Span#addAttributes(Attributes)} method. */
  @Override
  public void addAttributes(Attributes attributes) {}

  /** No-op implementation of the {@link Span#addAnnotation(String)} method. */
  @Override
  public void addAnnotation(String description) {}

  /** No-op implementation of the {@link Span#addAnnotation(String, Attributes)} method. */
  @Override
  public void addAnnotation(String description, Attributes attributes) {}

  /** No-op implementation of the {@link Span#addNetworkEvent(NetworkEvent)} method. */
  @Override
  public void addNetworkEvent(NetworkEvent networkEvent) {}

  /** No-op implementation of the {@link Span#addChildLink(Span)} method. */
  @Override
  public void addChildLink(Span childLink) {}

  /** No-op implementation of the {@link Span#end(EndSpanOptions)} method. */
  @Override
  public void end(EndSpanOptions options) {}

  @Override
  public String toString() {
    return "BlankSpan";
  }
}
