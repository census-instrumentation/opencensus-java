package io.opencensus.trace.unsafe;

import io.grpc.Context;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Ctx;
import io.opencensus.trace.Span;
import javax.annotation.Nullable;

/**
 * Default {@code ContextManager} implementation using io.grpc.Context
 */
public class ContextManagerImpl implements ContextManager {

  @Override
  public Ctx currentContext() {
    return wrapContext(Context.current());
  }

  @Override
  public Ctx withValue(Ctx ctx, @Nullable Span span) {
    return wrapContext(ContextUtils.withValue(unwrapContext(ctx), span));
  }

  @Override
  public Span getValue(Ctx ctx) {
    return ContextUtils.getValue(unwrapContext(ctx));
  }

  private static Ctx wrapContext(Context context) {
    return new CtxImpl(context);
  }

  private static Context unwrapContext(Ctx ctx) {
    return ((CtxImpl) ctx).getContext();
  }

  protected ContextManagerImpl() {
  }
}
