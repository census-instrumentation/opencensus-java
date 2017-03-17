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

import com.google.common.base.Objects;
import com.google.instrumentation.common.Timestamp;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A class that enables overriding the default values used when starting a {@link Span}. Allows
 * overriding the {@link Timestamp start time}, the {@link Sampler sampler}, the parent links, and
 * option to record all the events even if the {@code Span} is not sampled.
 */
final class StartSpanOptions {
  private Timestamp startTime;
  private Sampler sampler;
  private List<Span> parentLinks;
  private Boolean recordEvents;

  StartSpanOptions() {
    this.startTime = null;
    this.sampler = null;
    this.parentLinks = null;
    this.recordEvents = null;
  }

  /**
   * Returns start time to be used, or {@code null} if default.
   *
   * @return start time to be used, or {@code null} if default.
   */
  @Nullable
  Timestamp getStartTime() {
    return startTime;
  }

  /**
   * Returns the {@link Sampler} to be used, or {@code null} if default.
   *
   * @return the {@code Sampler} to be used, or {@code null} if default.
   */
  @Nullable
  Sampler getSampler() {
    return sampler;
  }

  /**
   * Returns the parent links to be set for the {@link Span}.
   *
   * @return the parent links to be set for the {@code Span}.
   */
  List<Span> getParentLinks() {
    // Return an unmodifiable list.
    return parentLinks == null
        ? Collections.<Span>emptyList()
        : Collections.unmodifiableList(parentLinks);
  }

  /**
   * Returns the record events option setting.
   *
   * @return the record events option setting.
   */
  @Nullable
  Boolean getRecordEvents() {
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

  void setStartTime(@Nullable Timestamp startTime) {
    this.startTime = startTime;
  }

  void setSampler(@Nullable Sampler sampler) {
    this.sampler = sampler;
  }

  void setParentLinks(@Nullable List<Span> parentLinks) {
    this.parentLinks = parentLinks;
  }

  void setRecordEvents(@Nullable Boolean recordEvents) {
    this.recordEvents = recordEvents;
  }
}
