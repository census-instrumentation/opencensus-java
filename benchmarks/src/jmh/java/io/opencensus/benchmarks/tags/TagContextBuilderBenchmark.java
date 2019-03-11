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

import io.opencensus.common.Scope;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link io.opencensus.trace.TagContextBuilder} */
public class TagContextBuilderBenchmark {

  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    private Tagger tagger;
    private TagContext[] tagContexts;

    @Param({"impl", "impl-lite"})
    String implementation;

    @Param({"0", "1", "2", "4", "8"})
    int numTags;

    @Param({"0", "1", "2", "4", "8"})
    int baseNumTags;

    @Setup
    public void setup() {
      tagger = BenchmarksUtil.getTagger(implementation);
      TagContext[] tmpTagContexts = {
        putTags(tagger.emptyBuilder(), 0).build(),
        putTags(tagger.emptyBuilder(), 1).build(), putTags(tagger.emptyBuilder(), 2).build(),
        putTags(tagger.emptyBuilder(), 3).build(), putTags(tagger.emptyBuilder(), 4).build(),
        putTags(tagger.emptyBuilder(), 5).build(), putTags(tagger.emptyBuilder(), 6).build(),
        putTags(tagger.emptyBuilder(), 7).build(), putTags(tagger.emptyBuilder(), 8).build()
      };
      tagContexts = tmpTagContexts;
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContextBuilder timeNestedTagContextBuilder(Data data) {
    return getTagContextBuilder(data);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext timeNestedTagContext(Data data) {
    return getTagContextBuilder(data).build();
  }

  static TagContextBuilder putTags(TagContextBuilder tagsBuilder, int numTags) {
    for (int i = 0; i < numTags; i++) {
     tagsBuilder.put(BenchmarksUtil.TAG_KEYS[i], BenchmarksUtil.TAG_VALUES[i]);
    }
    return tagsBuilder;
  }

  static TagContextBuilder getTagContextBuilder(Data data) {
    TagContextBuilder result = data.tagger.toBuilder(data.tagContexts[data.baseNumTags]);
    return putTags(result, data.numTags);
  }
}
