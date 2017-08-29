/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.tags;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import io.opencensus.common.Function;
import io.opencensus.internal.StringUtil;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import javax.annotation.concurrent.Immutable;

/** A validated tag value. */
@Immutable
public abstract class TagValue {

  TagValue() {}

  /**
   * Applies a function to a tag value. The function that is called depends on the type of the
   * value. This is similar to the visitor pattern. {@code match} also takes a function to handle
   * the default case, for backwards compatibility when tag types are added.
   *
   * @param stringFunction the function to call when the tag value has type {@code String}.
   * @param longFunction the function to call when the tag value has type {@code long}.
   * @param booleanFunction the function to call when the tag value has type {@code boolean}.
   * @param defaultFunction the function to call when the tag value has a type other than {@code
   *     String}, {@code long}, or {@code boolean}.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag value's type.
   */
  public abstract <T> T match(
      Function<? super TagValueString, T> stringFunction,
      Function<? super TagValueLong, T> longFunction,
      Function<? super TagValueBoolean, T> booleanFunction,
      Function<? super TagValue, T> defaultFunction);

  /**
   * A validated tag value associated with a {@link TagKeyString}.
   *
   * <p>Validation ensures that the {@code String} has a maximum length of {@link #MAX_LENGTH} and
   * contains only printable ASCII characters.
   */
  @Immutable
  @AutoValue
  public abstract static class TagValueString extends TagValue {
    /** The maximum length for a {@code String} tag value. The value is {@value #MAX_LENGTH}. */
    public static final int MAX_LENGTH = 256;

    TagValueString() {}

    /**
     * Constructs a {@code TagValueString} from the given string. The string must meet the following
     * requirements:
     *
     * <ol>
     *   <li>It cannot be longer than {@link #MAX_LENGTH}.
     *   <li>It can only contain printable ASCII characters.
     * </ol>
     *
     * @param value the tag value.
     * @throws IllegalArgumentException if the {@code String} is not valid.
     */
    public static TagValueString create(String value) {
      Preconditions.checkArgument(isValid(value));
      return new AutoValue_TagValue_TagValueString(value);
    }

    @Override
    public final <T> T match(
        Function<? super TagValueString, T> stringFunction,
        Function<? super TagValueLong, T> longFunction,
        Function<? super TagValueBoolean, T> booleanFunction,
        Function<? super TagValue, T> defaultFunction) {
      return stringFunction.apply(this);
    }

    /**
     * Returns the tag value as a {@code String}.
     *
     * @return the tag value as a {@code String}.
     */
    public abstract String asString();

    /**
     * Determines whether the given {@code String} is a valid tag value.
     *
     * @param value the tag value to be validated.
     * @return whether the value is valid.
     */
    private static boolean isValid(String value) {
      return value.length() <= MAX_LENGTH && StringUtil.isPrintableString(value);
    }
  }

  /** A tag value associated with a {@link TagKeyLong}. */
  @Immutable
  @AutoValue
  public abstract static class TagValueLong extends TagValue {

    TagValueLong() {}

    /**
     * Constructs a {@code TagValueLong} from the given {@code long}.
     *
     * @param value the tag value.
     */
    public static TagValueLong create(long value) {
      return new AutoValue_TagValue_TagValueLong(value);
    }

    @Override
    public final <T> T match(
        Function<? super TagValueString, T> stringFunction,
        Function<? super TagValueLong, T> longFunction,
        Function<? super TagValueBoolean, T> booleanFunction,
        Function<? super TagValue, T> defaultFunction) {
      return longFunction.apply(this);
    }

    /**
     * Returns the tag value as a {@code long}.
     *
     * @return the tag value as a {@code long}.
     */
    public abstract long asLong();
  }

  /** A tag value associated with a {@link TagKeyBoolean}. */
  @Immutable
  @AutoValue
  public abstract static class TagValueBoolean extends TagValue {
    private static final TagValueBoolean TRUE_VALUE = createInternal(true);
    private static final TagValueBoolean FALSE_VALUE = createInternal(false);

    TagValueBoolean() {}

    /**
     * Constructs a {@code TagValueBoolean} from the given {@code boolean}.
     *
     * @param value the tag value.
     */
    public static TagValueBoolean create(boolean value) {
      return value ? TRUE_VALUE : FALSE_VALUE;
    }

    private static TagValueBoolean createInternal(boolean value) {
      return new AutoValue_TagValue_TagValueBoolean(value);
    }

    @Override
    public final <T> T match(
        Function<? super TagValueString, T> stringFunction,
        Function<? super TagValueLong, T> longFunction,
        Function<? super TagValueBoolean, T> booleanFunction,
        Function<? super TagValue, T> defaultFunction) {
      return booleanFunction.apply(this);
    }

    /**
     * Returns the tag value as a {@code boolean}.
     *
     * @return the tag value as a {@code boolean}.
     */
    public abstract boolean asBoolean();
  }
}
