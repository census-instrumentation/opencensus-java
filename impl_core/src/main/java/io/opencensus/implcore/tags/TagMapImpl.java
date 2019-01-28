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

import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder.TagScope;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class TagMapImpl extends TagContext {

  public static final TagMapImpl EMPTY =
      new TagMapImpl(Collections.<TagKey, TagValue>emptyMap(), TagScope.LOCAL);

  // The types of the TagKey and value must match for each entry.
  private final Map<TagKey, TagValue> tags;

  private final TagScope tagScope;

  public TagMapImpl(Map<? extends TagKey, ? extends TagValue> tags, TagScope tagScope) {
    this.tags = Collections.unmodifiableMap(new HashMap<TagKey, TagValue>(tags));
    this.tagScope = tagScope;
  }

  public Map<TagKey, TagValue> getTags() {
    return tags;
  }

  @Override
  public TagScope getTagScope() {
    return tagScope;
  }

  @Override
  protected Iterator<Tag> getIterator() {
    return new TagIterator(tags);
  }

  @Override
  public boolean equals(@Nullable Object other) {
    // Directly compare the tags when both objects are TagMapImpls, for efficiency.
    if (other instanceof TagMapImpl) {
      return getTags().equals(((TagMapImpl) other).getTags());
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
      return Tag.create(next.getKey(), next.getValue());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("TagIterator.remove()");
    }
  }
}
