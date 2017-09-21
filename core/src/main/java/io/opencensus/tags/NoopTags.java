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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Scope;
import io.opencensus.internal.NoopScope;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagPropagationComponent;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;

/** No-op implementations of tagging classes. */
final class NoopTags {

  private NoopTags() {}

  /**
   * Returns a {@code TagsComponent} that has a no-op implementation for {@link Tagger}.
   *
   * @return a {@code TagsComponent} that has a no-op implementation for {@code Tagger}.
   */
  static TagsComponent getNoopTagsComponent() {
    return NoopTagsComponent.INSTANCE;
  }

  /**
   * Returns a {@code Tagger} that only produces {@link TagContext}s with no tags.
   *
   * @return a {@code Tagger} that only produces {@code TagContext}s with no tags.
   */
  static Tagger getNoopTagger() {
    return NoopTagger.INSTANCE;
  }

  /**
   * Returns a {@code TagContextBuilder} that ignores all calls to {@link TagContextBuilder#put}.
   *
   * @return a {@code TagContextBuilder} that ignores all calls to {@link TagContextBuilder#put}.
   */
  static TagContextBuilder getNoopTagContextBuilder() {
    return NoopTagContextBuilder.INSTANCE;
  }

  /**
   * Returns a {@code TagContext} that does not contain any tags.
   *
   * @return a {@code TagContext} that does not contain any tags.
   */
  static TagContext getNoopTagContext() {
    return NoopTagContext.INSTANCE;
  }

  /** Returns a {@code TagPropagationComponent} that contains no-op serializers. */
  static TagPropagationComponent getNoopTagPropagationComponent() {
    return NoopTagPropagationComponent.INSTANCE;
  }

  /**
   * Returns a {@code TagContextBinarySerializer} that serializes all {@code TagContext}s to zero
   * bytes and deserializes all inputs to empty {@code TagContext}s.
   */
  static TagContextBinarySerializer getNoopTagContextBinarySerializer() {
    return NoopTagContextBinarySerializer.INSTANCE;
  }

  @Immutable
  private static final class NoopTagsComponent extends TagsComponent {
    static final TagsComponent INSTANCE = new NoopTagsComponent();

    @Override
    public Tagger getTagger() {
      return getNoopTagger();
    }

    @Override
    public TagPropagationComponent getTagPropagationComponent() {
      return getNoopTagPropagationComponent();
    }
  }

  @Immutable
  private static final class NoopTagger extends Tagger {
    static final Tagger INSTANCE = new NoopTagger();

    @Override
    public TagContext empty() {
      return getNoopTagContext();
    }

    @Override
    public TagContext getCurrentTagContext() {
      return getNoopTagContext();
    }

    @Override
    public TagContextBuilder emptyBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public TagContextBuilder toBuilder(TagContext tags) {
      checkNotNull(tags, "tags");
      return getNoopTagContextBuilder();
    }

    @Override
    public TagContextBuilder currentBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public Scope withTagContext(TagContext tags) {
      checkNotNull(tags, "tags");
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagContextBuilder extends TagContextBuilder {
    static final TagContextBuilder INSTANCE = new NoopTagContextBuilder();

    @Override
    public TagContextBuilder put(TagKeyString key, TagValueString value) {
      checkNotNull(key, "key");
      checkNotNull(value, "value");
      return this;
    }

    @Override
    public TagContextBuilder put(TagKeyLong key, TagValueLong value) {
      checkNotNull(key, "key");
      checkNotNull(value, "value");
      return this;
    }

    @Override
    public TagContextBuilder put(TagKeyBoolean key, TagValueBoolean value) {
      checkNotNull(key, "key");
      checkNotNull(value, "value");
      return this;
    }

    @Override
    public TagContextBuilder remove(TagKey key) {
      checkNotNull(key, "key");
      return this;
    }

    @Override
    public TagContext build() {
      return getNoopTagContext();
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagContext extends TagContext {
    static final TagContext INSTANCE = new NoopTagContext();

    // TODO(sebright): Is there any way to let the user know that their tags were ignored?
    @Override
    public Iterator<Tag> iterator() {
      return Collections.<Tag>emptySet().iterator();
    }
  }

  @Immutable
  private static final class NoopTagPropagationComponent extends TagPropagationComponent {
    static final TagPropagationComponent INSTANCE = new NoopTagPropagationComponent();

    @Override
    public TagContextBinarySerializer getBinarySerializer() {
      return getNoopTagContextBinarySerializer();
    }
  }

  @Immutable
  private static final class NoopTagContextBinarySerializer extends TagContextBinarySerializer {
    static final TagContextBinarySerializer INSTANCE = new NoopTagContextBinarySerializer();
    static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] toByteArray(TagContext tags) {
      checkNotNull(tags, "tags");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public TagContext fromByteArray(byte[] bytes) {
      checkNotNull(bytes, "bytes");
      return getNoopTagContext();
    }
  }
}
