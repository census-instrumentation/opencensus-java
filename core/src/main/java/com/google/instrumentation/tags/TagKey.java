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

import static com.google.instrumentation.tags.TagKey.TagType.TAG_BOOL;
import static com.google.instrumentation.tags.TagKey.TagType.TAG_INT;
import static com.google.instrumentation.tags.TagKey.TagType.TAG_STRING;

import com.google.auto.value.AutoValue;
import com.google.instrumentation.internal.StringUtil;
import javax.annotation.concurrent.Immutable;


/**
 * Tag key.
 *
 * @param <TagValueT> The type of value that can be paired with this {@code TagKey}.
 */
@Immutable
@AutoValue
public abstract class TagKey<TagValueT> {
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  enum TagType {
    TAG_STRING,
    TAG_INT,
    TAG_BOOL
  }

  TagKey() {}

  /**
   * Constructs a {@link TagKey} from the given string. The string will be sanitized such that:
   *
   * <ol>
   *   <li>length is restricted to {@link StringUtil#MAX_LENGTH}, strings longer than that will be
   *       truncated.
   *   <li>characters are restricted to printable ascii characters, non-printable characters will be
   *       replaced by an underscore '_'.
   * </ol>
   */
  public static TagKey<String> createString(String key) {
    return new AutoValue_TagKey<String>(StringUtil.sanitize(key), TAG_STRING);
  }

  public static TagKey<Long> createInt(String key) {
    return new AutoValue_TagKey<Long>(StringUtil.sanitize(key), TAG_INT);
  }

  public static TagKey<Boolean> createBoolean(String key) {
    return new AutoValue_TagKey<Boolean>(StringUtil.sanitize(key), TAG_BOOL);
  }

  abstract String getName();

  abstract TagType getTagType();
}
