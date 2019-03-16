/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.benchmarks.tags;

import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.impllite.tags.TagsComponentImplLite;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.propagation.TagContextBinarySerializer;

/** Util class for Tags Benchmarks. */
public final class TagsBenchmarksUtil {
  private static final TagsComponentImplBase tagsComponentImplBase = new TagsComponentImplBase();
  private static final TagsComponentImplLite tagsComponentImplLite = new TagsComponentImplLite();

  public static final TagKey[] TAG_KEYS = createTagKeys(16, "key");
  public static final TagValue[] TAG_VALUES = createTagValues(16, "val");

  public static Tagger getTagger(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return tagsComponentImplBase.getTagger();
    } else if (implementation.equals("impl-lite")) {
      return tagsComponentImplLite.getTagger();
    } else {
      throw new RuntimeException("Invalid tagger implementation specified.");
    }
  }

  public static TagContextBinarySerializer getTagContextBinarySerializer(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return tagsComponentImplBase.getTagPropagationComponent().getBinarySerializer();
    } else if (implementation.equals("impl-lite")) {
      return tagsComponentImplLite.getTagPropagationComponent().getBinarySerializer();
    } else {
      throw new RuntimeException("Invalid binary serializer implementation specified.");
    }
  }

  public static TagKey[] createTagKeys(int size, String name) {
    TagKey[] keys = new TagKey[size];
    for (int i = 0; i < size; i++) {
      keys[i] = TagKey.create(name + i);
    }
    return keys;
  }

  public static TagValue[] createTagValues(int size, String name) {
    TagValue[] values = new TagValue[size];
    for (int i = 0; i < size; i++) {
      values[i] = TagValue.create(name + i);
    }
    return values;
  }

  public static TagContext createTagContext(TagContextBuilder tagsBuilder, int numTags) {
    for (int i = 0; i < numTags; i++) {
      tagsBuilder.put(TAG_KEYS[i], TAG_VALUES[i]);
    }
    return tagsBuilder.build();
  }

  // Avoid instances of this class.
  private TagsBenchmarksUtil() {}
}
