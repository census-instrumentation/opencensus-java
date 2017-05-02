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

package com.google.instrumentation.stats;

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
  private final StatsManagerImplBase statsManager;
  final Map<TagKey, TagValue> tags;

  StatsContextImpl(StatsManagerImplBase statsManager, Map<TagKey, TagValue> tags) {
    this.statsManager = Preconditions.checkNotNull(statsManager);
    this.tags = Preconditions.checkNotNull(tags);
  }

  @Override
  public Builder builder() {
    return new Builder(statsManager, tags);
  }

  @Override
  public StatsContextImpl record(MeasurementMap stats) {
    statsManager.record(this, stats);
    return this;
  }

  /**
   * Serializes a {@link StatsContextImpl} into {@code CensusContextProto} serialized format.
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

  private static final class Builder extends StatsContext.Builder {
    private final StatsManagerImplBase statsManager;
    private final HashMap<TagKey, TagValue> tags;

    private Builder(StatsManagerImplBase statsManager, Map<TagKey, TagValue> tags) {
      this.statsManager = statsManager;
      this.tags = new HashMap<TagKey, TagValue>(tags);
    }

    @Override
    public Builder set(TagKey key, TagValue value) {
      tags.put(key, value);
      return this;
    }

    @Override
    public StatsContext build() {
      return new StatsContextImpl(
          statsManager, Collections.unmodifiableMap(new HashMap<TagKey, TagValue>(tags)));
    }
  }
}
