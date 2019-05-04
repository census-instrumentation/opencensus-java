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

package io.opencensus.implcore.trace;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.internal.ConcurrentIntrusiveList.Element;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

// TODO(hailongwen): remove the usage of `NetworkEvent` in the future.
/** Implementation for the {@link Span} class that records trace events. */
@ThreadSafe
public final class RecordEventsSpanImpl extends Span implements Element<RecordEventsSpanImpl> {
  private static final Logger logger = Logger.getLogger(Tracer.class.getName());

  private static final EnumSet<Span.Options> RECORD_EVENTS_SPAN_OPTIONS =
      EnumSet.of(Span.Options.RECORD_EVENTS);

  // The parent SpanId of this span. Null if this is a root span.
  @Nullable private final SpanId parentSpanId;
  // True if the parent is on a different process.
  @Nullable private final Boolean hasRemoteParent;
  // Active trace params when the Span was created.
  private final TraceParams traceParams;
  // Handler called when the span starts and ends.
  private final StartEndHandler startEndHandler;
  // The displayed name of the span.
  private final String name;
  // The kind of the span.
  @Nullable private final Kind kind;
  // The clock used to get the time.
  private final Clock clock;
  // The time converter used to convert nano time to Timestamp. This is needed because Java has
  // millisecond granularity for Timestamp and tracing events are recorded more often.
  private final TimestampConverter timestampConverter;
  // The start time of the span.
  private final long startNanoTime;
  // Set of recorded attributes. DO NOT CALL any other method that changes the ordering of events.
  @GuardedBy("this")
  @Nullable
  private AttributesWithCapacity attributes;
  // List of recorded annotations.
  @GuardedBy("this")
  @Nullable
  private TraceEvents<EventWithNanoTime<Annotation>> annotations;
  // List of recorded network events.
  @GuardedBy("this")
  @Nullable
  private TraceEvents<EventWithNanoTime<io.opencensus.trace.MessageEvent>> messageEvents;
  // List of recorded links to parent and child spans.
  @GuardedBy("this")
  @Nullable
  private TraceEvents<Link> links;
  // The number of children.
  @GuardedBy("this")
  private int numberOfChildren;
  // The status of the span.
  @GuardedBy("this")
  @Nullable
  private Status status;
  // The end time of the span.
  @GuardedBy("this")
  private long endNanoTime;
  // True if the span is ended.
  @GuardedBy("this")
  private boolean hasBeenEnded;

  @GuardedBy("this")
  private boolean sampleToLocalSpanStore;

  // Pointers for the ConcurrentIntrusiveList$Element. Guarded by the ConcurrentIntrusiveList.
  @Nullable private RecordEventsSpanImpl next = null;
  @Nullable private RecordEventsSpanImpl prev = null;

  /**
   * Creates and starts a span with the given configuration.
   *
   * @param context supplies the trace_id and span_id for the newly started span.
   * @param name the displayed name for the new span.
   * @param parentSpanId the span_id of the parent span, or null if the new span is a root span.
   * @param hasRemoteParent {@code true} if the parentContext is remote. {@code null} if this is a
   *     root span.
   * @param traceParams trace parameters like sampler and probability.
   * @param startEndHandler handler called when the span starts and ends.
   * @param timestampConverter null if the span is a root span or the parent is not sampled. If the
   *     parent is sampled, we should use the same converter to ensure ordering between tracing
   *     events.
   * @param clock the clock used to get the time.
   * @return a new and started span.
   */
  @VisibleForTesting
  public static RecordEventsSpanImpl startSpan(
      SpanContext context,
      String name,
      @Nullable Kind kind,
      @Nullable SpanId parentSpanId,
      @Nullable Boolean hasRemoteParent,
      TraceParams traceParams,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    RecordEventsSpanImpl span =
        new RecordEventsSpanImpl(
            context,
            name,
            kind,
            parentSpanId,
            hasRemoteParent,
            traceParams,
            startEndHandler,
            timestampConverter,
            clock);
    // Call onStart here instead of calling in the constructor to make sure the span is completely
    // initialized.
    startEndHandler.onStart(span);
    return span;
  }

  /**
   * Returns the name of the {@code Span}.
   *
   * @return the name of the {@code Span}.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the status of the {@code Span}. If not set defaults to {@link Status#OK}.
   *
   * @return the status of the {@code Span}.
   */
  public Status getStatus() {
    synchronized (this) {
      return getStatusWithDefault();
    }
  }

  /**
   * Returns the end nano time (see {@link System#nanoTime()}). If the current {@code Span} is not
   * ended then returns {@link Clock#nowNanos()}.
   *
   * @return the end nano time.
   */
  public long getEndNanoTime() {
    synchronized (this) {
      return hasBeenEnded ? endNanoTime : clock.nowNanos();
    }
  }

  /**
   * Returns the latency of the {@code Span} in nanos. If still active then returns now() - start
   * time.
   *
   * @return the latency of the {@code Span} in nanos.
   */
  public long getLatencyNs() {
    synchronized (this) {
      return hasBeenEnded ? endNanoTime - startNanoTime : clock.nowNanos() - startNanoTime;
    }
  }

  /**
   * Returns if the name of this {@code Span} must be register to the {@code SampledSpanStore}.
   *
   * @return if the name of this {@code Span} must be register to the {@code SampledSpanStore}.
   */
  public boolean getSampleToLocalSpanStore() {
    synchronized (this) {
      checkState(hasBeenEnded, "Running span does not have the SampleToLocalSpanStore set.");
      return sampleToLocalSpanStore;
    }
  }

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   */
  @Nullable
  public Kind getKind() {
    return kind;
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
  public SpanData toSpanData() {
    synchronized (this) {
      SpanData.Attributes attributesSpanData =
          attributes == null
              ? SpanData.Attributes.create(Collections.<String, AttributeValue>emptyMap(), 0)
              : SpanData.Attributes.create(attributes, attributes.getNumberOfDroppedAttributes());
      SpanData.TimedEvents<Annotation> annotationsSpanData =
          createTimedEvents(getInitializedAnnotations(), timestampConverter);
      SpanData.TimedEvents<io.opencensus.trace.MessageEvent> messageEventsSpanData =
          createTimedEvents(getInitializedNetworkEvents(), timestampConverter);
      SpanData.Links linksSpanData =
          links == null
              ? SpanData.Links.create(Collections.<Link>emptyList(), 0)
              : SpanData.Links.create(
                  new ArrayList<Link>(links.events), links.getNumberOfDroppedEvents());
      return SpanData.create(
          getContext(),
          parentSpanId,
          hasRemoteParent,
          name,
          kind,
          timestampConverter.convertNanoTime(startNanoTime),
          attributesSpanData,
          annotationsSpanData,
          messageEventsSpanData,
          linksSpanData,
          numberOfChildren,
          hasBeenEnded ? getStatusWithDefault() : null,
          hasBeenEnded ? timestampConverter.convertNanoTime(endNanoTime) : null);
    }
  }

  @Override
  public void putAttribute(String key, AttributeValue value) {
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(value, "value");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling putAttributes() on an ended Span.");
        return;
      }
      getInitializedAttributes().putAttribute(key, value);
    }
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(attributes, "attributes");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling putAttributes() on an ended Span.");
        return;
      }
      getInitializedAttributes().putAttributes(attributes);
    }
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    Preconditions.checkNotNull(description, "description");
    Preconditions.checkNotNull(attributes, "attribute");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling addAnnotation() on an ended Span.");
        return;
      }
      getInitializedAnnotations()
          .addEvent(
              new EventWithNanoTime<Annotation>(
                  clock.nowNanos(),
                  Annotation.fromDescriptionAndAttributes(description, attributes)));
    }
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    Preconditions.checkNotNull(annotation, "annotation");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling addAnnotation() on an ended Span.");
        return;
      }
      getInitializedAnnotations()
          .addEvent(new EventWithNanoTime<Annotation>(clock.nowNanos(), annotation));
    }
  }

  @Override
  public void addMessageEvent(io.opencensus.trace.MessageEvent messageEvent) {
    Preconditions.checkNotNull(messageEvent, "messageEvent");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling addNetworkEvent() on an ended Span.");
        return;
      }
      getInitializedNetworkEvents()
          .addEvent(
              new EventWithNanoTime<io.opencensus.trace.MessageEvent>(
                  clock.nowNanos(), checkNotNull(messageEvent, "networkEvent")));
    }
  }

  @Override
  public void addLink(Link link) {
    Preconditions.checkNotNull(link, "link");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling addLink() on an ended Span.");
        return;
      }
      getInitializedLinks().addEvent(link);
    }
  }

  @Override
  public void setStatus(Status status) {
    Preconditions.checkNotNull(status, "status");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling setStatus() on an ended Span.");
        return;
      }
      this.status = status;
    }
  }

  @Override
  public void end(EndSpanOptions options) {
    Preconditions.checkNotNull(options, "options");
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      if (options.getStatus() != null) {
        status = options.getStatus();
      }
      sampleToLocalSpanStore = options.getSampleToLocalSpanStore();
      endNanoTime = clock.nowNanos();
      hasBeenEnded = true;
    }
    startEndHandler.onEnd(this);
  }

  void addChild() {
    synchronized (this) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      numberOfChildren++;
    }
  }

  @GuardedBy("this")
  private AttributesWithCapacity getInitializedAttributes() {
    if (attributes == null) {
      attributes = new AttributesWithCapacity(traceParams.getMaxNumberOfAttributes());
    }
    return attributes;
  }

  @GuardedBy("this")
  private TraceEvents<EventWithNanoTime<Annotation>> getInitializedAnnotations() {
    if (annotations == null) {
      annotations =
          new TraceEvents<EventWithNanoTime<Annotation>>(traceParams.getMaxNumberOfAnnotations());
    }
    return annotations;
  }

  @GuardedBy("this")
  private TraceEvents<EventWithNanoTime<io.opencensus.trace.MessageEvent>>
      getInitializedNetworkEvents() {
    if (messageEvents == null) {
      messageEvents =
          new TraceEvents<EventWithNanoTime<io.opencensus.trace.MessageEvent>>(
              traceParams.getMaxNumberOfMessageEvents());
    }
    return messageEvents;
  }

  @GuardedBy("this")
  private TraceEvents<Link> getInitializedLinks() {
    if (links == null) {
      links = new TraceEvents<Link>(traceParams.getMaxNumberOfLinks());
    }
    return links;
  }

  @GuardedBy("this")
  private Status getStatusWithDefault() {
    return status == null ? Status.OK : status;
  }

  private static <T> SpanData.TimedEvents<T> createTimedEvents(
      TraceEvents<EventWithNanoTime<T>> events, TimestampConverter timestampConverter) {
    if (events == null) {
      return SpanData.TimedEvents.create(Collections.<TimedEvent<T>>emptyList(), 0);
    }
    List<TimedEvent<T>> eventsList = new ArrayList<TimedEvent<T>>(events.events.size());
    for (EventWithNanoTime<T> networkEvent : events.events) {
      eventsList.add(networkEvent.toSpanDataTimedEvent(timestampConverter));
    }
    return SpanData.TimedEvents.create(eventsList, events.getNumberOfDroppedEvents());
  }

  @Override
  @Nullable
  public RecordEventsSpanImpl getNext() {
    return next;
  }

  @Override
  public void setNext(@Nullable RecordEventsSpanImpl element) {
    next = element;
  }

  @Override
  @Nullable
  public RecordEventsSpanImpl getPrev() {
    return prev;
  }

  @Override
  public void setPrev(@Nullable RecordEventsSpanImpl element) {
    prev = element;
  }

  /**
   * Interface to handle the start and end operations for a {@link Span} only when the {@code Span}
   * has {@link Options#RECORD_EVENTS} option.
   *
   * <p>Implementation must avoid high overhead work in any of the methods because the code is
   * executed on the critical path.
   *
   * <p>One instance can be called by multiple threads in the same time, so the implementation must
   * be thread-safe.
   */
  public interface StartEndHandler {
    void onStart(RecordEventsSpanImpl span);

    void onEnd(RecordEventsSpanImpl span);
  }

  // A map implementation with a fixed capacity that drops events when the map gets full. Eviction
  // is based on the access order.
  private static final class AttributesWithCapacity extends LinkedHashMap<String, AttributeValue> {
    private final int capacity;
    private int totalRecordedAttributes = 0;
    // Here because -Werror complains about this: [serial] serializable class AttributesWithCapacity
    // has no definition of serialVersionUID. This class shouldn't be serialized.
    private static final long serialVersionUID = 42L;

    private AttributesWithCapacity(int capacity) {
      // Capacity of the map is capacity + 1 to avoid resizing because removeEldestEntry is invoked
      // by put and putAll after inserting a new entry into the map. The loadFactor is set to 1
      // to avoid resizing because. The accessOrder is set to true.
      super(capacity + 1, 1, /*accessOrder=*/ true);
      this.capacity = capacity;
    }

    // Users must call this method instead of put to keep count of the total number of entries
    // inserted.
    private void putAttribute(String key, AttributeValue value) {
      totalRecordedAttributes += 1;
      put(key, value);
    }

    // Users must call this method instead of putAll to keep count of the total number of entries
    // inserted.
    private void putAttributes(Map<String, AttributeValue> attributes) {
      totalRecordedAttributes += attributes.size();
      putAll(attributes);
    }

    private int getNumberOfDroppedAttributes() {
      return totalRecordedAttributes - size();
    }

    // It is called after each put or putAll call in order to determine if the eldest inserted
    // entry should be removed or not.
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, AttributeValue> eldest) {
      return size() > this.capacity;
    }
  }

  private static final class TraceEvents<T> {
    private int totalRecordedEvents = 0;
    private final EvictingQueue<T> events;

    private int getNumberOfDroppedEvents() {
      return totalRecordedEvents - events.size();
    }

    TraceEvents(int maxNumEvents) {
      events = EvictingQueue.create(maxNumEvents);
    }

    void addEvent(T event) {
      totalRecordedEvents++;
      events.add(event);
    }
  }

  // Timed event that uses nanoTime to represent the Timestamp.
  private static final class EventWithNanoTime<T> {
    private final long nanoTime;
    private final T event;

    private EventWithNanoTime(long nanoTime, T event) {
      this.nanoTime = nanoTime;
      this.event = event;
    }

    private TimedEvent<T> toSpanDataTimedEvent(TimestampConverter timestampConverter) {
      return TimedEvent.create(timestampConverter.convertNanoTime(nanoTime), event);
    }
  }

  private RecordEventsSpanImpl(
      SpanContext context,
      String name,
      @Nullable Kind kind,
      @Nullable SpanId parentSpanId,
      @Nullable Boolean hasRemoteParent,
      TraceParams traceParams,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    super(context, RECORD_EVENTS_SPAN_OPTIONS);
    this.parentSpanId = parentSpanId;
    this.hasRemoteParent = hasRemoteParent;
    this.name = name;
    this.kind = kind;
    this.traceParams = traceParams;
    this.startEndHandler = startEndHandler;
    this.clock = clock;
    this.hasBeenEnded = false;
    this.sampleToLocalSpanStore = false;
    this.numberOfChildren = 0;
    this.timestampConverter =
        timestampConverter != null ? timestampConverter : TimestampConverter.now(clock);
    startNanoTime = clock.nowNanos();
  }

  @SuppressWarnings("NoFinalizer")
  @Override
  protected void finalize() throws Throwable {
    synchronized (this) {
      if (!hasBeenEnded) {
        logger.log(Level.WARNING, "Span " + name + " is GC'ed without being ended.");
      }
    }
    super.finalize();
  }
}
