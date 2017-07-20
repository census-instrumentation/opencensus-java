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
import javax.annotation.concurrent.Immutable;

/**
 * Factory for new {@link TagContext}s and {@code TagContext}s based on the current context.
 *
 * <p>This class returns {@link TagContextBuilder builders} that can be used to create the
 * implementation-dependent {@link TagContext}s.
 */
// TODO(sebright): Pick a more descriptive name for this class.
public abstract class TagContextFactory {
  private static final TagContextFactory NOOP_TAG_CONTEXT_FACTORY = new NoopTagContextFactory();

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
  public abstract TagContextBuilder emptyBuilder();

  /**
   * Returns a builder based on this {@code TagContext}.
   *
   * @return a builder based on this {@code TagContext}.
   */
  public abstract TagContextBuilder toBuilder(TagContext tags);

  /**
   * Returns a new builder created from the current {@code TagContext}.
   *
   * @return a new builder created from the current {@code TagContext}.
   */
  public final TagContextBuilder currentBuilder() {
    return toBuilder(getCurrentTagContext());
  }

  /**
   * Enters the scope of code where the given {@code TagContext} is in the current context and
   * returns an object that represents that scope. The scope is exited when the returned object is
   * closed.
   *
   * @param tags the {@code TagContext} to be set to the current context.
   * @return an object that defines a scope where the given {@code TagContext} is set to the current
   *     context.
   */
  public final Scope withTagContext(TagContext tags) {
    return CurrentTagContextUtils.withTagContext(tags);
  }

  /**
   * Returns a {@code TagContextFactory} that only produces {@link TagContext}s with no tags.
   *
   * @return a {@code TagContextFactory} that only produces {@code TagContext}s with no tags.
   */
  static TagContextFactory getNoopTagContextFactory() {
    return NOOP_TAG_CONTEXT_FACTORY;
  }

  @Immutable
  private static final class NoopTagContextFactory extends TagContextFactory {

    @Override
    public TagContext empty() {
      return TagContext.getNoopTagContext();
    }

    @Override
    public TagContextBuilder emptyBuilder() {
      return TagContextBuilder.getNoopTagContextBuilder();
    }

    @Override
    public TagContextBuilder toBuilder(TagContext tags) {
      return TagContextBuilder.getNoopTagContextBuilder();
    }
  }
}
