package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceService}. */
@RunWith(JUnit4.class)
public class TraceServiceTest {
  @Test
  public void defaultTracer() {
    assertThat(TraceService.getNoopTraceService().getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void defaultBinaryPropagationHandler() {
    assertThat(TraceService.getNoopTraceService().getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandler.getNoopBinaryPropagationHandler());
  }
}
