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

import com.google.auto.value.AutoValue;
import io.opencensus.internal.StringUtil;
import javax.annotation.concurrent.Immutable;

/** A key to a value stored in a {@link TagContext}. */
@Immutable
public abstract class TagKey {
  /** The maximum length for a tag key name. */
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  TagKey() {}

  /**
   * Constructs a {@code TagKeyString} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   * <li>It cannot be longer than {@link #MAX_LENGTH}.
   * <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @return a {@code TagKey<String>} with the given name.
   * @throws IllegalArgumentException if the name is not valid.
   */
  public static TagKeyString createStringKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return TagKeyString.create(name);
  }

  /**
   * Constructs a {@code TagKeyLong} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   * <li>It cannot be longer than {@link #MAX_LENGTH}.
   * <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static TagKeyLong createLongKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return TagKeyLong.create(name);
  }

  /**
   * Constructs a {@code TagKeyBoolean} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   * <li>It cannot be longer than {@link #MAX_LENGTH}.
   * <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @throws IllegalArgumentException if the name is not valid.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static TagKeyBoolean createBooleanKey(String name) {
    checkArgument(StringUtil.isValid(name));
    return TagKeyBoolean.create(name);
  }

  public abstract String getName();

  /**
   * A {@code TagKey} for values of type {@code String}.
   */
  @Immutable
  @AutoValue
  public abstract static class TagKeyString extends TagKey {
    static TagKeyString create(String name) {
      return new AutoValue_TagKey_TagKeyString(name);
    }
  }

  /**
   * A {@code TagKey} for values of type {@code long}.
   */
  @Immutable
  @AutoValue
  public abstract static class TagKeyLong extends TagKey {
    static TagKeyLong create(String name) {
      return new AutoValue_TagKey_TagKeyLong(name);
    }
  }

  /**
   * A {@code TagKey} for values of type {@code boolean}.
   */
  @Immutable
  @AutoValue
  public abstract static class TagKeyBoolean extends TagKey {
    static TagKeyBoolean create(String name) {
      return new AutoValue_TagKey_TagKeyBoolean(name);
    }
  }
}
