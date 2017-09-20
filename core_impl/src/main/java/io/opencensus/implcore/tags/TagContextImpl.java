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
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class TagContextImpl extends TagContext {

  static final TagContextImpl EMPTY = new TagContextImpl(Collections.<TagKey, TagValue>emptyMap());

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey, TagValue> tags;

  public TagContextImpl(Map<? extends TagKey, ? extends TagValue> tags) {
    this.tags = Collections.unmodifiableMap(new HashMap<TagKey, TagValue>(tags));
  }

  public Map<TagKey, TagValue> getTags() {
    return tags;
  }

  @Override
  public Iterator<Tag> unsafeGetIterator() {
    return new TagIterator(tags);
  }

  @Override
  public boolean equals(Object other) {
    // Directly compare the tags when both objects are TagContextImpls, for efficiency.
    if (other instanceof TagContextImpl) {
      return getTags().equals(((TagContextImpl) other).getTags());
    }
    return super.equals(other);
  }

  private static final class TagIterator implements Iterator<Tag> {
    Iterator<Map.Entry<TagKey, TagValue>> iterator;

    TagIterator(Map<TagKey, TagValue> tags) {
      iterator = tags.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Tag next() {
      final Entry<TagKey, TagValue> next = iterator.next();
      TagValue value = next.getValue();
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

    private static final class NewTagString implements Function<TagKeyString, Tag> {
      private final TagValue value;

      NewTagString(TagValue value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyString key) {
        return TagString.create(key, (TagValueString) value);
      }
    }

    private static final class NewTagLong implements Function<TagKeyLong, Tag> {
      private final TagValue value;

      NewTagLong(TagValue value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyLong key) {
        return TagLong.create(key, (TagValueLong) value);
      }
    }

    private static final class NewTagBoolean implements Function<TagKeyBoolean, Tag> {
      private final TagValue value;

      NewTagBoolean(TagValue value) {
        this.value = value;
      }

      @Override
      public Tag apply(TagKeyBoolean key) {
        return TagBoolean.create(key, (TagValueBoolean) value);
      }
    }
  }
}
