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

import io.opencensus.benchmarks.tags.TagsBenchmarksUtil;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import java.util.Arrays;
import java.util.List;
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
public class RecordDifferentTagValuesBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"0", "1", "2", "3"})
    int numTags;

    @Param({"impl", "impl-lite"})
    String implementation;

    private StatsRecorder recorder;
    private ViewManager manager;
    private Tagger tagger;
    private List<TagContext> contexts;

    @Setup
    public void setup() throws Exception {
      manager = StatsBenchmarksUtil.getViewManager(implementation);
      recorder = StatsBenchmarksUtil.getStatsRecorder(implementation);
      tagger = TagsBenchmarksUtil.getTagger(implementation);
      contexts = createContexts(numTags);
      manager.registerView(StatsBenchmarksUtil.DOUBLE_COUNT_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.LONG_COUNT_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.DOUBLE_SUM_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.LONG_SUM_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.LONG_DISTRIBUTION_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.DOUBLE_LASTVALUE_VIEWS[0]);
      manager.registerView(StatsBenchmarksUtil.LONG_LASTVALUE_VIEWS[0]);
    }

    private List<TagContext> createContexts(int size) {
      TagContext[] contexts = new TagContext[size];
      for (int i = 0; i < size; i++) {
        contexts[i] = tagger.emptyBuilder().put(TagsBenchmarksUtil.TAG_KEYS[0], TagsBenchmarksUtil.TAG_VALUES[i]).build();
      }
      return Arrays.asList(contexts);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordDoubleCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.DOUBLE_COUNT_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordLongCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.LONG_COUNT_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordDoubleSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.DOUBLE_SUM_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordLongSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.LONG_SUM_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordDoubleDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordLongDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordDoubleLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordLongLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(StatsBenchmarksUtil.LONG_LASTVALUE_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }
}
