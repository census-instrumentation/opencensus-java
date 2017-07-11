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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * A map from keys to values that can be used to label anything that is associated with a specific
 * operation.
 *
 * <p>For example, {@code TagContext}s can be used to label stats, log messages, or debugging
 * information.
 */
@Immutable
public final class TagContext {

  /** The empty {@code TagContext}. */
  public static final TagContext EMPTY = emptyBuilder().build();

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey, Object> tags;

  TagContext(Map<? extends TagKey, ?> tags) {
    this.tags = Collections.unmodifiableMap(new HashMap<TagKey, Object>(tags));
  }

  Map<TagKey, Object> getTags() {
    return tags;
  }

  /**
   * Returns a builder based on this {@code TagContext}.
   *
   * @return a builder based on this {@code TagContext}.
   */
  public Builder toBuilder() {
    return new Builder(getTags());
  }

  /**
   * Returns a new empty {@code Builder}.
   *
   * @return a new empty {@code Builder}.
   */
  public static Builder emptyBuilder() {
    return new Builder();
  }

  /**
   * Returns the current {@code TagContext}.
   *
   * @return the current {@code TagContext}.
   */
  public static TagContext getCurrentTags() {
    return CurrentTagsUtils.getCurrentTagContext();
  }

  /**
   * Returns a new {@code Builder} created from the current {@code TagContext}.
   *
   * @return a new {@code Builder} created from the current {@code TagContext}.
   */
  public static Builder currentBuilder() {
    return getCurrentTags().toBuilder();
  }

  /** Builder for the {@link TagContext} class. */
  public static final class Builder {
    private final Map<TagKey, Object> tags;

    private Builder(Map<TagKey, Object> tags) {
      this.tags = new HashMap<TagKey, Object>(tags);
    }

    Builder() {
      this.tags = new HashMap<TagKey, Object>();
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if either argument is null.
     */
    public Builder set(TagKeyString key, TagValueString value) {
      return setInternal(key, checkNotNull(value, "value"));
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if the key is null.
     */
    // TODO(sebright): Make this public once we support types other than String.
    Builder set(TagKeyLong key, long value) {
      return setInternal(key, value);
    }

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if the key is null.
     */
    // TODO(sebright): Make this public once we support types other than String.
    Builder set(TagKeyBoolean key, boolean value) {
      return setInternal(key, value);
    }

    private Builder setInternal(TagKey key, Object value) {
      tags.put(checkNotNull(key), value);
      return this;
    }

    /**
     * Removes the key if it exists.
     *
     * @param key the {@code TagKey} which will be cleared.
     * @return this
     */
    public Builder clear(TagKey key) {
      tags.remove(key);
      return this;
    }

    /**
     * Creates a {@code TagContext} from this builder.
     *
     * @return a {@code TagContext} with the same tags as this builder.
     */
    public TagContext build() {
      return new TagContext(tags);
    }

    /**
     * Enters the scope of code where the {@link TagContext} created from this builder is in the
     * current context and returns an object that represents that scope. The scope is exited when
     * the returned object is closed.
     *
     * @return an object that defines a scope where the {@code TagContext} created from this builder
     *     is set to the current context.
     */
    public NonThrowingCloseable buildScoped() {
      return CurrentTagsUtils.withTagContext(build());
    }
  }
}
