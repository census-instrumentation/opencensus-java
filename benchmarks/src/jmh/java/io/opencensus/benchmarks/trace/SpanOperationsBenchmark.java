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

/*
 * ./gradlew --no-daemon -PjmhIncludeSingleClass=SpanOperationsBenchmark clean :opencensus-benchmarks:jmh
 */

package io.opencensus.benchmarks.trace;

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.samplers.Samplers;
import java.util.HashMap;
import java.util.Map;
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
public class SpanOperationsBenchmark {
  private static final String SPAN_NAME = "SpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";
  private static final long NETWORK_MESSAGE_ID = 1042;
  private static final Status STATUS_OK = Status.OK;

  @State(Scope.Benchmark)
  public static class Data {
    private Span attributeSpan;
    private Span annotationSpanEmpty;
    private Span annotationSpanAttributes;
    private Span annotationSpanAnnotation;
    private Span networkEventSpan;
    private Span linkSpan;
    private Tracer tracer;
    private AttributeValue[] attributeValues;
    private String[] attributeKeys;
    private Map<String, AttributeValue> attributeMap;
    private NetworkEvent[] networkEvents;
    private Link[] links;
    private Span[] setSpans;
    private Span[] endSpans;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"true", "false"})
    boolean recorded;

    @Param({"true", "false"})
    boolean sampled;

    // @Param({"0", "1", "4", "8", "16"})
    @Param({"0", "1", "16"})
    int size;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      attributeSpan = createSpan("Attribute");
      annotationSpanEmpty = createSpan("AnnotaionSpanEmpty");
      annotationSpanAttributes = createSpan("AnnotaionSpanAttributes");
      annotationSpanAnnotation = createSpan("AnnotaionSpanAnnotation");
      networkEventSpan = createSpan("NetworkEventSpan");
      linkSpan = createSpan("LinkSpan");
      initAttributes();
    }

    @TearDown
    public void doTearDown() {
      attributeSpan.end();
      annotationSpanEmpty.end();
      annotationSpanAttributes.end();
      annotationSpanAnnotation.end();
      networkEventSpan.end();
      linkSpan.end();
      for (int i = 0; i < size; i++) {
        setSpans[i].end();
      }
    }

    private Span createSpan(String suffix) {
      return tracer
          .spanBuilderWithExplicitParent(SPAN_NAME + suffix, null)
          .setRecordEvents(recorded)
          .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
          .startSpan();
    }

    private void initAttributes() {
      attributeValues = getAttributeValues(size);
      attributeKeys = new String[size];
      attributeMap = new HashMap<>(size);
      networkEvents = new NetworkEvent[size];
      links = new Link[size];
      setSpans = new Span[size];
      endSpans = new Span[size];
      for (int i = 0; i < size; i++) {
        attributeKeys[i] = ATTRIBUTE_KEY + "-i";
        attributeMap.put(attributeKeys[i], attributeValues[i]);
        networkEvents[i] =
            NetworkEvent.builder(NetworkEvent.Type.SENT, NETWORK_MESSAGE_ID + i).build();
        links[i] =
            Link.fromSpanContext(
                SpanContext.create(
                    TraceId.fromBytes(
                        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, (byte) i}),
                    SpanId.fromBytes(new byte[] {1, 2, 3, 4, 5, 6, 7, (byte) i}),
                    TraceOptions.DEFAULT),
                Link.Type.PARENT_LINKED_SPAN);
        setSpans[i] = createSpan("SetSpan-" + i);
        endSpans[i] = createSpan("EndSpan-" + i);
      }
    }
  }

  /** Add attributes individually. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span putAttribute(Data data) {
    Span span = data.attributeSpan;
    for (int i = 0; i < data.size; i++) {
      span.putAttribute(data.attributeKeys[i], data.attributeValues[i]);
    }
    return span;
  }

  /** Add attributes as a map. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span putAttributes(Data data) {
    Span span = data.attributeSpan;
    span.putAttributes(data.attributeMap);
    return span;
  }

  /** Add an annotation as description only */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotationEmpty(Data data) {
    Span span = data.annotationSpanEmpty;
    span.addAnnotation(ANNOTATION_DESCRIPTION);
    return span;
  }

  /** Add an annotation with attributes */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotationWithAttributes(Data data) {
    Span span = data.annotationSpanAttributes;
    span.addAnnotation(ANNOTATION_DESCRIPTION, data.attributeMap);
    return span;
  }

  /** Add an annotation with an annotation */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotationWithAnnotation(Data data) {
    Span span = data.annotationSpanAnnotation;
    Annotation annotation =
        Annotation.fromDescriptionAndAttributes(ANNOTATION_DESCRIPTION, data.attributeMap);
    span.addAnnotation(annotation);
    return span;
  }

  /** Add network events */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addNetworkEvent(Data data) {
    Span span = data.networkEventSpan;
    for (int i = 0; i < data.size; i++) {
      span.addNetworkEvent(data.networkEvents[i]);
    }
    return span;
  }

  /** Add links */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addLink(Data data) {
    Span span = data.linkSpan;
    for (int i = 0; i < data.size; i++) {
      span.addLink(data.links[i]);
    }
    return span;
  }

  /** Set status on spans */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span[] setStatusOnSpans(Data data) {
    for (int i = 0; i < data.size; i++) {
      data.setSpans[i].setStatus(STATUS_OK);
    }
    return data.setSpans;
  }

  /** End spans */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span[] endSpan(Data data) {
    for (int i = 0; i < data.size; i++) {
      data.endSpans[i].end();
    }
    return data.endSpans;
  }

  private static AttributeValue[] getAttributeValues(int size) {
    AttributeValue[] attributeValues = new AttributeValue[size];
    for (int i = 0; i < size; i++) {
      attributeValues[i] = AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE + "-i");
    }
    return attributeValues;
  }
}
