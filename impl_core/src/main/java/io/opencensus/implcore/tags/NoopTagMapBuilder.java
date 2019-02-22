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

import io.opencensus.common.Scope;
import io.opencensus.implcore.internal.NoopScope;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagValue;

/** {@link TagContextBuilder} that is used when tagging is disabled. */
final class NoopTagMapBuilder extends TagContextBuilder {
  static final NoopTagMapBuilder INSTANCE = new NoopTagMapBuilder();

  private NoopTagMapBuilder() {}

  @Override
  public TagContextBuilder put(TagKey key, TagValue value) {
    return this;
  }

  @Override
  public TagContextBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
    return this;
  }

  @Override
  public TagContextBuilder remove(TagKey key) {
    return this;
  }

  @Override
  public TagContext build() {
    return TagMapImpl.EMPTY;
  }

  @Override
  public Scope buildScoped() {
    return NoopScope.getInstance();
  }
}
