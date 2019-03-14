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
    int numValues;

    @Param({"impl", "impl-lite"})
    String implementation;

    private StatsRecorder recorder;
    private ViewManager manager;
    private Tagger tagger;
    private List<TagContext> contexts;

    @Setup
    public void setup() throws Exception {
      manager = BenchmarksUtil.getViewManager(implementation);
      recorder = BenchmarksUtil.getStatsRecorder(implementation);
      tagger = BenchmarksUtil.getTagger(implementation);
      tags = createTags(1);
      manager.registerView(BenchmarksUtil.DOUBLE_COUNT_VIEWS[0]);
      manager.registerView(BenchmarksUtil.LONG_COUNT_VIEWS[0]);
      manager.registerView(BenchmarksUtil.DOUBLE_SUM_VIEWS[0]);
      manager.registerView(BenchmarksUtil.LONG_SUM_VIEWS[0]);
      manager.registerView(BenchmarksUtil.DOUBLE_DISTRIBUTION_VIEWS[0]);
      manager.registerView(BenchmarksUtil.LONG_DISTRIBUTION_VIEWS[0]);
      manager.registerView(BenchmarksUtil.DOUBLE_LASTVALUE_VIEWS[0]);
      manager.registerView(BenchmarksUtil.LONG_LASTVALUE_VIEWS[0]);
    }

    private List<TagContext> createTags(int numTags) {
	TagContext[] contexts = new TagContext[numValues];
	for (int i = 0; i < numTags; i++) {
	    contexts[i] =   tagger.emptyBuilder().put(BenchmarksUtil.TAG_KEYS[0], BenchmarksUtil.TAG_VALUES[i]).build();
	}
	return Arrays.asList(contexts);
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.DOUBLE_COUNT_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongCount(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.LONG_COUNT_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.DOUBLE_SUM_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongSum(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.LONG_SUM_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongDistribution(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.DOUBLE_DISTRIBUTION_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedDoubleLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.DOUBLE_LASTVALUE_MEASURES[0], (double) 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public MeasureMap timeRecordBatchedLongLastValue(Data data) {
    MeasureMap map = data.recorder.newMeasureMap();
    map.put(BenchmarksUtil.LONG_LASTVALUE_MEASURES[0], 11);
    for (TagContext tags : data.contexts) {
	map.record(tags);
    }
    return map;
  }
}
