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
import static io.opencensus.contrib.exemplar.util.ExemplarUtils.ATTACHMENT_KEY_SPAN_ID;
import static io.opencensus.contrib.exemplar.util.ExemplarUtils.ATTACHMENT_KEY_TRACE_ID;

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.MeasureMap;
import io.opencensus.tags.TagContext;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ExemplarUtils}. */
@RunWith(JUnit4.class)
public class ExemplarUtilsTest {

  private static final Random RANDOM = new Random(1234);
  private static final TraceId TRACE_ID = TraceId.generateRandomId(RANDOM);
  private static final SpanId SPAN_ID = SpanId.generateRandomId(RANDOM);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void putSpanContext() {
    FakeMeasureMap measureMap = new FakeMeasureMap();
    ExemplarUtils.putSpanContextAttachments(measureMap, SPAN_CONTEXT);
    assertThat(measureMap.attachments)
        .containsExactly(
            ATTACHMENT_KEY_TRACE_ID,
            TRACE_ID.toLowerBase16(),
            ATTACHMENT_KEY_SPAN_ID,
            SPAN_ID.toLowerBase16());
  }

  @Test
  public void putSpanContext_PreventNullMeasureMap() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measureMap");
    ExemplarUtils.putSpanContextAttachments(null, SPAN_CONTEXT);
  }

  @Test
  public void putSpanContext_PreventNullSpanContext() {
    FakeMeasureMap measureMap = new FakeMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("spanContext");
    ExemplarUtils.putSpanContextAttachments(measureMap, null);
  }

  private static final class FakeMeasureMap extends MeasureMap {

    private final Map<String, String> attachments = new HashMap<String, String>();

    @Override
    public MeasureMap putAttachment(String key, String value) {
      attachments.put(key, value);
      return this;
    }

    @Override
    public MeasureMap put(MeasureDouble measure, double value) {
      return this;
    }

    @Override
    public MeasureMap put(MeasureLong measure, long value) {
      return this;
    }

    @Override
    public void record() {}

    @Override
    public void record(TagContext tags) {}
  }
}
