package io.opencensus.trace;

public interface ContextManager {
  Ctx currentContext();
  Ctx withValue(Ctx ctx, @javax.annotation.Nullable Span span);
  Span getValue(Ctx ctx);
}
