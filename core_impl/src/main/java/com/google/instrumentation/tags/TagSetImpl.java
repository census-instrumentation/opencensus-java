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

/** A set of tags. */
// The class is immutable, except for the logger.
public final class TagSetImpl extends TagSet {

  private final Logger logger;

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey<?>, Object> tags;

  TagSetImpl(Logger logger, Map<TagKey<?>, Object> tags) {
    this.logger = logger;
    this.tags = tags;
  }

  Map<TagKey<?>, Object> getTags() {
    return tags;
  }

  @Override
  public boolean tagKeyExists(TagKey<?> key) {
    return tags.containsKey(key);
  }

  @Nullable
  @Override
  public String getStringTagValue(TagKey<String> key) {
    return (String) tags.get(key);
  }

  @Override
  public long getIntTagValue(TagKey<Long> key, long defaultValue) {
    Long value = (Long) tags.get(key);
    return value == null ? defaultValue : value;
  }

  @Override
  public boolean getBooleanTagValue(TagKey<Boolean> key, boolean defaultValue) {
    Boolean value = (Boolean) tags.get(key);
    return value == null ? defaultValue : value;
  }

  @Override
  public <T> T getTagValue(TagKey<T> key) {
    // An unchecked cast is okay, because we validate the values when they are inserted.
    @SuppressWarnings("unchecked")
    T value = (T) tags.get(key);
    return value;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(logger, getTags());
  }

  public static final class Builder extends TagSet.Builder {
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

    @Override
    public Builder insert(TagKey<String> key, String value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
      return insertInternal(key, StringUtil.sanitize(value));
    }

    @Override
    public Builder insert(TagKey<Long> key, long value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
      return insertInternal(key, value);
    }

    @Override
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

    @Override
    public Builder set(TagKey<String> key, String value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
      return setInternal(key, StringUtil.sanitize(value));
    }

    @Override
    public Builder set(TagKey<Long> key, long value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
      return setInternal(key, value);
    }

    @Override
    public Builder set(TagKey<Boolean> key, boolean value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_BOOL);
      return setInternal(key, value);
    }

    private <TagValueT> Builder setInternal(TagKey<TagValueT> key, TagValueT value) {
      tags.put(key, value);
      return this;
    }

    @Override
    public Builder update(TagKey<String> key, String value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_STRING);
      return updateInternal(key, StringUtil.sanitize(value));
    }

    @Override
    public Builder update(TagKey<Long> key, long value) {
      Preconditions.checkArgument(key.getTagType() == TagType.TAG_INT);
      return updateInternal(key, value);
    }

    @Override
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

    @Override
    public Builder clear(TagKey<?> key) {
      tags.remove(key);
      return this;
    }

    @Override
    public TagSetImpl build() {
      return new TagSetImpl(logger, new HashMap<TagKey<?>, Object>(tags));
    }
  }
}
