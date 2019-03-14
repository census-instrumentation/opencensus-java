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
import io.opencensus.stats.ViewManager;
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

/** Benchmarks for {@link io.opencensus.stats.StatsRecorder}. */
public class RecordBatchedBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"0", "1", "2", "3"})
    int numValues;

    @Param({"impl", "impl-lite"})
    String implementation;

    private StatsRecorder recorder;
    private ViewManager manager;
    private Tagger tagger;
    private TagContext tags;

    @Setup
    public void setup() throws Exception {
      manager = StatsBenchmarksUtil.getViewManager(implementation);
      recorder = StatsBenchmarksUtil.getStatsRecorder(implementation);
      tagger = TagsBenchmarksUtil.getTagger(implementation);
      tags = TagsBenchmarksUtil.createTagContext(tagger.emptyBuilder(), 1);
      for (int i = 0; i < numValues; i++) {
        manager.registerView(StatsBenchmarksUtil.DOUBLE_COUNT_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_COUNT_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_SUM_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_SUM_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_DISTRIBUTION_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.DOUBLE_LASTVALUE_VIEWS[i]);
        manager.registerView(StatsBenchmarksUtil.LONG_LASTVALUE_VIEWS[i]);
      }
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_COUNT_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_COUNT_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_SUM_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_SUM_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[i], (double) i);
    }
    map.record(data.tags);
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    for (int i = 0; i < data.numValues; i++) {
      map.put(StatsBenchmarksUtil.LONG_LASTVALUE_MEASURES[i], i);
    }
    map.record(data.tags);
    return map;
  }
}
