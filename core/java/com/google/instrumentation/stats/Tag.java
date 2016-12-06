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

/**
 * A pair of consisting of an associated {@link TagKey} and {@link TagValue}.
 */
public final class Tag {
  /**
   * Constructs a new {@link Tag} from the given key and value.
   */
  public static Tag create(TagKey key, TagValue value) {
    return new Tag(key, value);
  }

  /**
   * Returns the associated tag key.
   */
  public TagKey getKey() {
    return key;
  }

  /**
   * Returns the associated tag key.
   */
  public TagValue getValue() {
    return value;
  }

  private final TagKey key;
  private final TagValue value;

  private Tag(TagKey key, TagValue value) {
    this.key = key;
    this.value = value;
  }
}
