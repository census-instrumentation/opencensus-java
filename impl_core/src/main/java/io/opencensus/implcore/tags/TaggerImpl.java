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

import io.opencensus.common.Scope;
import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.implcore.internal.NoopScope;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.Tagger;
import java.util.Iterator;

/** Implementation of {@link Tagger}. */
public final class TaggerImpl extends Tagger {
  // All methods in this class use TagMapImpl and TagMapBuilderImpl. For example,
  // withTagContext(...) always puts a TagMapImpl into scope, even if the argument is another
  // TagContext subclass.

  private final CurrentState state;

  TaggerImpl(CurrentState state) {
    this.state = state;
  }

  @Override
  public TagMapImpl empty() {
    return TagMapImpl.EMPTY;
  }

  @Override
  public TagMapImpl getCurrentTagContext() {
    return state.getInternal() == State.DISABLED
        ? TagMapImpl.EMPTY
        : toTagMapImpl(CurrentTagMapUtils.getCurrentTagMap());
  }

  @Override
  public TagContextBuilder emptyBuilder() {
    return state.getInternal() == State.DISABLED
        ? NoopTagMapBuilder.INSTANCE
        : new TagMapBuilderImpl();
  }

  @Override
  public TagContextBuilder currentBuilder() {
    return state.getInternal() == State.DISABLED
        ? NoopTagMapBuilder.INSTANCE
        : toBuilder(CurrentTagMapUtils.getCurrentTagMap());
  }

  @Override
  public TagContextBuilder toBuilder(TagContext tags) {
    return state.getInternal() == State.DISABLED
        ? NoopTagMapBuilder.INSTANCE
        : toTagMapBuilderImpl(tags);
  }

  @Override
  public Scope withTagContext(TagContext tags) {
    return state.getInternal() == State.DISABLED
        ? NoopScope.getInstance()
        : CurrentTagMapUtils.withTagMap(toTagMapImpl(tags));
  }

  private static TagMapImpl toTagMapImpl(TagContext tags) {
    if (tags instanceof TagMapImpl) {
      return (TagMapImpl) tags;
    } else {
      Iterator<Tag> i = InternalUtils.getTags(tags);
      if (!i.hasNext()) {
        return TagMapImpl.EMPTY;
      }
      TagMapBuilderImpl builder = new TagMapBuilderImpl();
      while (i.hasNext()) {
        Tag tag = i.next();
        if (tag != null) {
          TagContextUtils.addTagToBuilder(tag, builder);
        }
      }
      return builder.build();
    }
  }

  private static TagMapBuilderImpl toTagMapBuilderImpl(TagContext tags) {
    // Copy the tags more efficiently in the expected case, when the TagContext is a TagMapImpl.
    if (tags instanceof TagMapImpl) {
      return new TagMapBuilderImpl(((TagMapImpl) tags).getTags());
    } else {
      TagMapBuilderImpl builder = new TagMapBuilderImpl();
      for (Iterator<Tag> i = InternalUtils.getTags(tags); i.hasNext(); ) {
        Tag tag = i.next();
        if (tag != null) {
          TagContextUtils.addTagToBuilder(tag, builder);
        }
      }
      return builder;
    }
  }
}
