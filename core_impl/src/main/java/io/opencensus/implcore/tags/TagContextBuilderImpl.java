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

package io.opencensus.implcore.tags;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.util.HashMap;
import java.util.Map;

final class TagContextBuilderImpl extends TagContextBuilder {
  private final Map<TagKey, Object> tags;

  TagContextBuilderImpl(Map<TagKey, Object> tags) {
    this.tags = new HashMap<TagKey, Object>(tags);
  }

  TagContextBuilderImpl() {
    this.tags = new HashMap<TagKey, Object>();
  }

  @Override
  public TagContextBuilderImpl set(TagKeyString key, TagValueString value) {
    return setInternal(key, checkNotNull(value, "value"));
  }

  @Override
  public TagContextBuilderImpl set(TagKeyLong key, long value) {
    return setInternal(key, value);
  }

  @Override
  public TagContextBuilderImpl set(TagKeyBoolean key, boolean value) {
    return setInternal(key, value);
  }

  private TagContextBuilderImpl setInternal(TagKey key, Object value) {
    tags.put(checkNotNull(key), value);
    return this;
  }

  @Override
  public TagContextBuilderImpl clear(TagKey key) {
    tags.remove(key);
    return this;
  }

  @Override
  public TagContextImpl build() {
    return new TagContextImpl(tags);
  }
}
