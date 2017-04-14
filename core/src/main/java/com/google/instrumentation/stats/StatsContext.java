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

import java.io.IOException;

/**
 * An immutable context for stats operations.
 */
public abstract class StatsContext {
  /**
   * Returns a builder based on this {@link StatsContext}.
   */
  public abstract Builder builder();

  /** Shorthand for builder().set(k1, v1).build() */
  public final StatsContext with(TagKey k1, TagValue v1) {
    return builder().set(k1, v1).build();
  }

  /** Shorthand for builder().set(k1, v1).set(k2, v2).build() */
  public final StatsContext with(TagKey k1, TagValue v1, TagKey k2, TagValue v2) {
    return builder().set(k1, v1).set(k2, v2).build();
  }

  /** Shorthand for builder().set(k1, v1).set(k2, v2).set(k3, v3).build() */
  public final StatsContext with(
      TagKey k1, TagValue v1, TagKey k2, TagValue v2, TagKey k3, TagValue v3) {
    return builder().set(k1, v1).set(k2, v2).set(k3, v3).build();
  }

  /**
   * Records the given measurements against this {@link StatsContext}.
   *
   * @param measurements the measurements to record against the saved {@link StatsContext}
   * @return this
   */
  public abstract StatsContext record(MeasurementMap measurements);

  /**
   * Serializes the {@link StatsContext} into the on-the-wire representation.
   *
   * <p>The inverse of {@link StatsContextFactory#deserialize(byte[])} and should be
   * based on the {@link StatsContext} binary representation.
   *
   * @return a byte array that represents the serialized form of this {@link StatsContext}.
   */
  public abstract byte[] serialize() throws IOException;

  /**
   * Builder for {@link StatsContext}.
   */
  public abstract static class Builder {
    /**
     * Associates the given tag key with the given tag value.
     *
     * @param key the key
     * @param value the value to be associated with {@code key}
     * @return {@code this}
     */
    public abstract Builder set(TagKey key, TagValue value);

    /**
     * Builds a {@link StatsContext} from the specified keys and values.
     */
    public abstract StatsContext build();
  }
}
