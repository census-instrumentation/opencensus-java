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

import io.opencensus.common.Scope;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;

/**
 * Factory for new {@link TagContext}s and {@code TagContext}s based on the current context.
 *
 * <p>This class returns {@link Builder builders} that can be used to create the
 * implementation-dependent {@link TagContext}s.
 */
// TODO(sebright): Pick a more descriptive name for this class.
public abstract class TagContextFactory {

  /**
   * Returns an empty {@code TagContext}.
   *
   * @return an empty {@code TagContext}.
   */
  public abstract TagContext empty();

  /**
   * Returns the current {@code TagContext}.
   *
   * @return the current {@code TagContext}.
   */
  // TODO(sebright): Should we let the implementation override this method?
  public final TagContext getCurrentTagContext() {
    TagContext tags = CurrentTagContextUtils.getCurrentTagContext();
    return tags == null ? empty() : tags;
  }

  /**
   * Returns a new empty {@code Builder}.
   *
   * @return a new empty {@code Builder}.
   */
  public abstract Builder emptyBuilder();

  /**
   * Returns a builder based on this {@code TagContext}.
   *
   * @return a builder based on this {@code TagContext}.
   */
  public abstract Builder toBuilder(TagContext tags);

  /**
   * Returns a new {@code Builder} created from the current {@code TagContext}.
   *
   * @return a new {@code Builder} created from the current {@code TagContext}.
   */
  public final Builder currentBuilder() {
    return toBuilder(getCurrentTagContext());
  }

  /** Builder for the {@link TagContext} class. */
  public abstract static class Builder {

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if either argument is null.
     */
    public abstract Builder set(TagKeyString key, TagValueString value);

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if the key is null.
     */
    // TODO(sebright): Make this public once we support types other than String.
    abstract Builder set(TagKeyLong key, long value);

    /**
     * Adds the key/value pair regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the value to set for the given key.
     * @return this
     * @throws IllegalArgumentException if the key is null.
     */
    // TODO(sebright): Make this public once we support types other than String.
    abstract Builder set(TagKeyBoolean key, boolean value);

    /**
     * Removes the key if it exists.
     *
     * @param key the {@code TagKey} which will be cleared.
     * @return this
     */
    public abstract Builder clear(TagKey key);

    /**
     * Creates a {@code TagContext} from this builder.
     *
     * @return a {@code TagContext} with the same tags as this builder.
     */
    public abstract TagContext build();

    /**
     * Enters the scope of code where the {@link TagContextFactory} created from this builder is in
     * the current context and returns an object that represents that scope. The scope is exited
     * when the returned object is closed.
     *
     * @return an object that defines a scope where the {@code TagContext} created from this builder
     *     is set to the current context.
     */
    public final Scope buildScoped() {
      return CurrentTagContextUtils.withTagContext(build());
    }
  }
}
