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
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
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

/** Benchmarks for {@link io.opencensus.trace.Tagger}. */
public class TaggerBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    //@Param({"0", "1", "2", "4", "8"})
    @Param({"0", "1"})
    int numTags;

    @Param({"impl", "impl-lite"})
    String implementation;

    private Tagger tagger;
    private TagContextBinarySerializer serializer;
    private TagContext[] tagContexts;
    private byte[][] serializedTagContexts;

    @Setup
    public void setup() throws Exception {
      tagger = BenchmarksUtil.getTagger(implementation);
      serializer = BenchmarksUtil.getTagContextBinarySerializer(implementation);
      TagContext[] tmpTagContexts = {
        createTags(0), createTags(1), createTags(2), createTags(3), createTags(4),
        createTags(5), createTags(6), createTags(7), createTags(8)
      };
      tagContexts = tmpTagContexts;

      byte[][] tmpSerializedTagContexts = {
        serialize(0), serialize(1), serialize(2), serialize(3), serialize(4),
        serialize(5), serialize(6), serialize(7), serialize(8)
      };
      serializedTagContexts = tmpSerializedTagContexts;
    }

    private TagContext createTags(int numTags) {
      TagContextBuilder tagsBuilder = tagger.emptyBuilder();
      for (int i = 0; i < numTags; i++) {
        tagsBuilder.put(BenchmarksUtil.TAG_KEYS[i], BenchmarksUtil.TAG_VALUES[i]);
      }
      return tagsBuilder.build();
    }

    private byte[] serialize(int numTags) throws Exception {
      return serializer.toByteArray(tagContexts[numTags]);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Scope timeWithTagContext(Data data) {
    Scope scope = data.tagger.withTagContext(data.tagContexts[data.numTags]);
    scope.close();
    return scope;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext timeGetCurrentTagContext(Data data) {
    return data.tagger.getCurrentTagContext();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] timeToByteArray(Data data) throws Exception {
    return data.serializer.toByteArray(data.tagContexts[data.numTags]);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext timeFromByteArray(Data data) throws Exception {
    return data.serializer.fromByteArray(data.serializedTagContexts[data.numTags]);
  }
}
