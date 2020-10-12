package io.opencensus.trace.unsafe;

import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Ctx;
import io.opencensus.trace.Span;

public class CtxUtils {
  // No instance of this class.
  private CtxUtils() {}

  private static final ContextManager DEFAULT_CONTEXT_MANAGER = new ContextManagerImpl();
  private static ContextManager contextManager = DEFAULT_CONTEXT_MANAGER;

  /**
   * Overrides context manager with a custom implementation
   * @param cm custom {@code ContextManager} to be used instead of a default one.
   */
  public static void setContextManager(ContextManager cm) {
    contextManager = cm;
  }

  public static Ctx currentContext() {
    return contextManager.currentContext();
  }

  /**
   * Creates a new {@code Ctx} with the given value set.
   *
   * @param context the parent {@code Ctx}.
   * @param span the value to be set.
   * @return a new context with the given value set.
   */
  public static Ctx withValue(Ctx context, @javax.annotation.Nullable Span span) {
    return contextManager.withValue(context, span);
  }

  /**
   * Returns the value from the specified {@code Ctx}.
   *
   * @param context the specified {@code Ctx}.
   * @return the value from the specified {@code Ctx}.
   */
  public static Span getValue(Ctx context) {
    return contextManager.getValue(context);
  }
}
