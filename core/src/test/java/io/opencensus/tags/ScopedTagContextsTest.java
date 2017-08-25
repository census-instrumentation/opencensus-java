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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opencensus.common.Scope;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for the methods in {@link TagContexts} and {@link TagContextBuilder} that interact
 * with the current {@link TagContext}.
 */
@RunWith(JUnit4.class)
public class ScopedTagContextsTest {
  private static final TagKeyString KEY_1 = TagKeyString.create("key 1");
  private static final TagKeyString KEY_2 = TagKeyString.create("key 2");

  private static final TagValueString VALUE_1 = TagValueString.create("value 1");
  private static final TagValueString VALUE_2 = TagValueString.create("value 2");

  private final TagContext emptyTagContext = new SimpleTagContext();

  private final TagContexts tagContexts =
      new TagContexts() {
        @Override
        public TagContextBuilder toBuilder(TagContext tags) {
          return new SimpleTagContextBuilder(((SimpleTagContext) tags).tags);
        }

        @Override
        public TagContext empty() {
          return emptyTagContext;
        }
      };

  @Test
  public void defaultTagContext() {
    assertThat(tagContexts.getCurrentTagContext()).isSameAs(emptyTagContext);
  }

  @Test
  public void withTagContext() {
    assertThat(tagContexts.getCurrentTagContext()).isSameAs(emptyTagContext);
    TagContext scopedTags = new SimpleTagContext(TagString.create(KEY_1, VALUE_1));
    Scope scope = tagContexts.withTagContext(scopedTags);
    try {
      assertThat(tagContexts.getCurrentTagContext()).isSameAs(scopedTags);
    } finally {
      scope.close();
    }
    assertThat(tagContexts.getCurrentTagContext()).isSameAs(emptyTagContext);
  }

  @Test
  public void createBuilderFromCurrentTags() {
    TagContext scopedTags = new SimpleTagContext(TagString.create(KEY_1, VALUE_1));
    Scope scope = tagContexts.withTagContext(scopedTags);
    try {
      TagContext newTags = tagContexts.currentBuilder().set(KEY_2, VALUE_2).build();
      assertThat(asList(newTags))
          .containsExactly(TagString.create(KEY_1, VALUE_1), TagString.create(KEY_2, VALUE_2));
      assertThat(tagContexts.getCurrentTagContext()).isSameAs(scopedTags);
    } finally {
      scope.close();
    }
  }

  @Test
  public void setCurrentTagsWithBuilder() {
    assertThat(tagContexts.getCurrentTagContext()).isSameAs(emptyTagContext);
    Scope scope = tagContexts.emptyBuilder().set(KEY_1, VALUE_1).buildScoped();
    try {
      assertThat(asList(tagContexts.getCurrentTagContext()))
          .containsExactly(TagString.create(KEY_1, VALUE_1));
    } finally {
      scope.close();
    }
    assertThat(tagContexts.getCurrentTagContext()).isSameAs(emptyTagContext);
  }

  @Test
  public void addToCurrentTagsWithBuilder() {
    TagContext scopedTags = new SimpleTagContext(TagString.create(KEY_1, VALUE_1));
    Scope scope1 = tagContexts.withTagContext(scopedTags);
    try {
      Scope scope2 = tagContexts.currentBuilder().set(KEY_2, VALUE_2).buildScoped();
      try {
        assertThat(asList(tagContexts.getCurrentTagContext()))
            .containsExactly(TagString.create(KEY_1, VALUE_1), TagString.create(KEY_2, VALUE_2));
      } finally {
        scope2.close();
      }
      assertThat(tagContexts.getCurrentTagContext()).isSameAs(scopedTags);
    } finally {
      scope1.close();
    }
  }

  private static List<Tag> asList(TagContext tags) {
    return Lists.newArrayList(tags.unsafeGetIterator());
  }

  private static final class SimpleTagContextBuilder extends TagContextBuilder {
    private final Map<TagKeyString, TagValueString> tagMap;

    SimpleTagContextBuilder(Set<TagString> tags) {
      Map<TagKeyString, TagValueString> tagMap = Maps.newHashMap();
      for (TagString tag : tags) {
        tagMap.put(tag.getKey(), tag.getValue());
      }
      this.tagMap = Maps.newHashMap(tagMap);
    }

    @Override
    public TagContextBuilder set(TagKeyString key, TagValueString value) {
      tagMap.put(key, value);
      return this;
    }

    @Override
    public TagContextBuilder set(TagKeyLong key, long value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TagContextBuilder set(TagKeyBoolean key, boolean value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TagContextBuilder clear(TagKey key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TagContext build() {
      Set<TagString> tags = new HashSet<TagString>();
      for (Entry<TagKeyString, TagValueString> entry : tagMap.entrySet()) {
        tags.add(TagString.create(entry.getKey(), entry.getValue()));
      }
      return new SimpleTagContext(tags);
    }
  }

  private static final class SimpleTagContext extends TagContext {
    private final Set<TagString> tags;

    public SimpleTagContext(TagString... tags) {
      this(Arrays.asList(tags));
    }

    public SimpleTagContext(Collection<TagString> tags) {
      this.tags = Collections.unmodifiableSet(new HashSet<TagString>(tags));
    }

    @Override
    public Iterator<Tag> unsafeGetIterator() {
      return Iterators.<TagString, Tag>transform(
          tags.iterator(), com.google.common.base.Functions.<TagString>identity());
    }
  }
}
