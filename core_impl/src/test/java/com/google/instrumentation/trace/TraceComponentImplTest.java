package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/** Unit tests for {@link TraceComponentImpl}. */
public class TraceComponentImplTest {
  @Test
  public void implementationOfTracer() {
    // TODO(bdrutu): Change this when TracerImpl is available.
    assertThat(Tracing.getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void implementationOfBinaryPropagationHandler() {
    assertThat(Tracing.getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandlerImpl.INSTANCE);
  }
}
