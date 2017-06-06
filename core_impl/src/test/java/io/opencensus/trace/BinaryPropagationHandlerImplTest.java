/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.text.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BinaryPropagationHandlerImpl}. */
@RunWith(JUnit4.class)
public class BinaryPropagationHandlerImplTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {97, 98, 99, 100, 101, 102, 103, 104};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final byte[] TRACE_OPTIONS_BYTES = new byte[] {1};
  private static final TraceOptions TRACE_OPTIONS = TraceOptions.fromBytes(TRACE_OPTIONS_BYTES);
  private static final byte[] EXAMPLE_BYTES =
      new byte[] {
        0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
        101, 102, 103, 104, 2, 1
      };
  private static final SpanContext EXAMPLE_SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS);
  @Rule public ExpectedException expectedException = ExpectedException.none();
  private final BinaryPropagationHandler binaryPropagationHandler =
      new BinaryPropagationHandlerImpl();

  private void testSpanContextConversion(SpanContext spanContext) throws ParseException {
    SpanContext propagatedBinarySpanContext =
        binaryPropagationHandler.fromBinaryValue(
            binaryPropagationHandler.toBinaryValue(spanContext));

    assertWithMessage("Binary propagated context is not equal with the initial context.")
        .that(propagatedBinarySpanContext)
        .isEqualTo(spanContext);
  }

  @Test
  public void propagate_SpanContextTracingEnabled() throws ParseException {
    testSpanContextConversion(
        SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.builder().setIsSampled().build()));
  }

  @Test
  public void propagate_SpanContextNoTracing() throws ParseException {
    testSpanContextConversion(SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT));
  }

  @Test(expected = NullPointerException.class)
  public void toBinaryValue_NullSpanContext() {
    binaryPropagationHandler.toBinaryValue(null);
  }

  @Test
  public void toBinaryValue_InvalidSpanContext() {
    assertThat(binaryPropagationHandler.toBinaryValue(SpanContext.INVALID))
        .isEqualTo(
            new byte[] {
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0
            });
  }

  @Test
  public void fromBinaryValue_BinaryExampleValue() throws ParseException {
    assertThat(binaryPropagationHandler.fromBinaryValue(EXAMPLE_BYTES))
        .isEqualTo(EXAMPLE_SPAN_CONTEXT);
  }

  @Test(expected = NullPointerException.class)
  public void fromBinaryValue_NullInput() throws ParseException {
    binaryPropagationHandler.fromBinaryValue(null);
  }

  @Test
  public void fromBinaryValue_EmptyInput() throws ParseException {
    expectedException.expect(ParseException.class);
    expectedException.expectMessage("Unsupported version.");
    binaryPropagationHandler.fromBinaryValue(new byte[0]);
  }

  @Test
  public void fromBinaryValue_UnsupportedVersionId() throws ParseException {
    expectedException.expect(ParseException.class);
    expectedException.expectMessage("Unsupported version.");
    binaryPropagationHandler.fromBinaryValue(
        new byte[] {
          66, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
          102, 103, 104, 1
        });
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdFirst() throws ParseException {
    assertThat(
            binaryPropagationHandler.fromBinaryValue(
                new byte[] {
                  0, 4, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98,
                  99, 100, 101, 102, 103, 104, 2, 1
                }))
        .isEqualTo(SpanContext.create(TraceId.INVALID, SpanId.INVALID, TraceOptions.DEFAULT));
  }

  @Test
  public void fromBinaryValue_UnsupportedFieldIdSecond() throws ParseException {
    assertThat(
            binaryPropagationHandler.fromBinaryValue(
                new byte[] {
                  0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 3, 97, 98,
                  99, 100, 101, 102, 103, 104, 2, 1
                }))
        .isEqualTo(
            SpanContext.create(
                TraceId.fromBytes(
                    new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}),
                SpanId.INVALID,
                TraceOptions.DEFAULT));
  }

  @Test
  public void fromBinaryValue_ShorterTraceId() throws ParseException {
    expectedException.expect(ParseException.class);
    expectedException.expectMessage("Invalid input: java.lang.ArrayIndexOutOfBoundsException");
    binaryPropagationHandler.fromBinaryValue(
        new byte[] {0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76});
  }

  @Test
  public void fromBinaryValue_ShorterSpanId() throws ParseException {
    expectedException.expect(ParseException.class);
    expectedException.expectMessage("Invalid input: java.lang.ArrayIndexOutOfBoundsException");
    binaryPropagationHandler.fromBinaryValue(new byte[] {0, 1, 97, 98, 99, 100, 101, 102, 103});
  }

  @Test
  public void fromBinaryValue_ShorterTraceOptions() throws ParseException {
    expectedException.expect(ParseException.class);
    expectedException.expectMessage("Invalid input: java.lang.IndexOutOfBoundsException");
    binaryPropagationHandler.fromBinaryValue(
        new byte[] {
          0, 0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 1, 97, 98, 99, 100,
          101, 102, 103, 104, 2
        });
  }
}
