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

import javax.annotation.concurrent.Immutable;

/**
 * Factory for new {@link TagContext}s and {@code TagContext}s based on the current context.
 */
// TODO(sebright): Pick a more descriptive name for this class.
public abstract class TagContextFactory {
  private static final TagContextFactory NOOP_TAG_CONTEXT_FACTORY = new NoopTagContextFactory();

  // TODO(sebright): Add TagContext related methods to this class.

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
  }
}
