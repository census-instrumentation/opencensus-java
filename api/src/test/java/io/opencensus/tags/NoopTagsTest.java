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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.internal.NoopScope;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import io.opencensus.tags.propagation.TagContextSerializationException;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopTags}. */
@RunWith(JUnit4.class)
public final class NoopTagsTest {
  private static final TagKey KEY = TagKey.create("key");
  private static final TagValue VALUE = TagValue.create("value");

  private static final TagContext TAG_CONTEXT =
      new TagContext() {

        @Override
        protected Iterator<Tag> getIterator() {
          return Arrays.<Tag>asList(Tag.create(KEY, VALUE)).iterator();
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopTagsComponent() {
    assertThat(NoopTags.newNoopTagsComponent().getTagger()).isSameAs(NoopTags.getNoopTagger());
    assertThat(NoopTags.newNoopTagsComponent().getTagPropagationComponent())
        .isSameAs(NoopTags.getNoopTagPropagationComponent());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void noopTagsComponent_SetState_DisallowsNull() {
    TagsComponent noopTagsComponent = NoopTags.newNoopTagsComponent();
    thrown.expect(NullPointerException.class);
    noopTagsComponent.setState(null);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void preventSettingStateAfterGettingState_DifferentState() {
    TagsComponent noopTagsComponent = NoopTags.newNoopTagsComponent();
    noopTagsComponent.setState(TaggingState.DISABLED);
    noopTagsComponent.getState();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("State was already read, cannot set state.");
    noopTagsComponent.setState(TaggingState.ENABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void preventSettingStateAfterGettingState_SameState() {
    TagsComponent noopTagsComponent = NoopTags.newNoopTagsComponent();
    noopTagsComponent.setState(TaggingState.DISABLED);
    noopTagsComponent.getState();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("State was already read, cannot set state.");
    noopTagsComponent.setState(TaggingState.DISABLED);
  }

  @Test
  public void noopTagger() {
    Tagger noopTagger = NoopTags.getNoopTagger();
    assertThat(noopTagger.empty()).isSameAs(NoopTags.getNoopTagContext());
    assertThat(noopTagger.getCurrentTagContext()).isSameAs(NoopTags.getNoopTagContext());
    assertThat(noopTagger.emptyBuilder()).isSameAs(NoopTags.getNoopTagContextBuilder());
    assertThat(noopTagger.toBuilder(TAG_CONTEXT)).isSameAs(NoopTags.getNoopTagContextBuilder());
    assertThat(noopTagger.currentBuilder()).isSameAs(NoopTags.getNoopTagContextBuilder());
    assertThat(noopTagger.withTagContext(TAG_CONTEXT)).isSameAs(NoopScope.getInstance());
  }

  @Test
  public void noopTagger_ToBuilder_DisallowsNull() {
    Tagger noopTagger = NoopTags.getNoopTagger();
    thrown.expect(NullPointerException.class);
    noopTagger.toBuilder(null);
  }

  @Test
  public void noopTagger_WithTagContext_DisallowsNull() {
    Tagger noopTagger = NoopTags.getNoopTagger();
    thrown.expect(NullPointerException.class);
    noopTagger.withTagContext(null);
  }

  @Test
  public void noopTagContextBuilder() {
    assertThat(NoopTags.getNoopTagContextBuilder().build()).isSameAs(NoopTags.getNoopTagContext());
    assertThat(NoopTags.getNoopTagContextBuilder().put(KEY, VALUE).build())
        .isSameAs(NoopTags.getNoopTagContext());
    assertThat(NoopTags.getNoopTagContextBuilder().buildScoped()).isSameAs(NoopScope.getInstance());
    assertThat(NoopTags.getNoopTagContextBuilder().put(KEY, VALUE).buildScoped())
        .isSameAs(NoopScope.getInstance());
  }

  @Test
  public void noopTagContextBuilder_Put_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE);
  }

  @Test
  public void noopTagContextBuilder_Put_DisallowsNullValue() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, null);
  }

  @Test
  public void noopTagContextBuilder_Put_DisallowsNullTagMetadata() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, VALUE, null);
  }

  @Test
  public void noopTagContextBuilder_Remove_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }

  @Test
  public void noopTagContext() {
    assertThat(Lists.newArrayList(NoopTags.getNoopTagContext().getIterator())).isEmpty();
  }

  @Test
  public void noopTagPropagationComponent() {
    assertThat(NoopTags.getNoopTagPropagationComponent().getBinarySerializer())
        .isSameAs(NoopTags.getNoopTagContextBinarySerializer());
  }

  @Test
  public void noopTagContextBinarySerializer()
      throws TagContextDeserializationException, TagContextSerializationException {
    assertThat(NoopTags.getNoopTagContextBinarySerializer().toByteArray(TAG_CONTEXT))
        .isEqualTo(new byte[0]);
    assertThat(NoopTags.getNoopTagContextBinarySerializer().fromByteArray(new byte[5]))
        .isEqualTo(NoopTags.getNoopTagContext());
  }

  @Test
  public void noopTagContextBinarySerializer_ToByteArray_DisallowsNull()
      throws TagContextSerializationException {
    TagContextBinarySerializer noopSerializer = NoopTags.getNoopTagContextBinarySerializer();
    thrown.expect(NullPointerException.class);
    noopSerializer.toByteArray(null);
  }

  @Test
  public void noopTagContextBinarySerializer_FromByteArray_DisallowsNull()
      throws TagContextDeserializationException {
    TagContextBinarySerializer noopSerializer = NoopTags.getNoopTagContextBinarySerializer();
    thrown.expect(NullPointerException.class);
    noopSerializer.fromByteArray(null);
  }
}
