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

package io.opencensus.benchmarks.stats;

import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.ViewManager;
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
public class RecorderBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    //@Param({"0", "1", "2", "4", "8"})
    @Param({"0"})
    int numTags;

    @Param({"0", "1", "2", "3"})
    int numValues;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    private StatsRecorder recorder;
    private ViewManager manager;
    private Tagger tagger;
    private TagContext tags;

    @Setup
    public void setup() throws Exception {
      manager = BenchmarksUtil.getViewManager(implementation);
      recorder = BenchmarksUtil.getStatsRecorder(implementation);
      tagger = BenchmarksUtil.getTagger(implementation);
      tags = createTags(numTags);
      manager.registerView(BenchmarksUtil.VIEW_ML0);
    }

    private TagContext createTags(int numTags) {
      TagContextBuilder tagsBuilder = tagger.emptyBuilder();
      for (int i = 0; i < numTags; i++) {
        tagsBuilder.put(BenchmarksUtil.TAG_KEYS[i], BenchmarksUtil.TAG_VALUES[i]);
      }
      return tagsBuilder.build();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordMeasureDouble(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(BenchmarksUtil.MEASURE_DOUBLES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordMeasureLong(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(BenchmarksUtil.MEASURE_LONGS[i], i);
    }
    map.record(data.tags);
    return map;
  }
}
