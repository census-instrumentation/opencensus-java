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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.UnreleasedApiAccessor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagContextImpl} and {@link TagContextBuilderImpl}. */
// TODO(sebright): Add more tests once the API is finalized.
@RunWith(JUnit4.class)
public class TagContextImplTest {
  private final Tagger tagger =
      new TaggerImpl(new AtomicReference<TaggingState>(TaggingState.ENABLED));

  private static final TagKeyString KS1 = TagKeyString.create("k1");
  private static final TagKeyString KS2 = TagKeyString.create("k2");

  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void allowMutlipleKeysWithSameNameButDifferentTypes() {
    TagKeyString stringKey = TagKeyString.create("key");
    TagKeyLong longKey = UnreleasedApiAccessor.createTagKeyLong("key");
    TagKeyBoolean boolKey = UnreleasedApiAccessor.createTagKeyBoolean("key");
    assertThat(
            tagContextToList(
                tagger
                    .emptyBuilder()
                    .put(stringKey, TagValueString.create("value"))
                    .put(longKey, TagValueLong.create(123))
                    .put(boolKey, TagValueBoolean.create(true))
                    .build()))
        .containsExactly(
            TagString.create(stringKey, TagValueString.create("value")),
            TagLong.create(longKey, TagValueLong.create(123L)),
            TagBoolean.create(boolKey, TagValueBoolean.create(true)));
  }

  @Test
  public void testSet() {
    TagContext tags = tagger.emptyBuilder().put(KS1, V1).build();
    assertThat(tagContextToList(tagger.toBuilder(tags).put(KS1, V2).build()))
        .containsExactly(TagString.create(KS1, V2));
    assertThat(tagContextToList(tagger.toBuilder(tags).put(KS2, V2).build()))
        .containsExactly(TagString.create(KS1, V1), TagString.create(KS2, V2));
  }

  @Test
  public void testClear() {
    TagContext tags = tagger.emptyBuilder().put(KS1, V1).build();
    assertThat(tagContextToList(tagger.toBuilder(tags).remove(KS1).build())).isEmpty();
    assertThat(tagContextToList(tagger.toBuilder(tags).remove(KS2).build()))
        .containsExactly(TagString.create(KS1, V1));
  }

  @Test
  public void testIterator() {
    TagContext tags = tagger.emptyBuilder().put(KS1, V1).put(KS2, V2).build();
    Iterator<Tag> i = InternalUtils.getTags(tags);
    assertTrue(i.hasNext());
    Tag tag1 = i.next();
    assertTrue(i.hasNext());
    Tag tag2 = i.next();
    assertFalse(i.hasNext());
    assertThat(Arrays.asList(tag1, tag2))
        .containsExactly(TagString.create(KS1, V1), TagString.create(KS2, V2));
    thrown.expect(NoSuchElementException.class);
    i.next();
  }

  @Test
  public void disallowCallingRemoveOnIterator() {
    TagContext tags = tagger.emptyBuilder().put(KS1, V1).put(KS2, V2).build();
    Iterator<Tag> i = InternalUtils.getTags(tags);
    i.next();
    thrown.expect(UnsupportedOperationException.class);
    i.remove();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            tagger.emptyBuilder().put(KS1, V1).put(KS2, V2).build(),
            tagger.emptyBuilder().put(KS1, V1).put(KS2, V2).build(),
            tagger.emptyBuilder().put(KS2, V2).put(KS1, V1).build(),
            new TagContext() {
              @Override
              protected Iterator<Tag> getIterator() {
                return Lists.<Tag>newArrayList(TagString.create(KS1, V1), TagString.create(KS2, V2))
                    .iterator();
              }
            })
        .addEqualityGroup(tagger.emptyBuilder().put(KS1, V1).put(KS2, V1).build())
        .addEqualityGroup(tagger.emptyBuilder().put(KS1, V2).put(KS2, V1).build())
        .testEquals();
  }
}
