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
 * ./gradlew --no-daemon -PjmhIncludeSingleClass=BasicDataBenchmark clean :opencensus-benchmarks:jmh
 */

package io.opencensus.benchmarks.trace;

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Tracer;
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
public class BasicDataBenchmark {
  private static final String ANNOTATION_DESCRIPTION = "MyAnnotation";
  private static final String ATTRIBUTE_KEY = "MyAttributeKey";
  private static final String ATTRIBUTE_VALUE_STRING = "MyAttributeValue";
  private static final long ATTRIBUTE_VALUE_LONG = 90215;

  @State(Scope.Benchmark)
  public static class Data {
    private Tracer tracer;
    private AttributeValue[] attributeValues;
    private String[] attributeKeys;
    Map<String, AttributeValue> attributeMap;

    // @Param({"impl", "impl-lite"})
    @Param({"impl"})
    String implementation;

    @Param({"0", "1", "4", "8", "16"})
    // @Param({"0", "1", "16"})
    int size;

    @Param({"string", "boolean", "long"})
    String attributeType;

    @Setup
    public void setup() {
      tracer = BenchmarksUtil.getTracer(implementation);
      attributeValues = getAttributeValues(size, attributeType);
      attributeKeys = new String[size];
      attributeMap = new HashMap<>(size);
      for (int i = 0; i < size; i++) {
        attributeKeys[i] = ATTRIBUTE_KEY + "-i";
        attributeMap.put(attributeKeys[i], attributeValues[i]);
      }
    }

    @TearDown
    public void doTearDown() {}
  }

  /** Create attribute values. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public AttributeValue[] createAttributeValues(Data data) {
    return getAttributeValues(data.size, data.attributeType);
  }

  /** Create an AttributeMap. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Map<String, AttributeValue> createAttributeMap(Data data) {
    Map<String, AttributeValue> attributeMap = new HashMap<>(data.size);
    for (int i = 0; i < data.size; i++) {
      attributeMap.put(data.attributeKeys[i], data.attributeValues[i]);
    }
    return attributeMap;
  }

  /** Create an Annotation. */
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Annotation createAnnotation(Data data) {
    return Annotation.fromDescriptionAndAttributes(ANNOTATION_DESCRIPTION, data.attributeMap);
  }

  private static AttributeValue[] getAttributeValues(int size, String attributeType) {
    AttributeValue[] attributeValues = new AttributeValue[size];
    switch (attributeType) {
      case "string":
        for (int i = 0; i < size; i++) {
          attributeValues[i] = AttributeValue.stringAttributeValue(ATTRIBUTE_VALUE_STRING + "-i");
        }
        break;
      case "boolean":
        for (int i = 0; i < size; i++) {
          attributeValues[i] = AttributeValue.booleanAttributeValue(i % 3 == 0);
        }
        break;
      case "long":
        for (int i = 0; i < size; i++) {
          attributeValues[i] = AttributeValue.longAttributeValue(ATTRIBUTE_VALUE_LONG + i);
        }
        break;
    }
    return attributeValues;
  }
}
