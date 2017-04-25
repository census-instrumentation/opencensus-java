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

import com.google.auto.value.AutoValue;
import com.google.instrumentation.internal.StringUtil;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/** A set of tags. */
@Immutable
@AutoValue
public abstract class TagSet {
  /** The maximum length for a string tag value. */
  public static final int MAX_STRING_LENGTH = StringUtil.MAX_LENGTH;

  private static final TagSet EMPTY = createInternal(new HashMap<TagKey<?>, Object>());

  TagSet() {}

  static TagSet createInternal(Map<TagKey<?>, Object> tags) {
    return new AutoValue_TagSet(tags);
  }

  abstract Map<TagKey<?>, Object> getTags();

  /**
   * Returns an empty {@code TagSet}.
   *
   * @return an empty {@code TagSet}.
   */
  public static TagSet empty() {
    return EMPTY;
  }

  /**
   * Returns an empty builder.
   *
   * @return an empty builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a builder based on this {@code TagSet}.
   *
   * @return a builder based on this {@code TagSet}.
   */
  public Builder toBuilder() {
    return new Builder(getTags());
  }

  /**
   * Builder for the {@link TagSet} class.
   */
  public static final class Builder {
    private final Map<TagKey<?>, Object> tags;

    private Builder(Map<TagKey<?>, Object> tags) {
      this.tags = new HashMap<TagKey<?>, Object>(tags);
    }

    private Builder() {
      tags = new HashMap<TagKey<?>, Object>();
    }

    /**
     * Adds the key/value pair if the key is not present.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<String> key, String value) {
      return insertInternal(key, StringUtil.sanitize(value));
    }

    /**
     * Adds the key/value pair if the key is not present.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<Long> key, long value) {
      return insertInternal(key, value);
    }

    /**
     * Adds the key/value pair if the key is not present.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<Boolean> key, boolean value) {
      return insertInternal(key, value);
    }

    private <TagValueT> Builder insertInternal(TagKey<TagValueT> key, TagValueT value) {
      if (!tags.containsKey(key)) {
        tags.put(key, value);
      }
      return this;
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public Builder set(TagKey<String> key, String value) {
      return setInternal(key, StringUtil.sanitize(value));
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public Builder set(TagKey<Long> key, long value) {
      return setInternal(key, value);
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the key to look up.
     * @param value the value to set for the given key.
     * @return this
     */
    public Builder set(TagKey<Boolean> key, boolean value) {
      return setInternal(key, value);
    }

    private <TagValueT> Builder setInternal(TagKey<TagValueT> key, TagValueT value) {
      tags.put(key, value);
      return this;
    }

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public Builder update(TagKey<String> key, String value) {
      return updateInternal(key, StringUtil.sanitize(value));
    }

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public Builder update(TagKey<Long> key, long value) {
      return updateInternal(key, value);
    }

    /**
     * Adds the key/value pair only if the key is already present.
     *
     * @param key the key to look up.
     * @param value the value to update for the given key.
     * @return this
     */
    public Builder update(TagKey<Boolean> key, boolean value) {
      return updateInternal(key, value);
    }

    private <TagValueT> Builder updateInternal(TagKey<TagValueT> key, TagValueT value) {
      if (tags.containsKey(key)) {
        tags.put(key, value);
      }
      return this;
    }

    /**
     * Removes the key if it exists.
     *
     * @param key the key to look up.
     * @return this
     */
    public Builder clear(TagKey<?> key) {
      tags.remove(key);
      return this;
    }

    public TagSet build() {
      return createInternal(new HashMap<TagKey<?>, Object>(tags));
    }
  }
}
