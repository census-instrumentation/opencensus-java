package io.opencensus.spring.sleuth;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.cloud.sleuth.Span;

/** Unit tests for {@link OpenCensusSleuthSpan}. */
@RunWith(JUnit4.class)
public class OpenCensusSleuthSpanTest {
  @Test
  public void testFromSleuthSampled() {
    Span sleuthSpan = Span.builder()
                      .name("name")
                      .traceIdHigh(12L)
                      .traceId(22L)
                      .spanId(23L)
                      .exportable(true)
                      .build();
    assertSpanEquals(new OpenCensusSleuthSpan(sleuthSpan), sleuthSpan);
  }

  @Test
  public void testFromSleuthNotSampled() {
    Span sleuthSpan = Span.builder()
                      .name("name")
                      .traceIdHigh(12L)
                      .traceId(22L)
                      .spanId(23L)
                      .exportable(false)
                      .build();
    assertSpanEquals(new OpenCensusSleuthSpan(sleuthSpan), sleuthSpan);
  }

  private static final void assertSpanEquals(io.opencensus.trace.Span span, Span sleuthSpan) {
    assertThat(span.getContext().isValid()).isTrue();
    assertThat(Long.parseLong(span.getContext().getTraceId().toLowerBase16().substring(0, 16), 16))
        .isEqualTo(sleuthSpan.getTraceIdHigh());
    assertThat(Long.parseLong(span.getContext().getTraceId().toLowerBase16().substring(16, 32), 16))
        .isEqualTo(sleuthSpan.getTraceId());
    assertThat(Long.parseLong(span.getContext().getSpanId().toLowerBase16(), 16))
        .isEqualTo(sleuthSpan.getSpanId());
    assertThat(span.getContext().getTraceOptions().isSampled())
        .isEqualTo(sleuthSpan.isExportable());
  }

}
