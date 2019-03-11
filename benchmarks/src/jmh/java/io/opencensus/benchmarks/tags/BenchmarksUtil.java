/*
 * Copyright 2018, OpenCensus Authors
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

import io.opencensus.impllite.tags.TagsComponentImplLite;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.propagation.TagContextBinarySerializer;

/** Util class for Benchmarks. */
final class BenchmarksUtil {
  static final TagKey[] TAG_KEYS = {
    TagKey.create("key0"), TagKey.create("key1"), TagKey.create("key2"),
    TagKey.create("key3"), TagKey.create("key4"), TagKey.create("key5"),
    TagKey.create("key6"), TagKey.create("key7")
  };

  static final TagValue[] TAG_VALUES = {
    TagValue.create("val0"), TagValue.create("val1"), TagValue.create("val2"),
    TagValue.create("val3"), TagValue.create("val4"), TagValue.create("val5"),
    TagValue.create("val6"), TagValue.create("val7")
  };

  private static final TagsComponentImplLite tagsComponentImplLite = new TagsComponentImplLite();

  static Tagger getTagger(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return Tags.getTagger();
    } else if (implementation.equals("impl-lite")) {
      return tagsComponentImplLite.getTagger();
    } else {
      throw new RuntimeException("Invalid tagger implementation specified.");
    }
  }

  static TagContextBinarySerializer getTagContextBinarySerializer(String implementation) {
    if (implementation.equals("impl")) {
      // We can return the global tracer here because if impl is linked the global tracer will be
      // the impl one.
      // TODO(bdrutu): Make everything not be a singleton (disruptor, etc.) and use a new
      // TraceComponentImpl similar to TraceComponentImplLite.
      return Tags.getTagPropagationComponent().getBinarySerializer();
    } else if (implementation.equals("impl-lite")) {
      return tagsComponentImplLite.getTagPropagationComponent().getBinarySerializer();
    } else {
      throw new RuntimeException("Invalid binary serializer implementation specified.");
    }
  }

  // Avoid instances of this class.
  private BenchmarksUtil() {}
}
