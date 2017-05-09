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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation for the {@link Span} class. */
@ThreadSafe
final class SpanImpl extends Span {
  private static final Logger logger = Logger.getLogger(Tracer.class.getName());

  // The parent SpanId of this span. Null if this is a root.
  private final SpanId parentSpanId;
  // True if the parent is on a different process.
  private final boolean hasRemoteParent;
  // Handler called when the span starts and ends.
  private final StartEndHandler startEndHandler;
  // The displayed name of the span.
  private final String name;
  // The clock used to get the time.
  private final Clock clock;
  // The time converter used to convert nano time to Timestamp. This is needed because java has
  // milliseconds granularity for Timestamp and tracing events are recorded more often.
  private final TimestampConverter timestampConverter;
  // The start time of the span. Set when the span is created iff the RECORD_EVENTS options is
  // set, otherwise 0.
  private final long startNanoTime;
  // The status of the span. Set when the span is ended iff the RECORD_EVENTS options is set.
  @GuardedBy("this")
  private Status status;
  // The end time of the span. Set when the span is ended iff the RECORD_EVENTS options is set,
  // otherwise 0.
  @GuardedBy("this")
  private long endNanoTime;
  // True if the span is ended.
  @GuardedBy("this")
  private boolean hasBeenEnded;

  // Creates and starts a span with the given configuration. TimestampConverter is null if the
  // Span is a root span or the parent is not sampled. If the parent is sampled we should use the
  // same converter to ensure ordering between tracing events.
  static SpanImpl startSpan(
      SpanContext context,
      @Nullable EnumSet<Options> options,
      String name,
      @Nullable SpanId parentSpanId,
      boolean hasRemoteParent,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    SpanImpl span =
        new SpanImpl(
            context, options, name, parentSpanId, hasRemoteParent, startEndHandler,
            timestampConverter, clock);
    // Call onStart here instead of calling in the constructor to make sure the span is completely
    // initialized.
    if (span.getOptions().contains(Options.RECORD_EVENTS)) {
      startEndHandler.onStart(span);
    }
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
   * Returns the {@code TimestampConverter} used by this {@code Span}.
   *
   * @return the {@code TimestampConverter} used by this {@code Span}.
   */
  @Nullable
  TimestampConverter getTimestampConverter() {
    return timestampConverter;
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
        "Getting SpanData for a Span without RECORD_EVENTS option.");
    synchronized (this) {
      // TODO(bdrutu): Set the attributes, annotations, network events and links in the SpanData
      // when add the support for them.
      return SpanData.create(
          getContext(),
          parentSpanId,
          hasRemoteParent,
          name,
          timestampConverter.convertNanoTime(startNanoTime),
          SpanData.Attributes.create(Collections.<String, AttributeValue>emptyMap(), 0),
          SpanData.TimedEvents.create(Collections.<SpanData.TimedEvent<Annotation>>emptyList(), 0),
          SpanData.TimedEvents.create(
              Collections.<SpanData.TimedEvent<NetworkEvent>>emptyList(), 0),
          SpanData.Links.create(Collections.<Link>emptyList(), 0),
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
   *
   * <p>One instance can be called by multiple threads in the same time, so the implementation must
   * be thread-safe.
   */
  abstract static class StartEndHandler {
    abstract void onStart(SpanImpl span);

    abstract void onEnd(SpanImpl span);
  }

  private SpanImpl(
      SpanContext context,
      @Nullable EnumSet<Options> options,
      String name,
      @Nullable SpanId parentSpanId,
      boolean hasRemoteParent,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    super(context, options);
    this.parentSpanId = parentSpanId;
    this.hasRemoteParent = hasRemoteParent;
    this.name = name;
    this.startEndHandler = startEndHandler;
    this.clock = clock;
    this.hasBeenEnded = false;
    if (getOptions().contains(Options.RECORD_EVENTS)) {
      this.timestampConverter =
          timestampConverter != null ? timestampConverter : TimestampConverter.now(clock);
      startNanoTime = clock.nowNanos();
    } else {
      this.startNanoTime = 0;
      this.timestampConverter = timestampConverter;
    }
  }
}
