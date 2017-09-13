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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.HashMap;
import java.util.Map;

final class TagContextBuilderImpl extends TagContextBuilder {
  private final Map<TagKey, TagValue> tags;

  TagContextBuilderImpl(Map<TagKey, TagValue> tags) {
    this.tags = new HashMap<TagKey, TagValue>(tags);
  }

  TagContextBuilderImpl() {
    this.tags = new HashMap<TagKey, TagValue>();
  }

  @Override
  public TagContextBuilderImpl put(TagKeyString key, TagValueString value) {
    return setInternal(key, checkNotNull(value, "value"));
  }

  @Override
  public TagContextBuilderImpl put(TagKeyLong key, TagValueLong value) {
    return setInternal(key, value);
  }

  @Override
  public TagContextBuilderImpl put(TagKeyBoolean key, TagValueBoolean value) {
    return setInternal(key, value);
  }

  private TagContextBuilderImpl setInternal(TagKey key, TagValue value) {
    tags.put(checkNotNull(key), value);
    return this;
  }

  @Override
  public TagContextBuilderImpl remove(TagKey key) {
    tags.remove(key);
    return this;
  }

  @Override
  public TagContextImpl build() {
    return new TagContextImpl(tags);
  }
}
