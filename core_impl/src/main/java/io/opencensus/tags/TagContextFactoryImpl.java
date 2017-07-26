/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.tags;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class TagContextFactoryImpl extends TagContextFactory {
  TagContextFactoryImpl() {}

  private static final TagContextImpl EMPTY =
      new TagContextImpl(Collections.<TagKey, Object>emptyMap());

  @Override
  public TagContextImpl empty() {
    return EMPTY;
  }

  @Override
  public Builder emptyBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder(TagContext tags) {
    if (tags instanceof TagContextImpl) {
      return new Builder(((TagContextImpl) tags).getTags());
    } else {
      // TODO(sebright): Write a test for this.
      final Builder builder = new Builder();
      for (Tag tag : tags) {
        TagContextUtils.addTagToBuilder(tag, builder);
      }
      return builder;
    }
  }

  public static final class Builder extends TagContextFactory.Builder {
    private final Map<TagKey, Object> tags;

    private Builder(Map<TagKey, Object> tags) {
      this.tags = new HashMap<TagKey, Object>(tags);
    }

    Builder() {
      this.tags = new HashMap<TagKey, Object>();
    }

    @Override
    public Builder set(TagKeyString key, TagValueString value) {
      return setInternal(key, checkNotNull(value, "value"));
    }

    @Override
    Builder set(TagKeyLong key, long value) {
      return setInternal(key, value);
    }

    @Override
    Builder set(TagKeyBoolean key, boolean value) {
      return setInternal(key, value);
    }

    private Builder setInternal(TagKey key, Object value) {
      tags.put(checkNotNull(key), value);
      return this;
    }

    @Override
    public Builder clear(TagKey key) {
      tags.remove(key);
      return this;
    }

    @Override
    public TagContextImpl build() {
      return new TagContextImpl(tags);
    }
  }
}
