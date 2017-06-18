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

package io.opencensus.testing.trace;

import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Annotation;
import io.opencensus.trace.base.AttributeValue;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.NetworkEvent;
import io.opencensus.trace.base.StartSpanOptions;
import java.util.EnumSet;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A {@link Span} that allows users to test all the configurations set when start a new {@code
 * Span}.
 */
public class FakeSpan extends Span {
  @Nullable private final SpanContext parentSpanContext;
  private final String name;
  private final StartSpanOptions startSpanOptions;

  FakeSpan(
      SpanContext parentSpanContext,
      SpanContext context,
      EnumSet<Span.Options> options,
      String name,
      StartSpanOptions startSpanOptions) {
    super(context, options);
    this.parentSpanContext = parentSpanContext;
    this.name = name;
    this.startSpanOptions = startSpanOptions;
  }

  /**
   * Returns the parent {@code SpanContext} or {@code null} if this is a root {@code Span}.
   *
   * @return the parent {@code SpanContext} or {@code null} if this is a root {@code Span}.
   */
  @Nullable
  public SpanContext getParentSpanContext() {
    return parentSpanContext;
  }

  /**
   * Returns the configured name for this {@code Span}.
   *
   * @return the configured name for this {@code Span}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the configured {@code StartSpanOptions} for this {@code Span}.
   *
   * @return the configured {@code StartSpanOptions} for this {@code Span}.
   */
  public StartSpanOptions getStartSpanOptions() {
    return startSpanOptions;
  }

  @Override
  public void addAttributes(Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(Annotation annotation) {}

  @Override
  public void addNetworkEvent(NetworkEvent networkEvent) {}

  @Override
  public void addLink(Link link) {}

  @Override
  public void end(EndSpanOptions options) {}
}
