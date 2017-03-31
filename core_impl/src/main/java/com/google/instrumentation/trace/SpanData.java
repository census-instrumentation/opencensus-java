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

import com.google.common.base.MoreObjects;
import com.google.instrumentation.common.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of all data collected by the {@link Span} class. */
@Immutable
public final class SpanData {
  private final SpanContext context;
  private final SpanId parentSpanId;
  private final String displayName;
  private final Timestamp startTimestamp;
  private final Map<String, AttributeValue> attributes;
  private final List<TimedEvent<Annotation>> annotations;
  private final List<TimedEvent<NetworkEvent>> networkEvents;
  private final List<Link> links;
  private final Status status;
  private final Timestamp endTimestamp;

  // This constructor must be called only by the implementation of the Span.
  SpanData(
      SpanContext context,
      @Nullable SpanId parentSpanId,
      String displayName,
      Timestamp startTimestamp,
      Map<String, AttributeValue> attributes,
      List<TimedEvent<Annotation>> annotations,
      List<TimedEvent<NetworkEvent>> networkEvents,
      List<Link> links,
      @Nullable Status status,
      @Nullable Timestamp endTimestamp) {
    this.context = context;
    this.parentSpanId = parentSpanId;
    this.displayName = displayName;
    this.startTimestamp = startTimestamp;
    this.attributes = Collections.unmodifiableMap(attributes);
    this.annotations = Collections.unmodifiableList(annotations);
    this.networkEvents = Collections.unmodifiableList(networkEvents);
    this.links = Collections.unmodifiableList(links);
    this.status = status;
    this.endTimestamp = endTimestamp;
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   */
  public SpanContext getContext() {
    return context;
  }

  /**
   * Returns the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   *
   * @return the parent {@code SpanId} or {@code null} if the {@code Span} is a root {@code Span}.
   */
  @Nullable
  public SpanId getParentSpanId() {
    return parentSpanId;
  }

  /**
   * Returns the display name of this {@code Span}.
   *
   * @return the display name of this {@code Span}.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Returns the start {@code Timestamp} of this {@code Span}.
   *
   * @return the start {@code Timestamp} of this {@code Span}.
   */
  public Timestamp getStartTimestamp() {
    return startTimestamp;
  }

  /**
   * Returns the set of attributes recorded for this {@code Span}.
   *
   * @return the set of attributes recorded for this {@code Span}.
   */
  public Map<String, AttributeValue> getAttributes() {
    return attributes;
  }

  /**
   * Returns the list of {@code Annotation}s recorded for this {@code Span}.
   *
   * @return the list of {@code Annotation}s recorded for this {@code Span}.
   */
  public List<TimedEvent<Annotation>> getAnnotations() {
    return annotations;
  }

  /**
   * Returns the list of {@code NetworkEvent}s recorded for this {@code Span}.
   *
   * @return the list of {@code NetworkEvent}s recorded for this {@code Span}.
   */
  public List<TimedEvent<NetworkEvent>> getNetworkEvents() {
    return networkEvents;
  }

  /**
   * Returns the list of {@code Link}s recorded for this {@code Span}.
   *
   * @return the list of {@code Link}s recorded for this {@code Span}.
   */
  public List<Link> getLinks() {
    return links;
  }

  /**
   * Returns the {@code Status} or {@code null} if {@code Span} is still active.
   *
   * @return the {@code Status} or {@code null} if {@code Span} is still active.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Returns the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   *
   * @return the end {@code Timestamp} or {@code null} if the {@code Span} is still active.
   */
  @Nullable
  public Timestamp getEndTimestamp() {
    return endTimestamp;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("context", context)
        .add("parentSpanId", parentSpanId)
        .add("displayName", displayName)
        .add("startTimestamp", startTimestamp)
        .add("attributes", attributes)
        .add("annotations", annotations)
        .add("networkEvents", networkEvents)
        .add("links", links)
        .add("status", status)
        .add("endTimestamp", endTimestamp)
        .toString();
  }
}
