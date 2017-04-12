/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

/**
 * Class that holds the implementation instances for {@link Tracer} and {@link
 * BinaryPropagationHandler}.
 *
 * <p>This implementation of this class is loaded using reflection by the {@link Trace}.
 */
public abstract class TraceService {
  private static final NoopTraceService noopTraceService = new NoopTraceService();
  private final Tracer tracer;
  private final BinaryPropagationHandler binaryPropagationHandler;

  /**
   * Returns the {@link Tracer} with the provided implementations. If no implementation is provided
   * then no-op implementations will be used.
   *
   * @return the {@code Tracer} implementation.
   */
  public Tracer getTracer() {
    return tracer;
  }

  /**
   * Returns the {@link BinaryPropagationHandler} with the provided implementations. If no
   * implementation is provided then no-op implementation will be used.
   *
   * @return the {@code BinaryPropagationHandler} implementation.
   */
  public BinaryPropagationHandler getBinaryPropagationHandler() {
    return binaryPropagationHandler;
  }

  // Disallow external overrides until we define the final API.
  TraceService(Tracer tracer, BinaryPropagationHandler binaryPropagationHandler) {
    this.tracer = tracer;
    this.binaryPropagationHandler = binaryPropagationHandler;
  }

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   */
  static TraceService getNoopTraceService() {
    return noopTraceService;
  }

  private static final class NoopTraceService extends TraceService {
    private NoopTraceService() {
      super(Tracer.getNoopTracer(), BinaryPropagationHandler.getNoopBinaryPropagationHandler());
    }
  }
}
