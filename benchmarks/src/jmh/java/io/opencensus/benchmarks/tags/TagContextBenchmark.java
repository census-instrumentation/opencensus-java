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
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link io.opencensus.trace.Tagger}. */
public class TagContextBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"0", "1", "2", "4", "8", "16"})
    int numTags;

    @Param({"impl", "impl-lite"})
    String implementation;

    private Scope scope;
    private Tagger tagger;
    private TagContextBinarySerializer serializer;
    private TagContext tagContext;
    private byte[] serializedTagContext;

    @Setup
    public void setup() throws Exception {
      tagger = TagsBenchmarksUtil.getTagger(implementation);
      serializer = TagsBenchmarksUtil.getTagContextBinarySerializer(implementation);
      tagContext = TagsBenchmarksUtil.createTagContext(tagger.emptyBuilder(), numTags);
      scope = tagger.withTagContext(tagContext);
      serializedTagContext = serializer.toByteArray(tagContext);
    }

    @TearDown
    public void tearDown() {
      scope.close();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext tagContextCreation(Data data) {
    return TagsBenchmarksUtil.createTagContext(data.tagger.emptyBuilder(), data.numTags);
  }

  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Scope scopeTagContext(Data data) {
    Scope scope = data.tagger.withTagContext(data.tagContext);
    scope.close();
    return scope;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext getCurrentTagContext(Data data) {
    return data.tagger.getCurrentTagContext();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public byte[] serializeTagContext(Data data) throws Exception {
    return data.serializer.toByteArray(data.tagContext);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public TagContext deserializeTagContext(Data data) throws Exception {
    return data.serializer.fromByteArray(data.serializedTagContext);
  }
}
