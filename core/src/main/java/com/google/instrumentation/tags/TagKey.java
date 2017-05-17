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
import com.google.common.base.Preconditions;
import com.google.instrumentation.internal.StringUtil;
import javax.annotation.concurrent.Immutable;

/**
 * A key to a value stored in a {@link TagMap}.
 *
 * @param <TagValueT> The type of value that can be paired with this {@code TagKey}. {@code TagKey}s
 *     can only be instantiated with types {@code String}, {@code Long}, and {@code Boolean}.
 */
@Immutable
@AutoValue
public abstract class TagKey<TagValueT> {
  /** The maximum length for a tag key name. */
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  enum TagType {
    TAG_STRING,
    TAG_INT,
    TAG_BOOL
  }

  TagKey() {}

  /**
   * Constructs a {@code TagKey<String>} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ascii characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<String> createString(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createStringInternal(name);
  }

  // Creates a TagKey<String> by sanitizing the given String.
  static TagKey<String> createStringSanitized(String name) {
    return createStringInternal(StringUtil.sanitize(name));
  }

  private static TagKey<String> createStringInternal(String name) {
    return new AutoValue_TagKey<String>(name, TAG_STRING);
  }

  /**
   * Constructs a {@code TagKey<Long>} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ascii characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<Long> createInt(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createIntInternal(name);
  }

  // Creates a TagKey<Long> by sanitizing the given String.
  static TagKey<Long> createIntSanitized(String name) {
    return createIntInternal(StringUtil.sanitize(name));
  }

  private static TagKey<Long> createIntInternal(String name) {
    return new AutoValue_TagKey<Long>(name, TAG_INT);
  }

  /**
   * Constructs a {@code TagKey<Boolean>} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ascii characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<Boolean> createBool(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createBoolInternal(name);
  }

  // Creates a TagKey<Boolean> by sanitizing the given String.
  static TagKey<Boolean> createBoolSanitized(String name) {
    return createBoolInternal(StringUtil.sanitize(name));
  }

  private static TagKey<Boolean> createBoolInternal(String name) {
    return new AutoValue_TagKey<Boolean>(name, TAG_BOOL);
  }

  abstract String getName();

  abstract TagType getTagType();
}
