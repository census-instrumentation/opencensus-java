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

/** State of the {@link TagsComponent}. */
public enum TaggingState {
  // TODO(sebright): Should we add a state that propagates the tags, but doesn't allow
  // modifications?

  /**
   * State that fully enables tagging.
   *
   * <p>The {@link TagsComponent} can add tags to {@link TagContext}s, propagate {@code TagContext}s
   * in the current context, and serialize {@code TagContext}s.
   */
  ENABLED,

  /**
   * State that disables tagging.
   *
   * <p>The {@link TagsComponent} may not add tags to {@link TagContext}s, propagate {@code
   * TagContext}s in the current context, or serialize {@code TagContext}s.
   */
  // TODO(sebright): Document how this interacts with stats collection.
  DISABLED
}
