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

/**
 * Class that holds the implementation for {@link Tagger}.
 *
 * <p>All objects returned by methods on {@code TagsComponent} are cacheable.
 */
public abstract class TagsComponent {

  /** Returns the {@link Tagger} for this implementation. */
  public abstract Tagger getTagger();

  /** Returns the {@link TagPropagationComponent} for this implementation. */
  public abstract TagPropagationComponent getTagPropagationComponent();
}
