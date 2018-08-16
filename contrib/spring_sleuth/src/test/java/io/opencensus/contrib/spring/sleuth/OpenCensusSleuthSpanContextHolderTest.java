package io.opencensus.contrib.spring.sleuth;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OpenCensusSleuthSpanContextHolder}. */
@RunWith(JUnit4.class)
public class OpenCensusSleuthSpanContextHolderTest {
  private static final Tracer tracer = Tracing.getTracer();

  @After
  @Before
  public void verifyNotTracing() {
    assertThat(OpenCensusSleuthSpanContextHolder.isTracing()).isFalse();
    assertThat(tracer.getCurrentSpan().getContext().isValid()).isFalse();
  }

  @Test
  public void testFromSleuthSampled() {
    org.springframework.cloud.sleuth.Span sleuthSpan =
        createSleuthSpan(21, 22, 23, /* exportable= */true);
    OpenCensusSleuthSpanContextHolder.setCurrentSpan(sleuthSpan);
    assertThat(OpenCensusSleuthSpanContextHolder.isTracing()).isTrue();
    assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpan);
    assertSpanEquals(tracer.getCurrentSpan(), sleuthSpan);
    assertThat(tracer.getCurrentSpan().getContext().getTraceOptions().isSampled()).isTrue();
    OpenCensusSleuthSpanContextHolder.close();
  }

  @Test
  public void testFromSleuthUnsampled() {
    org.springframework.cloud.sleuth.Span sleuthSpan =
        createSleuthSpan(21, 22, 23, /* exportable= */false);
    OpenCensusSleuthSpanContextHolder.setCurrentSpan(sleuthSpan);
    assertThat(OpenCensusSleuthSpanContextHolder.isTracing()).isTrue();
    assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpan);
    assertSpanEquals(tracer.getCurrentSpan(), sleuthSpan);
    assertThat(tracer.getCurrentSpan().getContext().getTraceOptions().isSampled()).isFalse();
    OpenCensusSleuthSpanContextHolder.close();
  }

  @Test
  public void testSpanStackSimple() {
    org.springframework.cloud.sleuth.Span[] sleuthSpans = createSleuthSpans(4);
    // push all the spans
    for (int i = 0; i < sleuthSpans.length; i++) {
      OpenCensusSleuthSpanContextHolder.push(sleuthSpans[i], /* autoClose= */false);
      assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpans[i]);
      assertSpanEquals(tracer.getCurrentSpan(), sleuthSpans[i]);
    }
    // pop all the spans
    for (int i = sleuthSpans.length - 1; i >= 0; i--) {
      assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpans[i]);
      assertSpanEquals(tracer.getCurrentSpan(), sleuthSpans[i]);
      OpenCensusSleuthSpanContextHolder.close();
    }
   }

  @Test
  public void testSpanStackAutoClose() {
    org.springframework.cloud.sleuth.Span[] sleuthSpans = createSleuthSpans(4);
    // push all the spans
    for (int i = 0; i < sleuthSpans.length; i++) {
      // set autoclose for all the spans except 2
      OpenCensusSleuthSpanContextHolder.push(sleuthSpans[i], /* autoClose= */i != 2);
      assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpans[i]);
      assertSpanEquals(tracer.getCurrentSpan(), sleuthSpans[i]);
    }
    // verify autoClose pops stack to index 2
    OpenCensusSleuthSpanContextHolder.close();
    assertThat(OpenCensusSleuthSpanContextHolder.getCurrentSpan()).isEqualTo(sleuthSpans[2]);
    assertSpanEquals(tracer.getCurrentSpan(), sleuthSpans[2]);
    // verify autoClose closes pops rest of stack
    OpenCensusSleuthSpanContextHolder.close();
  }

  @Test
  public void testSpanStackCloseSpanFunction() {
    final org.springframework.cloud.sleuth.Span[] sleuthSpans = createSleuthSpans(4);
    // push all the spans
    for (int i = 0; i < sleuthSpans.length; i++) {
      OpenCensusSleuthSpanContextHolder.push(sleuthSpans[i], /* autoClose= */false);
    }
    // pop all the spans, verify that given SpanFunction is called on the closed span.
    for (int i = sleuthSpans.length - 1; i >= 0; i--) {
      final int index = i;
      OpenCensusSleuthSpanContextHolder.close(
          new OpenCensusSleuthSpanContextHolder.SpanFunction() {
            @Override public void apply(org.springframework.cloud.sleuth.Span span) {
              assertThat(span).isEqualTo(sleuthSpans[index]);
            }
          });
    }
  }

  org.springframework.cloud.sleuth.Span[] createSleuthSpans(int len) {
    org.springframework.cloud.sleuth.Span[] spans = new org.springframework.cloud.sleuth.Span[len];
    for (int i = 0; i < len; i++) {
      spans[i] = createSleuthSpan(i * 10 + 1, i * 10 + 2, i * 10 + 3, /* exportable= */true);
    }
    return spans;
  }

  private static org.springframework.cloud.sleuth.Span createSleuthSpan(
      long tIdHi, long tIdLo, long sId, boolean exportable) {
    return org.springframework.cloud.sleuth.Span.builder()
        .name("name").traceIdHigh(tIdHi).traceId(tIdLo).spanId(sId).exportable(exportable).build();
  }

  private static void assertSpanEquals(
      Span span, org.springframework.cloud.sleuth.Span sleuthSpan) {
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
