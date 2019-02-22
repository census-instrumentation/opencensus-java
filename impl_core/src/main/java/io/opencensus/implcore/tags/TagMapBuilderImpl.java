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
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import java.util.HashMap;
import java.util.Map;

final class TagMapBuilderImpl extends TagContextBuilder {

  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);

  private final Map<TagKey, TagValueWithMetadata> tags;

  TagMapBuilderImpl(Map<TagKey, TagValueWithMetadata> tags) {
    this.tags = new HashMap<TagKey, TagValueWithMetadata>(tags);
  }

  TagMapBuilderImpl() {
    this.tags = new HashMap<TagKey, TagValueWithMetadata>();
  }

  @Override
  public TagMapBuilderImpl put(TagKey key, TagValue value) {
    put(key, value, METADATA_UNLIMITED_PROPAGATION);
    return this;
  }

  @Override
  public TagContextBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
    TagValueWithMetadata valueWithMetadata =
        TagValueWithMetadata.create(
            checkNotNull(value, "value"), checkNotNull(tagMetadata, "tagMetadata"));
    tags.put(checkNotNull(key, "key"), valueWithMetadata);
    return this;
  }

  @Override
  public TagMapBuilderImpl remove(TagKey key) {
    tags.remove(checkNotNull(key, "key"));
    return this;
  }

  @Override
  public TagMapImpl build() {
    return new TagMapImpl(tags);
  }

  @Override
  public Scope buildScoped() {
    return CurrentTagMapUtils.withTagMap(build());
  }
}
