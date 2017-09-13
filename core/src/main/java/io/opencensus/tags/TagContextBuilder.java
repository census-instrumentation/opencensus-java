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
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import javax.annotation.concurrent.Immutable;

/** Builder for the {@link TagContext} class. */
// TODO(sebright): Decide what to do when 'put' is called with a key that has the same name as an
// existing key, but a different type.  We currently keep both keys.
public abstract class TagContextBuilder {
  private static final TagContextBuilder NOOP_TAG_CONTEXT_BUILDER = new NoopTagContextBuilder();

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   */
  public abstract TagContextBuilder put(TagKeyString key, TagValueString value);

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   */
  public abstract TagContextBuilder put(TagKeyLong key, TagValueLong value);

  /**
   * Adds the key/value pair regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the value to set for the given key.
   * @return this
   */
  public abstract TagContextBuilder put(TagKeyBoolean key, TagValueBoolean value);

  /**
   * Removes the key if it exists.
   *
   * @param key the {@code TagKey} which will be removed.
   * @return this
   */
  public abstract TagContextBuilder remove(TagKey key);

  /**
   * Creates a {@code TagContext} from this builder.
   *
   * @return a {@code TagContext} with the same tags as this builder.
   */
  public abstract TagContext build();

  /**
   * Enters the scope of code where the {@link TagContext} created from this builder is in the
   * current context and returns an object that represents that scope. The scope is exited when the
   * returned object is closed.
   *
   * @return an object that defines a scope where the {@code TagContext} created from this builder
   *     is set to the current context.
   */
  public final Scope buildScoped() {
    return CurrentTagContextUtils.withTagContext(build());
  }

  /**
   * Returns a {@code TagContextBuilder} that ignores all calls to {@link #put}.
   *
   * @return a {@code TagContextBuilder} that ignores all calls to {@link #put}.
   */
  static TagContextBuilder getNoopTagContextBuilder() {
    return NOOP_TAG_CONTEXT_BUILDER;
  }

  @Immutable
  private static final class NoopTagContextBuilder extends TagContextBuilder {

    @Override
    public TagContextBuilder put(TagKeyString key, TagValueString value) {
      return this;
    }

    @Override
    public TagContextBuilder put(TagKeyLong key, TagValueLong value) {
      return this;
    }

    @Override
    public TagContextBuilder put(TagKeyBoolean key, TagValueBoolean value) {
      return this;
    }

    @Override
    public TagContextBuilder remove(TagKey key) {
      return this;
    }

    @Override
    public TagContext build() {
      return TagContext.getNoopTagContext();
    }
  }
}
