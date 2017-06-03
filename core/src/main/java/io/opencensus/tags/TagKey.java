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

import static com.google.common.base.Preconditions.checkArgument;
import static io.opencensus.tags.TagKey.TagType.TAG_BOOLEAN;
import static io.opencensus.tags.TagKey.TagType.TAG_LONG;
import static io.opencensus.tags.TagKey.TagType.TAG_STRING;

import com.google.auto.value.AutoValue;
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
    TAG_BOOLEAN
  }

  TagKey() {}

  /**
   * Constructs a {@code TagKey<String>} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKey<String> createStringKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return new AutoValue_TagKey<String>(name, TAG_STRING);
  }

  /**
   * Constructs a {@code TagKey<Long>} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static TagKey<Long> createLongKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return new AutoValue_TagKey<Long>(name, TAG_LONG);
  }

  /**
   * Constructs a {@code TagKey<Boolean>} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static TagKey<Boolean> createBooleanKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return new AutoValue_TagKey<Boolean>(name, TAG_BOOLEAN);
  }

  abstract String getName();

  abstract TagType getTagType();
}
