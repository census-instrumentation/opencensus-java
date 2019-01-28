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

import io.opencensus.common.Scope;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.HashMap;
import java.util.Map;

final class TagMapBuilderImpl extends TagContextBuilder {
  private final Map<TagKey, TagValue> tags;
  private TagScope tagScope = TagScope.LOCAL;

  TagMapBuilderImpl(Map<TagKey, TagValue> tags) {
    this.tags = new HashMap<TagKey, TagValue>(tags);
  }

  TagMapBuilderImpl() {
    this.tags = new HashMap<TagKey, TagValue>();
  }

  @Override
  public TagMapBuilderImpl put(TagKey key, TagValue value) {
    tags.put(checkNotNull(key, "key"), checkNotNull(value, "value"));
    return this;
  }

  @Override
  public TagMapBuilderImpl remove(TagKey key) {
    tags.remove(checkNotNull(key, "key"));
    return this;
  }

  @Override
  public TagContextBuilder setTagScope(TagScope tagScope) {
    this.tagScope = checkNotNull(tagScope, "tagScope");
    return this;
  }

  @Override
  public TagMapImpl build() {
    return new TagMapImpl(tags, tagScope);
  }

  @Override
  public Scope buildScoped() {
    return CurrentTagMapUtils.withTagMap(build());
  }
}
