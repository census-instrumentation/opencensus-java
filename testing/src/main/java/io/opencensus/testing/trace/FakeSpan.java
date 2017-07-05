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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Annotation;
import io.opencensus.trace.base.AttributeValue;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.NetworkEvent;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A {@link Span} that allows users to test all the configurations set when start a new {@code
 * Span}.
 */
public class FakeSpan extends Span {
  private static final SecureRandom random = new SecureRandom();
  private final String name;
  @Nullable private final Span parentSpan;
  @Nullable private final SpanContext remoteParentSpanContext;
  private final List<Span> parentLinks;

  FakeSpan(
      SpanContext context,
      EnumSet<Span.Options> options,
      String name,
      @Nullable Span parentSpan,
      @Nullable SpanContext remoteParentSpanContext,
      List<Span> parentLinks) {
    super(context, options);
    this.parentSpan = parentSpan;
    this.remoteParentSpanContext = remoteParentSpanContext;
    this.name = name;
    this.parentLinks = parentLinks;
  }

  /**
   * Returns a new {@code Span} that can be used in tests as a parent {@code Span} for other spans.
   *
   * @return a new {@code Span} that can be used in tests as a parent {@code Span} for other spans.
   */
  public static FakeSpan generateParentSpan(String name) {
    return new FakeSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT),
        EnumSet.noneOf(Span.Options.class),
        checkNotNull(name, "name"),
        null,
        null,
        Collections.<Span>emptyList());
  }

  /**
   * Returns {@code true} if this {@code Span} is a remote child of the given {@code SpanContext}.
   *
   * @param spanContext the remote parent {@code SpanContext}.
   * @return {@code true} if this {@code Span} is a remote child of the given {@code SpanContext}.
   */
  public final boolean isRemoteChildOf(SpanContext spanContext) {
    return spanContext != null && Objects.equal(remoteParentSpanContext, spanContext);
  }

  /**
   * Returns {@code true} if this {@code Span} is a child of the given {@code Span}.
   *
   * @param span the parent {@code Span}.
   * @return {@code true} if this {@code Span} is a child of the given {@code Span}.
   */
  public final boolean isChildOf(Span span) {
    return span != null
        && parentSpan != null
        && Objects.equal(parentSpan.getContext(), span.getContext());
  }

  /**
   * Returns {@code true} if this is a root {@code Span}.
   *
   * @return {@code true} if this is a root {@code Span}.
   */
  public final boolean isRoot() {
    return parentSpan == null && remoteParentSpanContext == null;
  }

  /**
   * Returns the configured name for this {@code Span}.
   *
   * @return the configured name for this {@code Span}.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the list of parent links configured when started this {@code Span}.
   *
   * @return the list of parent links configured when started this {@code Span}.
   */
  public final List<Span> getParentLinks() {
    return parentLinks;
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
