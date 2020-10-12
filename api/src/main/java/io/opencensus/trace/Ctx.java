package io.opencensus.trace;

public interface Ctx {
  Ctx attach();
  void detach(Ctx ctx);
}
