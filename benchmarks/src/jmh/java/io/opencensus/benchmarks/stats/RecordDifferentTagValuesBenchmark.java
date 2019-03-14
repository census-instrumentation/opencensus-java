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
import io.opencensus.stats.Measure;
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
    @Param({"0", "1", "2", "3", "8"})
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

    // creates 'size' tag contexts mapping "key0" -> "valueN"
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
  public MeasureMap recordDoubleCount(Data data) {
    return record(data, StatsBenchmarksUtil.DOUBLE_COUNT_MEASURES[0], (double) 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordLongCount(Data data) {
    return record(data, StatsBenchmarksUtil.LONG_COUNT_MEASURES[0], 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordDoubleSum(Data data) {
    return record(data, StatsBenchmarksUtil.DOUBLE_SUM_MEASURES[0], (double) 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordLongSum(Data data) {
    return record(data, StatsBenchmarksUtil.LONG_SUM_MEASURES[0], 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordDoubleDistribution(Data data) {
    return record(data, StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], (double) 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordLongDistribution(Data data) {
    return record(data, StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordDoubleLastValue(Data data) {
    return record(data, StatsBenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[0], (double) 11);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap recordLongLastValue(Data data) {
    return record(data, StatsBenchmarksUtil.LONG_LASTVALUE_MEASURES[0], 11);
  }

  private static MeasureMap record(Data data, Measure.MeasureLong measure, int value) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(measure, value);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }

  private static MeasureMap record(Data data, Measure.MeasureDouble measure, double value) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(measure, value);
    for (TagContext tags : data.contexts) {
      map.record(tags);
    }
    return map;
  }
}
