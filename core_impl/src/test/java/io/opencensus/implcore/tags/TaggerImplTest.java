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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.tags.TagsTestUtil.tagContextToList;

import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.implcore.internal.NoopScope;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.unsafe.ContextUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TaggerImpl}. */
@RunWith(JUnit4.class)
public class TaggerImplTest {
  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final Tagger tagger = tagsComponent.getTagger();

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");

  private static final Tag TAG1 = Tag.create(K1, V1);
  private static final Tag TAG2 = Tag.create(K2, V2);
  private static final Tag TAG3 = Tag.create(K3, V3);

  @Test
  public void empty() {
    assertThat(tagContextToList(tagger.empty())).isEmpty();
    assertThat(tagger.empty()).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void empty_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagContextToList(tagger.empty())).isEmpty();
    assertThat(tagger.empty()).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void emptyBuilder() {
    TagContextBuilder builder = tagger.emptyBuilder();
    assertThat(builder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(builder.build())).isEmpty();
  }

  @Test
  public void emptyBuilder_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagger.emptyBuilder()).isSameAs(NoopTagContextBuilder.INSTANCE);
  }

  @Test
  public void emptyBuilder_TaggingReenabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagger.emptyBuilder()).isSameAs(NoopTagContextBuilder.INSTANCE);
    tagsComponent.setState(TaggingState.ENABLED);
    TagContextBuilder builder = tagger.emptyBuilder();
    assertThat(builder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(builder.put(K1, V1).build()))
        .containsExactly(Tag.create(K1, V1));
  }

  @Test
  public void currentBuilder() {
    TagContext tags = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContextBuilder result = getResultOfCurrentBuilder(tags);
    assertThat(result).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(result.build())).containsExactly(TAG1, TAG2, TAG3);
  }

  @Test
  public void currentBuilder_DefaultIsEmpty() {
    TagContextBuilder currentBuilder = tagger.currentBuilder();
    assertThat(currentBuilder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(currentBuilder.build())).isEmpty();
  }

  @Test
  public void currentBuilder_RemoveDuplicateTags() {
    Tag tag1 = Tag.create(K1, V1);
    Tag tag2 = Tag.create(K1, V2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContextBuilder result = getResultOfCurrentBuilder(tagContextWithDuplicateTags);
    assertThat(tagContextToList(result.build())).containsExactly(tag2);
  }

  @Test
  public void currentBuilder_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContextBuilder result = getResultOfCurrentBuilder(tagContextWithNullTag);
    assertThat(tagContextToList(result.build())).containsExactly(TAG1, TAG2);
  }

  @Test
  public void currentBuilder_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(getResultOfCurrentBuilder(new SimpleTagContext(TAG1)))
        .isSameAs(NoopTagContextBuilder.INSTANCE);
  }

  @Test
  public void currentBuilder_TaggingReenabled() {
    TagContext tags = new SimpleTagContext(TAG1);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(getResultOfCurrentBuilder(tags)).isSameAs(NoopTagContextBuilder.INSTANCE);
    tagsComponent.setState(TaggingState.ENABLED);
    TagContextBuilder builder = getResultOfCurrentBuilder(tags);
    assertThat(builder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(builder.build())).containsExactly(TAG1);
  }

  private TagContextBuilder getResultOfCurrentBuilder(TagContext tagsToSet) {
    Context orig = Context.current().withValue(ContextUtils.TAG_CONTEXT_KEY, tagsToSet).attach();
    try {
      return tagger.currentBuilder();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void toBuilder_ConvertUnknownTagContextToTagContextImpl() {
    TagContext unknownTagContext = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContext newTagContext = tagger.toBuilder(unknownTagContext).build();
    assertThat(tagContextToList(newTagContext)).containsExactly(TAG1, TAG2, TAG3);
    assertThat(newTagContext).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void toBuilder_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = Tag.create(K1, V1);
    Tag tag2 = Tag.create(K1, V2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext newTagContext = tagger.toBuilder(tagContextWithDuplicateTags).build();
    assertThat(tagContextToList(newTagContext)).containsExactly(tag2);
  }

  @Test
  public void toBuilder_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext newTagContext = tagger.toBuilder(tagContextWithNullTag).build();
    assertThat(tagContextToList(newTagContext)).containsExactly(TAG1, TAG2);
  }

  @Test
  public void toBuilder_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagger.toBuilder(new SimpleTagContext(TAG1)))
        .isSameAs(NoopTagContextBuilder.INSTANCE);
  }

  @Test
  public void toBuilder_TaggingReenabled() {
    TagContext tags = new SimpleTagContext(TAG1);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagger.toBuilder(tags)).isSameAs(NoopTagContextBuilder.INSTANCE);
    tagsComponent.setState(TaggingState.ENABLED);
    TagContextBuilder builder = tagger.toBuilder(tags);
    assertThat(builder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(tagContextToList(builder.build())).containsExactly(TAG1);
  }

  @Test
  public void getCurrentTagContext_DefaultIsEmptyTagContextImpl() {
    TagContext currentTagContext = tagger.getCurrentTagContext();
    assertThat(tagContextToList(currentTagContext)).isEmpty();
    assertThat(currentTagContext).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void getCurrentTagContext_ConvertUnknownTagContextToTagContextImpl() {
    TagContext unknownTagContext = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContext result = getResultOfGetCurrentTagContext(unknownTagContext);
    assertThat(result).isInstanceOf(TagContextImpl.class);
    assertThat(tagContextToList(result)).containsExactly(TAG1, TAG2, TAG3);
  }

  @Test
  public void getCurrentTagContext_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = Tag.create(K1, V1);
    Tag tag2 = Tag.create(K1, V2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext result = getResultOfGetCurrentTagContext(tagContextWithDuplicateTags);
    assertThat(tagContextToList(result)).containsExactly(tag2);
  }

  @Test
  public void getCurrentTagContext_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext result = getResultOfGetCurrentTagContext(tagContextWithNullTag);
    assertThat(tagContextToList(result)).containsExactly(TAG1, TAG2);
  }

  @Test
  public void getCurrentTagContext_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagContextToList(getResultOfGetCurrentTagContext(new SimpleTagContext(TAG1))))
        .isEmpty();
  }

  @Test
  public void getCurrentTagContext_TaggingReenabled() {
    TagContext tags = new SimpleTagContext(TAG1);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagContextToList(getResultOfGetCurrentTagContext(tags))).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
    assertThat(tagContextToList(getResultOfGetCurrentTagContext(tags))).containsExactly(TAG1);
  }

  private TagContext getResultOfGetCurrentTagContext(TagContext tagsToSet) {
    Context orig = Context.current().withValue(ContextUtils.TAG_CONTEXT_KEY, tagsToSet).attach();
    try {
      return tagger.getCurrentTagContext();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void withTagContext_ConvertUnknownTagContextToTagContextImpl() {
    TagContext unknownTagContext = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContext result = getResultOfWithTagContext(unknownTagContext);
    assertThat(result).isInstanceOf(TagContextImpl.class);
    assertThat(tagContextToList(result)).containsExactly(TAG1, TAG2, TAG3);
  }

  @Test
  public void withTagContext_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = Tag.create(K1, V1);
    Tag tag2 = Tag.create(K1, V2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext result = getResultOfWithTagContext(tagContextWithDuplicateTags);
    assertThat(tagContextToList(result)).containsExactly(tag2);
  }

  @Test
  public void withTagContext_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext result = getResultOfWithTagContext(tagContextWithNullTag);
    assertThat(tagContextToList(result)).containsExactly(TAG1, TAG2);
  }

  @Test
  public void withTagContext_ReturnsNoopScopeWhenTaggingIsDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagger.withTagContext(new SimpleTagContext(TAG1))).isSameAs(NoopScope.getInstance());
  }

  @Test
  public void withTagContext_TaggingDisabled() {
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagContextToList(getResultOfWithTagContext(new SimpleTagContext(TAG1)))).isEmpty();
  }

  @Test
  public void withTagContext_TaggingReenabled() {
    TagContext tags = new SimpleTagContext(TAG1);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(tagContextToList(getResultOfWithTagContext(tags))).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
    assertThat(tagContextToList(getResultOfWithTagContext(tags))).containsExactly(TAG1);
  }

  private TagContext getResultOfWithTagContext(TagContext tagsToSet) {
    Scope scope = tagger.withTagContext(tagsToSet);
    try {
      return ContextUtils.TAG_CONTEXT_KEY.get();
    } finally {
      scope.close();
    }
  }

  private static final class SimpleTagContext extends TagContext {
    private final List<Tag> tags;

    SimpleTagContext(Tag... tags) {
      this.tags = Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    protected Iterator<Tag> getIterator() {
      return tags.iterator();
    }
  }
}
