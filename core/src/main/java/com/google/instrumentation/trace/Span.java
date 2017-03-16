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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * An abstract class that represents a span. It has an associated {@link SpanContext} and a set of
 * {@link Options}.
 *
 * <p>Spans are created by the {@link SpanBuilder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()} or {@link #end(EndSpanOptions)}
 */
public abstract class Span {
  // Contains the identifiers associated with this Span.
  private final SpanContext context;

  // Contains the options associated with this Span. This object is immutable.
  private final EnumSet<Options> options;

  /**
   * {@code Span} options. These options are NOT propagated to child spans. These options determine
   * features such as whether a {@code Span} should record any annotations or events.
   */
  public enum Options {
    /**
     * This option is set if the Span is part of a sampled distributed trace OR the {@link
     * StartSpanOptions#getRecordEvents()} is true.
     */
    RECORD_EVENTS;
  }

  /**
   * Creates a new {@code Span}.
   *
   * @param context The context associated with this {@code Span}.
   * @param options The options associated with this {@code Span}. If {@code null} then default
   *     options will be set.
   * @throws NullPointerException if context is {@code null}.
   */
  protected Span(SpanContext context, @Nullable EnumSet<Options> options) {
    this.context = checkNotNull(context, "context");
    this.options = options == null ? EnumSet.noneOf(Options.class) : EnumSet.copyOf(options);
  }

  /**
   * Adds a set of labels to the {@code Span}.
   *
   * @param labels The labels that will be added and associated with the {@code Span}.
   */
  public abstract void addLabels(Labels labels);

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description The description of the annotation time event.
   */
  public abstract void addAnnotation(String description);

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description The description of the annotation time event.
   * @param labels The labels that will be added; these are associated with this annotation, not the
   *     {@code Span} as for {@link #addLabels}.
   */
  public abstract void addAnnotation(String description, Labels labels);

  /**
   * Adds a NetworkEvent to the {@code Span}.
   *
   * <p>This function is only intended to be used by RPC systems (either client or server), not by
   * higher level applications.
   *
   * @param networkEvent The network event to add.
   */
  public abstract void addNetworkEvent(NetworkEvent networkEvent);

  /**
   * Adds a child link to the {@code Span}.
   *
   * <p>Used (for example) in batching operations, where a single batch handler processes multiple
   * requests from different traces.
   *
   * @param childLink The child link to add.
   */
  public abstract void addChildLink(Span childLink);

  /**
   * Marks the end of {@code Span} execution with the given options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @param options The options to be used for the end of the {@code Span}.
   */
  public abstract void end(EndSpanOptions options);

  /**
   * Marks the end of {@code Span} execution with the default options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   */
  public final void end() {
    end(EndSpanOptions.DEFAULT);
  }

  /**
   * Returns the span context associated with this {@code Span}.
   *
   * @return the span context associated with this {@code Span}.
   */
  public final SpanContext getContext() {
    return context;
  }

  /**
   * Returns the options associated with this {@code Span}.
   *
   * @return the options associated with this {@code Span}.
   */
  public final Set<Options> getOptions() {
    return options;
  }

  /** Ends the current {@code Span} by calling {@link #end}. */
  public final void close() {
    end(EndSpanOptions.DEFAULT);
  }
}
