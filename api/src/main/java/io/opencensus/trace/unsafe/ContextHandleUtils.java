/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace.unsafe;

import io.grpc.Context;
import io.opencensus.internal.Provider;
import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Span;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class ContextHandleUtils {

  // No instance of this class.
  private ContextHandleUtils() {}

  private static final Logger LOGGER = Logger.getLogger(ContextHandleUtils.class.getName());
  private static final ContextManager CONTEXT_MANAGER =
      loadContextManager(ContextManager.class.getClassLoader());

  private static ContextManager loadContextManager(@Nullable ClassLoader classLoader) {
    try {
      return Provider.createInstance(
          Class.forName(
              "io.opentelemetry.opencensusshim.OpenTelemetryContextManager",
              /*initialize=*/ true,
              classLoader),
          ContextManager.class);
    } catch (ClassNotFoundException e) {
      LOGGER.log(
          Level.FINE,
          "Couldn't load full implementation for OpenTelemetry context manager, now loading "
              + "original implementation.",
          e);
    }
    return new ContextManagerImpl();
  }

  public static ContextHandle currentContext() {
    return CONTEXT_MANAGER.currentContext();
  }

  /**
   * Creates a new {@code ContextHandle} with the given value set.
   *
   * @param context the parent {@code ContextHandle}.
   * @param span the value to be set.
   * @return a new context with the given value set.
   */
  public static ContextHandle withValue(
      ContextHandle context, @javax.annotation.Nullable Span span) {
    return CONTEXT_MANAGER.withValue(context, span);
  }

  /**
   * Returns the value from the specified {@code ContextHandle}.
   *
   * @param context the specified {@code ContextHandle}.
   * @return the value from the specified {@code ContextHandle}.
   */
  public static Span getValue(ContextHandle context) {
    return CONTEXT_MANAGER.getValue(context);
  }

  /**
   * Attempts to pull the {@see io.grpc.Context} out of an OpenCensus {@code ContextHandle}.
   *
   * @return The context, or null if not a GRPC backed context handle.
   */
  public static Context tryExtractGrpcContext(ContextHandle handle) {
    if (handle instanceof ContextHandleImpl) {
      return ((ContextHandleImpl) handle).getContext();
    }
    // TODO: see if we can do something for the OpenTelemetry shim.
    return null;
  }
}
