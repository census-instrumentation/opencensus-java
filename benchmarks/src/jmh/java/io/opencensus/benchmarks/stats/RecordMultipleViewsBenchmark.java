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
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Benchmarks for {@link io.opencensus.trace.Tagger}. */
public class RecordMultipleViewsBenchmark {
  @State(org.openjdk.jmh.annotations.Scope.Benchmark)
  public static class Data {
    @Param({"0", "1", "2", "3", "6", "8"})
    int numViews;

    @Param({"impl", "impl-lite"})
    String implementation;

    private StatsRecorder recorder;
    private ViewManager manager;
    private Tagger tagger;
    private TagContext tagContext;

    @Setup
    public void setup() throws Exception {
      manager = StatsBenchmarksUtil.getViewManager(implementation);
      recorder = StatsBenchmarksUtil.getStatsRecorder(implementation);
      tagger = TagsBenchmarksUtil.getTagger(implementation);
      tagContext = createContext(numViews);

      for (int i = 0; i < numViews; i++) {
        // count
        manager.registerView(StatsBenchmarksUtil.createView(
            "DC" + i,
            StatsBenchmarksUtil.DOUBLE_COUNT_MEASURES[0],
            Aggregation.Count.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
        manager.registerView(StatsBenchmarksUtil.createView(
            "LC" + i,
            StatsBenchmarksUtil.LONG_COUNT_MEASURES[0],
            Aggregation.Count.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
        // sum
        manager.registerView(StatsBenchmarksUtil.createView(
            "DS" + i,
            StatsBenchmarksUtil.DOUBLE_SUM_MEASURES[0],
            Aggregation.Sum.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
        manager.registerView(StatsBenchmarksUtil.createView(
            "LS" + i,
            StatsBenchmarksUtil.LONG_SUM_MEASURES[0],
            Aggregation.Sum.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
        // distribution
        manager.registerView(StatsBenchmarksUtil.createView(
            "DD" + i,
            StatsBenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0],
            StatsBenchmarksUtil.DISTRIBUTION,
            TagsBenchmarksUtil.TAG_KEYS[i]));
        manager.registerView(StatsBenchmarksUtil.createView(
            "LD" + i,
            StatsBenchmarksUtil.LONG_DISTRIBUTION_MEASURES[0],
            StatsBenchmarksUtil.DISTRIBUTION,
            TagsBenchmarksUtil.TAG_KEYS[i]));
        // last value
        manager.registerView(StatsBenchmarksUtil.createView(
            "DL" + i,
            StatsBenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[0],
            Aggregation.LastValue.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
        manager.registerView(StatsBenchmarksUtil.createView(
            "LL" + i,
            StatsBenchmarksUtil.LONG_LASTVALUE_MEASURES[0],
            Aggregation.LastValue.create(),
            TagsBenchmarksUtil.TAG_KEYS[i]));
      }
    }

    // creates tag context with n tags mapping "keyN" -> "value0"
    private TagContext createContext(int size) {
      TagContextBuilder builder = tagger.emptyBuilder();
      for (int i = 0; i < size; i++) {
        builder.put(TagsBenchmarksUtil.TAG_KEYS[i], TagsBenchmarksUtil.TAG_VALUES[0]);
      }
      return builder.build();
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
    map.put(measure, value).record(data.tagContext);
    return map;
  }

  private static MeasureMap record(Data data, Measure.MeasureDouble measure, double value) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(measure, value).record(data.tagContext);
    return map;
  }
}
