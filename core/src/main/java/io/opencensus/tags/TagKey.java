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
import io.opencensus.common.Function;
import io.opencensus.internal.StringUtil;
import javax.annotation.concurrent.Immutable;

/** A key to a value stored in a {@link TagContext}. */
@Immutable
public abstract class TagKey {
  /** The maximum length for a tag key name. */
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  TagKey() {}

  public abstract String getName();

  /**
   * Applies a function to the {@code TagKey} subclass. The function that is called depends on the
   * type of the tag key. This is similar to the visitor pattern. {@code match} also takes a
   * function to handle the default case, for backwards compatibility when tag types are added. For
   * example, this code creates a {@code Tag} from a {@code TagKey}. It handles new tag types by
   * logging an error and returning a {@code TagKeyString}.
   *
   * <pre>{@code
   * Tag tag =
   *     tagKey.match(
   *         stringKey -> TagString.create(stringKey, TagValueString.create("string value")),
   *         longKey -> TagLong.create(longKey, 100L),
   *         booleanKey -> TagBoolean.create(booleanKey, true),
   *         unknownKey -> {
   *           logger.log(Level.WARNING, "Unknown tag key type: " + unknownKey.toString());
   *           return TagString.create(
   *               TagKeyString.create(unknownKey.getName()),
   *               TagValueString.create("string value"));
   *         });
   * }</pre>
   *
   * <p>Without lambdas:
   *
   * <pre><code>
   *   Tag tag =
   *       tagKey.match(
   *           new Function&lt;TagKeyString, Tag&gt;() {
   *             &#064;Override
   *             public Tag apply(TagKeyString stringKey) {
   *               return TagString.create(stringKey, TagValueString.create("string value"));
   *             }
   *           },
   *           new Function&lt;TagKeyLong, Tag&gt;() {
   *             &#064;Override
   *             public Tag apply(TagKeyLong longKey) {
   *               return TagLong.create(longKey, 100L);
   *             }
   *           },
   *           new Function&lt;TagKeyBoolean, Tag&gt;() {
   *             &#064;Override
   *             public Tag apply(TagKeyBoolean booleanKey) {
   *               return TagBoolean.create(booleanKey, true);
   *             }
   *           },
   *           new Function&lt;TagKey, Tag&gt;() {
   *             &#064;Override
   *             public Tag apply(TagKey unknownKey) {
   *               logger.log(Level.WARNING, "Unknown tag key type: " + unknownKey.toString());
   *               return TagString.create(
   *                   TagKeyString.create(unknownKey.getName()),
   *                   TagValueString.create("string value"));
   *             }
   *           });
   * </code></pre>
   *
   * @param stringFunction the function to call when the {@code TagKey} is a {@code TagKeyString}.
   * @param longFunction the function to call when the {@code TagKey} is a {@code TagKeyLong}.
   * @param booleanFunction the function to call when the {@code TagKey} is a {@code TagKeyBoolean}.
   * @param defaultFunction the function to call when the tag key has a type other than {@code
   *     String}, {@code long}, or {@code boolean}.
   * @param <T> The result type of the function.
   * @return The result of calling the function that matches the tag key's type.
   */
  // TODO(sebright): Should we make this public in the first release?
  public abstract <T> T match(
      Function<? super TagKeyString, T> stringFunction,
      Function<? super TagKeyLong, T> longFunction,
      Function<? super TagKeyBoolean, T> booleanFunction,
      Function<? super TagKey, T> defaultFunction);

  /** A {@code TagKey} for values of type {@code String}. */
  @Immutable
  @AutoValue
  public abstract static class TagKeyString extends TagKey {

    /**
     * Constructs a {@code TagKeyString} with the given name.
     *
     * <p>The name must meet the following requirements:
     *
     * <ol>
     *   <li>It cannot be longer than {@link #MAX_LENGTH}.
     *   <li>It can only contain printable ASCII characters.
     * </ol>
     *
     * @param name the name of the key.
     * @return a {@code TagKeyString} with the given name.
     * @throws IllegalArgumentException if the name is not valid.
     */
    public static TagKeyString create(String name) {
      checkArgument(StringUtil.isValid(name));
      return new AutoValue_TagKey_TagKeyString(name);
    }

    @Override
    public <T> T match(
        Function<? super TagKeyString, T> stringFunction,
        Function<? super TagKeyLong, T> longFunction,
        Function<? super TagKeyBoolean, T> booleanFunction,
        Function<? super TagKey, T> defaultFunction) {
      return stringFunction.apply(this);
    }
  }

  /** A {@code TagKey} for values of type {@code long}. */
  @Immutable
  @AutoValue
  public abstract static class TagKeyLong extends TagKey {

    /**
     * Constructs a {@code TagKeyLong} with the given name.
     *
     * <p>The name must meet the following requirements:
     *
     * <ol>
     *   <li>It cannot be longer than {@link #MAX_LENGTH}.
     *   <li>It can only contain printable ASCII characters.
     * </ol>
     *
     * @param name the name of the key.
     * @return a {@code TagKeyLong} with the given name.
     * @throws IllegalArgumentException if the name is not valid.
     */
    // TODO(sebright): Make this public once we support types other than String.
    static TagKeyLong create(String name) {
      checkArgument(StringUtil.isValid(name));
      return new AutoValue_TagKey_TagKeyLong(name);
    }

    @Override
    public <T> T match(
        Function<? super TagKeyString, T> stringFunction,
        Function<? super TagKeyLong, T> longFunction,
        Function<? super TagKeyBoolean, T> booleanFunction,
        Function<? super TagKey, T> defaultFunction) {
      return longFunction.apply(this);
    }
  }

  /** A {@code TagKey} for values of type {@code boolean}. */
  @Immutable
  @AutoValue
  public abstract static class TagKeyBoolean extends TagKey {

    /**
     * Constructs a {@code TagKeyBoolean} with the given name.
     *
     * <p>The name must meet the following requirements:
     *
     * <ol>
     *   <li>It cannot be longer than {@link #MAX_LENGTH}.
     *   <li>It can only contain printable ASCII characters.
     * </ol>
     *
     * @param name the name of the key.
     * @return a {@code TagKeyBoolean} with the given name.
     * @throws IllegalArgumentException if the name is not valid.
     */
    // TODO(sebright): Make this public once we support types other than String.
    static TagKeyBoolean create(String name) {
      checkArgument(StringUtil.isValid(name));
      return new AutoValue_TagKey_TagKeyBoolean(name);
    }

    @Override
    public <T> T match(
        Function<? super TagKeyString, T> stringFunction,
        Function<? super TagKeyLong, T> longFunction,
        Function<? super TagKeyBoolean, T> booleanFunction,
        Function<? super TagKey, T> defaultFunction) {
      return booleanFunction.apply(this);
    }
  }
}
