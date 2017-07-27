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

/**
 * Factory for new {@link TagContext}s and {@code TagContext}s based on the current context.
 *
 * <p>This class returns {@link TagContextBuilder builders} that can be used to create the
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
}
