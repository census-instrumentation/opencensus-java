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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TagMapImpl} and {@link TagMapBuilderImpl}.
 *
 * <p>Tests for {@link TagMapBuilderImpl#buildScoped()} are in {@link ScopedTagMapTest}.
 */
@RunWith(JUnit4.class)
public class TagMapImplTest {
  private final Tagger tagger = new TaggerImpl(new CurrentState(State.ENABLED));

  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);
  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagTtl.NO_PROPAGATION);

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");

  private static final TagValueWithMetadata VM1 =
      TagValueWithMetadata.create(V1, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata VM2 =
      TagValueWithMetadata.create(V2, METADATA_UNLIMITED_PROPAGATION);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getTags_empty() {
    TagMapImpl tags = new TagMapImpl(ImmutableMap.<TagKey, TagValueWithMetadata>of());
    assertThat(tags.getTags()).isEmpty();
  }

  @Test
  public void getTags_nonEmpty() {
    TagMapImpl tags = new TagMapImpl(ImmutableMap.of(K1, VM1, K2, VM2));
    assertThat(tags.getTags()).containsExactly(K1, VM1, K2, VM2);
  }

  @Test
  public void put_newKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    assertThat(((TagMapImpl) tagger.toBuilder(tags).put(K2, V2).build()).getTags())
        .containsExactly(K1, VM1, K2, VM2);
  }

  @Test
  public void put_existingKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    assertThat(((TagMapImpl) tagger.toBuilder(tags).put(K1, V2).build()).getTags())
        .containsExactly(K1, VM2);
  }

  @Test
  public void put_nullKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    TagContextBuilder builder = tagger.toBuilder(tags);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.put(null, V2);
  }

  @Test
  public void put_nullValue() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    TagContextBuilder builder = tagger.toBuilder(tags);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value");
    builder.put(K2, null);
  }

  @Test
  public void putLocal() {
    TagContext tags1 = tagger.emptyBuilder().put(K1, V1, METADATA_NO_PROPAGATION).build();
    TagContext tags2 = tagger.emptyBuilder().putLocal(K1, V1).build();
    assertThat(tags1).isEqualTo(tags2);
  }

  @Test
  public void remove_existingKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1, K2, VM2));
    assertThat(((TagMapImpl) tagger.toBuilder(tags).remove(K1).build()).getTags())
        .containsExactly(K2, VM2);
  }

  @Test
  public void remove_differentKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    assertThat(((TagMapImpl) tagger.toBuilder(tags).remove(K2).build()).getTags())
        .containsExactly(K1, VM1);
  }

  @Test
  public void remove_nullKey() {
    TagContext tags = new TagMapImpl(ImmutableMap.of(K1, VM1));
    TagContextBuilder builder = tagger.toBuilder(tags);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.remove(null);
  }

  @Test
  public void testIterator() {
    TagMapImpl tags = new TagMapImpl(ImmutableMap.of(K1, VM1, K2, VM2));
    Iterator<Tag> i = tags.getIterator();
    assertTrue(i.hasNext());
    Tag tag1 = i.next();
    assertTrue(i.hasNext());
    Tag tag2 = i.next();
    assertFalse(i.hasNext());
    assertThat(Arrays.asList(tag1, tag2)).containsExactly(Tag.create(K1, V1), Tag.create(K2, V2));
    thrown.expect(NoSuchElementException.class);
    i.next();
  }

  @Test
  public void disallowCallingRemoveOnIterator() {
    TagMapImpl tags = new TagMapImpl(ImmutableMap.of(K1, VM1, K2, VM2));
    Iterator<Tag> i = tags.getIterator();
    i.next();
    thrown.expect(UnsupportedOperationException.class);
    i.remove();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            tagger.emptyBuilder().put(K1, V1).put(K2, V2).build(),
            tagger.emptyBuilder().put(K1, V1).put(K2, V2).build(),
            tagger.emptyBuilder().put(K2, V2).put(K1, V1).build(),
            new TagContext() {
              @Override
              protected Iterator<Tag> getIterator() {
                return Lists.<Tag>newArrayList(Tag.create(K1, V1), Tag.create(K2, V2)).iterator();
              }
            })
        .addEqualityGroup(tagger.emptyBuilder().put(K1, V1).put(K2, V1).build())
        .addEqualityGroup(tagger.emptyBuilder().put(K1, V2).put(K2, V1).build())
        .testEquals();
  }
}
