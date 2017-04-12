package com.google.instrumentation.trace;

/** Implementation of the {@link TraceService}. */
public final class TraceServiceImpl extends TraceService {
  public TraceServiceImpl() {
    super(Tracer.getNoopTracer(), BinaryPropagationHandlerImpl.INSTANCE);
  }
}
