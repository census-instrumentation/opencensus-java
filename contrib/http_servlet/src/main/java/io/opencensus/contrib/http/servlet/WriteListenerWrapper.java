/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.contrib.http.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import io.grpc.Context;
import io.opencensus.common.ExperimentalApi;
import java.io.IOException;
import javax.servlet.WriteListener;

/**
 * This class is a wrapper class for {@link WriteListener}. It facilitates executing asynchronous
 * onWritePossible method in the original context at the time of creating the listener.
 *
 * @since 0.25.0
 */
@ExperimentalApi
public class WriteListenerWrapper implements WriteListener {
  private final io.grpc.Context context;
  private final WriteListener writeListener;

  /**
   * Creates an instance of {@code WriteListenerWrapper}. It saves current {@link Context} at the
   * time of creation.
   *
   * @param writeListener {@link WriteListener} object being wrapped.
   * @since 0.25.0
   */
  public WriteListenerWrapper(WriteListener writeListener) {
    checkNotNull(writeListener, "WriteListener is null");
    context = io.grpc.Context.current();
    this.writeListener = writeListener;
  }

  /**
   * It executes onWritePossible() method of the object being wrapped in the saved context. It saves
   * current context before executing the method and restores it after it is finished executing.
   *
   * @since 0.25.0
   */
  @Override
  public void onWritePossible() throws IOException {
    Context previousContext = context.attach();
    try {
      writeListener.onWritePossible();
    } finally {
      context.detach(previousContext);
    }
  }

  @Override
  public void onError(final Throwable t) {
    writeListener.onError(t);
  }
}
