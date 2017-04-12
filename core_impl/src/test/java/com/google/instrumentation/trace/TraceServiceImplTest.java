package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/** Unit tests for {@link TraceServiceImpl}. */
public class TraceServiceImplTest {
  @Test
  public void implementationOfTracer() {
    // TODO(bdrutu): Change this when TracerImpl is available.
    assertThat(Trace.getTracer()).isSameAs(Tracer.getNoopTracer());
  }

  @Test
  public void implementationOfBinaryPropagationHandler() {
    assertThat(Trace.getBinaryPropagationHandler())
        .isSameAs(BinaryPropagationHandlerImpl.INSTANCE);
  }
}
