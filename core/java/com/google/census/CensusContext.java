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

package com.google.census;

import java.nio.ByteBuffer;

/**
 * An immutable Census-specific context for an operation.
 */
public interface CensusContext {
  /** Returns a builder based on this {@link CensusContext} */
  Builder builder();

  /** Shorthand for builder().set(k1, v1).build() */
  CensusContext with(TagKey k1, TagValue v1);

  /** Shorthand for builder().set(k1, v1).set(k2, v2).build() */
  CensusContext with(TagKey k1, TagValue v1, TagKey k2, TagValue v2);

  /** Shorthand for builder().set(k1, v1).set(k2, v2).set(k3, v3).build() */
  CensusContext with(TagKey k1, TagValue v1, TagKey k2, TagValue v2, TagKey k3, TagValue v3);

  /**
   * Records the given metrics against this {@link CensusContext}.
   *
   * @param metrics the metrics to record against the saved {@link CensusContext}
   * @return this
   */
  CensusContext record(MetricMap metrics);

  /**
   * Serializes the {@link CensusContext} into the on-the-wire representation.
   *
   * @return serialized bytes.
   */
  ByteBuffer serialize();

  /** Sets the current thread-local {@link CensusContext}. */
  void setCurrent();

  /** Builder for {@link CensusContext}. */
  interface Builder {
    /**
     * Associates the given tag key with the given tag value.
     *
     * @param key the key
     * @param value the value to be associated with {@code key}
     * @return {@code this}
     */
    Builder set(TagKey key, TagValue value);

    /**
     * Builds a {@link CensusContext} from the specified keys and values.
     */
    CensusContext build();
  }
}
