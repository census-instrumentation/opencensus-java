/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.trace.propagation;

/**
 * Container class for all the supported propagation formats. Currently supports only Binary format
 * see {@link BinaryFormat} but more formats will be added.
 */
public abstract class PropagationComponent {
  private static final PropagationComponent NOOP_PROPAGATION_COMPONENT =
      new NoopPropagationComponent();

  /**
   * Returns the {@link BinaryFormat} with the provided implementations. If no implementation is
   * provided then no-op implementation will be used.
   *
   * @return the {@code BinaryFormat} implementation.
   */
  public abstract BinaryFormat getBinaryFormat();

  /**
   * Returns an instance that contains no-op implementations for all the instances.
   *
   * @return an instance that contains no-op implementations for all the instances.
   */
  public static PropagationComponent getNoopPropagationComponent() {
    return NOOP_PROPAGATION_COMPONENT;
  }

  private static final class NoopPropagationComponent extends PropagationComponent {
    @Override
    public BinaryFormat getBinaryFormat() {
      return BinaryFormat.getNoopBinaryFormat();
    }
  }
}
