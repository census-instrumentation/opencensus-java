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

package io.opencensus.trace.export;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of all data collected by the {@link Span} class. */
@Immutable
@AutoValue
public abstract class SpanData {

  /**
   * Returns a new immutable {@code SpanData}.
   *
   * @param context the {@code SpanContext} of the {@code Span}.
   * @param parentSpanId the parent {@code SpanId} of the {@code Span}. {@code null} if the {@code
   *     Span} is a root.
   * @param hasRemoteParent {@code true} if the parent {@code Span} is remote. {@code null} if this
   *     is a root span.
   * @param name the name of the {@code Span}.
   * @param startTimestamp the start {@code Timestamp} of the {@code Span}.
   * @param attributes the attributes associated with the {@code Span}.
   * @param annotations the annotations associated with the {@code Span}.
   * @param networkEvents the network events associated with the {@code Span}.
   * @param links the links associated with the {@code Span}.
   * @param childSpanCount the number of child spans that were generated while the span was active.
   * @param status the {@code Status} of the {@code Span}. {@code null} if the {@code Span} is still
   *     active.
   * @param endTimestamp the end {@code Timestamp} of the {@code Span}. {@code null} if the {@code
   *     Span} is still active.
   * @return a new immutable {@code SpanData}.
   */
  public static SpanData create(
      SpanContext context,
      @Nullable SpanId parentSpanId,
      @Nullable Boolean hasRemoteParent,
      String name,
      Timestamp startTimestamp,
      Attributes attributes,
      TimedEvents<Annotation> annotations,
      TimedEvents<NetworkEvent> networkEvents,
      Links links,
      @Nullable Integer childSpanCount,
      @Nullable Status status,
      @Nullable Timestamp endTimestamp) {
    return new AutoValue_SpanData(
        context,
        parentSpanId,
        hasRemoteParent,
        name,
        startTimestamp,
        attributes,
        annotations,
        networkEvents,
        links,
        childSpanCount,
        status,
        endTimestamp);
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   */
  public abstract SpanContext getContext();

  /**
   * Returns the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   *
   * @return the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   */
  @Nullable
  public abstract SpanId getParentSpanId();

  /**
   * Returns {@code true} if the parent is on a different process. {@code null} if this is a root
   * span.
   *
   * @return {@code true} if the parent is on a different process. {@code null} if this is a root
   *     span.
   */
  @Nullable
  public abstract Boolean getHasRemoteParent();

  /**
   * Returns the name of this {@code Span}.
   *
   * @return the name of this {@code Span}.
   */
  public abstract String getName();

  /**
   * Returns the start {@code Timestamp} of this {@code Span}.
   *
   * @return the start {@code Timestamp} of this {@code Span}.
   */
  public abstract Timestamp getStartTimestamp();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   */
  public abstract Attributes getAttributes();

  /**
   * Returns the annotations recorded for this {@code Span}.
   *
   * @return the annotations recorded for this {@code Span}.
   */
  public abstract TimedEvents<Annotation> getAnnotations();

  /**
   * Returns network events recorded for this {@code Span}.
   *
   * @return network events recorded for this {@code Span}.
   */
  public abstract TimedEvents<NetworkEvent> getNetworkEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   */
  public abstract Links getLinks();

  /**
   * Returns the number of child spans that were generated while the {@code Span} was running. If
   * not {@code null} allows service implementations to detect missing child spans.
   *
   * <p>This information is not always available.
   *
   * @return the number of child spans that were generated while the {@code Span} was running.
   */
  @Nullable
  public abstract Integer getChildSpanCount();

  /**
   * Returns the {@code Status} or {@code null} if {@code Span} is still active.
   *
   * @return the {@code Status} or {@code null} if {@code Span} is still active.
   */
  @Nullable
  public abstract Status getStatus();

  /**
   * Returns the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   *
   * @return the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   */
  @Nullable
  public abstract Timestamp getEndTimestamp();

  SpanData() {}

  /**
   * A timed event representation.
   *
   * @param <T> the type of value that is timed.
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvent<T> {
    /**
     * Returns a new immutable {@code TimedEvent<T>}.
     *
     * @param timestamp the {@code Timestamp} of this event.
     * @param event the event.
     * @param <T> the type of value that is timed.
     * @return a new immutable {@code TimedEvent<T>}
     */
    public static <T> TimedEvent<T> create(Timestamp timestamp, T event) {
      return new AutoValue_SpanData_TimedEvent<T>(timestamp, event);
    }

    /**
     * Returns the {@code Timestamp} of this event.
     *
     * @return the {@code Timestamp} of this event.
     */
    public abstract Timestamp getTimestamp();

    /**
     * Returns the event.
     *
     * @return the event.
     */
    public abstract T getEvent();

    TimedEvent() {}
  }

  /**
   * A list of timed events and the number of dropped events representation.
   *
   * @param <T> the type of value that is timed.
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvents<T> {
    /**
     * Returns a new immutable {@code TimedEvents<T>}.
     *
     * @param events the list of events.
     * @param droppedEventsCount the number of dropped events.
     * @param <T> the type of value that is timed.
     * @return a new immutable {@code TimedEvents<T>}
     */
    public static <T> TimedEvents<T> create(List<TimedEvent<T>> events, int droppedEventsCount) {
      return new AutoValue_SpanData_TimedEvents<T>(
          Collections.unmodifiableList(
              new ArrayList<TimedEvent<T>>(checkNotNull(events, "events"))),
          droppedEventsCount);
    }

    /**
     * Returns the list of events.
     *
     * @return the list of events.
     */
    public abstract List<TimedEvent<T>> getEvents();

    /**
     * Returns the number of dropped events.
     *
     * @return the number of dropped events.
     */
    public abstract int getDroppedEventsCount();

    TimedEvents() {}
  }

  /** A set of attributes and the number of dropped attributes representation. */
  @Immutable
  @AutoValue
  public abstract static class Attributes {
    /**
     * Returns a new immutable {@code Attributes}.
     *
     * @param attributeMap the set of attributes.
     * @param droppedAttributesCount the number of dropped attributes.
     * @return a new immutable {@code Attributes}.
     */
    public static Attributes create(
        Map<String, AttributeValue> attributeMap, int droppedAttributesCount) {
      // TODO(bdrutu): Consider to use LinkedHashMap here and everywhere else, less test flakes
      // for others on account of determinism.
      return new AutoValue_SpanData_Attributes(
          Collections.unmodifiableMap(
              new HashMap<String, AttributeValue>(checkNotNull(attributeMap, "attributeMap"))),
          droppedAttributesCount);
    }

    /**
     * Returns the set of attributes.
     *
     * @return the set of attributes.
     */
    public abstract Map<String, AttributeValue> getAttributeMap();

    /**
     * Returns the number of dropped attributes.
     *
     * @return the number of dropped attributes.
     */
    public abstract int getDroppedAttributesCount();

    Attributes() {}
  }

  /** A list of links and the number of dropped links representation. */
  @Immutable
  @AutoValue
  public abstract static class Links {
    /**
     * Returns a new immutable {@code Links}.
     *
     * @param links the list of links.
     * @param droppedLinksCount the number of dropped links.
     * @return a new immutable {@code Links}.
     */
    public static Links create(List<Link> links, int droppedLinksCount) {
      return new AutoValue_SpanData_Links(
          Collections.unmodifiableList(new ArrayList<Link>(checkNotNull(links, "links"))),
          droppedLinksCount);
    }

    /**
     * Returns the list of links.
     *
     * @return the list of links.
     */
    public abstract List<Link> getLinks();

    /**
     * Returns the number of dropped links.
     *
     * @return the number of dropped links.
     */
    public abstract int getDroppedLinksCount();

    Links() {}
  }
}
