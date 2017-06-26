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

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** {@link TagKey} paired with an optional value. */
@Immutable
public abstract class Tag {

  /**
   * Returns the tag's key.
   *
   * @return the tag's key.
   */
  public abstract TagKey getKey();

  /**
   * Applies a function to the tag's key and value. The function that is called depends on the type
   * of the tag. This is similar to the visitor pattern.
   *
   * @param stringFunction the function to call when the tag has a {@code String} value.
   * @param longFunction the function to call when the tag has a {@code Long} value.
   * @param booleanFunction the function to call when the tag has a {@code Boolean} value.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag's type.
   */
  public <T> T match(
      Function<? super TagString, T> stringFunction,
      Function<? super TagLong, T> longFunction,
      Function<? super TagBoolean, T> booleanFunction) {
    return matchWithDefault(
        stringFunction, longFunction, booleanFunction, TagUtils.<T>throwAssertionError());
  }

  /**
   * Applies a function to the tag's key and value. This method is like {@link #match(Function,
   * Function, Function)}, except that it has a default case, for backwards compatibility when tag
   * types are added.
   *
   * @param stringFunction the function to call when the tag has a {@code String} value.
   * @param longFunction the function to call when the tag has a {@code Long} value.
   * @param booleanFunction the function to call when the tag has a {@code Boolean} value.
   * @param defaultFunction the function to call when the tag has a value other than {@code String},
   *     {@code Long}, or {@code Boolean}.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag's type.
   */
  // TODO(sebright): How should we deal with the possibility of adding more tag types in the future?
  //                 Will this method work? What type of parameter should we use for the default
  //                 case? Should we remove the non-backwards compatible "match" method?
  public abstract <T> T matchWithDefault(
      Function<? super TagString, T> stringFunction,
      Function<? super TagLong, T> longFunction,
      Function<? super TagBoolean, T> booleanFunction,
      Function<? super Tag, T> defaultFunction);

  /**
   * Creates a {@code Tag} from the given {@code String} key and value.
   *
   * @param key the tag key.
   * @param value the tag value, or {@code null} if the key is not paired with a value.
   * @return a {@code Tag} with the given key and value.
   */
  public static Tag createStringTag(TagKeyString key, @Nullable TagValueString value) {
    return TagString.create(key, value);
  }

  /**
   * Creates a {@code Tag} from the given {@code Long} key and value.
   *
   * @param key the tag key.
   * @param value the tag value, or {@code null} if the key is not paired with a value.
   * @return a {@code Tag} with the given key and value.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static Tag createLongTag(TagKeyLong key, @Nullable Long value) {
    return TagLong.create(key, value);
  }

  /**
   * Creates a {@code Tag} from the given {@code Boolean} key and value.
   *
   * @param key the tag key.
   * @param value the tag value, or {@code null} if the key is not paired with a value.
   * @return a {@code Tag} with the given key and value.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static Tag createBooleanTag(TagKeyBoolean key, @Nullable Boolean value) {
    return TagBoolean.create(key, value);
  }

  /** A tag with a {@code String} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagString extends Tag {
    TagString() {}

    static TagString create(TagKeyString key, @Nullable TagValueString value) {
      return new AutoValue_Tag_TagString(key, value);
    }

    @Override
    public abstract TagKeyString getKey();

    /**
     * Returns the associated tag value, or {@code null} if the key is not paired with a value.
     *
     * @return the associated tag value, or {@code null} if the key is not paired with a value.
     */
    @Nullable
    public abstract TagValueString getValue();

    @Override
    public <T> T matchWithDefault(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return stringFunction.apply(this);
    }
  }

  /** A tag with a {@code Long} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagLong extends Tag {
    TagLong() {}

    static TagLong create(TagKeyLong key, @Nullable Long value) {
      return new AutoValue_Tag_TagLong(key, value);
    }

    @Override
    public abstract TagKeyLong getKey();

    /**
     * Returns the associated tag value, or {@code null} if the key is not paired with a value.
     *
     * @return the associated tag value, or {@code null} if the key is not paired with a value.
     */
    @Nullable
    public abstract Long getValue();

    @Override
    public <T> T matchWithDefault(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return longFunction.apply(this);
    }
  }

  /** A tag with a {@code Boolean} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagBoolean extends Tag {
    TagBoolean() {}

    static TagBoolean create(TagKeyBoolean key, @Nullable Boolean value) {
      return new AutoValue_Tag_TagBoolean(key, value);
    }

    @Override
    public abstract TagKeyBoolean getKey();

    /**
     * Returns the associated tag value, or {@code null} if the key is not paired with a value.
     *
     * @return the associated tag value, or {@code null} if the key is not paired with a value.
     */
    @Nullable
    public abstract Boolean getValue();

    @Override
    public <T> T matchWithDefault(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return booleanFunction.apply(this);
    }
  }
}
