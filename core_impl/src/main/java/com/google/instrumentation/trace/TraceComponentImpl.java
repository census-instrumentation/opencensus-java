package com.google.instrumentation.trace;

/** Implementation of the {@link TraceComponent}. */
public final class TraceComponentImpl extends TraceComponent {
  public TraceComponentImpl() {
    super(Tracer.getNoopTracer(), BinaryPropagationHandlerImpl.INSTANCE);
  }
}
