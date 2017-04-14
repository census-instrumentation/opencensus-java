package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceComponent}. */
@RunWith(JUnit4.class)
public class TraceComponentTest {
  @Test
  public void defaultTracer() {
    assertThat(TraceComponent.getNoopTraceComponent().getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void defaultBinaryPropagationHandler() {
    assertThat(TraceComponent.getNoopTraceComponent().getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandler.getNoopBinaryPropagationHandler());
  }
}
