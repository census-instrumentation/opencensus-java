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

package io.opencensus.tags;

import static io.opencensus.tags.TagKey.TagType.TAG_BOOL;
import static io.opencensus.tags.TagKey.TagType.TAG_LONG;
import static io.opencensus.tags.TagKey.TagType.TAG_STRING;

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
    TAG_LONG,
    TAG_BOOL
  }

  TagKey() {}

  /**
   * Constructs a {@code TagKey<String>} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<String> createString(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createStringInternal(name);
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
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<Long> createLong(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createLongInternal(name);
  }

  private static TagKey<Long> createLongInternal(String name) {
    return new AutoValue_TagKey<Long>(name, TAG_LONG);
  }

  /**
   * Constructs a {@code TagKey<Boolean>} from the given string. The string must meet the following
   * requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<Boolean> createBool(String name) {
    Preconditions.checkArgument(StringUtil.isValid(name));
    return createBoolInternal(name);
  }

  private static TagKey<Boolean> createBoolInternal(String name) {
    return new AutoValue_TagKey<Boolean>(name, TAG_BOOL);
  }

  abstract String getName();

  abstract TagType getTagType();
}
