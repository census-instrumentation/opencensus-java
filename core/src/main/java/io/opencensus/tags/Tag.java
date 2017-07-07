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

/** {@link TagKey} paired with a value. */
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
   * of the tag. This is similar to the visitor pattern. {@code match} also takes a function to
   * handle the default case, for backwards compatibility when tag types are added. For example,
   * this code serializes a {@code Tag} and tries to handle new tag types by calling {@code
   * toString()}.
   *
   * <pre>{@code
   * byte[] serializedValue =
   *     tag.match(
   *         stringTag -> serializeString(stringTag.getValue().asString()),
   *         longTag -> serializeLong(longTag.getValue()),
   *         booleanTag -> serializeBoolean(booleanTag.getValue()),
   *         unknownTag -> serializeString(unknownTag.toString()));
   * }</pre>
   *
   * @param stringFunction the function to call when the tag has a {@code String} value.
   * @param longFunction the function to call when the tag has a {@code long} value.
   * @param booleanFunction the function to call when the tag has a {@code boolean} value.
   * @param defaultFunction the function to call when the tag has a value other than {@code String},
   *     {@code long}, or {@code boolean}.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag's type.
   */
  public abstract <T> T match(
      Function<? super TagString, T> stringFunction,
      Function<? super TagLong, T> longFunction,
      Function<? super TagBoolean, T> booleanFunction,
      Function<? super Tag, T> defaultFunction);

  /** A tag with a {@code String} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagString extends Tag {
    TagString() {}

    /**
     * Creates a {@code TagString} from the given {@code String} key and value.
     *
     * @param key the tag key.
     * @param value the tag value.
     * @return a {@code TagString} with the given key and value.
     */
    public static Tag create(TagKeyString key, TagValueString value) {
      return new AutoValue_Tag_TagString(key, value);
    }

    @Override
    public abstract TagKeyString getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract TagValueString getValue();

    @Override
    public <T> T match(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return stringFunction.apply(this);
    }
  }

  /** A tag with a {@code long} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagLong extends Tag {
    TagLong() {}

    /**
     * Creates a {@code TagLong} from the given {@code long} key and value.
     *
     * @param key the tag key.
     * @param value the tag value.
     * @return a {@code TagLong} with the given key and value.
     */
    // TODO(sebright): Make this public once we support types other than String.
    static Tag create(TagKeyLong key, long value) {
      return new AutoValue_Tag_TagLong(key, value);
    }

    @Override
    public abstract TagKeyLong getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract long getValue();

    @Override
    public <T> T match(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return longFunction.apply(this);
    }
  }

  /** A tag with a {@code boolean} key and value. */
  @Immutable
  @AutoValue
  public abstract static class TagBoolean extends Tag {
    TagBoolean() {}

    /**
     * Creates a {@code TagBoolean} from the given {@code boolean} key and value.
     *
     * @param key the tag key.
     * @param value the tag value.
     * @return a {@code TagBoolean} with the given key and value.
     */
    // TODO(sebright): Make this public once we support types other than String.
    static Tag create(TagKeyBoolean key, boolean value) {
      return new AutoValue_Tag_TagBoolean(key, value);
    }

    @Override
    public abstract TagKeyBoolean getKey();

    /**
     * Returns the associated tag value.
     *
     * @return the associated tag value.
     */
    public abstract boolean getValue();

    @Override
    public <T> T match(
        Function<? super TagString, T> stringFunction,
        Function<? super TagLong, T> longFunction,
        Function<? super TagBoolean, T> booleanFunction,
        Function<? super Tag, T> defaultFunction) {
      return booleanFunction.apply(this);
    }
  }
}
