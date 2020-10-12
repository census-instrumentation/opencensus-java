package io.opencensus.trace.unsafe;

import io.grpc.Context;
import io.opencensus.trace.Ctx;

/**
 * {@code Ctx} implementation using {@see io.grpc.Context}
 */
class CtxImpl implements Ctx {
  private final Context context;

  public CtxImpl(Context context) {
    this.context = context;
  }

  Context getContext() {
    return context;
  }

  @Override
  public Ctx attach() {
    return new CtxImpl(context.attach());
  }

  @Override
  public void detach(Ctx ctx) {
    CtxImpl impl = (CtxImpl) ctx;
    context.detach(impl.context);
  }
}
