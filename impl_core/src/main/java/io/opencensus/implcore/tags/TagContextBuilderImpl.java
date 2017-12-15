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

final class TagContextBuilderImpl extends TagContextBuilder {
  private final Map<TagKey, TagValue> tags;

  TagContextBuilderImpl(Map<TagKey, TagValue> tags) {
    this.tags = new HashMap<TagKey, TagValue>(tags);
  }

  TagContextBuilderImpl() {
    this.tags = new HashMap<TagKey, TagValue>();
  }

  @Override
  public TagContextBuilderImpl put(TagKey key, TagValue value) {
    tags.put(checkNotNull(key, "key"), checkNotNull(value, "value"));
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

  @Override
  public Scope buildScoped() {
    return CurrentTagContextUtils.withTagContext(build());
  }
}
