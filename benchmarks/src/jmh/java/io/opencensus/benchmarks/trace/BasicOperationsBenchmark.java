/*
 * Copyright 2017, OpenCensus Authors
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

/*
 * ./gradlew --no-daemon -PjmhIncludeSingleClass=BasicOperationsBenchmark clean :opencensus-benchmarks:jmh
 *
 * Benchmark                                              (implementation)  (recorded)  (sampled)  Mode  Cnt      Score       Error  Units
 * BasicOperationsBenchmark.createRootSpan                            impl        true       true  avgt   10   3953.535 ±  4093.254  ns/op
 * BasicOperationsBenchmark.createRootSpan                            impl        true      false  avgt   10    440.787 ±    12.625  ns/op
 * BasicOperationsBenchmark.createRootSpan                            impl       false       true  avgt   10   2204.395 ±   859.481  ns/op
 * BasicOperationsBenchmark.createRootSpan                            impl       false      false  avgt   10     38.682 ±     1.279  ns/op
 * BasicOperationsBenchmark.createSpanWithCurrentSpan                 impl        true       true  avgt   10   7110.834 ± 21847.359  ns/op
 * BasicOperationsBenchmark.createSpanWithCurrentSpan                 impl        true      false  avgt   10    435.371 ±    12.737  ns/op
 * BasicOperationsBenchmark.createSpanWithCurrentSpan                 impl       false       true  avgt   10   2596.745 ±  2114.754  ns/op
 * BasicOperationsBenchmark.createSpanWithCurrentSpan                 impl       false      false  avgt   10     45.818 ±     1.667  ns/op
 * BasicOperationsBenchmark.createSpanWithExplicitParent              impl        true       true  avgt   10  10381.251 ± 37198.355  ns/op
 * BasicOperationsBenchmark.createSpanWithExplicitParent              impl        true      false  avgt   10    443.697 ±    14.675  ns/op
 * BasicOperationsBenchmark.createSpanWithExplicitParent              impl       false       true  avgt   10   6376.731 ± 18736.403  ns/op
 * BasicOperationsBenchmark.createSpanWithExplicitParent              impl       false      false  avgt   10     40.152 ±     0.810  ns/op
 * BasicOperationsBenchmark.createSpanWithRemoteParent                impl        true       true  avgt   10   2604.721 ±  2778.348  ns/op
 * BasicOperationsBenchmark.createSpanWithRemoteParent                impl        true      false  avgt   10    442.076 ±    10.171  ns/op
 * BasicOperationsBenchmark.createSpanWithRemoteParent                impl       false       true  avgt   10   2262.443 ±  1206.583  ns/op
 * BasicOperationsBenchmark.createSpanWithRemoteParent                impl       false      false  avgt   10     39.317 ±     1.044  ns/op
 */

package io.opencensus.benchmarks.trace;

import static com.google.common.base.Preconditions.checkState;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.samplers.Samplers;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/** Benchmarks for {@link Span} to record trace events. */
@State(Scope.Benchmark)
public class BasicOperationsBenchmark {
  private static final String SPAN_NAME = "MySpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";

  @State(Scope.Benchmark)
  public static class Data {
    private Span span;
    private Tracer tracer;

    //@Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"true", "false"})
    boolean recorded;

    @Param({"true", "false"})
    boolean sampled;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      span = tracer
             .spanBuilderWithExplicitParent("TopLevelSpan", null)
             .setRecordEvents(recorded)
             .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
             .startSpan();
    }

    @TearDown
    public void doTearDown() {
      span.end();
    }
  }

  /** Creates a root span */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createRootSpan(Data data) {
    Span span = data.tracer.spanBuilderWithExplicitParent("RootSpan", null)
                .setRecordEvents(data.recorded)
                .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
                .startSpan();
    span.end();
    return span;
  }

  /** Creates a child span */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithExplicitParent(Data data) {
    Span span = data.tracer.spanBuilderWithExplicitParent("ChildSpan", data.span)
                .setRecordEvents(data.recorded)
                .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
                .startSpan();
    span.end();
    return span;
  }

  /** Creates a child span from the current span */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithCurrentSpan(Data data) {
    Span span = data.tracer.spanBuilder("ChildSpanFromCurrent")
                .setRecordEvents(data.recorded)
                .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
                .startSpan();
    span.end();
    return span;
  }

  /** Creates a child span with a remote parent */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span createSpanWithRemoteParent(Data data) {
    Span span = data.tracer.spanBuilderWithRemoteParent(
                              "ChildSpanFromRemoteParent", data.span.getContext())
                .setRecordEvents(data.recorded)
                .setSampler(data.sampled ? Samplers.alwaysSample() : Samplers.neverSample())
                .startSpan();
    span.end();
    return span;
  }


}
