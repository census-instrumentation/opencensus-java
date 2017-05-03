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

import com.google.common.base.Preconditions;
import com.google.instrumentation.internal.StringUtil;
import com.google.instrumentation.internal.logging.Logger;
import com.google.instrumentation.tags.TagKey.TagType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/** A set of tags. */
// The class is immutable, except for the logger.
public final class TagSet {
  /** The maximum length for a string tag value. */
  public static final int MAX_STRING_LENGTH = StringUtil.MAX_LENGTH;

  private final Logger logger;

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey<?>, Object> tags;

  TagSet(Logger logger, Map<TagKey<?>, Object> tags) {
    this.logger = logger;
    this.tags = tags;
  }

  Map<TagKey<?>, Object> getTags() {
    return tags;
  }

  /**
   * Determines whether a key is present.
   *
   * @param key the key to look up.
   * @return {@code true} if the key is present.
   */
  public boolean tagKeyExists(TagKey<?> key) {
    return tags.containsKey(key);
  }

  // TODO(sebright): Which is better, the generic "get" method or three specialized "get" methods?
  // Should these methods take defaults or throw exceptions to handle missing keys?

  /**
   * Gets a value of type {@code String}.
   *
   * @param key the key to look up.
   * @return the tag value, or {@code null} if the key isn't present.
   */
  @Nullable
  public String getStringTagValue(TagKey<String> key) {
    return (String) tags.get(key);
  }

  /**
   * Gets a value of type {@code long}.
   *
   * @param key the key to look up.
   * @param defaultValue the value to return if the key is not preset.
   * @return the tag value, or the default value if the key is not present.
   */
  public long getIntTagValue(TagKey<Long> key, long defaultValue) {
    Long value = (Long) tags.get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Gets a value of type {@code boolean}.
   *
   * @param key the key to look up.
   * @param defaultValue the value to return if the key is not preset.
   * @return the tag value, or the default value if the key is not present.
   */
  public boolean getBooleanTagValue(TagKey<Boolean> key, boolean defaultValue) {
    Boolean value = (Boolean) tags.get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Gets a value of any type.
   *
   * @param key the key to look up.
   * @return the tag value, or {@code null} if the key isn't present.
   */
  @Nullable
  public <T> T getTagValue(TagKey<T> key) {
    // An unchecked cast is okay, because we validate the values when they are inserted.
    @SuppressWarnings("unchecked")
    T value = (T) tags.get(key);
    return value;
  }

  public Builder toBuilder() {
    return new Builder(logger, getTags());
  }

  /** Builder for the {@link TagSet} class. */
  @NotThreadSafe
  public static final class Builder {
    private final Logger logger;
    private final Map<TagKey<?>, Object> tags;

    private Builder(Logger logger, Map<TagKey<?>, Object> tags) {
      this.logger = logger;
      this.tags = new HashMap<TagKey<?>, Object>(tags);
    }

    Builder(Logger logger) {
      this.logger = logger;
      this.tags = new HashMap<TagKey<?>, Object>();
    }

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<String> key, String value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
      return insertInternal(key, StringUtil.sanitize(value));
    }

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<Long> key, long value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
      return insertInternal(key, value);
    }

    /**
     * Adds the key/value pair if the key is not present. If the key is present, it logs an error.
     *
     * @param key the key to look up.
     * @param value the value to insert for the given key.
     * @return this
     */
    public Builder insert(TagKey<Boolean> key, boolean value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_BOOL);
      return insertInternal(key, value);
    }

    private <TagValueT> Builder insertInternal(TagKey<TagValueT> key, TagValueT value) {
      if (!tags.containsKey(key)) {
        tags.put(key, value);
      } else {
        logger.log(Level.WARNING, "Tag key already exists: " + key);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_BOOL);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
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
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_BOOL);
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
      return new TagSet(logger, new HashMap<TagKey<?>, Object>(tags));
    }
  }
}
