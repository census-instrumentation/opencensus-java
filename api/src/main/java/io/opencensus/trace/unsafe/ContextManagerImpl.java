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
import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Span;
import javax.annotation.Nullable;

/** Default {@code ContextManager} implementation using io.grpc.Context */
public class ContextManagerImpl implements ContextManager {

  @Override
  public ContextHandle currentContext() {
    return wrapContext(Context.current());
  }

  @Override
  @SuppressWarnings({"deprecation"})
  public ContextHandle withValue(ContextHandle contextHandle, @Nullable Span span) {
    return wrapContext(ContextUtils.withValue(unwrapContext(contextHandle), span));
  }

  @Override
  @SuppressWarnings({"deprecation"})
  public Span getValue(ContextHandle contextHandle) {
    return ContextUtils.getValue(unwrapContext(contextHandle));
  }

  private static ContextHandle wrapContext(Context context) {
    return new ContextHandleImpl(context);
  }

  private static Context unwrapContext(ContextHandle contextHandle) {
    return ((ContextHandleImpl) contextHandle).getContext();
  }

  protected ContextManagerImpl() {}
}
