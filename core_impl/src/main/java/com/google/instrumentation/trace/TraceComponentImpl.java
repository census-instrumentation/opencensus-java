package com.google.instrumentation.trace;

/** Implementation of the {@link TraceComponent}. */
public final class TraceComponentImpl extends TraceComponent {
  private static final Tracer tracer = Tracer.getNoopTracer();
  private static final BinaryPropagationHandler binaryPropagationHandler =
      BinaryPropagationHandlerImpl.INSTANCE;

  @Override
  public Tracer getTracer() {
    return tracer;
  }

  @Override
  public BinaryPropagationHandler getBinaryPropagationHandler() {
    return binaryPropagationHandler;
  }

  public TraceComponentImpl() {}
}
