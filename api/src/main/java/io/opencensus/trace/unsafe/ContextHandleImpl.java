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

/** {@code ContextHandle} implementation using {@see io.grpc.Context}. */
class ContextHandleImpl implements ContextHandle {

  private final Context context;

  public ContextHandleImpl(Context context) {
    this.context = context;
  }

  Context getContext() {
    return context;
  }

  @Override
  public ContextHandle attach() {
    return new ContextHandleImpl(context.attach());
  }

  @Override
  public void detach(ContextHandle contextHandle) {
    ContextHandleImpl impl = (ContextHandleImpl) contextHandle;
    context.detach(impl.context);
  }
}
