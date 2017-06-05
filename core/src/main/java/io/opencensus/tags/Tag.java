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
import javax.annotation.concurrent.Immutable;

/**
 * Paired {@link TagKey} and value.
 */
@Immutable
@AutoValue
public abstract class Tag {

  // The tag is either a TagString, TagLong, or TagBoolean.
  abstract Object getTag();

  /**
   * Returns the tag's key.
   *
   * @return the tag's key.
   */
  public TagKey getKey() {
    return match(GET_TAG_KEY_STRING, GET_TAG_KEY_LONG, GET_TAG_KEY_BOOLEAN);
  }

  private static final Function<TagString, TagKey> GET_TAG_KEY_STRING =
      new Function<TagString, TagKey>() {
        @Override
        public TagKey apply(TagString tag) {
          return tag.getKey();
        }
      };

  private static final Function<TagLong, TagKey> GET_TAG_KEY_LONG =
      new Function<TagLong, TagKey>() {
        @Override
        public TagKey apply(TagLong tag) {
          return tag.getKey();
        }
      };

  private static final Function<TagBoolean, TagKey> GET_TAG_KEY_BOOLEAN =
      new Function<TagBoolean, TagKey>() {
        @Override
        public TagKey apply(TagBoolean tag) {
          return tag.getKey();
        }
      };

  /**
   * Applies a function to the tag's key and value. The function that is called depends on the type
   * of the tag. This is similar to the visitor pattern.
   *
   * @param stringFunction the function to call when the tag has a {@code String} value.
   * @param longFunction the function to call when the tag has a {@code long} value.
   * @param booleanFunction the function to call when the tag has a {@code boolean} value.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag's type.
   */
  public <T> T match(
      Function<? super TagString, T> stringFunction,
      Function<? super TagLong, T> longFunction,
      Function<? super TagBoolean, T> booleanFunction) {
    return matchWithDefault(
        stringFunction, longFunction, booleanFunction, Tag.<T>throwAssertionError());
  }

  /**
   * Applies a function to the tag's key and value. This method is like {@link #match(Function,
   * Function, Function)}, except that it has a default case, for backwards compatibility when tag
   * types are added.
   *
   * @param stringFunction the function to call when the tag has a {@code String} value.
   * @param longFunction the function to call when the tag has a {@code long} value.
   * @param booleanFunction the function to call when the tag has a {@code boolean} value.
   * @param defaultFunction the function to call when the tag has a value other than {@code String},
   *     {@code long}, or {@code boolean}.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag's type.
   */
  // TODO(sebright): How should we deal with the possibility of adding more tag types in the future?
  //                 Will this method work? What type of parameter should we use for the default
  //                 case? Should we remove the non-backwards compatible "match" method?
  public <T> T matchWithDefault(
      Function<? super TagString, T> stringFunction,
      Function<? super TagLong, T> longFunction,
      Function<? super TagBoolean, T> booleanFunction,
      Function<Object, T> defaultFunction) {
    Object tag = getTag();
    if (tag instanceof TagString) {
      return stringFunction.apply((TagString) tag);
    } else if (tag instanceof TagLong) {
      return longFunction.apply((TagLong) tag);
    } else if (tag instanceof TagBoolean) {
      return booleanFunction.apply((TagBoolean) tag);
    } else {
      return defaultFunction.apply(tag);
    }
  }

  private static <T> Function<Object, T> throwAssertionError() {
    @SuppressWarnings("unchecked")
    Function<Object, T> function = (Function<Object, T>) THROW_ASSERTION_ERROR;
    return function;
  }

  private static final Function<Object, Void> THROW_ASSERTION_ERROR =
      new Function<Object, Void>() {
        @Override
        public Void apply(Object tag) {
          throw new AssertionError();
        }
      };

  /**
   * Creates a {@code Tag} from the given {@code String} key and value.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @return a {@code Tag} with the given key and value.
   */
  public static Tag createStringTag(TagKeyString key, TagValueString value) {
    return createInternal(TagString.create(key, value));
  }

  /**
   * Creates a {@code Tag} from the given {@code long} key and value.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @return a {@code Tag} with the given key and value.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static Tag createLongTag(TagKeyLong key, long value) {
    return createInternal(TagLong.create(key, value));
  }

  /**
   * Creates a {@code Tag} from the given {@code boolean} key and value.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @return a {@code Tag} with the given key and value.
   */
  // TODO(sebright): Make this public once we support types other than String.
  static Tag createBooleanTag(TagKeyBoolean key, boolean value) {
    return createInternal(TagBoolean.create(key, value));
  }

  private static Tag createInternal(Object tag) {
    return new AutoValue_Tag(tag);
  }

  /**
   * A tag with a {@code String} key and value.
   */
  @Immutable
  @AutoValue
  public abstract static class TagString {
    static TagString create(TagKeyString key, TagValueString value) {
      return new AutoValue_Tag_TagString(key, value);
    }

    /**
     * Returns the associated tag key.
     *
     * @return the associated tag key.
     */
    public abstract TagKeyString getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract TagValueString getValue();
  }

  /**
   * A tag with a {@code long} key and value.
   */
  @Immutable
  @AutoValue
  public abstract static class TagLong {
    static TagLong create(TagKeyLong key, long value) {
      return new AutoValue_Tag_TagLong(key, value);
    }

    /**
     * Returns the associated tag key.
     *
     * @return the associated tag key.
     */
    public abstract TagKeyLong getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract long getValue();
  }

  /**
   * A tag with a {@code boolean} key and value.
   */
  @Immutable
  @AutoValue
  public abstract static class TagBoolean {
    static TagBoolean create(TagKeyBoolean key, boolean value) {
      return new AutoValue_Tag_TagBoolean(key, value);
    }

    /**
     * Returns the associated tag key.
     *
     * @return the associated tag key.
     */
    public abstract TagKeyBoolean getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract boolean getValue();
  }
}
