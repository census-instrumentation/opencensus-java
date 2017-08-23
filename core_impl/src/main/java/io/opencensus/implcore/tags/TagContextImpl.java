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

import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class TagContextImpl extends TagContext {

  static final TagContextImpl EMPTY = new TagContextImpl(Collections.<TagKey, Object>emptyMap());

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey, Object> tags;

  TagContextImpl(Map<? extends TagKey, ?> tags) {
    this.tags = Collections.unmodifiableMap(new HashMap<TagKey, Object>(tags));
  }

  public Map<TagKey, Object> getTags() {
    return tags;
  }

  @Override
  public Iterator<Tag> unsafeGetIterator() {
    return new TagIterator(tags);
  }

  private static final class TagIterator implements Iterator<Tag> {
    Iterator<Map.Entry<TagKey, Object>> iterator;

    TagIterator(Map<TagKey, Object> tags) {
      iterator = tags.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Tag next() {
      final Entry<TagKey, Object> next = iterator.next();
      Object value = next.getValue();
      return next.getKey()
          .match(
              new NewTagString(value),
              new NewTagLong(value),
              new NewTagBoolean(value),
              Functions.<Tag>throwAssertionError());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("TagIterator.remove()");
    }

    private static class NewTagString implements Function<TagKeyString, Tag> {
      private final Object value;

      NewTagString(Object value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyString key) {
        return TagString.create(key, (TagValueString) value);
      }
    }

    private static class NewTagLong implements Function<TagKeyLong, Tag> {
      private final Object value;

      NewTagLong(Object value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyLong key) {
        return TagLong.create(key, (Long) value);
      }
    }

    private static class NewTagBoolean implements Function<TagKeyBoolean, Tag> {
      private final Object value;

      NewTagBoolean(Object value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyBoolean key) {
        return TagBoolean.create(key, (Boolean) value);
      }
    }
  }
}
