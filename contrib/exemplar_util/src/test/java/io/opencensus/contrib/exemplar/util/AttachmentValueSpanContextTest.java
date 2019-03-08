/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.exemplar.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AttachmentValueSpanContext}. */
@RunWith(JUnit4.class)
public class AttachmentValueSpanContextTest {

  private static final Random RANDOM = new Random(1234);
  private static final TraceId TRACE_ID = TraceId.generateRandomId(RANDOM);
  private static final SpanId SPAN_ID = SpanId.generateRandomId(RANDOM);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getValue() {
    AttachmentValueSpanContext attachmentValue = AttachmentValueSpanContext.create(SPAN_CONTEXT);
    assertThat(attachmentValue.getValue()).isEqualTo(SPAN_CONTEXT.toString());
  }

  @Test
  public void getSpanContext() {
    AttachmentValueSpanContext attachmentValue =
        AttachmentValueSpanContext.create(SpanContext.INVALID);
    assertThat(attachmentValue.getSpanContext()).isEqualTo(SpanContext.INVALID);
  }

  @Test
  public void preventNullSpanContext() {
    thrown.expect(NullPointerException.class);
    AttachmentValueSpanContext.create(null);
  }
}
