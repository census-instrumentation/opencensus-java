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
import io.opencensus.internal.StringUtils;
import io.opencensus.internal.Utils;
import javax.annotation.concurrent.Immutable;

/**
 * A key to a value stored in a {@link TagContext}.
 *
 * <p>Each {@code TagKey} has a {@code String} name. Names have a maximum length of {@link
 * #MAX_LENGTH} and contain only printable ASCII characters.
 *
 * <p>{@code TagKey}s are designed to be used as constants. Declaring each key as a constant
 * prevents key names from being validated multiple times.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
public abstract class TagKey {
  /**
   * The maximum length for a tag key name. The value is {@value #MAX_LENGTH}.
   *
   * @since 0.8
   */
  public static final int MAX_LENGTH = 255;

  TagKey() {}

  /**
   * Constructs a {@code TagKey} with the given name.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * <p>Equivalent to {@code create(name, TagScope.LOCAL)}.
   *
   * @param name the name of the key.
   * @return a {@code TagKey} with the given name.
   * @throws IllegalArgumentException if the name is not valid.
   * @since 0.8
   */
  public static TagKey create(String name) {
    return create(name, TagScope.LOCAL);
  }

  /**
   * Constructs a {@code TagKey} with the given name and {@link TagScope}.
   *
   * <p>The name must meet the following requirements:
   *
   * <ol>
   *   <li>It cannot be longer than {@link #MAX_LENGTH}.
   *   <li>It can only contain printable ASCII characters.
   * </ol>
   *
   * @param name the name of the key.
   * @param tagScope the tag scope of the key.
   * @return a {@code TagKey} with the given name and tag scope.
   * @throws IllegalArgumentException if the name is not valid.
   * @since 0.20
   */
  public static TagKey create(String name, TagScope tagScope) {
    Utils.checkArgument(isValid(name), "Invalid TagKey name: %s", name);
    Utils.checkNotNull(tagScope, "tagScope");
    return new AutoValue_TagKey(name, tagScope);
  }

  /**
   * Returns the name of the key.
   *
   * @return the name of the key.
   * @since 0.8
   */
  public abstract String getName();

  /**
   * Returns the {@link TagScope} of this {@link TagContext}.
   *
   * @return the {@code TagScope} of this {@code TagContext}.
   * @since 0.20
   */
  public abstract TagScope getTagScope();

  /**
   * Determines whether the given {@code String} is a valid tag key.
   *
   * @param name the tag key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValid(String name) {
    return !name.isEmpty() && name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }

  /**
   * {@link TagScope} is used to determine the scope of a Tag.
   *
   * <p>The values for the TagScope are {@link TagScope#LOCAL} or {@link TagScope#REQUEST}.
   *
   * @since 0.20
   */
  public enum TagScope {

    /**
     * {@link TagKey}s with {@code LOCAL} scope are used within the process it created. Such tags
     * are not propagated across process boundaries. Even if the process is reentrant the tag MUST
     * be excluded from propagation when the call leaves the process.
     *
     * @since 0.20
     */
    LOCAL,

    /**
     * If a {@link TagKey} is created with the {@code REQUEST} scope then it is propagated across
     * process boundaries subject to outgoing and incoming (on remote side) filter criteria. See
     * TagPropagationFilter in [Tag Propagation](#Tag Propagation). Typically {@code REQUEST} tags
     * represents a request, processing of which may span multiple entities.
     *
     * @since 0.20
     */
    REQUEST
  }
}
