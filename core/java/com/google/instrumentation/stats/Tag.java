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

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Tag)
        && key.equals(((Tag) obj).key)
        && value.equals(((Tag) obj).value);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "Tag<" + key + "," + value + ">";
  }

  private final TagKey key;
  private final TagValue value;

  private Tag(TagKey key, TagValue value) {
    this.key = key;
    this.value = value;
  }
}
