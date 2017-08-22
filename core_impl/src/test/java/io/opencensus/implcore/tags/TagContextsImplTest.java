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
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContexts;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import io.opencensus.tags.UnreleasedApiAccessor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContextsImpl}. */
@RunWith(JUnit4.class)
public class TagContextsImplTest {
  private final TagContexts tagContexts = new TagContextsImpl();

  private static final TagKeyString KS = TagKeyString.create("ks");
  private static final TagKeyLong KL = UnreleasedApiAccessor.createTagKeyLong("kl");
  private static final TagKeyBoolean KB = UnreleasedApiAccessor.createTagKeyBoolean("kb");

  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void empty() {
    assertThat(asList(tagContexts.empty())).isEmpty();
  }

  @Test
  public void emptyBuilder() {
    assertThat(asList(tagContexts.emptyBuilder().build())).isEmpty();
  }

  @Test
  public void toBuilder_ConvertUnknownTagContextToTagContextImpl() {
    Tag tag1 = TagString.create(KS, V1);
    Tag tag2 = TagLong.create(KL, 10L);
    Tag tag3 = TagBoolean.create(KB, false);
    TagContext unknownTagContext = new SimpleTagContext(tag1, tag2, tag3);
    TagContext newTagContext = tagContexts.toBuilder(unknownTagContext).build();
    assertThat(asList(newTagContext)).containsExactly(tag1, tag2, tag3);
    assertThat(newTagContext).isInstanceOf(TagContextImpl.class);
  }

  @Test
  public void toBuilder_RemoveDuplicatesFromUnknownTagContext() {
    Tag tag1 = TagString.create(KS, V1);
    Tag tag2 = TagString.create(KS, V2);
    TagContext unknownTagContext = new SimpleTagContext(tag1, tag2);
    TagContext newTagContext = tagContexts.toBuilder(unknownTagContext).build();
    assertThat(asList(newTagContext)).containsExactly(tag2);
  }

  @Test
  public void toBuilder_HandleNullTag() {
    TagContext unknownTagContext =
        new SimpleTagContext(TagString.create(KS, V2), null, TagLong.create(KL, 5L));
    thrown.expect(NullPointerException.class);
    tagContexts.toBuilder(unknownTagContext).build();
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
