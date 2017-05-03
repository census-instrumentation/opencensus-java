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

package com.google.instrumentation.tags;

import com.google.instrumentation.internal.StringUtil;
import javax.annotation.Nullable;

/** A set of tags. */
public abstract class TagSet {
  /** The maximum length for a string tag value. */
  public static final int MAX_STRING_LENGTH = StringUtil.MAX_LENGTH;

  /**
   * Determines whether a key is present.
   *
   * @param key the key to look up.
   * @return {@code true} if the key is present.
   */
  public abstract boolean tagKeyExists(TagKey<?> key);

  // TODO(sebright): Which is better, the generic "get" method or three specialized "get" methods?
  // Should these methods take defaults or throw exceptions to handle missing keys?

  /**
   * Gets a value of type {@code String}.
   *
   * @param key the key to look up.
   * @return the tag value, or {@code null} if the key isn't present.
   */
  @Nullable
  public abstract String getStringTagValue(TagKey<String> key);

  /**
   * Gets a value of type {@code long}.
   *
   * @param key the key to look up.
   * @param defaultValue the value to return if the key is not preset.
   * @return the tag value, or the default value if the key is not present.
   */
  public abstract long getIntTagValue(TagKey<Long> key, long defaultValue);

  /**
   * Gets a value of type {@code boolean}.
   *
   * @param key the key to look up.
   * @param defaultValue the value to return if the key is not preset.
   * @return the tag value, or the default value if the key is not present.
   */
  public abstract boolean getBooleanTagValue(TagKey<Boolean> key, boolean defaultValue);

  /**
   * Gets a value of any type.
   *
   * @param key the key to look up.
   * @return the tag value, or {@code null} if the key isn't present.
   */
  @Nullable
  public abstract <T> T getTagValue(TagKey<T> key);

  /**
   * Returns a builder based on this {@code TagSet}.
   *
   * @return a builder based on this {@code TagSet}.
   */
  public abstract Builder toBuilder();

  /** Builder for the {@link TagSet} class. */
  public abstract static class Builder {

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public abstract Builder insert(TagKey<String> key, String value);

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public abstract Builder insert(TagKey<Long> key, long value);

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public abstract Builder insert(TagKey<Boolean> key, boolean value);

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public abstract Builder set(TagKey<String> key, String value);

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public abstract Builder set(TagKey<Long> key, long value);

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public abstract Builder set(TagKey<Boolean> key, boolean value);

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public abstract Builder update(TagKey<String> key, String value);

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public abstract Builder update(TagKey<Long> key, long value);

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public abstract Builder update(TagKey<Boolean> key, boolean value);

    /**
     * Removes the key if it exists.
     *
     * @param key the key to look up.
     * @return this
     */
    public abstract Builder clear(TagKey<?> key);

    /**
     * Creates a {@code TagSet} from this builder.
     *
     * @return a {@code TagSet} with the same tags as this builder.
     */
    public abstract TagSet build();
  }
}
