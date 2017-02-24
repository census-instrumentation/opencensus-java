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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.instrumentation.common.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A class that enables overriding the default values used when starting a {@link Span}. Allows
 * overriding the {@link Timestamp start time}, the {@link Sampler sampler}, the parent links, and
 * option to record all the events even if the {@code Span} is not sampled.
 */
public final class StartSpanOptions {
  private static final StartSpanOptions DEFAULT_OPTIONS = builder().build();
  private final Timestamp startTime;
  private final Sampler sampler;
  // This object is an unmodifiable List.
  private final List<Span> parentLinks;
  private final Boolean recordEvents;

  /**
   * Returns default {@code StartSpanOptions}.
   *
   * @return default {@code StartSpanOptions}.
   */
  public static StartSpanOptions getDefault() {
    return DEFAULT_OPTIONS;
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new Builder();
  }

  private StartSpanOptions(
      @Nullable Timestamp startTime,
      @Nullable Sampler sampler,
      @Nullable List<Span> parentLinks,
      @Nullable Boolean recordEvents) {
    this.startTime = startTime;
    this.sampler = sampler;
    // Make parentLinks an unmodifiable list.
    this.parentLinks =
        parentLinks == null
            ? Collections.<Span>emptyList()
            : Collections.unmodifiableList(new ArrayList<Span>(parentLinks));
    this.recordEvents = recordEvents;
  }

  /**
   * Returns start time to be used, or {@code null} if default.
   *
   * @return start time to be used, or {@code null} if default.
   */
  @Nullable
  public Timestamp getStartTime() {
    return startTime;
  }

  /**
   * Returns the {@link Sampler} to be used, or {@code null} if default.
   *
   * @return the {@code Sampler} to be used, or {@code null} if default.
   */
  @Nullable
  public Sampler getSampler() {
    return sampler;
  }

  /**
   * Returns the parent links to be set for the {@link Span}.
   *
   * @return the parent links to be set for the {@code Span}.
   */
  public List<Span> getParentLinks() {
    // It is safe to directly return parentLinks because it is an unmodifiable list.
    return parentLinks;
  }

  /**
   * Returns the record events option setting.
   *
   * @return the record events option setting.
   */
  @Nullable
  public Boolean getRecordEvents() {
    return recordEvents;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof StartSpanOptions)) {
      return false;
    }

    StartSpanOptions that = (StartSpanOptions) obj;
    return Objects.equal(startTime, that.startTime)
        && Objects.equal(sampler, that.sampler)
        && Objects.equal(parentLinks, that.parentLinks)
        && Objects.equal(recordEvents, that.recordEvents);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(startTime, sampler, parentLinks, recordEvents);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("startTime", startTime)
        .add("sampler", sampler)
        .add("parentLinks", parentLinks)
        .add("recordEvents", recordEvents)
        .toString();
  }

  /**
   * Builder class for {@link StartSpanOptions}.
   */
  public static final class Builder {
    private Timestamp startTime;
    private Sampler sampler;
    private List<Span> parentLinks;
    private Boolean recordEvents;

    private Builder() {}

    /**
     * Sets the start time for the {@link Span}.
     *
     * @param startTime The start time for the {@code Span}. If {@code null} is used, then the
     *     current system time at the point at which {@link Tracer#startSpan} is called will be
     *     used.
     * @return this.
     */
    public Builder setStartTime(@Nullable Timestamp startTime) {
      this.startTime = startTime;
      return this;
    }

    /**
     * Sets the {@link Sampler} to use. If a {@code null} value is passed, the implementation will
     * provide a default.
     *
     * @param sampler The {@code Sampler} to use when determining sampling for a {@code Span}.
     * @return this.
     */
    public Builder setSampler(@Nullable Sampler sampler) {
      this.sampler = sampler;
      return this;
    }

    /**
     * Adds one parent link. Links are used to link {@link Span}s in different traces. Used (for
     * example) in batching operations, where a single batch handler processes multiple requests
     * from different traces.
     *
     * @param parentLink The new {@code Span} parent link.
     * @return this.
     * @throws NullPointerException if {@code parentLink} is {@code null}.
     */
    public Builder addParentLink(Span parentLink) {
      if (parentLinks == null) {
        parentLinks = new LinkedList<Span>();
      }
      parentLinks.add(checkNotNull(parentLink, "parentLink"));
      return this;
    }

    /**
     * Adds a {@code List} of parent links. See {@link #addParentLink}.
     *
     * @param parentLinks New links to be added.
     * @return this.
     * @throws NullPointerException if {@code parentLinks} is {@code null}.
     */
    public Builder addParentLinks(List<Span> parentLinks) {
      if (this.parentLinks == null) {
        this.parentLinks = new LinkedList<Span>();
      }
      this.parentLinks.addAll(checkNotNull(parentLinks, "parentLinks"));
      return this;
    }

    /**
     * Sets recordEvents.
     *
     * @param recordEvents New value determining if this {@code Span} should have events recorded.
     *     If a {@code null} value is passed, the implementation will provide a default.
     * @return this.
     */
    public Builder setRecordEvents(@Nullable Boolean recordEvents) {
      this.recordEvents = recordEvents;
      return this;
    }

    /**
     * Builds and returns a {@link StartSpanOptions} with the desired settings.
     *
     * @return a {@link StartSpanOptions} with the desired settings.
     */
    public StartSpanOptions build() {
      return new StartSpanOptions(startTime, sampler, parentLinks, recordEvents);
    }
  }
}
