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

import static com.google.common.base.Preconditions.checkState;

import com.google.instrumentation.common.Clock;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation for the {@link Span} class. */
@ThreadSafe
final class SpanImpl extends Span {
  private static final Logger logger = Logger.getLogger(Tracer.class.getName());

  private final SpanId parentSpanId;
  private final StartEndHandler startEndHandler;
  private final String name;
  // All the following variables are initialized iff the Span has RECORD_EVENTS option.
  private final Clock clock;
  private TimestampConverter timestampConverter;
  private long startNanoTime;
  private Status status;
  private long endNanoTime;
  private boolean hasBeenEnded;

  static SpanImpl startSpan(SpanContext context,
      @Nullable EnumSet<Options> options,
      String name,
      @Nullable SpanId parentSpanId,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    SpanImpl span = new SpanImpl(context, options, name, parentSpanId, startEndHandler, clock);
    span.start(timestampConverter);
    return span;
  }

  /**
   * Returns the name of the {@code Span}.
   *
   * @return the name of the {@code Span}.
   */
  String getName() {
    return name;
  }

  /**
   * Returns an immutable representation of all the data from this {@code Span}.
   *
   * @return an immutable representation of all the data from this {@code Span}.
   * @throws IllegalStateException if the Span doesn't have RECORD_EVENTS option.
   */
  SpanData toSpanData() {
    checkState(
        getOptions().contains(Options.RECORD_EVENTS),
        "Getting SpanData for a Span without RECORD_EVENTS options.");
    synchronized (this) {
      return new SpanData(
          getContext(),
          parentSpanId,
          name,
          timestampConverter.convertNanoTime(startNanoTime),
          Collections.<String, AttributeValue>emptyMap(),
          Collections.<TimedEvent<Annotation>>emptyList(),
          Collections.<TimedEvent<NetworkEvent>>emptyList(),
          Collections.<Link>emptyList(),
          hasBeenEnded ? status : null,
          hasBeenEnded ? timestampConverter.convertNanoTime(endNanoTime) : null);
    }
  }

  @Override
  public void addAttributes(Map<String, AttributeValue> attributes) {
    // TODO(bdrutu): Implement this.
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    // TODO(bdrutu): Implement this.
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    // TODO(bdrutu): Implement this.
  }

  @Override
  public void addNetworkEvent(NetworkEvent networkEvent) {
    // TODO(bdrutu): Implement this.
  }

  @Override
  public void addLink(Link link) {
    // TODO(bdrutu): Implement this.
  }

  @Override
  public void end(EndSpanOptions options) {
    if (!getOptions().contains(Options.RECORD_EVENTS)) {
      return;
    }
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      status = options.getStatus();
      endNanoTime = clock.nowNanos();
      startEndHandler.onEnd(this);
      hasBeenEnded = true;
    }
  }

  /**
   * Abstract class to handle the start and end operations for a {@link Span} only when the {@code
   * Span} has {@link Span.Options#RECORD_EVENTS} option.
   *
   * <p>Implementation must avoid high overhead work in any of the methods because the code is
   * executed on the critical path.
   */
  @ThreadSafe
  abstract static class StartEndHandler {
    abstract void onStart(SpanImpl span);

    abstract void onEnd(SpanImpl span);
  }

  private void start(@Nullable TimestampConverter timestampConverter) {
    if (getOptions().contains(Options.RECORD_EVENTS)) {
      synchronized (this) {
        this.timestampConverter =
            timestampConverter != null ? timestampConverter : TimestampConverter.now(clock);
        startNanoTime = clock.nowNanos();
        startEndHandler.onStart(this);
      }
    }
  }

  private SpanImpl(
      SpanContext context,
      @Nullable EnumSet<Options> options,
      String name,
      @Nullable SpanId parentSpanId,
      StartEndHandler startEndHandler,
      Clock clock) {
    super(context, options);
    this.parentSpanId = parentSpanId;
    this.name = name;
    this.startEndHandler = startEndHandler;
    this.clock = clock;
    this.hasBeenEnded = false;
  }
}
