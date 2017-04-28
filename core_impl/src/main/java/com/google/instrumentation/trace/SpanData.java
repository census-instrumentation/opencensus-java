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

import com.google.auto.value.AutoValue;
import com.google.instrumentation.common.Timestamp;
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
   * @param displayName the name of the {@code Span}.
   * @param startTimestamp the start {@code Timestamp} of the {@code Span}.
   * @param attributes the attributes associated with the {@code Span}.
   * @param annotations the annotations associated with the {@code Span}.
   * @param networkEvents the network events associated with the {@code Span}.
   * @param links the links associated with the {@code Span}.
   * @param status the {@code Status} of the {@code Span}. {@code null} if the {@code Span} is still
   *     active.
   * @param endTimestamp the end {@code Timestamp} of the {@code Span}. {@code null} if the {@code
   *     Span} is still active.
   * @return a new immutable {@code SpanData}.
   */
  public static SpanData create(
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
    return new AutoValue_SpanData(
        context,
        parentSpanId,
        displayName,
        startTimestamp,
        Collections.unmodifiableMap(new HashMap<String, AttributeValue>(attributes)),
        Collections.unmodifiableList(new ArrayList<TimedEvent<Annotation>>(annotations)),
        Collections.unmodifiableList(new ArrayList<TimedEvent<NetworkEvent>>(networkEvents)),
        Collections.unmodifiableList(new ArrayList<Link>(links)),
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
   * Returns the display name of this {@code Span}.
   *
   * @return the display name of this {@code Span}.
   */
  public abstract String getDisplayName();

  /**
   * Returns the start {@code Timestamp} of this {@code Span}.
   *
   * @return the start {@code Timestamp} of this {@code Span}.
   */
  public abstract Timestamp getStartTimestamp();

  /**
   * Returns the set of attributes recorded for this {@code Span}.
   *
   * @return the set of attributes recorded for this {@code Span}.
   */
  public abstract Map<String, AttributeValue> getAttributes();

  /**
   * Returns the list of {@code Annotation}s recorded for this {@code Span}.
   *
   * @return the list of {@code Annotation}s recorded for this {@code Span}.
   */
  public abstract List<TimedEvent<Annotation>> getAnnotations();

  /**
   * Returns the list of {@code NetworkEvent}s recorded for this {@code Span}.
   *
   * @return the list of {@code NetworkEvent}s recorded for this {@code Span}.
   */
  public abstract List<TimedEvent<NetworkEvent>> getNetworkEvents();

  /**
   * Returns the list of {@code Link}s recorded for this {@code Span}.
   *
   * @return the list of {@code Link}s recorded for this {@code Span}.
   */
  public abstract List<Link> getLinks();

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
}
