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

package io.opencensus.stats;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Native Implementation of {@link StatsContext}.
 */
final class StatsContextImpl extends StatsContext {
  private final StatsRecorderImpl statsRecorder;
  final Map<TagKey, TagValue> tags;

  StatsContextImpl(StatsRecorderImpl statsRecorder, Map<TagKey, TagValue> tags) {
    this.statsRecorder = Preconditions.checkNotNull(statsRecorder);
    this.tags = Preconditions.checkNotNull(tags);
  }

  @Override
  public Builder builder() {
    return new Builder(statsRecorder, tags);
  }

  @Override
  public StatsContextImpl record(MeasurementMap stats) {
    statsRecorder.record(this, stats);
    return this;
  }

  /**
   * Serializes a {@link StatsContextImpl} using the format defined in {@code StatsSerializer}.
   *
   * <p>The encoded tags are of the form: {@code <version_id><encoded_tags>}
   */
  @Override
  public void serialize(OutputStream output) throws IOException {
    StatsSerializer.serialize(this, output);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof StatsContextImpl) && tags.equals(((StatsContextImpl) obj).tags);
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  @Override
  public String toString() {
    return tags.toString();
  }

  static final class Builder extends StatsContext.Builder {
    private final StatsRecorderImpl statsRecorder;
    private final HashMap<TagKey, TagValue> tags;

    private Builder(StatsRecorderImpl statsRecorder, Map<TagKey, TagValue> tags) {
      this.statsRecorder = statsRecorder;
      this.tags = new HashMap<TagKey, TagValue>(tags);
    }

    @Override
    public Builder set(TagKey key, TagValue value) {
      tags.put(key, value);
      return this;
    }

    @Override
    public StatsContextImpl build() {
      return new StatsContextImpl(
          statsRecorder, Collections.unmodifiableMap(new HashMap<TagKey, TagValue>(tags)));
    }
  }
}
