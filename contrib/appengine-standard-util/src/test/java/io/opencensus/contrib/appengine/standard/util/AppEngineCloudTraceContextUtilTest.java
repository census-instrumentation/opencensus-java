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

package io.opencensus.contrib.appengine.standard.util;

import static com.google.common.truth.Truth.assertThat;

import com.google.apphosting.api.CloudTraceContext;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link AppEngineCloudTraceContextUtilTest}. */
@RunWith(JUnit4.class)
public class AppEngineCloudTraceContextUtilTest {

  @Test
  public void toFromSpanContext() {
    CloudTraceContext cloudTraceContext =
        new CloudTraceContext(
            // Protobuf-encoded upper and lower 64 bits of the example trace ID
            // fae1c6346b9cf9a272cb6504b5a10dcc/123456789.
            new byte[] {
              (byte) 0x09,
              (byte) 0xa2,
              (byte) 0xf9,
              (byte) 0x9c,
              (byte) 0x6b,
              (byte) 0x34,
              (byte) 0xc6,
              (byte) 0xe1,
              (byte) 0xfa,
              (byte) 0x11,
              (byte) 0xcc,
              (byte) 0x0d,
              (byte) 0xa1,
              (byte) 0xb5,
              (byte) 0x04,
              (byte) 0x65,
              (byte) 0xcb,
              (byte) 0x72
            },
            Long.MIN_VALUE,
            // Trace enabled.
            1L);

    SpanContext spanContext = AppEngineCloudTraceContextUtil.toSpanContext(cloudTraceContext);

    assertThat(spanContext)
        .isEqualTo(
            SpanContext.create(
                TraceId.fromLowerBase16("fae1c6346b9cf9a272cb6504b5a10dcc"),
                SpanId.fromLowerBase16("8000000000000000"),
                TraceOptions.builder().setIsSampled(true).build()));

    // CloudTraceContext does not implement equals, so need to check every argument.
    CloudTraceContext newCloudTraceContext =
        AppEngineCloudTraceContextUtil.toCloudTraceContext(spanContext);
    assertThat(newCloudTraceContext.getTraceId()).isEqualTo(cloudTraceContext.getTraceId());
    assertThat(newCloudTraceContext.getSpanId()).isEqualTo(cloudTraceContext.getSpanId());
    assertThat(newCloudTraceContext.getTraceMask()).isEqualTo(cloudTraceContext.getTraceMask());
  }
}
