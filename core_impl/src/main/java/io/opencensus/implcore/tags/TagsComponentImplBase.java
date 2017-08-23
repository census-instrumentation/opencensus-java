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

package io.opencensus.implcore.tags;

import io.opencensus.tags.TagContexts;
import io.opencensus.tags.TagPropagationComponent;
import io.opencensus.tags.TagsComponent;

/** Base implementation of {@link TagsComponent}. */
public abstract class TagsComponentImplBase extends TagsComponent {
  private final TagContexts tagContexts = new TagContextsImpl();
  private final TagPropagationComponent tagPropagationComponent = new TagPropagationComponentImpl();

  @Override
  public TagContexts getTagContexts() {
    return tagContexts;
  }

  @Override
  public TagPropagationComponent getTagPropagationComponent() {
    return tagPropagationComponent;
  }
}
