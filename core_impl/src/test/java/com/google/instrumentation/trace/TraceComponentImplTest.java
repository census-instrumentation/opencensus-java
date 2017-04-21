package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceComponentImpl}. */
@RunWith(JUnit4.class)
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
