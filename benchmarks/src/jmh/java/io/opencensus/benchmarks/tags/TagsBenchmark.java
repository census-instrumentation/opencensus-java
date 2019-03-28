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

import io.opencensus.tags.Tag;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link io.opencensus.trace.Tagger}. */
public class TagsBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"impl", "impl-lite"})
    String implementation;

    @Param({"1", "8", "32", "128", "255"})
    int size;

    private String input;
    private TagKey tagKey;
    private TagValue tagValue;

    @Setup
    public void setup() throws Exception {
      StringBuilder builder = new StringBuilder(size);
      // build a string with characters from 'a' to 'z'
      for (int i = 0; i < size; i++) {
        builder.append((char) (97 + i % 26));
      }
      input = builder.toString();
      tagKey = TagKey.create(input);
      tagValue = TagValue.create(input);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagKey tagKeyCreation(Data data) {
    return TagKey.create("key");
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagValue tagValueCreation(Data data) {
    return TagValue.create("val");
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Tag tagCreation(Data data) {
    return Tag.create(data.tagKey, data.tagValue);
  }
}
