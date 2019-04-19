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

import io.opencensus.common.Scope;
import io.opencensus.tags.TagMetadata.TagTtl;

/**
 * Builder for the {@link TagContext} class.
 *
 * @since 0.8
 */
public abstract class TagContextBuilder {

  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagTtl.NO_PROPAGATION);

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * <p>For backwards-compatibility this method still produces propagating {@link Tag}s.
   *
   * <p>Equivalent to calling {@code put(key, value,
   * TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))}.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the {@code TagValue} to set for the given key.
   * @return this
   * @since 0.8
   * @deprecated in favor of {@link #put(TagKey, TagValue, TagMetadata)}, or {@link
   *     #putLocal(TagKey, TagValue)} if you only want in-process tags.
   */
  @Deprecated
  public abstract TagContextBuilder put(TagKey key, TagValue value);

  /**
   * Adds the key/value pair and metadata regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the {@code TagValue} to set for the given key.
   * @param tagMetadata the {@code TagMetadata} associated with this {@link Tag}.
   * @return this
   * @since 0.20
   */
  public TagContextBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
    @SuppressWarnings("deprecation")
    TagContextBuilder builder = put(key, value);
    return builder;
  }

  /**
   * Adds a non-propagating tag to this {@code TagContextBuilder}.
   *
   * <p>This is equivalent to calling {@code put(key, value,
   * TagMetadata.create(TagTtl.NO_PROPAGATION))}.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the {@code TagValue} to set for the given key.
   * @return this
   * @since 0.21
   */
  public final TagContextBuilder putLocal(TagKey key, TagValue value) {
    return put(key, value, METADATA_NO_PROPAGATION);
  }

  /**
   * Removes the key if it exists.
   *
   * @param key the {@code TagKey} which will be removed.
   * @return this
   * @since 0.8
   */
  public abstract TagContextBuilder remove(TagKey key);

  /**
   * Creates a {@code TagContext} from this builder.
   *
   * @return a {@code TagContext} with the same tags as this builder.
   * @since 0.8
   */
  public abstract TagContext build();

  /**
   * Enters the scope of code where the {@link TagContext} created from this builder is in the
   * current context and returns an object that represents that scope. The scope is exited when the
   * returned object is closed.
   *
   * @return an object that defines a scope where the {@code TagContext} created from this builder
   *     is set to the current context.
   * @since 0.8
   */
  public abstract Scope buildScoped();
}
