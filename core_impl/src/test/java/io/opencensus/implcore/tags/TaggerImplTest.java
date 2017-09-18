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

import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.UnreleasedApiAccessor;
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
  private final Tagger tagger = new TaggerImpl();

  private static final TagKeyString KS = TagKeyString.create("ks");
  private static final TagKeyLong KL = UnreleasedApiAccessor.createTagKeyLong("kl");
  private static final TagKeyBoolean KB = UnreleasedApiAccessor.createTagKeyBoolean("kb");

  private static final TagValueString VS1 = TagValueString.create("v1");
  private static final TagValueString VS2 = TagValueString.create("v2");
  private static final TagValueLong VL = TagValueLong.create(10L);
  private static final TagValueBoolean VB = TagValueBoolean.create(false);

  private static final Tag TAG1 = TagString.create(KS, VS1);
  private static final Tag TAG2 = TagLong.create(KL, VL);
  private static final Tag TAG3 = TagBoolean.create(KB, VB);

  @Test
  public void empty() {
    assertThat(asList(tagger.empty())).isEmpty();
    assertThat(tagger.empty()).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void emptyBuilder() {
    TagContextBuilder builder = tagger.emptyBuilder();
    assertThat(builder).isInstanceOf(TagContextBuilderImpl.class);
    assertThat(asList(builder.build())).isEmpty();
  }

  @Test
  public void toBuilder_ConvertUnknownTagContextToTagContextImpl() {
    TagContext unknownTagContext = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContext newTagContext = tagger.toBuilder(unknownTagContext).build();
    assertThat(asList(newTagContext)).containsExactly(TAG1, TAG2, TAG3);
    assertThat(newTagContext).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void toBuilder_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = TagString.create(KS, VS1);
    Tag tag2 = TagString.create(KS, VS2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext newTagContext = tagger.toBuilder(tagContextWithDuplicateTags).build();
    assertThat(asList(newTagContext)).containsExactly(tag2);
  }

  @Test
  public void toBuilder_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext newTagContext = tagger.toBuilder(tagContextWithNullTag).build();
    assertThat(asList(newTagContext)).containsExactly(TAG1, TAG2);
  }

  @Test
  public void getCurrentTagContext_DefaultIsEmptyTagContextImpl() {
    TagContext currentTagContext = tagger.getCurrentTagContext();
    assertThat(asList(currentTagContext)).isEmpty();
    assertThat(currentTagContext).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void getCurrentTagContext_ConvertUnknownTagContextToTagContextImpl() {
    TagContext unknownTagContext = new SimpleTagContext(TAG1, TAG2, TAG3);
    TagContext result = getResultOfGetCurrentTagContext(unknownTagContext);
    assertThat(result).isInstanceOf(TagContextImpl.class);
    assertThat(asList(result)).containsExactly(TAG1, TAG2, TAG3);
  }

  @Test
  public void getCurrentTagContext_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = TagString.create(KS, VS1);
    Tag tag2 = TagString.create(KS, VS2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext result = getResultOfGetCurrentTagContext(tagContextWithDuplicateTags);
    assertThat(asList(result)).containsExactly(tag2);
  }

  @Test
  public void getCurrentTagContext_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext result = getResultOfGetCurrentTagContext(tagContextWithNullTag);
    assertThat(asList(result)).containsExactly(TAG1, TAG2);
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
    assertThat(asList(result)).containsExactly(TAG1, TAG2, TAG3);
  }

  @Test
  public void withTagContext_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = TagString.create(KS, VS1);
    Tag tag2 = TagString.create(KS, VS2);
    TagContext tagContextWithDuplicateTags = new SimpleTagContext(tag1, tag2);
    TagContext result = getResultOfWithTagContext(tagContextWithDuplicateTags);
    assertThat(asList(result)).containsExactly(tag2);
  }

  @Test
  public void withTagContext_SkipNullTag() {
    TagContext tagContextWithNullTag = new SimpleTagContext(TAG1, null, TAG2);
    TagContext result = getResultOfWithTagContext(tagContextWithNullTag);
    assertThat(asList(result)).containsExactly(TAG1, TAG2);
  }

  private TagContext getResultOfWithTagContext(TagContext tagsToSet) {
    Scope scope = tagger.withTagContext(tagsToSet);
    try {
      return ContextUtils.TAG_CONTEXT_KEY.get();
    } finally {
      scope.close();
    }
  }

  private static List<Tag> asList(TagContext tags) {
    return Lists.newArrayList(tags.unsafeGetIterator());
  }

  private static final class SimpleTagContext extends TagContext {
    private final List<Tag> tags;

    SimpleTagContext(Tag... tags) {
      this.tags = Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    public Iterator<Tag> unsafeGetIterator() {
      return tags.iterator();
    }
  }
}
