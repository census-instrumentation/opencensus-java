/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.trace;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
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
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES = Collections.emptyMap();

  // Contains the identifiers associated with this Span.
  private final SpanContext context;

  // Contains the options associated with this Span. This object is immutable.
  private final Set<Options> options;

  /**
   * {@code Span} options. These options are NOT propagated to child spans. These options determine
   * features such as whether a {@code Span} should record any annotations or events.
   */
  public enum Options {
    /**
     * This option is set if the Span is part of a sampled distributed trace OR {@link
     * SpanBuilder#setRecordEvents(boolean)} was called with true.
     */
    RECORD_EVENTS;
  }

  private static final Set<Options> DEFAULT_OPTIONS =
      Collections.unmodifiableSet(EnumSet.noneOf(Options.class));

  /**
   * Creates a new {@code Span}.
   *
   * @param context the context associated with this {@code Span}.
   * @param options the options associated with this {@code Span}. If {@code null} then default
   *     options will be set.
   * @throws NullPointerException if context is {@code null}.
   * @throws IllegalArgumentException if the {@code SpanContext} is sampled but no RECORD_EVENTS
   *     options.
   */
  protected Span(SpanContext context, @Nullable EnumSet<Options> options) {
    this.context = checkNotNull(context, "context");
    this.options =
        options == null ? DEFAULT_OPTIONS : Collections.unmodifiableSet(EnumSet.copyOf(options));
    checkArgument(
        !context.getTraceOptions().isSampled() || (this.options.contains(Options.RECORD_EVENTS)),
        "Span is sampled, but does not have RECORD_EVENTS set.");
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   */
  public void putAttribute(String key, AttributeValue value) {
    // Not final because for performance reasons we want to override this in the implementation.
    // Also a default implementation is needed to not break the compatibility (users may extend this
    // for testing).
    putAttributes(Collections.singletonMap(key, value));
  }

  /**
   * Sets a set of attributes to the {@code Span}. The effect of this call is equivalent to that of
   * calling {@link #putAttribute(String, AttributeValue)} once for each element in the specified
   * map.
   *
   * @param attributes the attributes that will be added and associated with the {@code Span}.
   */
  public void putAttributes(Map<String, AttributeValue> attributes) {
    // Not final because we want to start overriding this method from the beginning, this will
    // allow us to remove the addAttributes faster. All implementations MUST override this method.
    addAttributes(attributes);
  }

  /**
   * @deprecated Use {@link #putAttributes(Map)}
   * @param attributes the attributes that will be added and associated with the {@code Span}.
   */
  @Deprecated
  public void addAttributes(Map<String, AttributeValue> attributes) {
    putAttributes(attributes);
  }

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description the description of the annotation time event.
   */
  public final void addAnnotation(String description) {
    addAnnotation(description, EMPTY_ATTRIBUTES);
  }

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param description the description of the annotation time event.
   * @param attributes the attributes that will be added; these are associated with this annotation,
   *     not the {@code Span} as for {@link #putAttributes(Map)}.
   */
  public abstract void addAnnotation(String description, Map<String, AttributeValue> attributes);

  /**
   * Adds an annotation to the {@code Span}.
   *
   * @param annotation the annotations to add.
   */
  public abstract void addAnnotation(Annotation annotation);

  /**
   * Adds a NetworkEvent to the {@code Span}.
   *
   * <p>This function is only intended to be used by RPC systems (either client or server), not by
   * higher level applications.
   *
   * @param networkEvent the network event to add.
   */
  public abstract void addNetworkEvent(NetworkEvent networkEvent);

  /**
   * Adds a {@link Link} to the {@code Span}.
   *
   * <p>Used (for example) in batching operations, where a single batch handler processes multiple
   * requests from different traces.
   *
   * @param link the link to add.
   */
  public abstract void addLink(Link link);

  /**
   * Sets the {@link Status} to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default is {@link Status#OK}.
   *
   * <p>If called multiple times, or status set via
   *
   * @param status the {@link Status} to set.
   */
  public void setStatus(Status status) {
    // Implemented as no-op for backwards compatibility (for example gRPC extends Span in tests).
    // Implementation must override this method.
  }

  /**
   * Marks the end of {@code Span} execution with the given options.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * @param options the options to be used for the end of the {@code Span}.
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
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
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
}
