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
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextParseException;
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
  private static final TagKeyString KEY_STRING = TagKeyString.create("key");
  private static final TagValueString VALUE_STRING = TagValueString.create("value");
  private static final TagKeyLong KEY_LONG = TagKeyLong.create("key");
  private static final TagValueLong VALUE_LONG = TagValueLong.create(1L);
  private static final TagKeyBoolean KEY_BOOLEAN = TagKeyBoolean.create("key");
  private static final TagValueBoolean VALUE_BOOLEAN = TagValueBoolean.create(true);

  private static final TagContext TAG_CONTEXT =
      new TagContext() {

        @Override
        public Iterator<Tag> unsafeGetIterator() {
          return Arrays.<Tag>asList(TagString.create(KEY_STRING, VALUE_STRING)).iterator();
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopTagsComponent() {
    assertThat(NoopTags.getNoopTagsComponent().getTagger()).isSameAs(NoopTags.getNoopTagger());
    assertThat(NoopTags.getNoopTagsComponent().getTagPropagationComponent())
        .isSameAs(NoopTags.getNoopTagPropagationComponent());
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
    assertThat(NoopTags.getNoopTagContextBuilder().put(KEY_STRING, VALUE_STRING).build())
        .isSameAs(NoopTags.getNoopTagContext());
    assertThat(NoopTags.getNoopTagContextBuilder().buildScoped()).isSameAs(NoopScope.getInstance());
    assertThat(NoopTags.getNoopTagContextBuilder().put(KEY_STRING, VALUE_STRING).buildScoped())
        .isSameAs(NoopScope.getInstance());
  }

  @Test
  public void noopTagContextBuilder_PutString_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE_STRING);
  }

  @Test
  public void noopTagContextBuilder_PutString_DisallowsNullValue() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY_STRING, null);
  }

  @Test
  public void noopTagContextBuilder_PutLong_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE_LONG);
  }

  @Test
  public void noopTagContextBuilder_PutLong_DisallowsNullValue() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY_LONG, null);
  }

  @Test
  public void noopTagContextBuilder_PutBoolean_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE_BOOLEAN);
  }

  @Test
  public void noopTagContextBuilder_PutBoolean_DisallowsNullValue() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY_BOOLEAN, null);
  }

  @Test
  public void noopTagContextBuilder_Remove_DisallowsNullKey() {
    TagContextBuilder noopBuilder = NoopTags.getNoopTagContextBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }

  @Test
  public void noopTagContext() {
    assertThat(Lists.newArrayList(NoopTags.getNoopTagContext().unsafeGetIterator())).isEmpty();
  }

  @Test
  public void noopTagPropagationComponent() {
    assertThat(NoopTags.getNoopTagPropagationComponent().getBinarySerializer())
        .isSameAs(NoopTags.getNoopTagContextBinarySerializer());
  }

  @Test
  public void noopTagContextBinarySerializer() throws TagContextParseException {
    assertThat(NoopTags.getNoopTagContextBinarySerializer().toByteArray(TAG_CONTEXT))
        .isEqualTo(new byte[0]);
    assertThat(NoopTags.getNoopTagContextBinarySerializer().fromByteArray(new byte[5]))
        .isEqualTo(NoopTags.getNoopTagContext());
  }

  @Test
  public void noopTagContextBinarySerializer_ToByteArray_DisallowsNull() {
    TagContextBinarySerializer noopSerializer = NoopTags.getNoopTagContextBinarySerializer();
    thrown.expect(NullPointerException.class);
    noopSerializer.toByteArray(null);
  }

  @Test
  public void noopTagContextBinarySerializer_FromByteArray_DisallowsNull()
      throws TagContextParseException {
    TagContextBinarySerializer noopSerializer = NoopTags.getNoopTagContextBinarySerializer();
    thrown.expect(NullPointerException.class);
    noopSerializer.fromByteArray(null);
  }
}
