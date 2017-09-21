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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.implcore.tags.propagation.TagPropagationComponentImpl;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagPropagationComponent;
import java.util.concurrent.atomic.AtomicReference;

/** Base implementation of {@link TagsComponent}. */
public class TagsComponentImplBase extends TagsComponent {
  private final AtomicReference<TaggingState> state =
      new AtomicReference<TaggingState>(TaggingState.ENABLED);
  private final Tagger tagger = new TaggerImpl();
  private final TagPropagationComponent tagPropagationComponent = new TagPropagationComponentImpl();

  @Override
  public Tagger getTagger() {
    return tagger;
  }

  @Override
  public TagPropagationComponent getTagPropagationComponent() {
    return tagPropagationComponent;
  }

  @Override
  public TaggingState getState() {
    return state.get();
  }

  @Override
  public void setState(TaggingState newState) {
    state.set(checkNotNull(newState));
    // TODO(sebright): Avoid setting tags when tagging is disabled.
  }
}
