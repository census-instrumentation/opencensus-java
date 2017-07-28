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

import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import javax.annotation.concurrent.Immutable;

/** Builder for the {@link TagContext} class. */
public abstract class TagContextBuilder {
  private static final TagContextBuilder NOOP_TAG_CONTEXT_BUILDER = new NoopTagContextBuilder();

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   * @throws IllegalArgumentException if either argument is null.
   */
  public abstract TagContextBuilder set(TagKeyString key, TagValueString value);

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   * @throws IllegalArgumentException if the key is null.
   */
  // TODO(sebright): Make this public once we support types other than String.
  protected abstract TagContextBuilder set(TagKeyLong key, long value);

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   * @throws IllegalArgumentException if the key is null.
   */
  // TODO(sebright): Make this public once we support types other than String.
  protected abstract TagContextBuilder set(TagKeyBoolean key, boolean value);

  /**
   * Removes the key if it exists.
   *
   * @param key the {@code TagKey} which will be cleared.
   * @return this
   */
  public abstract TagContextBuilder clear(TagKey key);

  /**
   * Creates a {@code TagContext} from this builder.
   *
   * @return a {@code TagContext} with the same tags as this builder.
   */
  public abstract TagContext build();

  /**
   * Returns a {@code TagContextBuilder} that ignores all calls to {@link #set}.
   *
   * @return a {@code TagContextBuilder} that ignores all calls to {@link #set}.
   */
  static TagContextBuilder getNoopTagContextBuilder() {
    return NOOP_TAG_CONTEXT_BUILDER;
  }

  @Immutable
  private static final class NoopTagContextBuilder extends TagContextBuilder {

    @Override
    public TagContextBuilder set(TagKeyString key, TagValueString value) {
      return this;
    }

    @Override
    protected TagContextBuilder set(TagKeyLong key, long value) {
      return this;
    }

    @Override
    protected TagContextBuilder set(TagKeyBoolean key, boolean value) {
      return this;
    }

    @Override
    public TagContextBuilder clear(TagKey key) {
      return this;
    }

    @Override
    public TagContext build() {
      return TagContext.getNoopTagContext();
    }
  }
}
