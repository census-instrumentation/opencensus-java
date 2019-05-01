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

package io.opencensus.benchmarks.trace;

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
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

/** Benchmarks for {@link Span}-related trace events. */
@State(Scope.Benchmark)
public class SpanOperationsBenchmark {
  private static final String SPAN_NAME = "SpanName";
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE = "MyAttributeValue";
  private static final long MESSAGE_ID = 1042;
  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();

  @State(Scope.Benchmark)
  public static class Data {
    private Span attributeSpan;
    private Span annotationSpanEmpty;
    private Span annotationSpanAttributes;
    private Span annotationSpanAnnotation;
    private Span messageEventSpan;
    private Span linkSpan;
    private Tracer tracer;
    private AttributeValue[] attributeValues;
    private String[] attributeKeys;
    private Map<String, AttributeValue> attributeMap;
    private MessageEvent[] messageEvents;
    private Link[] links;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"true", "false"})
    boolean recorded;

    @Param({"true", "false"})
    boolean sampled;

    @Param({"0", "1", "4", "8", "16"})
    // @Param({"0", "1", "16"})
    int size;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      attributeSpan = createSpan("Attribute");
      annotationSpanEmpty = createSpan("AnnotationSpanEmpty");
      annotationSpanAttributes = createSpan("AnnotationSpanAttributes");
      annotationSpanAnnotation = createSpan("AnnotationSpanAnnotation");
      messageEventSpan = createSpan("MessageEventSpan");
      linkSpan = createSpan("LinkSpan");
      initAttributes();
    }

    @TearDown
    public void doTearDown() {
      attributeSpan.end();
      annotationSpanEmpty.end();
      annotationSpanAttributes.end();
      annotationSpanAnnotation.end();
      messageEventSpan.end();
      linkSpan.end();
    }

    private Span createSpan(String suffix) {
      return tracer
          .spanBuilderWithExplicitParent(SPAN_NAME + suffix, null)
          .setRecordEvents(recorded)
          .setSampler(sampled ? Samplers.alwaysSample() : Samplers.neverSample())
          .startSpan();
    }

    private void initAttributes() {
      attributeValues = createAttributeValues(size);
      attributeKeys = new String[size];
      attributeMap = new HashMap<>(size);
      messageEvents = new MessageEvent[size];
      links = new Link[size];
      for (int i = 0; i < size; i++) {
        attributeKeys[i] = ATTRIBUTE_KEY + "-i";
        attributeMap.put(attributeKeys[i], attributeValues[i]);
        messageEvents[i] = MessageEvent.builder(MessageEvent.Type.SENT, MESSAGE_ID + i).build();
        links[i] =
            Link.fromSpanContext(
                SpanContext.create(
                    TraceId.fromBytes(
                        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, (byte) i}),
                    SpanId.fromBytes(new byte[] {1, 2, 3, 4, 5, 6, 7, (byte) i}),
                    TraceOptions.DEFAULT,
                    TRACESTATE_DEFAULT),
                Link.Type.PARENT_LINKED_SPAN);
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

  /** Add an annotation as description only. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotationEmpty(Data data) {
    Span span = data.annotationSpanEmpty;
    span.addAnnotation(ANNOTATION_DESCRIPTION);
    return span;
  }

  /** Add an annotation with attributes. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addAnnotationWithAttributes(Data data) {
    Span span = data.annotationSpanAttributes;
    span.addAnnotation(ANNOTATION_DESCRIPTION, data.attributeMap);
    return span;
  }

  /** Add an annotation with an annotation. */
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

  /** Add message events. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Span addMessageEvent(Data data) {
    Span span = data.messageEventSpan;
    for (int i = 0; i < data.size; i++) {
      span.addMessageEvent(data.messageEvents[i]);
    }
    return span;
  }

  /** Add links. */
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

  private static AttributeValue[] createAttributeValues(int size) {
    AttributeValue[] attributeValues = new AttributeValue[size];
    for (int i = 0; i < size; i++) {
      attributeValues[i] = AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE + "-" + i);
    }
    return attributeValues;
  }
}
